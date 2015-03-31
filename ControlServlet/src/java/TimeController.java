
import org.apache.log4j.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kostas
 */
public class TimeController implements Runnable{
    
    static Logger log = Logger.getLogger(TimeController.class .getName());
    
   
    long previousTime;
    long currentTime;
    Scheduler scheduler;
  
    
    public TimeController(Scheduler scheduler){
        this.scheduler=scheduler;
    }
    
    @Override
    public void run(){
        process();
    }
    
    public void process(){
        
        currentTime=System.currentTimeMillis();
        
     //   log.info("timer hit at "+currentTime+" after "+(currentTime-previousTime));
        scheduler.myresure();
        previousTime=currentTime;
        
    }
    
}
