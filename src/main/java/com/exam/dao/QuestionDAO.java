package com.exam.dao;

import com.exam.model.Question;
import com.exam.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    // Add a new question to a test
    public boolean addQuestion(Question question) throws SQLException {
        String query = "INSERT INTO questions (test_id, question_text, option_a, option_b, option_c, option_d, correct_answer, question_type, marks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, question.getTestId());
            stmt.setString(2, question.getQuestionText());
            stmt.setString(3, question.getOptionA());
            stmt.setString(4, question.getOptionB());
            stmt.setString(5, question.getOptionC());
            stmt.setString(6, question.getOptionD());
            stmt.setString(7, question.getCorrectAnswer());
            stmt.setString(8, question.getQuestionType());
            stmt.setInt(9, question.getMarks());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        question.setQuestionId(rs.getInt(1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Get all questions for a test
    public List<Question> getQuestionsByTestId(int testId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions WHERE test_id = ? ORDER BY question_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question question = new Question();
                    question.setQuestionId(rs.getInt("question_id"));
                    question.setTestId(rs.getInt("test_id"));
                    question.setQuestionText(rs.getString("question_text"));
                    question.setOptionA(rs.getString("option_a"));
                    question.setOptionB(rs.getString("option_b"));
                    question.setOptionC(rs.getString("option_c"));
                    question.setOptionD(rs.getString("option_d"));
                    question.setCorrectAnswer(rs.getString("correct_answer"));
                    question.setQuestionType(rs.getString("question_type"));
                    question.setMarks(rs.getInt("marks"));
                    questions.add(question);
                }
            }
        }
        return questions;
    }

    // Get a specific question
    public Question getQuestionById(int questionId) throws SQLException {
        String query = "SELECT * FROM questions WHERE question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Question question = new Question();
                    question.setQuestionId(rs.getInt("question_id"));
                    question.setTestId(rs.getInt("test_id"));
                    question.setQuestionText(rs.getString("question_text"));
                    question.setOptionA(rs.getString("option_a"));
                    question.setOptionB(rs.getString("option_b"));
                    question.setOptionC(rs.getString("option_c"));
                    question.setOptionD(rs.getString("option_d"));
                    question.setCorrectAnswer(rs.getString("correct_answer"));
                    question.setQuestionType(rs.getString("question_type"));
                    question.setMarks(rs.getInt("marks"));
                    return question;
                }
            }
        }
        return null;
    }

    // Update a question
    public boolean updateQuestion(Question question) throws SQLException {
        String query = "UPDATE questions SET question_text = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, correct_answer = ?, question_type = ?, marks = ? WHERE question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, question.getQuestionText());
            stmt.setString(2, question.getOptionA());
            stmt.setString(3, question.getOptionB());
            stmt.setString(4, question.getOptionC());
            stmt.setString(5, question.getOptionD());
            stmt.setString(6, question.getCorrectAnswer());
            stmt.setString(7, question.getQuestionType());
            stmt.setInt(8, question.getMarks());
            stmt.setInt(9, question.getQuestionId());
            
            return stmt.executeUpdate() > 0;
        }
    }

    // Delete a question
    public boolean deleteQuestion(int questionId) throws SQLException {
        String query = "DELETE FROM questions WHERE question_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, questionId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Get total marks for a test
    public int getTotalMarksForTest(int testId) throws SQLException {
        String query = "SELECT SUM(marks) AS total FROM questions WHERE test_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, testId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }
}