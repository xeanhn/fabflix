import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();

        try {

            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            JsonArray jsonArray = new JsonArray();
            ArrayList<String> saleIdArray = (ArrayList<String>) session.getAttribute("saleIdArray");


            double total = 0;

            String salesQuery = "SELECT s.id, m.title\n" +
                    "FROM sales s, movies m\n" +
                    "WHERE s.id = ? AND m.id = s.movieId;";
            PreparedStatement salesStatement = dbcon.prepareStatement(salesQuery);
            for (int i = 0; i < saleIdArray.size(); i++) {

                salesStatement.setString(1, saleIdArray.get(i));
                ResultSet salesRs = salesStatement.executeQuery();

                while (salesRs.next()) {
                    String sale_id = salesRs.getString("id");
                    String movie_title= salesRs.getString("title");
                    String id = salesRs.getString("id");
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("sale_id", sale_id);
                    jsonObject.addProperty("movie_title", movie_title);
                    jsonObject.addProperty("quantity", "1");
                    jsonArray.add(jsonObject);
                }

                salesRs.close();

                total += 10;
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("total", total);
            jsonArray.add(jsonObject);

            session.setAttribute("saleIdArray", null);
            session.setAttribute("previousItems", null);

            System.out.println(jsonArray);
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            salesStatement.close();
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