import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.ServletConfig;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.time.LocalDate;


@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        try {
            Connection dbcon = dataSource.getConnection();

            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String cardNum = request.getParameter("cardNum");
            String expDate = request.getParameter("expDate");

            JsonObject responseJsonObject = new JsonObject();

            String[] splitDate = expDate.split("-");
            if (splitDate.length != 3) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "incorrect payment information");
            }

            else if (!isInteger(splitDate[0]) || !isInteger(splitDate[1]) || !isInteger(splitDate[2]) ||
                    splitDate[0].length() != 4 || splitDate[1].length() != 2 || splitDate[2].length() != 2 ||
                    Integer.parseInt(splitDate[1]) < 1 || Integer.parseInt(splitDate[1]) > 12 ||
                    Integer.parseInt(splitDate[2]) < 1 || Integer.parseInt(splitDate[2]) > 31
            )
            {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "incorrect payment information");
            }

            else {

                String query = "SELECT * FROM creditcards CC\n" +
                        "WHERE CC.firstName = ? AND\n" +
                        "CC.lastName = ? AND\n" +
                        "CC.id = ? AND\n" +
                        "CC.expiration = ?;";

                PreparedStatement paymentStatement = dbcon.prepareStatement(query);
                paymentStatement.setString(1, firstName);
                paymentStatement.setString(2, lastName);
                paymentStatement.setString(3, cardNum);
                paymentStatement.setString(4, expDate);
                ResultSet paymentRs = paymentStatement.executeQuery();

                String sqlFirstName = "";
                String sqlLastName = "";
                String sqlCardNum = "";
                String sqlExpiration = "";

                while (paymentRs.next()) {
                    sqlFirstName = paymentRs.getString("firstName");
                    sqlLastName = paymentRs.getString("lastName");
                    sqlCardNum = paymentRs.getString("id");
                    sqlExpiration = paymentRs.getString("expiration");
                }

                if (firstName.equals(sqlFirstName) && lastName.equals(sqlLastName) &&
                        cardNum.equals(sqlCardNum) && expDate.equals(sqlExpiration)) {


                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");

                } else {

                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "incorrect payment information");
                }

                paymentStatement.close();
                paymentRs.close();

                dbcon.close();
            }

            out.write(responseJsonObject.toString());

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
