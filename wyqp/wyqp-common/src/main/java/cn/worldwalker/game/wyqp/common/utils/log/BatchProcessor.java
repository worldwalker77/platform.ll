package cn.worldwalker.game.wyqp.common.utils.log;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public  class BatchProcessor<T>{

  public final static Log logger = LogFactory.getLog(BatchProcessor.class);
  private  ExecutorService executor;  // 执行器
  private  ScheduledExecutorService scheduledExecutor; //调度器
  
  private AtomicInteger messageCounts = new AtomicInteger(0);
  
  private DiyQueue<T> localQueue=null;
  
  private BatchTask<T> task;

  public void doBatchProcess(List<T> list){
	  task.doBatchProcess(list);
  }
  
  private  boolean putAndP(T object) throws InterruptedException{
	    this.localQueue.put(object);
	    if (this.messageCounts.incrementAndGet() > this.localQueue.getBatchCount())
	    {
	      this.messageCounts.set(0);
	      return true;
	    }
	    return false;
  }
  private  void asyncProcess(){
	  executor.execute(getTask());
  }

  public  Runnable getTask(){
    return new Runnable(){
		public void run(){
	        try{
	          List<List<T>> toSend = localQueue.getSendLogs();
	          for (List<T> list : toSend) {
	        	  if(CollectionUtils.isNotEmpty(list)){
	        	  doBatchProcess(list); // 这里是异步还是同步关系很大，现在按照同步处理
	        	  }
	    	  }
	        }
	        catch (Exception e){
	        	logger.error("doBatchProcess error" ,e);
	        }
	      }
    };
  }
 
  public  void processAwait(T object){
   
	    try{
	      if (putAndP(object)) {  // 主动触发
	    	  asyncProcess();
	      }
	    }
	    catch (Exception e){
	      logger.error("", e);
	    }
  }

  public BatchProcessor(ScheduledExecutorService ses,ExecutorService ect,
		  int maxCount,int bacthCount,BatchTask<T> task){
     this.scheduledExecutor=ses;
     this.executor=ect;
     this.task=task;
     this.localQueue =new DiyQueue<T>(maxCount,bacthCount);
     scheduledExecutor.scheduleAtFixedRate(getScheduleTask(), 3000L, 5000L, TimeUnit.MILLISECONDS);
  }

private Runnable getScheduleTask() {
	return new Runnable(){
      public void run(){
       asyncProcess();
      }
    };
}
  
  
}
