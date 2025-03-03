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

/*
 * Servlet gia tin diaxeirisi twn ekdilwsewn
 * Xrisimopoieitai gia tin anaktisi kai emfanisi stoixeiwn ekdilwsewn
 */
public class EventServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        System.out.println("EventServlet: Fetching events");
        
        // Kathorizei ton typo tou response ws JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String pathInfo = request.getPathInfo();
        
        // Elegxos an zitithike sygkekrimeni ekdilwsi i oles
        if (pathInfo == null || pathInfo.equals("/")) {
            fetchAllEvents(out);
        } else {
            try {
                int eventId = Integer.parseInt(pathInfo.substring(1));
                fetchSingleEvent(eventId, out);
            } catch (NumberFormatException e) {
                out.println("{\"error\": \"Invalid event ID\"}");
            }
        }
    }
    
    // Methodos gia tin anaktisi olwn twn events
    private void fetchAllEvents(PrintWriter out) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            // SQL query gia tin anaktisi ekdilwsewn pou den exoun akyrwthei
            String sql = "SELECT * FROM event_details_view " +
            "WHERE status != 'cancelled' " +
            "ORDER BY event_date, event_time, event_id";
            
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            // Dimiourgia tou JSON response
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("[");
            int currentEventId = -1;
            boolean first = true;
            
            // Epeksergasia twn apotelesmatwn
            while (rs.next()) {
                int eventId = rs.getInt("event_id");
                
                // Elegxos gia neo event
                if (eventId != currentEventId) {
                    if (currentEventId != -1) {
                        jsonBuilder.append("]}");
                        jsonBuilder.append(",");
                    }
                    
                    // Prosthiki stoixeiwn event
                    jsonBuilder.append("{")
                        .append("\"event_id\":").append(eventId).append(",")
                        .append("\"name\":\"").append(escape(rs.getString("name"))).append("\",")
                        .append("\"event_type\":\"").append(escape(rs.getString("event_type"))).append("\",")
                        .append("\"event_date\":\"").append(rs.getDate("event_date")).append("\",")
                        .append("\"event_time\":\"").append(rs.getTime("event_time")).append("\",")
                        .append("\"venue_capacity\":").append(rs.getInt("venue_capacity")).append(",")
                        .append("\"status\":\"").append(escape(rs.getString("status"))).append("\",")
                        .append("\"tickets\":[");
                    
                    currentEventId = eventId;
                    first = true;
                }
                
                // Prosthiki stoixeiwn eisitiriwn
                if (rs.getObject("type_id") != null) {
                    if (!first) {
                        jsonBuilder.append(",");
                    }
                    first = false;
                    
                    jsonBuilder.append("{")
                        .append("\"type_id\":").append(rs.getInt("type_id")).append(",")
                        .append("\"type_name\":\"").append(escape(rs.getString("type_name"))).append("\",")
                        .append("\"price\":").append(rs.getDouble("price")).append(",")
                        .append("\"quantity_available\":").append(rs.getInt("quantity_available")).append(",")
                        .append("\"revenue\":").append(rs.getDouble("revenue"))
                        .append("}");
                }
            }
            
            // Kleisimo tou teleutaiou JSON object
            if (currentEventId != -1) {
                jsonBuilder.append("]}");
            }
            
            jsonBuilder.append("]");
            out.println(jsonBuilder.toString());
            
        } catch (Exception e) {
            System.err.println("Error fetching all events:");
            e.printStackTrace();
            out.println("{\"error\": \"" + escape(e.getMessage()) + "\"}");
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
    
    // Methodos gia tin anaktisi mias sygkekrimenis ekdilwsis
    private void fetchSingleEvent(int eventId, PrintWriter out) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            // SQL query gia tin anaktisi mias ekdilwsis
            String sql = "SELECT * FROM event_details_view " +
            "WHERE event_id = ? AND status != 'cancelled'";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, eventId);
            rs = stmt.executeQuery();
            
            // Dimiourgia tou JSON response
            if (rs.next()) {
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{")
                    .append("\"event_id\":").append(eventId).append(",")
                    .append("\"name\":\"").append(escape(rs.getString("name"))).append("\",")
                    .append("\"event_type\":\"").append(escape(rs.getString("event_type"))).append("\",")
                    .append("\"event_date\":\"").append(rs.getDate("event_date")).append("\",")
                    .append("\"event_time\":\"").append(rs.getTime("event_time")).append("\",")
                    .append("\"venue_capacity\":").append(rs.getInt("venue_capacity")).append(",")
                    .append("\"status\":\"").append(escape(rs.getString("status"))).append("\",")
                    .append("\"tickets\":[");
                
                // Prosthiki stoixeiwn eisitiriwn
                boolean firstTicket = true;
                do {
                    if (rs.getObject("type_id") != null) {
                        if (!firstTicket) {
                            jsonBuilder.append(",");
                        }
                        firstTicket = false;
                        
                        jsonBuilder.append("{")
                            .append("\"type_id\":").append(rs.getInt("type_id")).append(",")
                            .append("\"type_name\":\"").append(escape(rs.getString("type_name"))).append("\",")
                            .append("\"price\":").append(rs.getDouble("price")).append(",")
                            .append("\"quantity_available\":").append(rs.getInt("quantity_available")).append(",")
                            .append("\"revenue\":").append(rs.getDouble("revenue"))
                            .append("}");
                    }
                } while (rs.next());
                
                jsonBuilder.append("]}");
                out.println(jsonBuilder.toString());
            } else {
                out.println("{\"error\": \"Event not found\"}");
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching single event:");
            e.printStackTrace();
            out.println("{\"error\": \"" + escape(e.getMessage()) + "\"}");
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