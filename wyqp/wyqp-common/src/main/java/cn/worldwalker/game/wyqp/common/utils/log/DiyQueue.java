package cn.worldwalker.game.wyqp.common.utils.log;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DiyQueue<T>{
	public final static Log logger = LogFactory.getLog(BatchProcessor.class);
  private int batchCount = 100;// 批次大小
  private int maxCount = 3000; // 队列最大大小
  private final BlockingQueue<T> blockingQueue = new LinkedBlockingQueue();
  
  
  public DiyQueue(int maxCount,int bacthCount){
	  this.maxCount=maxCount;
	  this.batchCount=bacthCount;
  }
  public void put(T bizLog)
    throws InterruptedException{
    if (this.blockingQueue.size() < maxCount){
      this.blockingQueue.put(bizLog);
    }else{
      logger.error(bizLog.getClass().getSimpleName() + "'s BlockingQueue is more than max count,drop current object.");
    }
  }
  /**
   * 每次拿一批次
   * @Title: getSendLogs    
   * @Description: TODO    
   * @return   
   * @return List<List<T>>
   */
  public List<List<T>> getSendLogs(){
    List<T> all = new ArrayList();
    List<List<T>> result = new ArrayList();
    this.blockingQueue.drainTo(all);
    int size = all.size();
    if ((size != 0) && (size <= this.batchCount)){
      result.add(all);
      return result;
    }
    List<T> curtList = new ArrayList();
    result.add(curtList);
    for (int i = 0; i < size; i++) {
      if ((i != 0) && (i % this.batchCount == 0)){
        curtList = new ArrayList();
        curtList.add(all.get(i));
        result.add(curtList);
      }else{
        curtList.add(all.get(i));
      }
    }
    return result;
  }
  
  public BlockingQueue<T> getBlockingQueue(){
    return this.blockingQueue;
  }
public int getBatchCount() {
	return batchCount;
}
public void setBatchCount(int batchCount) {
	this.batchCount = batchCount;
}
public int getMaxCount() {
	return maxCount;
}
public void setMaxCount(int maxCount) {
	this.maxCount = maxCount;
}
  

}
