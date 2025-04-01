package com.exam.dao;

import com.exam.model.Admin;
import com.exam.util.DBConnection;
import com.exam.util.PasswordHasher;
import java.sql.*;

public class AdminDAO {
    // Admin authentication
    public Admin authenticate(String username, String password) throws SQLException {
        String query = "SELECT * FROM admins WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String salt = rs.getString("salt");
                    
                    if (PasswordHasher.verifyPassword(password, salt, storedHash)) {
                        Admin admin = new Admin();
                        admin.setId(rs.getInt("id"));
                        admin.setUsername(rs.getString("username"));
                        admin.setPasswordHash(storedHash);
                        admin.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        return admin;
                    }
                }
            }
        }
        return null;
    }

    // Create new admin
    public boolean createAdmin(Admin admin) throws SQLException {
        String query = "INSERT INTO admins (username, password_hash, salt) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            String salt = PasswordHasher.generateSalt();
            String hashedPassword = PasswordHasher.hashPassword(admin.getPasswordHash(), salt);
            
            stmt.setString(1, admin.getUsername());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        admin.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Check if username exists
    public boolean usernameExists(String username) throws SQLException {
        String query = "SELECT 1 FROM admins WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}