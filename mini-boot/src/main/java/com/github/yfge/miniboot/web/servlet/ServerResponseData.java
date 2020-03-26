package com.github.yfge.miniboot.web.servlet;

public class ServerResponseData {
    private int code;
    private byte[] data;


    public ServerResponseData(int code,byte[]data)
    {
        this.code = code;
        this.data =data;
    }

    public int getCode(){
        return this.code;
    }
    public byte[] getData(){
        return this.data;
    }
}
