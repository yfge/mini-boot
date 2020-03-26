package com.github.yfge.javax.servlet;

public abstract class GenericServlet implements Servlet {
    public abstract void service(ServletRequest req, ServletResponse resp) throws ServletException;
}
