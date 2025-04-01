package com.exam.model;

import java.time.LocalDateTime;
import java.util.Map;

public class Result {
    private int resultId;
    private int studentId;
    private int testId;
    private int score;
    private int totalMarks;
    private double percentage;
    private String grade;
    private LocalDateTime completedAt;
    private Map<Integer, String> answers; // questionId to student's answer
    private Map<Integer, Boolean> correctness; // questionId to whether answer was correct

    // Constructors
    public Result() {}

    public Result(int studentId, int testId, int score, int totalMarks) {
        this.studentId = studentId;
        this.testId = testId;
        this.score = score;
        this.totalMarks = totalMarks;
        this.percentage = calculatePercentage();
        this.grade = calculateGrade();
    }

    // Getters and Setters
    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        this.percentage = calculatePercentage();
        this.grade = calculateGrade();
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
        this.percentage = calculatePercentage();
        this.grade = calculateGrade();
    }

    public double getPercentage() {
        return percentage;
    }

    public String getGrade() {
        return grade;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Map<Integer, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Integer, String> answers) {
        this.answers = answers;
    }

    public Map<Integer, Boolean> getCorrectness() {
        return correctness;
    }

    public void setCorrectness(Map<Integer, Boolean> correctness) {
        this.correctness = correctness;
    }

    // Helper methods
    private double calculatePercentage() {
        return (double) score / totalMarks * 100;
    }

    private String calculateGrade() {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        return "F";
    }

    // Business logic methods
    public void addAnswer(int questionId, String answer, boolean isCorrect) {
        this.answers.put(questionId, answer);
        this.correctness.put(questionId, isCorrect);
        if (isCorrect) {
            this.score += 1; // Assuming each question carries equal weight
        }
        this.percentage = calculatePercentage();
        this.grade = calculateGrade();
    }
}