import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.google.gson.JsonObject;
import javax.servlet.ServletConfig;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@WebServlet(name = "InsertServlet", urlPatterns = "/api/insert")
public class InsertServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession();
        try {
            Connection dbcon = dataSource.getConnection();

            User currentUser = (User) session.getAttribute("user");
            String username = currentUser.getUsername();
            ArrayList<String> itemsArray = (ArrayList<String>) session.getAttribute("previousItems");
            String currentTime = java.time.LocalDate.now().toString();

            ArrayList<String> saleIdArray = (ArrayList<String>) session.getAttribute("saleIdArray");

            if (saleIdArray == null) {
                saleIdArray = new ArrayList<String>();
                session.setAttribute("saleIdArray", saleIdArray);
            }

            String insertQuery = "INSERT INTO sales (customerId, movieId, saleDate)\n" +
                    "VALUES (?, ?, ?);";
            PreparedStatement insertStatement = dbcon.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);

            for (int i = 0; i < itemsArray.size(); i++) {

                insertStatement.setString(1, username);
                insertStatement.setString(2, itemsArray.get(i));
                insertStatement.setString(3, currentTime);

                insertStatement.execute();
                ResultSet insertRs = insertStatement.getGeneratedKeys();
                while (insertRs.next()) {
                    String saleIdKey = insertRs.getString(1);
                    saleIdArray.add(saleIdKey);
                }

                session.setAttribute("saleIdArray", saleIdArray);

                insertRs.close();
            }

            insertStatement.close();
            dbcon.close();

        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }
}
