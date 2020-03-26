package com.github.yfge.miniboot.server;


import com.github.yfge.miniboot.autoconfigure.Autowired;
import com.github.yfge.miniboot.autoconfigure.PostConstruct;
import com.github.yfge.miniboot.autoconfigure.Service;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

@Service
public class BootServer {
    @Autowired
    private ServerHandler serverHandler;

    @PostConstruct
    public void start() {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", serverHandler);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
