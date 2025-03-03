package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Anaktisi tou trexontos session, an yparxei (false = den dimiourgei neo an den yparxei)
        HttpSession session = request.getSession(false);
        
        // Elegxos an yparxei energo session
        if (session != null) {
            // Akyronoume to session
            session.invalidate();
        }      
        // Anakatefthinoume ton xristi stin arxiki selida
        response.sendRedirect("index.html");
    }
}