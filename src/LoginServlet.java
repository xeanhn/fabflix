import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.google.gson.Gson;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.servlet.ServletConfig;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = response.getWriter();
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        if (!request.getHeader("User-Agent").contains("Android")) {
            System.out.println("Desktop Device");
            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            } catch (Exception e) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("status", "fail");
                jsonObject.addProperty("message", e.getMessage());
                out.write(jsonObject.toString());
                out.close();
                return;
            }
        }
        try {
            System.out.println("ESTABLISHING DB CONNECTION");
            Connection dbcon = dataSource.getConnection();
            String username = request.getParameter("username");
            String password = request.getParameter("password");

        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
            JsonObject responseJsonObject = new JsonObject();
            String employeeQuery = "select * from employees where email = ?";
            System.out.println("ESTABLISHING EMPLOYEE PS");
            PreparedStatement employeeStatement = dbcon.prepareStatement(employeeQuery);
            employeeStatement.setString(1, username);
            System.out.println("EXECUTING EMPLOYEE QUERY");
            ResultSet employeeRs = employeeStatement.executeQuery();
            System.out.println("COMPLETED EMPLOYEE QUERY");
            String email = "";
            String pw = "";

            while (employeeRs.next()) {
                email = employeeRs.getString("email");
                pw = employeeRs.getString("password");
            }
            boolean success;

            if (pw.equals("")) {
                success = false;
            }
            else {
                success = new StrongPasswordEncryptor().checkPassword(password, pw);
            }

            HttpSession session = request.getSession();
            if (username.equals(email) && success) {

                // Login success:

                // set this user into the session
                User employee = new User(email);
                employee.makeUserEmployee();
                session.setAttribute("user", employee);
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

            }

            else {
                String customerQuery = "select * from customers where email = ?";
                System.out.println("ESTABLISHING CUSTOMER PS");
                PreparedStatement customerStatement = dbcon.prepareStatement(customerQuery);
                customerStatement.setString(1, username);
                System.out.println("EXECUTING CUSTOMER QUERY");
                ResultSet customerRs = customerStatement.executeQuery();
                System.out.println("FINISHING CUSTOMER QUERY");
                String cust_email = "";
                String cust_pw = "";
                String cust_id = "";
                while (customerRs.next()) {
                    cust_id = customerRs.getString("id");
                    cust_email = customerRs.getString("email");
                    cust_pw = customerRs.getString("password");
                }

                boolean cust_success;
                if (cust_pw.equals("")) {
                    cust_success = false;
                }
                else {
                    cust_success = new StrongPasswordEncryptor().checkPassword(password, cust_pw);
                }

                if (username.equals(cust_email) && cust_success) {
                    // set this user into the session
                    User customer = new User(cust_id);
                    session.setAttribute("user", customer);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }

                else {
                    // Login fail
                    responseJsonObject.addProperty("status", "fail");

                    // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                    responseJsonObject.addProperty("message", "incorrect login information");
                }

                customerRs.close();
                customerStatement.close();

            }
            out.write(responseJsonObject.toString());
            employeeRs.close();
            employeeStatement.close();
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