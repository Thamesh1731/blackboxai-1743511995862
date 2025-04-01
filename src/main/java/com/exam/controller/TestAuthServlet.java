package com.exam.controller;

import com.exam.dao.AdminDAO;
import com.exam.dao.StudentDAO;
import com.exam.model.Admin;
import com.exam.model.Student;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/test-auth")
public class TestAuthServlet extends BaseServlet {
    private AdminDAO adminDAO = new AdminDAO();
    private StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Test admin registration and authentication
            Admin admin = new Admin("testadmin", "admin123");
            boolean adminCreated = adminDAO.createAdmin(admin);
            Admin authAdmin = adminDAO.authenticate("testadmin", "admin123");
            boolean adminAuthSuccess = authAdmin != null;
            
            // Test student registration and authentication
            Student student = new Student("teststudent", "test@example.com", "student123");
            boolean studentCreated = studentDAO.registerStudent(student);
            Student authStudent = studentDAO.authenticate("teststudent", "student123");
            boolean studentAuthSuccess = authStudent != null;
            
            // Test wrong credentials
            Admin wrongAdmin = adminDAO.authenticate("testadmin", "wrongpass");
            Student wrongStudent = studentDAO.authenticate("teststudent", "wrongpass");
            
            sendJsonResponse(response, Map.of(
                "admin_created", adminCreated,
                "admin_auth_success", adminAuthSuccess,
                "admin_wrong_pass", wrongAdmin == null,
                "student_created", studentCreated,
                "student_auth_success", studentAuthSuccess,
                "student_wrong_pass", wrongStudent == null
            ));
            
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Test failed: " + e.getMessage());
        }
    }
}