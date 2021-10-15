
import java.util.ArrayList;
/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;
    private ArrayList<String> itemArray;
    private boolean isEmployee;

    public User(String username) {
        this.username = username;
        this.isEmployee = false;
    }

    public String toString(){
        return username;
    }

    public String getUsername(){
        return username;
    }

    public void updateItemArray(ArrayList<String> newArray){
        itemArray = newArray;
    }

    public ArrayList<String> returnItemArray(){
        return itemArray;
    }

    public void makeUserEmployee() {isEmployee = true;};

    public boolean getEmployeeStatus(){
        return isEmployee;
    }
}
