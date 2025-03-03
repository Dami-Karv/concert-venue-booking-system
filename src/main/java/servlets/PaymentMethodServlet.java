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

// Afto to servlet xirizetai tis leitourgies pliromis kai diaxirisis karton
public class PaymentMethodServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        
        // Debug logs
        System.out.println("PaymentMethodServlet: Session check");
        System.out.println("Session exists: " + (session != null));
        
        // Elegxos an o xristis einai sindedemenos
        if (session == null || session.getAttribute("customerId") == null) { // Changed from userId to customerId
            System.out.println("No valid session found");
            out.println("{\"success\": false, \"message\": \"Not logged in\"}");
            return;
        }
        
        int customerId = (Integer) session.getAttribute("customerId"); // Changed from userId to customerId
        
        // Anazitisi energon karton tou xristi
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
               "SELECT * FROM credit_cards WHERE customer_id = ? AND (status = 'active' OR status IS NULL)")) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"success\":true,\"cards\":[");
            boolean first = true;
            
            while (rs.next()) {
                if (!first) jsonBuilder.append(",");
                first = false;
                
                jsonBuilder.append("{")
                    .append("\"id\":").append(rs.getInt("card_id")).append(",")
                    .append("\"cardName\":\"").append(rs.getString("card_holder")).append("\",")
                    .append("\"cardNumber\":\"").append(rs.getString("card_number")).append("\",")
                    .append("\"expiry\":\"").append(rs.getString("expiry_date")).append("\"")
                    .append("}");
            }
            
            jsonBuilder.append("]}");
            out.println(jsonBuilder.toString());
            
        } catch (Exception e) {
            out.println("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) 
       throws ServletException, IOException {
        // Elegxos taftotitas xristi
   response.setContentType("application/json");
   PrintWriter out = response.getWriter();
   HttpSession session = request.getSession(false);
   
    // Elegxos an iparxei energi sinedria
   if (session == null || session.getAttribute("customerId") == null) {
       out.println("{\"success\": false, \"message\": \"Not logged in\"}");
       return;
   }
   
   int customerId = (Integer) session.getAttribute("customerId");
   Connection conn = null;
   PreparedStatement stmt = null;
   
   try {
       // Lipsi dedomenon formas
       String cardNumber = request.getParameter("cardNumber");
       if (cardNumber != null) {
           cardNumber = cardNumber.replaceAll("\\s+", ""); // Remove spaces
       }
       String expiry = request.getParameter("expiry");
       String cvv = request.getParameter("cvv");
       String cardName = request.getParameter("cardName");
       
       
       if (cardNumber == null || expiry == null || cvv == null || cardName == null ||
           cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty() || cardName.isEmpty()) {
           out.println("{\"success\": false, \"message\": \"All fields are required\"}");
           return;
       }
       
       conn = DatabaseConnection.getConnection();
       String sql = "INSERT INTO credit_cards (customer_id, card_holder, card_number, expiry_date, cvv) VALUES (?, ?, ?, ?, ?)";
       stmt = conn.prepareStatement(sql);
       
       stmt.setInt(1, customerId);  
       stmt.setString(2, cardName);
       stmt.setString(3, cardNumber);
       stmt.setString(4, expiry);
       stmt.setString(5, cvv);
       
       int result = stmt.executeUpdate();
       
       if (result > 0) {
           out.println("{\"success\": true, \"message\": \"Card added successfully\"}");
       } else {
           out.println("{\"success\": false, \"message\": \"Failed to add card\"}");
       }
       
   } catch (Exception e) {
       e.printStackTrace();
       out.println("{\"success\": false, \"message\": \"Database error: " + e.getMessage() + "\"}");
   } finally {
       try {
           if (stmt != null) stmt.close();
           if (conn != null) conn.close();
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
}

@Override
protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
            // Arxikopoiisi metavliton
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    HttpSession session = request.getSession(false);
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        // Elegxos taftotitas xristi
        if (session == null || session.getAttribute("customerId") == null) {
            out.println("{\"success\": false, \"message\": \"Not logged in\"}");
            return;
        }

        // Exago to ID tis kartas apo to monopati
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            out.println("{\"success\": false, \"message\": \"No card ID provided\"}");
            return;
        }

        // Lipsi ID kartas kai xristi
        int cardId = Integer.parseInt(pathInfo.substring(1));
        int customerId = (Integer) session.getAttribute("customerId");

        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);

           // Elegxos an i karta xrisimopoiitai se pliromis
           String checkSql = "SELECT COUNT(*) FROM payments WHERE card_id = ?";
        stmt = conn.prepareStatement(checkSql);
        stmt.setInt(1, cardId);
        rs = stmt.executeQuery();
        
        if (rs.next() && rs.getInt(1) > 0) {
                           // I karta xrisimopoiitai - apenergopoiisi anti gia diagrafi
            String updateSql = "UPDATE credit_cards SET status = 'inactive' WHERE card_id = ? AND customer_id = ?";
            stmt = conn.prepareStatement(updateSql);
            stmt.setInt(1, cardId);
            stmt.setInt(2, customerId);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                conn.commit();
                out.println("{\"success\": true, \"message\": \"Card deactivated successfully\"}");
            } else {
                conn.rollback();
                out.println("{\"success\": false, \"message\": \"Failed to deactivate card\"}");
            }
        } else {
            // I karta den xrisimopoiitai - asfaleis na diagrafei
            String deleteSql = "DELETE FROM credit_cards WHERE card_id = ? AND customer_id = ?";
            stmt = conn.prepareStatement(deleteSql);
            stmt.setInt(1, cardId);
            stmt.setInt(2, customerId);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                conn.commit();
                out.println("{\"success\": true, \"message\": \"Card deleted successfully\"}");
            } else {
                conn.rollback();
                out.println("{\"success\": false, \"message\": \"Failed to delete card\"}");
            }
        }
        
    } catch (Exception e) {
        // Se periptosi sfalmatos, anairesi olon ton allagon
        if (conn != null) {
            try {
                conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        e.printStackTrace();
        out.println("{\"success\": false, \"message\": \"Error processing request: " + e.getMessage() + "\"}");
    } finally {
         // Kleisimo olon ton sindeseon
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
}