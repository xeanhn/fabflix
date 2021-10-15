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
import java.util.ArrayList;
import java.util.Arrays;
import com.google.gson.Gson;
import javax.servlet.ServletConfig;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Enumeration;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json"); // Response mime type


        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        String numMovies = request.getParameter("n");
        String sortParam = request.getParameter("sort");
        String genreParam = request.getParameter("genre");
        String charParam = request.getParameter("char");
        String titleParam = request.getParameter("title");
        String yearParam = request.getParameter("year");
        String directorParam = request.getParameter("director");
        String starParam = request.getParameter("star");
        String pgParam = request.getParameter("pg");

//        System.out.println(genreParam);
//        System.out.println(yearParam);
//        System.out.println(directorParam);
//        System.out.println(starParam);

        int movieCount = numMovies != null ? Integer.parseInt(numMovies) : 10;

        int offsetValue = Integer.parseInt(pgParam) * movieCount - movieCount;
        String sortQueryFragment = "";
        if (!sortParam.equals("null")) {
            sortQueryFragment = "ORDER BY";
            String[] sortFragments = sortParam.split("-");
            if (sortFragments[0].equals("title")) {
                sortQueryFragment += " M.title";
            }
            else {
                sortQueryFragment += " R.rating";
            }
            if (sortFragments[1].equals("asc")) {
                sortQueryFragment += " ASC,";
            }
            else {
                sortQueryFragment += " DESC,";
            }
            if (sortFragments[2].equals("title")) {
                sortQueryFragment += " M.title";
            }
            else {
                sortQueryFragment += " R.rating";
            }
            if (sortFragments[3].equals("asc")) {
                sortQueryFragment += " ASC";
            }
            else {
                sortQueryFragment += " DESC";
            }
        }

        String searchQueryFragment = "";
        if (!titleParam.equals("")) {
            searchQueryFragment += " AND M.title LIKE ?";
        }
        if (!yearParam.equals("")) {
            searchQueryFragment += " AND M.year = ?";
        }
        if (!directorParam.equals("")) {
            searchQueryFragment += " AND M.director LIKE ?";
        }
        if (!starParam.equals("")) {
            searchQueryFragment += " AND S.name LIKE ?";
        }

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            // Declare our statement

//            ***** FOR GENRES & CHAR, UNCOMMENT ****
//            String genreParam2 = genreParam != null ? genreParam : "Drama";
//
//            String charParam2 = !charParam.equals("null") ? charParam : "A";
//            String regexExpression = !charParam2.equals("*") ? "^[" + charParam2 + "]" : "^[^(A-Z0-9)]";

            String regexExpression = !charParam.equals("*") ? "^[" + charParam + "]" : "^[^(A-Z0-9)]";

            String query;
            String countQuery = "";

//            *****Browse By Genre Query*****
            if (!genreParam.equals("null")) {
                countQuery = "SELECT COUNT(DISTINCT M.id) AS count\n" +
                        "FROM movies M\n" +
                        "\tLEFT JOIN ratings R ON M.id = R.movieId\n" +
                        "    JOIN genres_in_movies GIM ON M.id = GIM.movieId\n" +
                        "\tJOIN genres G ON GIM.genreId = G.id\n" +
                        "WHERE M.id = GIM.movieId and GIM.genreId = G.id and G.name = ?;";

                query = "SELECT DISTINCT M.id, M.title, M.year, M.director, R.rating\n" +
                        "FROM movies M\n" +
                        "\tLEFT JOIN ratings R ON M.id = R.movieId\n" +
                        "    JOIN genres_in_movies GIM ON M.id = GIM.movieId\n" +
                        "\tJOIN genres G ON GIM.genreId = G.id\n" +
                        "WHERE M.id = GIM.movieId and GIM.genreId = G.id and G.name = ?\n" +
                        sortQueryFragment + "\n" +
                        "LIMIT ? OFFSET ?;";
            }

            else if (!charParam.equals("null")) {
//            *****Browse By Character Query*****

                countQuery = "SELECT COUNT(DISTINCT M.id) AS count\n" +
                        "FROM movies M\n" +
                        "\tLEFT JOIN ratings R ON M.id = R.movieId\n" +
                        "WHERE M.title REGEXP ?;";

                query = "SELECT DISTINCT M.id, M.title, M.year, M.director, R.rating\n" +
                        "FROM movies M\n" +
                        "\tLEFT JOIN ratings R ON M.id = R.movieId\n" +
                        "WHERE M.title REGEXP ?\n" +
                        sortQueryFragment + "\n" +
                        "LIMIT ? offset ?;";
            }
//            ***** Search Query *****
            else {
                countQuery = "SELECT COUNT(DISTINCT M.id) AS count\n" +
                        "FROM movies M\n" +
                        "LEFT JOIN ratings R ON M.id = R.movieId\n" +
                        "JOIN stars_in_movies SIM ON M.id = SIM.movieId\n" +
                        "JOIN stars S ON S.id = SIM.starId\n" +
                        "WHERE M.id = SIM.movieId AND SIM.starId = S.id" + searchQueryFragment + ";";

                query = "SELECT DISTINCT M.id, M.title, M.year, M.director, R.rating\n" +
                        "FROM movies M" +
                        "\tLEFT JOIN ratings R ON M.id = R.movieId\n" +
                        "    JOIN stars_in_movies SIM ON M.id = SIM.movieId\n" +
                        "\tJOIN stars S ON S.id = SIM.starId\n" +
                        "WHERE M.id = SIM.movieId AND SIM.starId = S.id" + searchQueryFragment + "\n" +
                        sortQueryFragment + "\n" +
                        "LIMIT ? OFFSET ?;";
            }
            // Create the statements
            PreparedStatement statement = dbcon.prepareStatement(query);
            PreparedStatement countStatement = dbcon.prepareStatement(countQuery);

            //Update the statement queries
            if (!genreParam.equals("null")) {
                statement.setString(1, genreParam);
                statement.setInt(2, movieCount);
                statement.setInt(3, offsetValue);
                countStatement.setString(1, genreParam);

            }

            else if (!charParam.equals("null")) {
                statement.setString(1, regexExpression);
                statement.setInt(2, movieCount);
                statement.setInt(3, offsetValue);

                countStatement.setString(1, regexExpression);

            }


            else {
                int index = 1;
                if (!titleParam.equals("")) {
                    statement.setString(index, "%" + titleParam + "%");
                    countStatement.setString(index, "%" + titleParam + "%");
                    index++;
                }
                if (!yearParam.equals("")) {
                    statement.setString(index, yearParam);
                    countStatement.setString(index, yearParam);
                    index++;
                }
                if (!directorParam.equals("")) {
                    statement.setString(index, "%" + directorParam + "%");
                    countStatement.setString(index, "%" + directorParam + "%");
                    index++;
                }
                if (!starParam.equals("")) {
                    statement.setString(index, "%" + starParam + "%");
                    countStatement.setString(index, "%" + starParam + "%");
                    index++;
                }

                statement.setInt(index, movieCount);
                index++;
                statement.setInt(index, offsetValue);
            }

            //Execute the queries
            ResultSet rs = statement.executeQuery();
            ResultSet countRs = countStatement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                String actorQuery = "SELECT S.name, S.id FROM stars_in_movies Sim, stars S WHERE ? = Sim.movieId AND Sim.starId = S.id LIMIT 3";
                PreparedStatement actorStatement = dbcon.prepareStatement(actorQuery);
                actorStatement.setString(1, movie_id);
                ResultSet actorsRs = actorStatement.executeQuery();

                String genreQuery = "SELECT G.name FROM genres_in_movies Gim, genres G WHERE ? = Gim.movieId AND Gim.genreId = G.id LIMIT 3";
                PreparedStatement genreStatement = dbcon.prepareStatement(genreQuery);
                genreStatement.setString(1, movie_id);
                ResultSet genresRs = genreStatement.executeQuery();

                Gson gson = new Gson();
                JsonArray actorArray = new JsonArray();
                JsonArray genreArray = new JsonArray();
                JsonArray starIdArray = new JsonArray();

                while(actorsRs.next()){
                    String star = actorsRs.getString("name");
                    String starId = actorsRs.getString("id");
                    actorArray.add(star);
                    starIdArray.add(starId);
                }


                while(genresRs.next()){
                    String genre = genresRs.getString("name");
                    genreArray.add(genre);
                }

//                 Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.add("movie_stars", actorArray);
                jsonObject.add("movie_star_ids", starIdArray);
                jsonObject.add("movie_genres", genreArray);
                jsonObject.addProperty("rating", movie_rating);
                jsonArray.add(jsonObject);

                actorsRs.close();
                genresRs.close();
                actorStatement.close();
                genreStatement.close();

            }

            while (countRs.next()) {
                String count = countRs.getString("count");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("count", count);
                jsonArray.add(jsonObject);
            }

            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            countRs.close();
            rs.close();
            statement.close();
            countStatement.close();
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