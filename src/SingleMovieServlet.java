import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.ServletConfig;
import javax.naming.InitialContext;
import javax.naming.NamingException;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
//    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Construct a query with parameter represented by "?"
            String query = "SELECT DISTINCT sim.movieId, m.title, m.year, m.director from stars as s, stars_in_movies as sim, movies as m where m.id = sim.movieId and sim.starId = s.id and m.id = ?";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movie_rating = null;

                String ratingQuery = "SELECT R.rating FROM ratings R WHERE ? = R.movieId";
                PreparedStatement ratingStatement = dbcon.prepareStatement(ratingQuery);
                ratingStatement.setString(1, movieId);
                ResultSet ratingRs = ratingStatement.executeQuery();

                String actorQuery = "WITH MCOUNT AS (\n" +
                            "\tSELECT DISTINCT S.id as starId, S.name as starName, COUNT(M.id) as movieCount\n" +
                            "\tFROM stars_in_movies SIM, stars S, movies M\n" +
                            "\tWHERE SIM.starId = S.id and M.id = SIM.movieId\n" +
                            "\tGROUP BY SIM.starId)\n" +
                        "SELECT DISTINCT MCOUNT.starId, MCOUNT.starName, MCOUNT.movieCount\n" +
                        "FROM MCOUNT, movies M, stars_in_movies SIM\n" +
                        "WHERE SIM.movieId = ? and SIM.starId = MCOUNT.starId\n" +
                        "ORDER BY MCOUNT.movieCount DESC, MCOUNT.starName;";
                PreparedStatement actorStatement = dbcon.prepareStatement(actorQuery);
                actorStatement.setString(1, id);
                ResultSet actorsRs = actorStatement.executeQuery();

                String genreQuery = "SELECT G.name FROM genres_in_movies Gim, genres G WHERE ? = Gim.movieId AND Gim.genreId = G.id";
                PreparedStatement genreStatement = dbcon.prepareStatement(genreQuery);
                genreStatement.setString(1, id);
                ResultSet genresRs = genreStatement.executeQuery();

                // Create a JsonObject based on the data we retrieve from rs
                JsonArray actorArray = new JsonArray();
                JsonArray genreArray = new JsonArray();
                JsonArray actorIdArray = new JsonArray();

                while(ratingRs.next()) {
                    movie_rating = ratingRs.getString("rating");
                }

                while(actorsRs.next()){
                    String star = actorsRs.getString("starName");
                    String starId = actorsRs.getString("starId");
                    actorArray.add(star);
                    actorIdArray.add(starId);
                }


                while(genresRs.next()){
                    String genre = genresRs.getString("name");
                    genreArray.add(genre);
                }

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.add("movie_stars", actorArray);
                jsonObject.add("movie_star_ids", actorIdArray);
                jsonObject.add("movie_genres", genreArray);
                jsonObject.addProperty("rating", movie_rating);
                jsonArray.add(jsonObject);

                ratingRs.close();
                actorsRs.close();
                genresRs.close();
                ratingStatement.close();
                actorStatement.close();
                genreStatement.close();

            }

            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);


            rs.close();
            statement.close();
            dbcon.close();
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();

    }

}
