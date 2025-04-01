package com.exam.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static Connection connection = null;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/exam_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Properties connectionProps = new Properties();
            connectionProps.put("user", DB_USER);
            connectionProps.put("password", DB_PASSWORD);
            connectionProps.put("useSSL", "false");
            connectionProps.put("autoReconnect", "true");
            
            connection = DriverManager.getConnection(DB_URL, connectionProps);
        }
        return connection;
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}