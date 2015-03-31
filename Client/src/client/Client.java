/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;


import java.util.Hashtable;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
/**
 *
 * @author kostas
 */
public class Client {

    static int numberOfDomains=3;
    static int _requestID=0;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        
       int domainID=Integer.parseInt(args[0]);
       int interval=Integer.parseInt(args[1]);
      NewClientThread client1 =new NewClientThread(domainID, interval);
        
       client1.send();
     
    
    }
    
    public static class NewClientThread implements Runnable{
        
        int domainID;
        int period;
        int numberofRequests=10000;
        Thread t;
   
        public NewClientThread(int domainID, int period){
        
            this.domainID=domainID;
            this.period=period;
            t=new Thread();
            
            t.start();
            
        }
        
        public void run(){}
            
        public void send(){
            
        try {
        for (int i = 0; i < numberofRequests; i++) {
              String response=sendReceiveSoapMessagetoController(domainID);
             //  System.out.println(response); 
                    Thread.sleep(period);
                } 
        }
        catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
       
        }
        
    }
    public static String sendReceiveSoapMessagetoController(int domainID){
    
        String service="http://localhost:8080/ControlServlet/controlServlet";
        
        HttpPost post = new HttpPost(service);
        String soapRequest=prepareSoapMessage(domainID);
        
        try {
        
            post.setEntity(new StringEntity(soapRequest));
            post.setHeader("Content-type", "text/xml; charset=UTF-8");
            post.setHeader("SOAPAction", service);
            post.setHeader("Host","127.0.0.1:8080");

            final HttpParams hhtpParams=new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(hhtpParams,10000);
            HttpClient client = new DefaultHttpClient(hhtpParams);
            
            HttpResponse response = client.execute(post);
            String res_xml = EntityUtils.toString(response.getEntity());
            
            return res_xml;
            
        } catch (Exception e) {
        }
        
        return "";

        // SOAP response(xml) get
      

       
    }

    private static void handleResponse(String response) {
         
        // System.out.println(response);
         
         String result = response;
         String requestor= response;
         boolean passthrough=true; 
         
         if (passthrough){
         
             System.out.println(response);
         }
         else {
             int startTag = requestor.indexOf("<requestQuote>");
             int endTag   = requestor.indexOf("</requestQuote>");
             requestor =    requestor.substring(startTag,endTag).replaceAll("<requestQuote>","").replaceAll("</requestQuote>","");
             requestor = requestor.trim();

              int    startTagB  = result.indexOf("<result>");
              int    endTagB   = result.indexOf("</result>");
              result = result.substring(startTagB,endTagB).replaceAll("<result>","").replaceAll("</result>","");     
              result = result.trim();


              //DISPLAY RESULT.
              System.out.println("Requestor:"+requestor+" Result="+result);
         }
         
         
    }
    
    private static String prepareSoapMessage(int _domainID) {
       Random rand=new Random();
       
       String requestQuote  = "IBM";  
       String domainID=String.valueOf(_domainID);
       //domainID="2";
       UUID idOne = UUID.randomUUID();
     
        
       String customerID=idOne.toString();
       String requestID = String.valueOf(_requestID);
       _requestID++; 
      
       String  xml = "";            
                  xml += "<?xml version='1.0'?>                     \n"; 
                  xml += "<SOAP-ENV:Envelope>                       \n";            
                  xml += "  <SOAP-ENV:Body>                         \n";
                  xml += "    <domainID>                            \n"; 
                  xml += "      "+domainID+                        "\n"; 
                  xml += "    </domainID>                           \n";
                  xml += "    <customerID>                          \n"; 
                  xml += "      "+customerID+                      "\n"; 
                  xml += "    </customerID>                         \n";
                  xml += "    <requestID>                          \n"; 
                  xml += "      "+requestID+                      "\n"; 
                  xml += "    </requestID>                         \n";
                  xml += "    <requestQuote>                        \n"; 
                  xml += "      "+requestQuote+                    "\n"; 
                  xml += "    </requestQuote>                      \n";
                  xml += "  </SOAP-ENV:Body>                       \n";
                  xml += "</SOAP-ENV:Envelope>                      \n"; 
    
    //System.out.println(xml);
    //    System.out.println("Request for: "+requestQuote+"\n");
        
                  return xml;
    }
    
}
