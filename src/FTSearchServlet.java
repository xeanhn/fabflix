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

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;


@WebServlet(name = "FTSearchServlet", urlPatterns = "/api/ftsearch")
public class FTSearchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private long totalJDBCTime = 0;
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
        long startTime = System.nanoTime();
        response.setContentType("application/json"); // Response mime type


        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        String numMovies = request.getParameter("n");
        String titleParam = request.getParameter("title");
        String pgParam = request.getParameter("pg");
        String sortParam = request.getParameter("sort");

        JsonArray jsonArray = new JsonArray();

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

        if (titleParam == null || titleParam.equals("null") || titleParam.trim().isEmpty()) {
            System.out.println("GOT HERE");
            out.write(jsonArray.toString());
            out.close();
            return;
        }

        String[] tWords = titleParam.split(" ");

        String ftsQueryFragment = "";
        for (String word: tWords) {
            ftsQueryFragment += '+' + word + "* ";
        }
        ftsQueryFragment = ftsQueryFragment.substring(0, ftsQueryFragment.length() - 1);

        try {

            // Get a connection from dataSource
            long startJDBCTime = System.nanoTime();
            Connection dbcon = dataSource.getConnection();
            long endJDBCTime = System.nanoTime();
            totalJDBCTime += (endJDBCTime - startJDBCTime);
            // Declare our statement


            String countQuery = "SELECT COUNT(DISTINCT M.id) AS count\n" +
                    "FROM movies M\n" +
                    "LEFT JOIN ratings R ON M.id = R.movieId\n" +
                    "WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE);";

            String query = "SELECT DISTINCT M.id, M.title, M.year, M.director, R.rating\n" +
                    "FROM movies M" +
                    "\tLEFT JOIN ratings R ON M.id = R.movieId\n" +
                    "WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE)\n" +
                    sortQueryFragment + "\n" +
                    "LIMIT ? OFFSET ?;";

            // Create the statements
            startJDBCTime = System.nanoTime();
            PreparedStatement statement = dbcon.prepareStatement(query);
            PreparedStatement countStatement = dbcon.prepareStatement(countQuery);

            countStatement.setString(1, ftsQueryFragment);
            statement.setString(1, ftsQueryFragment);
            statement.setInt(2, movieCount);
            statement.setInt(3, offsetValue);
            //Execute the queries
            ResultSet rs = statement.executeQuery();
            ResultSet countRs = countStatement.executeQuery();

            endJDBCTime = System.nanoTime();
            totalJDBCTime += (endJDBCTime - startJDBCTime);
            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                String actorQuery = "SELECT S.name, S.id FROM stars_in_movies Sim, stars S WHERE ? = Sim.movieId AND Sim.starId = S.id LIMIT 3";
                String genreQuery = "SELECT G.name FROM genres_in_movies Gim, genres G WHERE ? = Gim.movieId AND Gim.genreId = G.id LIMIT 3";

                startJDBCTime = System.nanoTime();
                PreparedStatement actorStatement = dbcon.prepareStatement(actorQuery);
                actorStatement.setString(1, movie_id);
                ResultSet actorsRs = actorStatement.executeQuery();

                PreparedStatement genreStatement = dbcon.prepareStatement(genreQuery);
                genreStatement.setString(1, movie_id);
                ResultSet genresRs = genreStatement.executeQuery();
                endJDBCTime = System.nanoTime();
                totalJDBCTime += (endJDBCTime - startJDBCTime);

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

            System.out.println(jsonArray);
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            System.out.println(response.getStatus());

            startJDBCTime = System.nanoTime();
            countRs.close();
            rs.close();
            statement.close();
            countStatement.close();
            dbcon.close();
            endJDBCTime = System.nanoTime();
            totalJDBCTime += (endJDBCTime - startJDBCTime);

        } catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);

        }
        out.close();
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        double elapsedTimeMS = elapsedTime / 1000000.0;
        double totalJDBSTimeMS = totalJDBCTime / 1000000.0;

        try {
//            String log_path = "D:\\SPRING 2021\\CS122B\\projects\\cs122b-spring21-team-85\\fabflix\\samplelog.txt";
            String log_path = request.getServletContext().getRealPath("/") + "samplelog.txt";
            String line_info = elapsedTimeMS + " " + totalJDBSTimeMS + "\n";
            File log_file = new File(log_path);
            if (!log_file.exists()) {
                log_file.createNewFile();
                System.out.println("CREATED NEW FILE");
            }

            FileWriter fileWriter = new FileWriter(log_file, true);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write(line_info);
            bw.close();
//            fileWriter.write(line_info);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();;
        }

        totalJDBCTime = 0;
    }
}