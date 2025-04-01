package com.exam.dao;

import com.exam.model.Result;
import com.exam.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO {
    // Save exam result
    public boolean saveResult(Result result) throws SQLException {
        String query = "INSERT INTO results (student_id, test_id, score, total_marks) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, result.getStudentId());
            stmt.setInt(2, result.getTestId());
            stmt.setInt(3, result.getScore());
            stmt.setInt(4, result.getTotalMarks());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        result.setResultId(rs.getInt(1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Get result by ID
    public Result getResultById(int resultId) throws SQLException {
        String query = "SELECT r.*, t.title AS test_title FROM results r JOIN tests t ON r.test_id = t.test_id WHERE r.result_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, resultId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Result result = new Result();
                    result.setResultId(rs.getInt("result_id"));
                    result.setStudentId(rs.getInt("student_id"));
                    result.setTestId(rs.getInt("test_id"));
                    result.setScore(rs.getInt("score"));
                    result.setTotalMarks(rs.getInt("total_marks"));
                    result.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
                    return result;
                }
            }
        }
        return null;
    }

    // Get all results for a student
    public List<Result> getResultsByStudent(int studentId) throws SQLException {
        List<Result> results = new ArrayList<>();
        String query = "SELECT r.*, t.title AS test_title FROM results r JOIN tests t ON r.test_id = t.test_id WHERE r.student_id = ? ORDER BY r.completed_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Result result = new Result();
                    result.setResultId(rs.getInt("result_id"));
                    result.setStudentId(rs.getInt("student_id"));
                    result.setTestId(rs.getInt("test_id"));
                    result.setScore(rs.getInt("score"));
                    result.setTotalMarks(rs.getInt("total_marks"));
                    result.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
                    results.add(result);
                }
            }
        }
        return results;
    }

    // Get all results for a test
    public List<Result> getResultsByTest(int testId) throws SQLException {
        List<Result> results = new ArrayList<>();
        String query = "SELECT r.*, s.username AS student_name FROM results r JOIN students s ON r.student_id = s.id WHERE r.test_id = ? ORDER BY r.score DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Result result = new Result();
                    result.setResultId(rs.getInt("result_id"));
                    result.setStudentId(rs.getInt("student_id"));
                    result.setTestId(rs.getInt("test_id"));
                    result.setScore(rs.getInt("score"));
                    result.setTotalMarks(rs.getInt("total_marks"));
                    result.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
                    results.add(result);
                }
            }
        }
        return results;
    }

    // Get average score for a test
    public double getAverageScoreForTest(int testId) throws SQLException {
        String query = "SELECT AVG(score) AS average FROM results WHERE test_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("average");
                }
            }
        }
        return 0;
    }

    // Check if student has already taken the test
    public boolean hasStudentTakenTest(int studentId, int testId) throws SQLException {
        String query = "SELECT 1 FROM results WHERE student_id = ? AND test_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, studentId);
            stmt.setInt(2, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Get top performing students for a test
    public List<Result> getTopPerformers(int testId, int limit) throws SQLException {
        List<Result> results = new ArrayList<>();
        String query = "SELECT r.*, s.username AS student_name FROM results r JOIN students s ON r.student_id = s.id WHERE r.test_id = ? ORDER BY r.score DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, testId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Result result = new Result();
                    result.setResultId(rs.getInt("result_id"));
                    result.setStudentId(rs.getInt("student_id"));
                    result.setTestId(rs.getInt("test_id"));
                    result.setScore(rs.getInt("score"));
                    result.setTotalMarks(rs.getInt("total_marks"));
                    result.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
                    results.add(result);
                }
            }
        }
        return results;
    }
}