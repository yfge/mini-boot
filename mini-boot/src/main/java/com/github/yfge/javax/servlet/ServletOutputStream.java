package com.github.yfge.javax.servlet;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

public abstract class ServletOutputStream extends OutputStream {
    protected ServletOutputStream() { }
    public void print(String s) throws IOException {
        if (s==null) s="null";
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt (i);
            if ((c & 0xff00) != 0) {        // high order byte must be zero
                String errMsg = "err.not_iso8859_1";
                Object[] errArgs = new Object[1];
                errArgs[0] = Character.valueOf(c);
                errMsg = MessageFormat.format(errMsg, errArgs);
                throw new CharConversionException(errMsg);
            }
            write (c);
        }
    }


}
