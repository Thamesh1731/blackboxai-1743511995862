package com.exam.dao;

import com.exam.model.Student;
import com.exam.util.DBConnection;
import java.sql.*;

public class StudentDAO {
    // Student registration
    public boolean registerStudent(Student student) throws SQLException {
        String query = "INSERT INTO students (username, email, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, student.getUsername());
            stmt.setString(2, student.getEmail());
            stmt.setString(3, student.getPasswordHash());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        student.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Student authentication
    public Student authenticate(String username, String password) throws SQLException {
        String query = "SELECT * FROM students WHERE username = ? AND password_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setId(rs.getInt("id"));
                    student.setUsername(rs.getString("username"));
                    student.setEmail(rs.getString("email"));
                    student.setPasswordHash(rs.getString("password_hash"));
                    student.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return student;
                }
            }
        }
        return null;
    }

    // Check if username exists
    public boolean usernameExists(String username) throws SQLException {
        String query = "SELECT 1 FROM students WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Check if email exists
    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT 1 FROM students WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Get student by ID
    public Student getStudentById(int studentId) throws SQLException {
        String query = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setId(rs.getInt("id"));
                    student.setUsername(rs.getString("username"));
                    student.setEmail(rs.getString("email"));
                    student.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return student;
                }
            }
        }
        return null;
    }

    // Update student profile
    public boolean updateStudent(Student student) throws SQLException {
        String query = "UPDATE students SET email = ?, password_hash = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, student.getEmail());
            stmt.setString(2, student.getPasswordHash());
            stmt.setInt(3, student.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
}