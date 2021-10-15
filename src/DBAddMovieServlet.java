
import com.google.gson.JsonArray;
        import com.google.gson.JsonObject;

//import javax.annotation.Resource;
        import javax.servlet.ServletException;
        import javax.servlet.annotation.WebServlet;
        import javax.servlet.http.HttpServlet;
        import javax.servlet.http.HttpServletRequest;
        import javax.servlet.http.HttpServletResponse;
        import javax.sql.DataSource;
import java.awt.*;
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

@WebServlet(name = "DBAddMovieServlet", urlPatterns = "/api/DBAddMovie")
public class DBAddMovieServlet extends HttpServlet {

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
        PrintWriter out = response.getWriter();


        String movieTitle = request.getParameter("movieTitle");
        String movieYear = request.getParameter("movieYear");
        String movieDirector = request.getParameter("movieDirector");
        String starName = request.getParameter("sName");
        String genreName = request.getParameter("genreName");

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            // Declare our statement

            String query = "CALL add_movie(?, ?, ?, ?, ?);";
            PreparedStatement statement = dbcon.prepareStatement(query);



            statement.setString(1, movieTitle);
            try {
                statement.setInt(2, Integer.parseInt(movieYear));
            } catch (Exception e){
                statement.setNull(2, Types.BIGINT);
            }
            statement.setString(3, movieDirector);
            statement.setString(4, starName);
            statement.setString(5, genreName);

            ResultSet rs = statement.executeQuery();


            JsonObject jsonObject = new JsonObject();
            JsonArray testing = new JsonArray();

            while(rs.next()){
                testing.add(rs.getString("message"));
            }

            jsonObject.add("message", testing);
            out.write(jsonObject.toString());
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