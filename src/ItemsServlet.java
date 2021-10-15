import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// Declaring a WebServlet called ItemServlet, which maps to url "/items"
@WebServlet(name = "ItemServlet", urlPatterns = "/api/items")

public class ItemsServlet extends HttpServlet {
    private int getIndex(ArrayList<String> array, String element){
        for (int i = 0; i < array.size(); i++){
            if (array.get(i).equals(element)){
                return i;
            }
        }
        return -1;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();
        // Retrieve data named "previousItems" from session
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");

        // If "previousItems" is not found on session, means this is a new user, thus we create a new previousItems
        // ArrayList for the user
        if (previousItems == null) {

            // Add the newly created ArrayList to session, so that it could be retrieved next time
            previousItems = new ArrayList<>();
            session.setAttribute("previousItems", previousItems);
        }

        String newItem = request.getParameter("newItem"); // Get parameter that sent by GET request url
        String removeItem = request.getParameter("removeItem");
        // In order to prevent multiple clients, requests from altering previousItems ArrayList at the same time, we
        // lock the ArrayList while updating
        synchronized (previousItems) {
            if (newItem != null) {
                previousItems.add(newItem);
                session.setAttribute("previousItems", previousItems);
                User currentUser = (User)session.getAttribute("user");
                currentUser.updateItemArray(previousItems);

            }
            if (removeItem != null){
                out.println(previousItems);
                if(getIndex(previousItems,removeItem) != -1){
                    previousItems.remove(getIndex(previousItems,removeItem));
                    out.println(previousItems);
                    session.setAttribute("previousItems", previousItems);
                }
                User currentUser = (User)session.getAttribute("user");
                currentUser.updateItemArray(previousItems);
            }
        }

        response.setStatus(200);
        out.close();
    }
}
