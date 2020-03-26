package com.github.yfge.javax.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public interface ServletRequest {
    public Object getAttribute(String name);

    public Enumeration<String> getAttributeNames();

    public String getCharacterEncoding();


    public String getContentType();

    public ServletInputStream getInputStream() throws IOException;

    public String getParameter(String name);

    public Enumeration<String> getParameterNames();

    public String[] getParameterValues(String name);

    public Map<String, String[]> getParameterMap();

    public String getRemoteAddr();

    public String getRemoteHost();

    public void setAttribute(String name, Object o);

    public void removeAttribute(String name);

    public Locale getLocale();

    public Enumeration<Locale> getLocales();

    public int getRemotePort();

    public String getLocalName();

    public String getLocalAddr();

    public int getLocalPort();


}
