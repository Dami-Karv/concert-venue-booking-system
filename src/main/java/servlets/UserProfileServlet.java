package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import utils.DatabaseConnection;

// Afto to servlet xrisimopiite gia tin emfanisi ton stoixeion tou xristi
public class UserProfileServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Pernoume to session tou xristi
        HttpSession session = request.getSession(false);
        // Pernoume to ID tou pelati apo to session
        int customerId = (Integer) session.getAttribute("customerId");
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // Sindesi me ti vasi
            conn = DatabaseConnection.getConnection();         
            // Etoimazoume to query gia na paroume ta stoixeia tou xristi
            stmt = conn.prepareStatement(
    "SELECT customer_id, first_name, last_name, email " +
    "FROM customers WHERE customer_id = ?");
            
            // Vazoume to ID tou xristi sto query
            stmt.setInt(1, customerId);
            // Ektelesi tou query
            rs = stmt.executeQuery();
            
            // An vroume ton xristi
            if (rs.next()) {
                // Dimiourgoume to JSON response
                StringBuilder json = new StringBuilder();
                json.append("{\"success\": true, \"user\": {");
                json.append("\"customerId\":").append(rs.getInt("customer_id")).append(",");
                json.append("\"firstName\":\"").append(rs.getString("first_name")).append("\",");
                json.append("\"lastName\":\"").append(rs.getString("last_name")).append("\",");
                json.append("\"email\":\"").append(rs.getString("email")).append("\"");
                json.append("}}");
                
                // Stelnoume to JSON ston client
                response.getWriter().println(json.toString());
            }
        } catch (SQLException e) {
            // An kati paei strava
            throw new ServletException(e);
        } finally {
            try {
                // Kleisimo olwn ton resources
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        }
    }
}