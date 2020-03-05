package com.github.yfge.miniapp;
import com.github.yfge.miniboot.autoconfigure.BootApplication;
import com.github.yfge.miniboot.autoconfigure.Application;

@BootApplication(Name = "Hello")
public class App {
    public static void main(String[] args) {
     Application.run(com.github.yfge.miniapp.App.class);
    }
}
