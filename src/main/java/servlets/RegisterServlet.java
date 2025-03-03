package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.DatabaseConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

// Afto to servlet xirizetai tin eggrafi neon xriston sto sistima
public class RegisterServlet extends HttpServlet {
   
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) 
           throws ServletException, IOException {
       
       // Thetoume ton tipo tis apantisis se JSON
       response.setContentType("application/json");
       response.setCharacterEncoding("UTF-8");
       PrintWriter out = response.getWriter();
       Connection conn = null;
       PreparedStatement checkStmt = null;
       PreparedStatement insertStmt = null;
       ResultSet rs = null;
       
       try {
           // Pernoume ta stoixeia apo ti forma egrafis
           String firstName = request.getParameter("firstName");
           String lastName = request.getParameter("lastName");
           String email = request.getParameter("email");
           String password = request.getParameter("password");
           
           // Elegxoume an ola ta pedia einai simpliromena
           if (firstName == null || lastName == null || email == null || password == null ||
               firstName.trim().isEmpty() || lastName.trim().isEmpty() || 
               email.trim().isEmpty() || password.trim().isEmpty()) {
               out.println("{\"success\": false, \"message\": \"All fields are required\"}");
               return;
           }
           
           // Sindeomaste sti vasi
           conn = DatabaseConnection.getConnection();
           
           // Elegxoume an to email iparxi idi
           String checkSql = "SELECT customer_id FROM customers WHERE email = ?";
           checkStmt = conn.prepareStatement(checkSql);
           checkStmt.setString(1, email);
           rs = checkStmt.executeQuery();
           
           if (rs.next()) {
               out.println("{\"success\": false, \"message\": \"Email already registered\"}");
               return;
           }
           
           // Kanoume hash ton kodiko
           String hashedPassword = hashPassword(password);
           
           // SQL gia tin eisagogi tou neou xristi
           String sql = "INSERT INTO customers (first_name, last_name, email, password_hash) " +
           "VALUES (?, ?, ?, ?)";
           
           // Ekteloume tin eisagogi
           insertStmt = conn.prepareStatement(sql);
           insertStmt.setString(1, firstName);
           insertStmt.setString(2, lastName);
           insertStmt.setString(3, email);
           insertStmt.setString(4, hashedPassword);
           
           int rowsAffected = insertStmt.executeUpdate();
           
           // Elegxoume an i eggrafi egine epitixos
           if (rowsAffected > 0) {
               out.println("{\"success\": true, \"message\": \"Registration successful\"}");
           } else {
               out.println("{\"success\": false, \"message\": \"Registration failed\"}");
           }
           
       } catch (Exception e) {
           // Se periptosi sfalmatos
           e.printStackTrace();
           out.println("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
       } finally {
           // Kleisimo olon ton sindeseon
           try {
               if (rs != null) rs.close();
               if (checkStmt != null) checkStmt.close();
               if (insertStmt != null) insertStmt.close();
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
   }
   
   // Methodos gia to hashing tou kodikou
   private String hashPassword(String password) throws NoSuchAlgorithmException {
       MessageDigest digest = MessageDigest.getInstance("SHA-256");
       byte[] hash = digest.digest(password.getBytes());
       return Base64.getEncoder().encodeToString(hash);
   }
}