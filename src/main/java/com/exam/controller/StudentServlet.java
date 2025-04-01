package com.exam.controller;

import com.exam.dao.StudentDAO;
import com.exam.model.Student;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/student/*")
public class StudentServlet extends BaseServlet {
    private StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = getPathInfo(request);
        
        try {
            switch (action) {
                case "register":
                    handleRegister(request, response);
                    break;
                case "login":
                    handleLogin(request, response);
                    break;
                default:
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
                case "profile":
                    handleGetProfile(request, response);
                    break;
                case "check-username":
                    checkUsernameAvailability(request, response);
                    break;
                case "check-email":
                    checkEmailAvailability(request, response);
                    break;
                default:
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = getPathInfo(request);
        
        try {
            if ("profile".equals(action)) {
                handleUpdateProfile(request, response);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = getPathInfo(request);
        
        try {
            if ("password".equals(action)) {
                handlePasswordChange(request, response);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        if (username == null || email == null || password == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "All fields are required");
            return;
        }
        
        if (studentDAO.usernameExists(username)) {
            sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, "Username already exists");
            return;
        }
        
        if (studentDAO.emailExists(email)) {
            sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, "Email already registered");
            return;
        }
        
        Student student = new Student(username, email, password);
        if (studentDAO.registerStudent(student)) {
            sendJsonResponse(response, Map.of("success", true, "studentId", student.getId()));
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration failed");
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        if (username == null || password == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Username and password required");
            return;
        }
        
        Student student = studentDAO.authenticate(username, password);
        if (student != null) {
            HttpSession session = request.getSession();
            session.setAttribute("student", student);
            sendJsonResponse(response, student);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
        }
    }

    private void handleGetProfile(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("student") == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        
        Student student = (Student) session.getAttribute("student");
        Student fullProfile = studentDAO.getStudentById(student.getId());
        if (fullProfile != null) {
            sendJsonResponse(response, fullProfile);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Student not found");
        }
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("student") == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        
        Student currentStudent = (Student) session.getAttribute("student");
        String email = request.getParameter("email");
        
        if (email == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Email required");
            return;
        }
        
        Student updatedStudent = new Student();
        updatedStudent.setId(currentStudent.getId());
        updatedStudent.setEmail(email);
        
        if (studentDAO.updateStudent(updatedStudent)) {
            session.setAttribute("student", studentDAO.getStudentById(currentStudent.getId()));
            sendJsonResponse(response, Map.of("success", true));
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update failed");
        }
    }

    private void handlePasswordChange(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("student") == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        
        Student currentStudent = (Student) session.getAttribute("student");
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Both current and new password required");
            return;
        }
        
        Student verifiedStudent = studentDAO.authenticate(currentStudent.getUsername(), currentPassword);
        if (verifiedStudent == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Current password is incorrect");
            return;
        }
        
        if (studentDAO.updatePassword(currentStudent.getId(), newPassword)) {
            sendJsonResponse(response, Map.of("success", true));
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Password update failed");
        }
    }

    private void checkUsernameAvailability(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String username = request.getParameter("username");
        if (username == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Username parameter required");
            return;
        }
        boolean exists = studentDAO.usernameExists(username);
        sendJsonResponse(response, Map.of("exists", exists));
    }

    private void checkEmailAvailability(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String email = request.getParameter("email");
        if (email == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Email parameter required");
            return;
        }
        boolean exists = studentDAO.emailExists(email);
        sendJsonResponse(response, Map.of("exists", exists));
    }
}