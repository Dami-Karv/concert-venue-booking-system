package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

// Afto to servlet xirizetai tin prosthiki diaforetikou tipou eisitirion
public class TicketTypeServlet extends HttpServlet {
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) 
         throws ServletException, IOException {
      
      // Thetoume ton tipo tis apantisis se JSON
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      Connection conn = null;
      PreparedStatement stmt = null;

      try {
          // Diavazoume ta dedomena pou stelni o client
          StringBuilder buffer = new StringBuilder();
          BufferedReader reader = request.getReader();
          String line;
          while ((line = reader.readLine()) != null) {
              buffer.append(line);
          }

          // Metatrepoume to JSON se Java object
          ObjectMapper mapper = new ObjectMapper();
          JsonNode rootNode = mapper.readTree(buffer.toString());
          
          // Pernoume to ID tis ekdilosis kai ta stoixeia ton eisitirion
          int eventId = rootNode.get("eventId").asInt();
          JsonNode tickets = rootNode.get("tickets");

          // Sindeomaste me ti vasi kai ksekiname transaction
          conn = DatabaseConnection.getConnection();
          conn.setAutoCommit(false);

          // Etoimazoume to SQL gia eisagogi eisitirion
          stmt = conn.prepareStatement(
              "INSERT INTO ticket_types (event_id, type_name, price, quantity_available) VALUES (?, ?, ?, ?)");

          // Gia kathe tipo eisitiriou pou stalthike
          for (JsonNode ticket : tickets) {
              String ticketType = ticket.get("ticket_type").asText();
              double price = Double.parseDouble(ticket.get("ticket_price").asText());
              int amount = Integer.parseInt(ticket.get("ticket_amount").asText());

              // Prosthetoume ta stoixeia sti batch entoli
              stmt.setInt(1, eventId);
              stmt.setString(2, ticketType);
              stmt.setDouble(3, price);
              stmt.setInt(4, amount);
              stmt.addBatch();
          }

          // Ekteloume oles tis entoles mazi
          int[] results = stmt.executeBatch();
          boolean success = true;
          
          // Elegxoume an oles oi entoles ektelestikan epitixos
          for (int i = 0; i < results.length; i++) {
              if (results[i] <= 0) {
                  success = false;
                  break;
              }
          }

          // An ola pigan kala kanoume commit, allios rollback
          if (success) {
              conn.commit();
              out.println("{\"success\":true}");
          } else {
              conn.rollback();
              out.println("{\"success\":false}");
          }

      } catch (Exception e) {
          // Se periptosi sfalmatos
          out.println("{\"success\":false}");
          try {
              if (conn != null) {
                  conn.rollback();
              }
          } catch (SQLException ex) {
              ex.printStackTrace();
          }
      } finally {
          // Kleisimo olon ton sindeseon
          try {
              if (stmt != null) stmt.close();
              if (conn != null) {
                  conn.setAutoCommit(true);
                  conn.close();
              }
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }
  }
}