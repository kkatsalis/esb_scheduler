/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author kostas
 */
@WebServlet(name = "ESBServlet", urlPatterns = {"/ESBServlet"})
public class ESBServlet extends HttpServlet {

    static int messageCounter=0;
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
          
            
        ServletInputStream i = request.getInputStream();
        int c = 0;
        String xmlrpc = "";

        while((c = i.read()) != -1 ){ xmlrpc += (char)c; }
        
        String xmlResponse= prepareESBResponse(xmlrpc);
       
        //RETURN RESPONSE.    
        response.getWriter().println(xmlResponse);
         messageCounter++;    
            
            
        } finally {            
            out.close();
        }
    }

   public String prepareESBResponse(String soapRequestMessage)
    {
     //directly forward the esb response
//        if(true){
//            return esbResponse;
//        }
        
       Hashtable parameters=Utilities.getRequestParameters(soapRequestMessage);
        
        //PREPARE OUTPUT.
       String  xml = "";            
                  xml += "<?xml version='1.0'?>                     \n"; 
                  xml += "<SOAP-ENV:Envelope>                       \n";            
                  xml += "  <SOAP-ENV:Body>                         \n";
                  xml += "    <domainID>                            \n"; 
                  xml += "      "+parameters.get("domainID")+      "\n"; 
                  xml += "    </domainID>                           \n";
                  xml += "    <customerID>                          \n"; 
                  xml += "      "+parameters.get("customerID")+    "\n"; 
                  xml += "    </customerID>                         \n";
                  xml += "    <requestID>                          \n"; 
                  xml += "      "+parameters.get("requestID")+     "\n"; 
                  xml += "    </requestID>                         \n";
                  xml += "    <requestQuote>                        \n"; 
                  xml += "      "+parameters.get("requestQuote")+  "\n"; 
                  xml += "    </requestQuote>                      \n";
                  xml += "     <messageStatus>                      \n";
                  xml += "          ReceivedByESB                   \n";
                  xml += "     </messageStatus>                      \n";
                  xml += "  </SOAP-ENV:Body>                       \n";
                  xml += "</SOAP-ENV:Envelope>                      \n"; 
    
                return xml;
    }
    
   
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
