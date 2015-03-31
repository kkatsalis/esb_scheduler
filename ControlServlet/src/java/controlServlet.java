/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
/**
 *
 * @author kostas
 */
@WebServlet(name = "controlServlet", urlPatterns = {"/controlServlet"})
public class controlServlet extends HttpServlet {

    static Logger log = Logger.getLogger(controlServlet.class .getName());
    
//     BlockingQueue[] queue;
//     BlockingQueue[] esbResponses;
     
     BlockingQueue[] queue;
     Scheduler scheduler;
     TimeController timecontroller;
    
     static int messageCounter=0;
     final int numberOfDomains=3;
     private final ScheduledExecutorService executroService=Executors.newSingleThreadScheduledExecutor();;
     int[] requestsReceivedPSD;    
     
     public controlServlet(){
         
         queue=new BlockingQueue[numberOfDomains];
        
         requestsReceivedPSD=new int[numberOfDomains];
         
         for (int i = 0; i < numberOfDomains; i++) {
             queue[i]=new ArrayBlockingQueue(10000);
         }
         
         activateThreads();
         
      
    }
     
     public final void activateThreads(){
     scheduler=new Scheduler(numberOfDomains, queue);
      timecontroller=new TimeController(scheduler);
      executroService.scheduleWithFixedDelay(timecontroller, 1000, 50, TimeUnit.MILLISECONDS);
      scheduler.start();
     }
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
     static final long serialVersionUID = 1;
   
     
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
        
            //GET REQUEST BODY.       
            ServletInputStream i = request.getInputStream();
            int c = 0;
            String xmlrpc = "";

            while((c = i.read()) != -1 ){ xmlrpc += (char)c; }

             messageCounter++;
            
             
             //Step 1: Enqueue Request
            boolean messageAdded=addMessagetoQueue(xmlrpc);

            
            if(messageAdded)
            {
                String xmlResponse= prepareControllerResponseToClient(xmlrpc);

                //Step2: Return Message Received Response    
                response.getWriter().println(xmlResponse);
           }
            
        } 
        finally {            
            out.close();
        }
    }
 
    
    
    public String prepareControllerResponseToClient(String soapRequestMessage)
    {
        
       Hashtable parameters=Utilities.getMessageParameters(soapRequestMessage);
        
       String ackMessage="messageReceivedByController";
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
                  xml += "     "+ackMessage+"                       \n";
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

   

    private synchronized boolean addMessagetoQueue(String soapMessage) {
      
        try {
        
            Hashtable parameters=Utilities.getMessageParameters(soapMessage);

            String id=String.valueOf(parameters.get("domainID"));
            int domainID=Integer.valueOf(id);

            queue[domainID].add(soapMessage);

            requestsReceivedPSD[domainID]++;
            log.info("RS: "+
                    requestsReceivedPSD[0]+"("+scheduler.getRequestsServedPSD()[0]+")"+" - "+
                    requestsReceivedPSD[1]+"("+scheduler.getRequestsServedPSD()[1]+")"+" - "+
                    requestsReceivedPSD[2]+"("+scheduler.getRequestsServedPSD()[2]+")");
            return true;
        } 
        catch (Exception e) {
            log.error(e.getStackTrace());
        }
        
        return false;
    }
}
