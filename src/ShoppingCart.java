import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import javax.servlet.ServletConfig;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shoppingCart")
public class ShoppingCart extends HttpServlet {

    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = request.getSession();

        // Retrieve data named "previousItems" from session
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");

        ArrayList<String> previousItemsTitles = new ArrayList<>();

        // If "previousItems" is not found on session, means this is a new user, thus we create a new previousItems
        // ArrayList for the user

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String title = "Items Purchased";


        if (previousItems == null) {
            // Add the newly created ArrayList to session, so that it could be retrieved next time
            previousItems = new ArrayList<>();
            session.setAttribute("previousItems", previousItems);
        }


        try {
            Connection dbcon = dataSource.getConnection();

            String titleQuery = "SELECT M.title FROM movies M WHERE M.id = ?;";
            PreparedStatement titleStatement = dbcon.prepareStatement(titleQuery);

            for (int i = 0; i < previousItems.size(); i++) {
                titleStatement.setString(1, previousItems.get(i));
                ResultSet titleRs = titleStatement.executeQuery();
                while (titleRs.next()) {
                    String movie_title = titleRs.getString("title");
                    previousItemsTitles.add(movie_title);
                }
                titleRs.close();
            }

            response.setStatus(200);
            titleStatement.close();
            dbcon.close();

        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }

        int qty;
        int total = 0;
        out.println("<div class=\"receiptTitle\">Your Items</div>");
        out.println("<div id='items' class='items'>");

        Set<String> usedMovies = new HashSet<String>();
        for (int i =0; i < previousItems.size(); i++) {
            qty = Collections.frequency(previousItems, previousItems.get(i));
            int cost = qty*10;
            if(!usedMovies.contains(previousItems.get(i))){
                out.println(
                  "<div class='item'>" +
                      "<span class='movieTitle'>" + previousItemsTitles.get(i) + "</span>"+
                      "<button onclick=\"subtractQty("+ previousItems.get(i) +")\" class='" + previousItems.get(i) +"'>" + "&#9660;" + "</button>" +
                      "<span class='qty' id='" + previousItems.get(i) + "'>" + qty + "</span>" +
                      "<button onclick=\"addQty(" + previousItems.get(i) + ")\" class='" + previousItems.get(i) +"'>&#9650</button>" +
                      "<span class='total " +previousItems.get(i)  + "'>$" + cost + "</span>" +
                      "</div>"
                );
                total += cost;
                usedMovies.add(previousItems.get(i));
            }

        }

        session.setAttribute("totalCost", total);
        out.println("<div class='totalAmount'>Total: <span id='amt'>$"+ total + "</span></div>");
        out.println("</div>");
        out.close();
    }

}
