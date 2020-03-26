package com.github.yfge.javax.servlet.http;

import com.github.yfge.javax.servlet.GenericServlet;
import com.github.yfge.javax.servlet.ServletException;
import com.github.yfge.javax.servlet.ServletRequest;
import com.github.yfge.javax.servlet.ServletResponse;


/**
 * 基本HttpServlet
 */
public abstract class HttpServlet extends GenericServlet {

    private static final String METHOD_GET="GET";
    private static final String METHOD_POST="POST";
    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)req;
        HttpServletResponse httpServletResponse = (HttpServletResponse)resp;
        switch (httpServletRequest.getMethod()){
            case METHOD_GET:
                doGet(httpServletRequest,httpServletResponse);
                break;
            case METHOD_POST:
                doPost(httpServletRequest,httpServletResponse);
                break;
            default:
                throw new ServletException("Method "+httpServletRequest.getMethod()+" no support.");

        }
    }

    protected abstract void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);

    protected abstract void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
}
