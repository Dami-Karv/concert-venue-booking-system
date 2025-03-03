package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import utils.DatabaseConnection;
import java.sql.Date;
import java.sql.Time;

/*
* Servlet gia tin dimiourgia neas ekdilosis
* Dexetai JSON request me ta stoixeia tis ekdilosis kai ta kataxorei sti vasi
*/
public class CreateEventServlet extends HttpServlet {
   
@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) 
       throws ServletException, IOException {
   System.out.println("CreateEventServlet: Starting event creation");
   response.setContentType("application/json");
   response.setCharacterEncoding("UTF-8");
   PrintWriter out = response.getWriter();

   // Elegxos dikaiomaton admin
   HttpSession session = request.getSession(false);
   if (session == null || !"admin".equals(session.getAttribute("userRole"))) {
       out.println("{\"success\": false, \"message\": \"Unauthorized access\"}");
       return;
   }

   try {
       // read kai parse tou JSON body
       StringBuilder buffer = new StringBuilder();
       BufferedReader reader = request.getReader();
       String line;
       while ((line = reader.readLine()) != null) {
           buffer.append(line);
       }
       
       ObjectMapper mapper = new ObjectMapper();
       JsonNode rootNode = mapper.readTree(buffer.toString());
       
       // Eksagogi timwn apo to JSON
       String name = rootNode.get("name").asText();
       String eventType = rootNode.get("event_type").asText();
       String eventDate = rootNode.get("event_date").asText();
       String eventTime = rootNode.get("event_time").asText();
       int venueCapacity = rootNode.get("venue_capacity").asInt();
       String status = "active";

       // Elegxos xoritikotitas
       if (venueCapacity <= 0 || venueCapacity > 1500) {
           out.println("{\"success\": false, \"message\": \"Venue capacity must be between 1 and 1500\"}");
           return;
       }

       Connection conn = null;
       PreparedStatement stmt = null;

       try {
           conn = DatabaseConnection.getConnection();
           conn.setAutoCommit(false);

           // Eisagogi ekdilosis sti vasi
           String sql = "INSERT INTO events (name, event_type, event_date, event_time, " +
           "venue_capacity, status) " +
           "VALUES (?, ?, ?, ?, ?, ?)";

           stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
           stmt.setString(1, name);
           stmt.setString(2, eventType);
           stmt.setDate(3, Date.valueOf(eventDate));
           stmt.setTime(4, Time.valueOf(eventTime));
           stmt.setInt(5, venueCapacity);
           stmt.setString(6, status);

           int rowsAffected = stmt.executeUpdate();

           // Elegxos epityxias kai epistrofi ID
           if (rowsAffected > 0) {
               ResultSet generatedKeys = stmt.getGeneratedKeys();
               if (generatedKeys.next()) {
                   int eventId = generatedKeys.getInt(1);
                   conn.commit();
                   out.println("{\"success\": true, \"message\": \"Event created successfully\", \"eventId\": " + eventId + "}");
               } else {
                   conn.rollback();
                   out.println("{\"success\": false, \"message\": \"Failed to get event ID\"}");
               }
           } else {
               conn.rollback();
               out.println("{\"success\": false, \"message\": \"Failed to create event\"}");
           }

       } catch (SQLException e) {
           System.err.println("Sfalma sti vasi dedomenwn:");
           e.printStackTrace();
           if (conn != null) {
               try {
                   conn.rollback();
               } catch (SQLException ex) {
                   ex.printStackTrace();
               }
           }
           out.println("{\"success\": false, \"message\": \"Database error: " + e.getMessage() + "\"}");
       } finally {
           // Kleisimo porwn
           if (stmt != null) {
               try {
                   stmt.close();
               } catch (SQLException e) {
                   e.printStackTrace();
               }
           }
           if (conn != null) {
               try {
                   conn.setAutoCommit(true);
                   conn.close();
               } catch (SQLException e) {
                   e.printStackTrace();
               }
           }
       }
   } catch (Exception e) {
       System.err.println("Geniko sfalma:");
       e.printStackTrace();
       out.println("{\"success\": false, \"message\": \"Invalid request format\"}");
   }
}

   // Methodos gia xeirismo CORS requests
   @Override
   protected void doOptions(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
       response.setHeader("Access-Control-Allow-Origin", "*");
       response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
       response.setHeader("Access-Control-Allow-Headers", "Content-Type");
       response.setStatus(HttpServletResponse.SC_OK);
   }
}