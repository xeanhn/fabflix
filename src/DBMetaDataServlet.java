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
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import com.google.gson.Gson;
import javax.servlet.ServletConfig;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.transform.Result;
import java.util.Enumeration;
import java.util.Hashtable;

@WebServlet(name = "DBMetaDataServlet", urlPatterns = "/api/_dashboard")
public class DBMetaDataServlet extends HttpServlet {

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

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if(!user.getEmployeeStatus()){
            response.setStatus(404);
        }

        response.setContentType("application/json"); // Response mime type
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            // Declare our statement
            Statement statement = dbcon.createStatement();
            String query = "SELECT Table_name as TablesName from information_schema.tables where table_schema = 'moviedb';";

            ResultSet rs = statement.executeQuery(query);
            ArrayList<String> tableNames = new ArrayList<String>();
            while(rs.next()){
                String tableName = rs.getString("TablesName");
                tableNames.add(tableName);
            }

            JsonObject jsonObject = new JsonObject();
            JsonArray tableNameArray = new JsonArray();

            JsonArray tableSchemaArray = new JsonArray();
            for (int i = 0; i < tableNames.size(); i++){
                Statement tableSchemaStatement = dbcon.createStatement();

                String tableSchemaQuery = "DESCRIBE moviedb." + tableNames.get(i) + ";";
                ResultSet schemaRs = statement.executeQuery(tableSchemaQuery);

                JsonArray tableFieldsArray = new JsonArray();
                while(schemaRs.next()){
                    String tableSchema = "";
                    tableSchema += schemaRs.getString("Field");
                    tableSchema += " ";
                    tableSchema += schemaRs.getString("Type");
                    tableFieldsArray.add(tableSchema);

                }
                tableNameArray.add(tableNames.get(i));
                tableSchemaArray.add(tableFieldsArray);


                schemaRs.close();
                tableSchemaStatement.close();
            }

            jsonObject.add("tableNames", tableNameArray);
            jsonObject.add("tableSchemas", tableSchemaArray);
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(jsonObject);
            out.write(jsonArray.toString());

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