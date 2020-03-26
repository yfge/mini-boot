package com.github.yfge.javax.servlet;

import java.io.IOException;

public interface Servlet {
    public void service(ServletRequest req, ServletResponse res)
            throws ServletException, IOException;
    public String getServletInfo();
    public void destroy();
}
