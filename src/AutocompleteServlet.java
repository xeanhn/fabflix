
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// server endpoint URL
@WebServlet(name = "AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public AutocompleteServlet() {
        super();
    }

    /*
     *
     * Match the query against superheroes and return a JSON response.
     *
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
     * ]
     *
     * The format is like this because it can be directly used by the
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     *
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            PrintWriter out = response.getWriter();
            // setup the response json arrray
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String query = request.getParameter("query");
            query = query.trim();
            // return the empty json array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                out.write(jsonArray.toString());
                out.close();
                return;
            }

            String[] tWords = query.split(" ");

            String ftsQueryFragment = "";
            for (String word: tWords) {
                ftsQueryFragment += '+' + word + "* ";
            }
            ftsQueryFragment = ftsQueryFragment.substring(0, ftsQueryFragment.length() - 1);

            // search on superheroes and add the results to JSON Array
            // this example only does a substring match
            // TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars

            Connection dbcon = dataSource.getConnection();

            String ftsQuery = "SELECT id, title, year FROM movies WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE);";
            PreparedStatement ftsStatement = dbcon.prepareStatement(ftsQuery);
            ftsStatement.setString(1, ftsQueryFragment);
            ResultSet ftsRs = ftsStatement.executeQuery();

            while (ftsRs.next() && jsonArray.size() < 10) {
                String movie_id = ftsRs.getString("id");
                String movie_title = ftsRs.getString("title");
                String movie_year = ftsRs.getString("year");

                jsonArray.add(generateJsonObject(movie_id, movie_title));
            }
//            for (Integer id : superHeroMap.keySet()) {
//                String heroName = superHeroMap.get(id);
//                if (heroName.toLowerCase().contains(query.toLowerCase())) {
//                    jsonArray.add(generateJsonObject(id, heroName));
//                }
//            }

            ftsRs.close();
            ftsStatement.close();
            dbcon.close();
            out.write(jsonArray.toString());
            out.close();
            return;
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "heroID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movie_id, String movie_title) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movie_title);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieId", movie_id);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }


}
