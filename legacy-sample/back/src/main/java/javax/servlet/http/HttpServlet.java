package javax.servlet.http;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;

public class HttpServlet {

    // 샘플 프로젝트용 Servlet 베이스 스텁(IDE/컴파일 호환 목적).
    public void init() throws ServletException {
    }

    // 실제 Servlet 구현체에서 재정의한다.
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    }

    // 실제 Servlet 구현체에서 재정의한다.
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    }

    // 레거시 코드 호환을 위한 최소 ServletContext 객체를 반환한다.
    protected ServletContext getServletContext() {
        return new ServletContext() {
        };
    }
}
