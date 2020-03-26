package com.github.yfge.javax.servlet.http;

import com.github.yfge.javax.servlet.ServletRequest;

import java.util.Enumeration;

public interface HttpServletRequest extends ServletRequest {

    public String getHeader(String name);

    public Enumeration<String> getHeaders(String name);

    public Enumeration<String> getHeaderNames();

    public int getIntHeader(String name);


    public String getMethod();

    public String getPathInfo();


    public String getPathTranslated();

    public String getContextPath();


    public String getQueryString();

    public String getRemoteUser();

    public String getRequestedSessionId();

    public String getRequestURI();

    public StringBuffer getRequestURL();

    public String getServletPath();

}
