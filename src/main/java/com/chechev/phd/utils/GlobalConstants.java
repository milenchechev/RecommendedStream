package com.chechev.phd.utils;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Milen
 */
public class GlobalConstants {

    public static String APP_ID;
    public static String APP_SECRET;
    public static String APP_URL;
    public static int MAX_APP_USERS;
    public static int MAX_OBJECTS;
    public static int MONGODB_PORT;
    public static String SOLR_URL;
    public static int UPDATE_PAGE_TIME;
    public static String FILE_PATH_RESULTS;
    /*
    public static String APP_ID="173755836098113";
    public static String APP_SECRET="aab1be2291631cb3f26eb77f1a4a40f9";
    public static String APP_URL="https://apps.facebook.com/recommended_stream_d/";
    public static int MAX_APP_USERS=2000;
    public static int MAX_OBJECTS=5000000;
    public static int MONGODB_PORT=27017;//27017;
    public static String SOLR_URL="http://127.0.0.1:8080/solr";
    public static int UPDATE_PAGE_TIME=60;
    */
        static {
        try {
            Properties prop = new Properties();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String appScope = System.getProperty("APP_SCOPE");
            Logger.getLogger(GlobalConstants.class.getName()).log(Level.INFO, "!!!!!  APP_SCOPE = "+appScope);
            if( appScope!=null && appScope.equals("test") ){
                prop.load(classLoader.getResourceAsStream("/configuration_dev.properties"));
            }else {
                prop.load(classLoader.getResourceAsStream("/configuration.properties"));
            }
            APP_ID = prop.getProperty("APP_ID");
            APP_SECRET = prop.getProperty("APP_SECRET");
            APP_URL = prop.getProperty("APP_URL");
            MAX_APP_USERS = Integer.parseInt(prop.getProperty("MAX_APP_USERS"));
            MAX_OBJECTS = Integer.parseInt(prop.getProperty("MAX_OBJECTS"));
            MONGODB_PORT = Integer.parseInt(prop.getProperty("MONGODB_PORT"));
            SOLR_URL = prop.getProperty("SOLR_URL");
            UPDATE_PAGE_TIME = Integer.parseInt(prop.getProperty("UPDATE_PAGE_TIME"));
            FILE_PATH_RESULTS = prop.getProperty("FILE_PATH_RESULTS");
            
        } catch (IOException ex) {
            Logger.getLogger(GlobalConstants.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
