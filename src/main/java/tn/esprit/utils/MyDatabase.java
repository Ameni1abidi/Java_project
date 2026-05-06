package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private final String url="jdbc:mysql://localhost:3307/eduflex";
    private final String user="root";
    private final String password="";
    private Connection connection;
    private static MyDatabase instance;
    private  MyDatabase(){
        try {
            connection= DriverManager.getConnection(url,user,password);
            System.out.println("connected to database");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public Connection getConnection() {
        return connection;
    }

    public static MyDatabase getInstance() {
        if(instance==null){
            instance= new MyDatabase();
        }
        return instance;
    }

}