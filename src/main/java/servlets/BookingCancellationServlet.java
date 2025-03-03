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

// Afto to servlet asxolite me tin akirosi ton kratiseon
public class BookingCancellationServlet extends HttpServlet {
   
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) 
           throws ServletException, IOException {
       response.setContentType("application/json");
       PrintWriter out = response.getWriter();
       
       // Elegxos an o xristis einai sindedemenos
       HttpSession session = request.getSession(false);
       if (session == null || session.getAttribute("customerId") == null) {
           out.println("{\"success\": false, \"message\": \"Not logged in\"}");
           return;
       }
       
       // Pername to ID tou pelati apo to session
       int customerId = (Integer) session.getAttribute("customerId");
       int bookingId;
       
       // Elegxos an to bookingId einai egkiro
       try {
           bookingId = Integer.parseInt(request.getParameter("bookingId"));
       } catch (NumberFormatException e) {
           out.println("{\"success\": false, \"message\": \"Invalid booking ID\"}");
           return;
       }
       
       Connection conn = null;
       PreparedStatement stmt = null;
       ResultSet rs = null;
       
       try {
           // Sindesi me ti vasi kai enarxi sinalagis
           conn = DatabaseConnection.getConnection();
           conn.setAutoCommit(false);
           
           // Elegxos an i kratisi iparxi kai einai energi
           String checkSql = "SELECT b.ticket_type_id, tt.price, p.card_id " +
                        "FROM bookings b " +
                        "JOIN ticket_types tt ON b.ticket_type_id = tt.type_id " +
                        "JOIN payments p ON b.booking_id = p.booking_id " +
                        "WHERE b.booking_id = ? AND b.customer_id = ? " +
                        "AND b.status = 'confirmed' " +
                        "AND p.status = 1";
           stmt = conn.prepareStatement(checkSql);
           stmt.setInt(1, bookingId);
           stmt.setInt(2, customerId);
           rs = stmt.executeQuery();
           
           // An den vrethike i kratisi
           if (!rs.next()) {
               conn.rollback();
               out.println("{\"success\": false, \"message\": \"Booking not found or already cancelled\"}");
               return;
           }
           
           // Apothikefsi stoixeion gia tin epistrofi xrimaton
           int ticketTypeId = rs.getInt("ticket_type_id");
           double refundAmount = rs.getDouble("price");
           int cardId = rs.getInt("card_id");
           
           // Enimerosi katastasis kratisis se 'cancelled'
           String updateBookingSql = "UPDATE bookings SET status = 'cancelled' WHERE booking_id = ?";
           stmt = conn.prepareStatement(updateBookingSql);
           stmt.setInt(1, bookingId);
           int updateResult = stmt.executeUpdate();
           
           if (updateResult <= 0) {
               conn.rollback();
               out.println("{\"success\": false, \"message\": \"Failed to update booking status\"}");
               return;
           }
           
           // Dimiourgia neou record gia tin epistrofi xrimaton
           String refundSql = "INSERT INTO payments (booking_id, customer_id, amount, payment_date, card_id, status) " +
                            "VALUES (?, ?, ?, NOW(), ?, 0)";
           stmt = conn.prepareStatement(refundSql);
           stmt.setInt(1, bookingId);
           stmt.setInt(2, customerId);
           stmt.setDouble(3, refundAmount);
           stmt.setInt(4, cardId);
           
           int refundResult = stmt.executeUpdate();
           
           if (refundResult <= 0) {
               conn.rollback();
               out.println("{\"success\": false, \"message\": \"Failed to process refund\"}");
               return;
           }
           
           // Ayksisi diathesimou arithmou eisitirion
           String updateTicketsSql = "UPDATE ticket_types SET quantity_available = quantity_available + 1 WHERE type_id = ?";
           stmt = conn.prepareStatement(updateTicketsSql);
           stmt.setInt(1, ticketTypeId);
           
           int ticketResult = stmt.executeUpdate();
           
           if (ticketResult <= 0) {
               conn.rollback();
               out.println("{\"success\": false, \"message\": \"Failed to update ticket availability\"}");
               return;
           }
           
           // Oloklirosi sinalagis
           conn.commit();
           out.println("{\"success\": true, \"message\": \"Booking cancelled and refunded successfully\"}");
           
       } catch (Exception e) {
           // Se periptosi sfalmatos, anairesi twn allagon
           if (conn != null) {
               try {
                   conn.rollback();
               } catch (Exception ex) {
                   ex.printStackTrace();
               }
           }
           e.printStackTrace();
           out.println("{\"success\": false, \"message\": \"Error cancelling booking: " + e.getMessage() + "\"}");
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