package com.github.yfge.miniboot.autoconfigure;
import java.net.JarURLConnection;
import  java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {
    public List<String> loadClass(Class t){
        String packageName = t.getPackage().getName();
        return loadClass(packageName);
    }
    private List<String> loadClass(String packageName) {
        String packagePath = packageName.replace('.','/');
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        URL url = loader.getResource(packagePath);
        if (url!=null){
            String protocol = url .getProtocol();
            if (protocol.equals("file")){
                return getClassFromDir(url);
            }else if (protocol.equals("jar")){
                return getClassFromJar(url);
            }
        }else {
            System.out.println("loader fail.");
            return null;
        }
        return  null;
    }
    private List<String> getClassFromJar(URL url){
        try {
            JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            List<String> classNames = new ArrayList<>();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() == false) {
                    String entryName = entry.getName().replace('/','.');
                    if(entryName.endsWith(".class") && entryName.contains("$")==false ) {
                        classNames.add(entryName.replace(".class",""));
                    }
                }
            }
            return classNames;
        }catch (java.io.IOException ex){
            return null;
        }
    }
    private List<String> getClassFromDir(URL url ){
        return null;
    }

}
