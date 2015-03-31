
import java.util.Hashtable;
import java.util.UUID;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kostas
 */
public class Utilities {
    
    public static String prepareSoapToESB(Hashtable parameters){
    
        UUID uuid = UUID.randomUUID();
        
        String soap=
            "<?xml version='1.0'?>"+
           "<soapenv:Envelope xmlns:wsa=\"http://www.w3.org/2005/08/addressing/\" "+
                             "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
             " <soapenv:Header>"+ 
                 "<wsa:To>\"http://localhost:9000/services/SimpleStockQuoteService/\"</wsa:To>"+
                 "<wsa:MessageID>"+uuid.toString()+"</wsa:MessageID>"+
                 "<wsa:Action>urn:getQuote</wsa:Action>"+
             "</soapenv:Header>"+
            "<soapenv:Body>"+
                 "<m0:getQuote xmlns:m0=\"http://services.samples\">"+
                    "<m0:request>"+ 
                       "<m0:symbol>"+parameters.get("requestQuote") +"</m0:symbol>"+
                    "</m0:request>"+
                 "</m0:getQuote>"+
              "</soapenv:Body>"+
           "</soapenv:Envelope>";
         
         
        return soap;
    }
    
    
    
     public static Hashtable getMessageParameters(String soapMessage) {
      
        Hashtable<String,String> parametersList = new Hashtable<String, String>();
      
        String[] parameters=new String[4];
        
        for (int i = 0; i < 4; i++) {
            parameters[i]= soapMessage;
        }
        int startTag;
        int endTag;
        
          //EXTRACT PARAMETERS.
        startTag = parameters[0].indexOf("<domainID>");
        endTag   = parameters[0].indexOf("</domainID>");
        parameters[0] = parameters[0].substring(startTag,endTag).replaceAll("<domainID>","").replaceAll("</domainID>","").trim();
        parameters[0] = parameters[0].trim();
        parametersList.put("domainID", parameters[0]);
        
         startTag = parameters[1].indexOf("<customerID>");
        endTag   = parameters[1].indexOf("</customerID>");
        parameters[1] = parameters[1].substring(startTag,endTag).replaceAll("<customerID>","").replaceAll("</customerID>","").trim();
        parameters[1] = parameters[1].trim();
        parametersList.put("customerID", parameters[1]);
        
         startTag = parameters[2].indexOf("<requestID>");
        endTag   = parameters[2].indexOf("</requestID>");
        parameters[2] = parameters[2].substring(startTag,endTag).replaceAll("<requestID>","").replaceAll("</requestID>","").trim();
        parameters[2] = parameters[2].trim();
        parametersList.put("requestID", parameters[2]);
        
        startTag = parameters[3].indexOf("<requestQuote>");
        endTag   = parameters[3].indexOf("</requestQuote>");
        parameters[3] = parameters[3].substring(startTag,endTag).replaceAll("<requestQuote>","").replaceAll("</requestQuote>","").trim();
        parameters[3] = parameters[3].trim();
        parametersList.put("requestQuote", parameters[3]);
        
        return parametersList;
    }
     
     
     
     
}
