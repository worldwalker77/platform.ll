package cn.worldwalker.game.wyqp.common.utils.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.worldwalker.game.wyqp.common.result.Result;
import cn.worldwalker.game.wyqp.common.utils.ApplicationContextUtil;


public class ThreadPoolMgr {
	public final static Log logger = LogFactory.getLog(ThreadPoolMgr.class);
	
	  private static ExecutorService executor;  // 执行器
	  private static ScheduledExecutorService scheduledExecutor; //调度器
	  private static BlockingQueue<Runnable> queueToUse = new LinkedBlockingQueue<Runnable>(20);
	  
	  private static List<Runnable> cleanedTask=new ArrayList<>();
	  
	  private static volatile  BatchProcessor<Result>  logDataInsertProcessor;

	  private static ThreadPoolMgr instance=new ThreadPoolMgr();
	  
	  public static ThreadPoolMgr getInstance(){
		  return instance;
	  }
	  
	  public static synchronized BatchProcessor<Result> getLogDataInsertProcessor(){
		  if(logDataInsertProcessor!=null){
			  return logDataInsertProcessor;
		  }
		  try{
			  LogDataService task=ApplicationContextUtil.ctx.getBean(LogDataService.class);
			  logDataInsertProcessor=new BatchProcessor<Result>(scheduledExecutor, executor, 1000, 100,task);
		      cleanedTask.add(logDataInsertProcessor.getTask());
		      logger.info("init BatchProcessor<QdGroupMsgLogDetailModel> success");

		  }catch(Exception e){
			  logger.error("init BatchProcessor<QdGroupMsgLogDetailModel> error",e);
		  }
		 
		  return logDataInsertProcessor;
	  }
	  
	  private ThreadPoolMgr(){
		  try{
		      ThreadFactory tf = new ThreadFactory()
		      {
		        public Thread newThread(Runnable r)
		        {
		          Thread t = new Thread(r, "ThreadPoolMgr executor");
		          t.setDaemon(true);
		          return t;
		        }
		      };
		      
		      executor = new ThreadPoolExecutor(2, 8, 60L, TimeUnit.SECONDS, queueToUse, tf, new ThreadPoolExecutor.DiscardOldestPolicy());
		      ThreadFactory tf2 = new ThreadFactory()
		      {
		        public Thread newThread(Runnable r)
		        {
		          Thread t = new Thread(r, "ThreadPoolMgr-thread");
		          
		          return t;
		        }
		      };
		      scheduledExecutor = Executors.newScheduledThreadPool(2, tf2);
		      
		      Runtime.getRuntime().addShutdownHook(new Thread(getDistoryTask()));
		      
		    }catch (Exception e){
		      logger.error( "ThreadPoolMgr contruct failed",e);
		    }
	  }
		  
	  
	  static void cleanup(){
		  for (Runnable runnable : cleanedTask) {
			executor.execute(runnable);
		  }
	  }
	  
	  static class DistoryTask implements Runnable{
		    @Override
		    public void run() {
		        try {
		        	cleanup();
		            try {
		                destroy();
		                
		            } catch (Exception e) {
		                if (null !=logger) {
		                    logger.error("Error occurs for destroy().", e);
		                }
		            }
		            return;
		        } catch (Exception e) {
		        	cleanup();
		            try {
		                destroy();
		                
		            } catch (Exception ex) {
		                if (null !=logger) {
		                    logger.error("Error occurs for destroy().", ex);
		                }
		            }
		        } finally {
		        	cleanup();
		            try {
		                destroy();
		                
		            } catch (Exception e) {
		                if (null !=logger) {
		                    logger.error("Error occurs for destroy().", e);
		                }
		            }
		        }
		    }
	  }
	  
	  public static void destroy(){
	    try{
	      if (null != executor){
	        executor.shutdown();
	        executor.awaitTermination(1L, TimeUnit.MINUTES);
	      }
	    }
	    catch (Exception e){
	      if (null != logger) {
	        logger.error("Exception occurs when shuting down the executor:\n{}", e);
	      }
	    }
	    try{
	      if (null != scheduledExecutor){
	        scheduledExecutor.shutdown();
	        scheduledExecutor.awaitTermination(1L, TimeUnit.MINUTES);
	      }
	    }
	    catch (Exception e){
	      if (null != logger) {
	        logger.error("Exception occurs when shuting down the scheduledExecutor:\n{}", e);
	      }
	    }
	  }
		
		/**
	       * jvm shutdown  hook 
	       * 发送完  队列剩余的消息
	       * @return
	       */
      public static Runnable getDistoryTask() {
	      //由于sonar限制匿名内部类max20行，所以只能改为非匿名的内部类了。
		  return new DistoryTask();
	  }
}
