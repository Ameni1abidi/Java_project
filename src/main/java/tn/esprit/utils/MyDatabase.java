package tn.esprit.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class MyDatabase {
    private final String url="jdbc:mysql://localhost:3306/eduflex";
    private final String user="root";
    private final String password="";
    private Connection connection;
    private static MyDatabase instance;
    private  MyDatabase(){
        try {
            connection= DriverManager.getConnection(url,user,password);
            initializeSchema();
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

    private void initializeSchema() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            if (inputStream == null) {
                throw new RuntimeException("Fichier schema.sql introuvable");
            }

            String schema;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                schema = reader.lines().collect(Collectors.joining("\n"));
            }

            String[] statements = schema.split(";");
            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (trimmedStatement.isEmpty()) {
                    continue;
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(trimmedStatement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Erreur lors de l'initialisation du schema", e);
        }
    }

}
