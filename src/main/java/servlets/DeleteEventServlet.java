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

/*
* Servlet gia tin akyrwsi ekdilwsewn kai tin epistrofi xrimatwn sta eisitiria
* Xrisimopoieitai gia tin diaxeirisi akyrwsewn apo ton admin
*/
public class DeleteEventServlet extends HttpServlet {
   
 @Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) 
       throws ServletException, IOException {
   
   // Kathorizei ton typo tou response ws JSON
   response.setContentType("application/json");
   response.setCharacterEncoding("UTF-8");
   PrintWriter out = response.getWriter();
   
   Connection conn = null;
   PreparedStatement stmt = null;
   ResultSet rs = null;

   try {
       // Elegxos eksiousiodotisis admin
       HttpSession session = request.getSession(false);
       if (session == null || !"admin".equals(session.getAttribute("userRole"))) {
           sendErrorResponse(out, "Unauthorized access");
           return;
       }

       // Elegxos egkyrotitas tou event ID
       int eventId;
       try {
           eventId = Integer.parseInt(request.getParameter("eventId"));
       } catch (NumberFormatException e) {
           sendErrorResponse(out, "Invalid event ID");
           return;
       }

       conn = DatabaseConnection.getConnection();
       conn.setAutoCommit(false);

       // Query gia elegxo katastasis ekdilwsis kai pliroforiwn kratisewn
       String eventBookingsQuery = 
           "SELECT e.status as event_status, " +
           "       b.booking_id, b.customer_id, " +
           "       tt.price, p.card_id " +
           "FROM events e " +
           "LEFT JOIN bookings b ON e.event_id = b.event_id AND b.status = 'confirmed' " +
           "LEFT JOIN ticket_types tt ON b.ticket_type_id = tt.type_id " +
           "LEFT JOIN payments p ON b.booking_id = p.booking_id AND p.status = 1 " +
           "WHERE e.event_id = ?";

       stmt = conn.prepareStatement(eventBookingsQuery);
       stmt.setInt(1, eventId);
       rs = stmt.executeQuery();

       // Elegxos an yparxei i ekdilwsi
       if (!rs.next()) {
           conn.rollback();
           sendErrorResponse(out, "Event not found");
           return;
       }

       // Elegxos an i ekdilwsi einai idi akyrwmeni
       String eventStatus = rs.getString("event_status");
       if ("cancelled".equals(eventStatus)) {
           conn.rollback();
           sendErrorResponse(out, "Event is already cancelled");
           return;
       }

       // Epeksergasia epistrofwn xrimatwn
       PreparedStatement refundStmt = conn.prepareStatement(
           "INSERT INTO payments (booking_id, customer_id, amount, payment_date, card_id, status) " +
           "VALUES (?, ?, ?, NOW(), ?, 0)");
           
       PreparedStatement updateBookingStmt = conn.prepareStatement(
           "UPDATE bookings SET status = 'cancelled' WHERE booking_id = ?");

       // Epeksergasia kathe kratisis
       do {
           int bookingId = rs.getInt("booking_id");
           if (bookingId != 0) { // Elegxos an yparxei i kratisi
               // Dimiourgia epistrofis xrimatwn
               refundStmt.setInt(1, bookingId);
               refundStmt.setInt(2, rs.getInt("customer_id"));
               refundStmt.setDouble(3, rs.getDouble("price"));
               refundStmt.setInt(4, rs.getInt("card_id"));
               refundStmt.addBatch();

               // Enimerwsi katastasis kratisis
               updateBookingStmt.setInt(1, bookingId);
               updateBookingStmt.addBatch();
           }
       } while (rs.next());

       // Ektelesi omadikou update
       refundStmt.executeBatch();
       updateBookingStmt.executeBatch();

       // Akyrwsi event
       stmt = conn.prepareStatement("UPDATE events SET status = 'cancelled' WHERE event_id = ?");
       stmt.setInt(1, eventId);
       int eventUpdateResult = stmt.executeUpdate();

       // Oloklirwsi i anaklisi allagon
       if (eventUpdateResult > 0) {
           conn.commit();
           sendSuccessResponse(out, "Event cancelled and all bookings refunded successfully");
       } else {
           conn.rollback();
           sendErrorResponse(out, "Failed to cancel event");
       }

   } catch (Exception e) {
       try {
           if (conn != null) conn.rollback();
       } catch (Exception ex) {
           ex.printStackTrace();
       }
       e.printStackTrace();
       sendErrorResponse(out, "Error processing request: " + e.getMessage());
   } finally {
       // Kleisimo resources
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

   // Voithitiki methodos gia tin apostoli minimatos lathous / epitixias
   private void sendErrorResponse(PrintWriter out, String message) {
       out.println("{\"success\": false, \"message\": \"" + message + "\"}");
   }

   private void sendSuccessResponse(PrintWriter out, String message) {
       out.println("{\"success\": true, \"message\": \"" + message + "\"}");
   }

   // Methodos gia tin diaxeirisi CORS
   @Override
   protected void doOptions(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
       response.setHeader("Access-Control-Allow-Origin", "*");
       response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
       response.setHeader("Access-Control-Allow-Headers", "Content-Type");
       response.setStatus(HttpServletResponse.SC_OK);
   }
}