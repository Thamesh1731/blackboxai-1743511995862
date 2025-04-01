package com.exam.controller;

import com.exam.dao.ResultDAO;
import com.exam.dao.TestDAO;
import com.exam.model.Result;
import com.exam.model.Student;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/results/*")
public class ResultServlet extends BaseServlet {
    private ResultDAO resultDAO = new ResultDAO();
    private TestDAO testDAO = new TestDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = getPathInfo(request);
        
        try {
            if ("submit".equals(action)) {
                handleSubmitResult(request, response);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = getPathInfo(request);
        
        try {
            switch (action) {
                case "student":
                    handleGetStudentResults(request, response);
                    break;
                case "test":
                    handleGetTestResults(request, response);
                    break;
                case "details":
                    handleGetResultDetails(request, response);
                    break;
                case "analytics":
                    handleGetTestAnalytics(request, response);
                    break;
                default:
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    private void handleSubmitResult(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("student") == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        String testIdParam = request.getParameter("testId");
        String scoreParam = request.getParameter("score");
        String totalMarksParam = request.getParameter("totalMarks");
        
        if (testIdParam == null || scoreParam == null || totalMarksParam == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
            return;
        }

        try {
            int testId = Integer.parseInt(testIdParam);
            int score = Integer.parseInt(scoreParam);
            int totalMarks = Integer.parseInt(totalMarksParam);
            int studentId = ((Student) session.getAttribute("student")).getId();

            // Check if student has already taken this test
            if (resultDAO.hasStudentTakenTest(studentId, testId)) {
                sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, "Test already taken");
                return;
            }

            Result result = new Result(studentId, testId, score, totalMarks);
            if (resultDAO.saveResult(result)) {
                sendJsonResponse(response, Map.of(
                    "success", true,
                    "resultId", result.getResultId(),
                    "percentage", result.getPercentage(),
                    "grade", result.getGrade()
                ));
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save result");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid number format");
        }
    }

    private void handleGetStudentResults(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("student") == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        int studentId = ((Student) session.getAttribute("student")).getId();
        List<Result> results = resultDAO.getResultsByStudent(studentId);
        sendJsonResponse(response, results);
    }

    private void handleGetTestResults(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Admin access required");
            return;
        }

        String testIdParam = request.getParameter("testId");
        if (testIdParam == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "testId parameter required");
            return;
        }

        try {
            int testId = Integer.parseInt(testIdParam);
            List<Result> results = resultDAO.getResultsByTest(testId);
            sendJsonResponse(response, results);
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid testId format");
        }
    }

    private void handleGetResultDetails(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String resultIdParam = request.getParameter("resultId");
        if (resultIdParam == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "resultId parameter required");
            return;
        }

        try {
            int resultId = Integer.parseInt(resultIdParam);
            Result result = resultDAO.getResultById(resultId);
            if (result != null) {
                // Verify ownership (student can only see their own results)
                HttpSession session = request.getSession(false);
                if (session != null && session.getAttribute("student") != null) {
                    int studentId = ((Student) session.getAttribute("student")).getId();
                    if (result.getStudentId() != studentId) {
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
                        return;
                    }
                } else if (session == null || session.getAttribute("admin") == null) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Access denied");
                    return;
                }
                
                sendJsonResponse(response, result);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Result not found");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid resultId format");
        }
    }

    private void handleGetTestAnalytics(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Admin access required");
            return;
        }

        String testIdParam = request.getParameter("testId");
        if (testIdParam == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "testId parameter required");
            return;
        }

        try {
            int testId = Integer.parseInt(testIdParam);
            double averageScore = resultDAO.getAverageScoreForTest(testId);
            List<Result> topPerformers = resultDAO.getTopPerformers(testId, 5);
            
            sendJsonResponse(response, Map.of(
                "averageScore", averageScore,
                "topPerformers", topPerformers
            ));
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid testId format");
        }
    }
}