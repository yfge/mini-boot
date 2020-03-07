package com.github.yfge.miniboot.server;

import com.github.yfge.javax.servlet.http.HttpServlet;
import com.github.yfge.javax.servlet.http.HttpServletRequest;
import com.github.yfge.javax.servlet.http.HttpServletResponse;

public class BaseServerlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
