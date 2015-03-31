
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kostas
 */
public class Scheduler extends Thread
{
    static Logger log = Logger.getLogger(controlServlet.class .getName());
    
    BlockingQueue[] queue;
    BlockingQueue[] esbResponses;
    int numberOfDomains;
    double[] timeOfOperationPSD;
    double[] utilizationPSD;
    double[] sla;
    double totalTimeOfOperation=0;
    boolean suspendFlag=true;
    Thread thread;
    List<Integer> listOfDomainsBelowGoal;
    List<Boolean> listOfDomainsWithPacketsInQueue;
    int[] requestsServedPSD;
    boolean followingRequest=false;
    
    public Scheduler(int numberOfDomains,BlockingQueue[] queue) {
        this.numberOfDomains=numberOfDomains;
        this.queue = queue;
       
        this.suspendFlag=false;
        this.thread=new Thread(this,"scheduler");
        
        this.timeOfOperationPSD=new double[numberOfDomains];
        this.sla=new double[numberOfDomains];
        this.utilizationPSD=new double[numberOfDomains];
        this.requestsServedPSD=new int[numberOfDomains];
        
        this.esbResponses=new BlockingQueue[numberOfDomains];
        for (int i = 0; i < numberOfDomains; i++) {
             esbResponses[i]=new ArrayBlockingQueue(10000);
         }
        setTheSLA();
    }

    public Thread getThread() {
        return thread;
    }
    
    
        public void run()
        {
            while(true){
             
               scheduleTheRoundAndSend();
               mususpend();
                   //  log.info("Scheduler is active");
                  
               synchronized(this){
                  
                      while(suspendFlag){
                     //        log.info("Scheduler is inactive");
                       try { 
                              wait();
                          } catch (InterruptedException ex) {
                              java.util.logging.Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
                          }
                   }
               
           }
          
        }
    }
    
    void mususpend(){
        suspendFlag=true;
    }    
    synchronized void myresure(){
        suspendFlag=false;
     //    log.info("flag changed to false");
          
        notify();
    
    }
    private boolean addESBResponseMessagetoQueue(String soapMessage) {
      
        try {
        
            Hashtable parameters=Utilities.getMessageParameters(soapMessage);

            String id=String.valueOf(parameters.get("domainID"));
            int domainID=Integer.valueOf(id);

            requestsServedPSD[domainID]++;
            esbResponses[domainID].add(soapMessage);
            log.info("SERVER: "+requestsServedPSD[0]+" - "+requestsServedPSD[1]+"- "+requestsServedPSD[2]);
            
            return true;
        } 
        catch (Exception e) {
            
        }
        
        return false;
    }
   
    public String sendSoapMessagetoFakeESB(String data){
    
        String service= "http://localhost:8080/ESB/ESBServlet";
        HttpPost post = new HttpPost(service);
        String soapRequest=data;
        long timeBeforeTheRequest;
        long timeAfterTheResponse;
        double serviceDurationInESB;
        
        try {
        
            post.setEntity(new StringEntity(soapRequest));
            post.setHeader("Content-type", "text/xml; charset=UTF-8");
            post.setHeader("SOAPAction", service);
            post.setHeader("Host","127.0.0.1");
            
            final HttpParams hhtpParams=new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(hhtpParams,10000);
            HttpClient client = new DefaultHttpClient(hhtpParams);
            
            //Stub the time before the request is made
            timeBeforeTheRequest=System.currentTimeMillis();
            
            HttpResponse response = client.execute(post);
            
             //Stub the time after the response is received 
            timeAfterTheResponse=System.currentTimeMillis();
            
            serviceDurationInESB=timeAfterTheResponse-timeBeforeTheRequest;
            log.info("message sended to ESB.Duration: "+serviceDurationInESB);
            
            updateStatistics(serviceDurationInESB,soapRequest);
            
            String esbResponse = EntityUtils.toString(response.getEntity());
            
            return esbResponse;
            
        } 
        catch (Exception e) {
        }
        
        return "";

        // SOAP response(xml) get
      

       
    }
    
    
    public String sendReceiveSoapMessagetoESB(Hashtable parametersList){
    
        HttpPost post = new HttpPost("http://localhost:8280/services/StockQuoteProxy");
        String soapRequest=Utilities.prepareSoapToESB(parametersList);
        
        try {
        
            post.setEntity(new StringEntity(soapRequest));
            post.setHeader("Content-type", "text/xml; charset=UTF-8");
            post.setHeader("SOAPAction", "urn:getQuote");
            post.setHeader("Host","127.0.0.1:8280");

            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(post);
            String res_xml = EntityUtils.toString(response.getEntity());
            
            return res_xml;
            
        } catch (Exception e) {
        }
        
        return "";

        // SOAP response(xml) get
      

       
    }

    private void updateStatistics(double serviceDurationInESB,String soapRequest) {
       
        if(followingRequest){
            Hashtable parameters=Utilities.getMessageParameters(soapRequest);
            int domainID=Integer.valueOf(String.valueOf(parameters.get("domainID")));
       
        totalTimeOfOperation+=serviceDurationInESB;
        timeOfOperationPSD[domainID]+=serviceDurationInESB;
       
        for (int i = 0; i < numberOfDomains; i++) {
              utilizationPSD[i]=(double)timeOfOperationPSD[i]/totalTimeOfOperation;
        }
      
        log.info("CPU time: "+timeOfOperationPSD[0]+"-"+timeOfOperationPSD[1]+"-"+timeOfOperationPSD[2]);
        log.info("CPU util: "+utilizationPSD[0]+"-"+utilizationPSD[1]+"-"+utilizationPSD[2]);
        }
        
        followingRequest=true;
    }

    public void scheduleTheRoundAndSend() {
    
       
       
        List<Integer> listToSchedule= findScheduleList();
        
        String data;
        String esbResponse;
        
        try {
        
        if( listToSchedule.isEmpty()) {
            return;
        }
        
            for (int i = 0; i < listToSchedule.size(); i++) {
                data = (String) queue[listToSchedule.get(i)].poll();
                     log.info("List: "+listToSchedule.get(i));
            //String esbResponse=sendReceiveSoapMessagetoESB(Utilities.getRequestParameters(data));
                esbResponse=sendSoapMessagetoFakeESB(data);
                addESBResponseMessagetoQueue(esbResponse);
            }
           
           suspendFlag=true;
           
           
           
        } catch (Exception e) {
        }
      
    }
    
    public List<Integer> findAllDomainsBelowGoal()
    {
        // satisfaction = {under,over}
        
        List<Integer> _listOfDomainsBelowGoal=new ArrayList<Integer>();
        
        double tempDeviation;
        
        for (int i = 0; i < numberOfDomains; i++) {
            tempDeviation=utilizationPSD[i]-sla[i];

            if(tempDeviation<0){
               _listOfDomainsBelowGoal.add(i);
            }
         } 

         return _listOfDomainsBelowGoal;
    }
    
    public List<Integer> findListOfDomainsWithPacketsInQueue(){
    
        List<Integer> _listOfDomainsWithPacketsInQueue=new ArrayList<Integer>();
    
        for (int i = 0; i < numberOfDomains; i++) {
            
            if(queue[i].size()>0){
            _listOfDomainsWithPacketsInQueue.add(i);
            }
                
        }
        
        return _listOfDomainsWithPacketsInQueue;
    }

    private List<Integer> findScheduleList() {
        
        
        try {
            
        
        Random rand=new Random();
        List<Integer> allDomainsBelowGoal=findAllDomainsBelowGoal();
        List<Integer> listOfDomainsWithPackets=findListOfDomainsWithPacketsInQueue();
        List<Integer> idsToRound=new ArrayList<Integer>();
        boolean emptyRound;
        
             for (int j = 0; j < numberOfDomains; j++) {
                    if(allDomainsBelowGoal.contains(j)&listOfDomainsWithPackets.contains(j)){
                              idsToRound.add(j);
                    }
             }

             if(idsToRound.isEmpty()){
                emptyRound=true;
             }
             else {
                 emptyRound=false;
             }
             
             int validDomainsNumber;
             int validQueue;
             
             validDomainsNumber=listOfDomainsWithPackets.size();
             
             if(emptyRound==true&validDomainsNumber>0){
                    validQueue= listOfDomainsWithPackets.get(rand.nextInt(validDomainsNumber));
                  idsToRound.add(validQueue);
             }
        
             return idsToRound;
       } 
        catch (Exception e) {
      //      log.info(e.getStackTrace());
        }
        
        return null;
    }
    
    private void setTheSLA()
    {
        for (int i = 0; i < numberOfDomains; i++) {
            sla[i]=(double)1/numberOfDomains;
        }
        
//        sla[0]=0.5;
//        sla[1]=0.3;
//        sla[2]=0.2;
    }

    public int[] getRequestsServedPSD() {
        return requestsServedPSD;
    }
    
    
}
        

