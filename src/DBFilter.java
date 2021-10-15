import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "DBFilter", urlPatterns = "/_dashboard.html")
public class DBFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        User user = (User)httpRequest.getSession().getAttribute("user");

        if (user.getEmployeeStatus()) {
            chain.doFilter(request, response);
        } else{
            ((HttpServletResponse) response).sendError(404);
        }
    }

    public void init(FilterConfig fConfig) {

    }

    public void destroy() {
        // ignored.
    }

}
