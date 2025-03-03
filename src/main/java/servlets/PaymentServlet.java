package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import utils.DatabaseConnection;

/*
 * Servlet gia tin epeksergasia kai emfanisi statistikwn plirwmwn
 * Diaxeirizetai aitimata GET gia tin anaktisi dedomenw plirwmwn
 */
public class PaymentServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        // Elegxos eksiousiodotisis xristi
        HttpSession session = request.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("userRole"))) {
            out.println("{\"success\": false, \"message\": \"Unauthorized access\"}");
            return;
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String type = request.getParameter("type");
            String date = request.getParameter("date");
            
            // Dimiourgia tou SQL query gia tin anaktisi statistikwn
            StringBuilder sql = new StringBuilder(
    "SELECT p.* FROM payment_statistics_view p " +
    "WHERE p.event_id IN ( " +
    "    SELECT event_id FROM events " +
    "    WHERE event_date BETWEEN ? AND ? " +
    ") "
);

List<Object> params = new ArrayList<>();

// Prosthiki parametrwn imerominias sto query
if (date != null) {
    String[] dates = date.split(" to ");
    String startDate = dates[0];
    String endDate = dates.length > 1 ? dates[1] : startDate;
    params.add(startDate);
    params.add(endDate);
} else {
    // An den yparxei imerominia, xrisi prokathorismenwn timwn (eixame thema kapoia stigmi)
    params.add("1900-01-01");  
    params.add("2100-12-31");  
}

// Prosthiki filtrou typou an yparxei
if (type != null) {
    sql.append("AND status = ? ");
    params.add(Integer.parseInt(type));
}

sql.append("ORDER BY payment_date DESC");
            stmt = conn.prepareStatement(sql.toString());
            
            // Thetei tis times stis parametrous tou query
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) params.get(i));
                } else {
                    stmt.setString(i + 1, (String) params.get(i));
                }
            }
            
            rs = stmt.executeQuery();
            
            // Dimiourgia JSON response
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"success\":true,\"payments\":[");
            boolean first = true;
            
            // Metavlites gia statistika
            double totalRevenue = 0;
            String topEvent = null;
            double maxRevenue = 0;
            
            // Epeksergasia apotelesmatwn
            while (rs.next()) {
                if (!first) jsonBuilder.append(",");
                first = false;
                
                double eventRevenue = rs.getDouble("event_total_revenue");
                if (eventRevenue > maxRevenue) {
                    maxRevenue = eventRevenue;
                    topEvent = rs.getString("event_name");
                }
                
                jsonBuilder.append("{")
                    .append("\"payment_id\":").append(rs.getInt("payment_id")).append(",")
                    .append("\"event_id\":").append(rs.getInt("event_id")).append(",")
                    .append("\"booking_id\":").append(rs.getInt("booking_id")).append(",")
                    .append("\"customer_name\":\"").append(escape(rs.getString("customer_name"))).append("\",")
                    .append("\"event_name\":\"").append(escape(rs.getString("event_name"))).append("\",")
                    .append("\"ticket_type\":\"").append(escape(rs.getString("type_name"))).append("\",")
                    .append("\"amount\":").append(rs.getDouble("amount")).append(",")
                    .append("\"payment_date\":\"").append(rs.getTimestamp("payment_date")).append("\",")
                    .append("\"card_number\":\"").append(escape(rs.getString("card_number"))).append("\",")
                    .append("\"status\":").append(rs.getInt("status")).append(",")
                    .append("\"ticket_type_revenue\":").append(rs.getDouble("ticket_type_revenue"))
                    .append("}");
                    
                if (rs.getInt("status") == 1) {
                    totalRevenue += rs.getDouble("amount");
                }
            }
            
            // Prosthiki statistikwn sto JSON
            jsonBuilder.append("],")
                .append("\"statistics\":{")
                .append("\"totalRevenue\":").append(totalRevenue).append(",")
                .append("\"topEvent\":\"").append(escape(topEvent)).append("\"")
                .append("}}");
            
            out.println(jsonBuilder.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            out.println("{\"success\": false, \"message\": \"Error\"}");
        } finally {
            // Kleisimo twn resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Voithitiki methodos gia tin asfaleia twn strings sto JSON
    private String escape(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}