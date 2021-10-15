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
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
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
            String query = "SELECT DISTINCT s.id, s.name, s.birthYear " +
                    "FROM stars AS s, stars_in_movies AS sim, movies AS m " +
                    "WHERE m.id = sim.movieId AND sim.starId = s.id AND s.id = ?";

            PreparedStatement statement = dbcon.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String starId = rs.getString("id");
                String starName = rs.getString("name");
                String starDob = rs.getString("birthYear");

                String movieQuery = "SELECT DISTINCT s.id, s.birthYear, sim.movieId, m.title, m.year\n" +
                        "FROM stars AS s, stars_in_movies AS sim, movies AS m, ratings AS r\n" +
                        "WHERE sim.starId = s.id AND s.id = ? AND m.id = sim.movieId\n" +
                        "ORDER BY m.year DESC, m.title;";
                PreparedStatement movieStatement = dbcon.prepareStatement(movieQuery);
                movieStatement.setString(1, id);
                ResultSet moviesRs = movieStatement.executeQuery();

                // Create a JsonObject based on the data we retrieve from rs
                JsonArray movieArray = new JsonArray();
                JsonArray movieIdArray = new JsonArray();
                JsonArray movieYearArray = new JsonArray();

                while(moviesRs.next()){
                    String movie_title = moviesRs.getString("title");
                    String movie_id = moviesRs.getString("movieId");
                    String movie_year = moviesRs.getString("year");
                    movieArray.add(movie_title);
                    movieIdArray.add(movie_id);
                    movieYearArray.add(movie_year);
                }

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("star_id", starId);
                jsonObject.addProperty("star_name", starName);
                jsonObject.addProperty("star_dob", starDob);
                jsonObject.add("movies", movieArray);
                jsonObject.add("movies_ids", movieIdArray);
                jsonObject.add("movies_years", movieYearArray);

                jsonArray.add(jsonObject);

                moviesRs.close();
                movieStatement.close();
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
