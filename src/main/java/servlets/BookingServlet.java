package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import utils.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;


/*
 * Servlet gia tin diaxeirisi kratiseon
 * Ypostirizei dimiourgia neon kratiseon kai anaktisi yparxonton
 */
public class BookingServlet extends HttpServlet {
    

    /*
     * Methodos gia tin ektelesi tis synallagis kratisis
     * Elegxei tin diathesimotita, dimiourgei tin kratisi kai tin pliromi
     */
    private boolean processBookingTransaction(Connection conn, int eventId, int ticketTypeId, int cardId, int customerId) 
    throws SQLException {
PreparedStatement stmt = null;
ResultSet rs = null;

try {
 // Elegxos katastasis ekdilosis kai diathesimotitas eisitirion
     String checkSql = 
        "SELECT e.status as event_status, tt.quantity_available, tt.price " +
        "FROM events e " +
        "JOIN ticket_types tt ON e.event_id = tt.event_id " +
        "WHERE e.event_id = ? AND tt.type_id = ? " +
        "FOR UPDATE";
        
    stmt = conn.prepareStatement(checkSql);
    stmt.setInt(1, eventId);
    stmt.setInt(2, ticketTypeId);
    rs = stmt.executeQuery();
    
    if (!rs.next() || !"active".equals(rs.getString("event_status")) || 
        rs.getInt("quantity_available") <= 0) {
        return false;
    }
    
    double ticketPrice = rs.getDouble("price");
    
    // Dimiourgia neas kratisis
    String bookingSql = "INSERT INTO bookings (customer_id, event_id, ticket_type_id, booking_date, status) " +
        "VALUES (?, ?, ?, NOW(), 'confirmed')";
    stmt = conn.prepareStatement(bookingSql, Statement.RETURN_GENERATED_KEYS);
    stmt.setInt(1, customerId);
    stmt.setInt(2, eventId);
    stmt.setInt(3, ticketTypeId);
    
    int bookingResult = stmt.executeUpdate();
    if (bookingResult <= 0) {
        return false;
    }
    
    rs = stmt.getGeneratedKeys();
    if (!rs.next()) {
        return false;
    }
    int bookingId = rs.getInt(1);
    
    // Dimiourgia eggrafis plirwmis
    String paymentSql = "INSERT INTO payments (booking_id, customer_id, amount, payment_date, card_id, status) " +
        "VALUES (?, ?, ?, NOW(), ?, 1)";
    stmt = conn.prepareStatement(paymentSql);
    stmt.setInt(1, bookingId);
    stmt.setInt(2, customerId);
    stmt.setDouble(3, ticketPrice);
    stmt.setInt(4, cardId);
    
    int paymentResult = stmt.executeUpdate();
    if (paymentResult <= 0) {
        return false;
    }
    
  // Enimerwsi diathesimon eisitirion
    String updateSql = "UPDATE ticket_types " +
        "SET quantity_available = quantity_available - 1 " +
        "WHERE type_id = ? AND quantity_available > 0";
    stmt = conn.prepareStatement(updateSql);
    stmt.setInt(1, ticketTypeId);
    
    int updateResult = stmt.executeUpdate();
    return updateResult > 0;
    
} finally {
    if (rs != null) rs.close();
    if (stmt != null) stmt.close();
}
}

 // Diaxeirisi POST requests gia nees kratiseis
@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
response.setContentType("application/json");
PrintWriter out = response.getWriter();

HttpSession session = request.getSession(false);
if (session == null || session.getAttribute("customerId") == null) {
    out.println("{\"success\": false, \"message\": \"Not logged in\"}");
    return;
}

Connection conn = null;
try {
     // Parse tou JSON request
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(request.getReader());
    
    int eventId = rootNode.get("eventId").asInt();
    int ticketTypeId = rootNode.get("ticketTypeId").asInt();
    int cardId = rootNode.get("cardId").asInt();
    int customerId = (Integer) session.getAttribute("customerId");
    
    conn = DatabaseConnection.getConnection();
    conn.setAutoCommit(false);
    
    // Epexergasia tis kratisis
    boolean success = processBookingTransaction(conn, eventId, ticketTypeId, cardId, customerId);
    
    if (success) {
        conn.commit();
        out.println("{\"success\": true, \"message\": \"Booking successful\"}");
    } else {
        conn.rollback();
        out.println("{\"success\": false, \"message\": \"Failed to process booking\"}");
    }
    
} catch (Exception e) {
    if (conn != null) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    e.printStackTrace();
    out.println("{\"success\": false, \"message\": \"Error processing booking: " + e.getMessage() + "\"}");
} finally {
    if (conn != null) {
        try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
}

// Diaxeirisi GET requests gia anaktisi kratiseon
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // Elegxos energou session
            HttpSession session = request.getSession(false);
            if (session == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.println("{\"success\": false, \"message\": \"No active session found\"}");
                return;
            }
    
            Integer customerId = (Integer) session.getAttribute("customerId");
            if (customerId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.println("{\"success\": false, \"message\": \"User not logged in\"}");
                return;
            }
    
        // Sindesi sti vasi
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                throw new SQLException("error");
            }
    
            // Query gia tin anaktisi kratiseon
            String sql = "SELECT " +
                        "   b.booking_id, " +
                        "   b.status AS booking_status, " +
                        "   b.booking_date, " +
                        "   e.name AS event_name, " +
                        "   e.event_date, " +
                        "   e.event_time, " +
                        "   tt.type_name, " +
                        "   tt.price, " +
                        "   p.status AS payment_status " +
                        "FROM bookings b " +
                        "INNER JOIN events e ON b.event_id = e.event_id " +
                        "INNER JOIN ticket_types tt ON b.ticket_type_id = tt.type_id " +
                        "LEFT JOIN payments p ON b.booking_id = p.booking_id " +
                        "WHERE b.customer_id = ? " +
                        "ORDER BY b.booking_date DESC";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customerId);
            
           
            stmt.setQueryTimeout(30);
            rs = stmt.executeQuery();
            // Dimiourgia JSON response
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"success\": true, \"bookings\": [");
            
            boolean first = true;
            while (rs != null && rs.next()) {
                if (!first) jsonBuilder.append(",");
                first = false;
                
                try {
                    jsonBuilder.append("{")
                        .append("\"booking_id\":").append(rs.getInt("booking_id")).append(",")
                        .append("\"event_name\":\"").append(escape(rs.getString("event_name"))).append("\",")
                        .append("\"ticket_type\":\"").append(escape(rs.getString("type_name"))).append("\",")
                        .append("\"price\":").append(String.format("%.2f", rs.getDouble("price"))).append(",")
                        .append("\"booking_date\":\"").append(rs.getTimestamp("booking_date")).append("\",")
                        .append("\"status\":\"").append(escape(rs.getString("booking_status"))).append("\",")
                        .append("\"event_date\":\"").append(rs.getDate("event_date")).append("\",")
                        .append("\"event_time\":\"").append(rs.getTime("event_time")).append("\",")
                        .append("\"payment_status\":\"").append(escape(rs.getString("payment_status"))).append("\"")
                        .append("}");
                } catch (SQLException e) {
                    System.err.println("error: " + e.getMessage());
                    continue; 
                }
            }
            
            jsonBuilder.append("]}");
            
      // Elegxos an vrethikan kratiseis
            if (first) {
                out.println("{\"success\": true, \"bookings\": [], \"message\": \"No bookings found\"}");
            } else {
                out.println(jsonBuilder.toString());
            }
            
        } catch (SQLException e) {
            System.err.println("error: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"success\": false, \"message\": \"Database error: " + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"success\": false, \"message\": \"Server error: " + escape(e.getMessage()) + "\"}");
        } finally {
            // Kleisimo resources
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.err.println("error: " + e.getMessage());
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("error: " + e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("error: " + e.getMessage());
                }
            }
        }
    }
    
        // Voithitiki methodos gia tin asfaleia twn strings
    private String escape(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t")
                   .replace("\\", "\\\\");
    }
}