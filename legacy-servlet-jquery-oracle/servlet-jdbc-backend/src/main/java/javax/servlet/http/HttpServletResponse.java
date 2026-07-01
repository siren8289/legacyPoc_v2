package javax.servlet.http;

import java.io.PrintWriter;

public interface HttpServletResponse {
    void setContentType(String type);

    PrintWriter getWriter();
}
