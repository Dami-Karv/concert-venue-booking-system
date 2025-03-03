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
import javax.servlet.http.HttpSession;
import utils.DatabaseConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

// Afto to servlet xirizetai to login ton xriston
public class LoginServlet extends HttpServlet {
   
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) 
           throws ServletException, IOException {
       System.out.println("LoginServlet: Starting login process"); 
       
       response.setContentType("application/json");
       response.setCharacterEncoding("UTF-8");
       PrintWriter out = response.getWriter();
       
       try {
           String email = request.getParameter("email");
           String password = request.getParameter("password");
           System.out.println("Login attempt for email: " + email);
           
           // Elegxos gia login tou admin
           if (email.equals("admin") && password.equals("admin")) {
               HttpSession session = request.getSession();
               session.setAttribute("userRole", "admin");
               session.setAttribute("email", "admin");
               out.println("{\"success\": true, \"message\": \"Admin login successful\", \"redirect\": \"admin.html\"}");
               return;
           }
           
           // An den einai admin, sinexizoume me ton elegxo tou xristi
           Connection conn = DatabaseConnection.getConnection();
           
           // Kanoume hash ton kodiko
           String hashedPassword = hashPassword(password);
           
           // Elegxoume ta stoixeia sti vasi
           String sql = "SELECT customer_id, first_name, last_name, email FROM customers " +
                       "WHERE email = ? AND password_hash = ?";
           
           PreparedStatement stmt = conn.prepareStatement(sql);
           stmt.setString(1, email);
           stmt.setString(2, hashedPassword);
           ResultSet rs = stmt.executeQuery();
           
           if (rs.next()) {
               // Dimiourgoume to session gia ton xristi
               HttpSession session = request.getSession();
               session.setAttribute("customerId", rs.getInt("customer_id"));
               session.setAttribute("firstName", rs.getString("first_name"));
               session.setAttribute("lastName", rs.getString("last_name"));
               session.setAttribute("email", rs.getString("email"));
               session.setAttribute("userRole", "customer");
               
               System.out.println("Login successful for: " + email);
               out.println("{\"success\": true, \"message\": \"Login successful\", \"redirect\": \"index.html\"}");
           } else {
               System.out.println("Login failed for: " + email);
               out.println("{\"success\": false, \"message\": \"Invalid email or password\"}");
           }
           
           rs.close();
           stmt.close();
           
       } catch (Exception e) {
           System.err.println("Error in LoginServlet:");
           e.printStackTrace(System.err);
           out.println("{\"success\": false, \"message\": \"Server error occurred\"}");
       }
   }
   
   // Methodos gia na kanoume hash ton kodiko
   private String hashPassword(String password) throws NoSuchAlgorithmException {
       MessageDigest digest = MessageDigest.getInstance("SHA-256");
       byte[] hash = digest.digest(password.getBytes());
       return Base64.getEncoder().encodeToString(hash);
   }
}