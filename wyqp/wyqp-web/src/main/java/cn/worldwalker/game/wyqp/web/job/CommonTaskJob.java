package cn.worldwalker.game.wyqp.web.job;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.worldwalker.game.wyqp.common.channel.ChannelContainer;
import cn.worldwalker.game.wyqp.common.service.RedisOperationService;

@Component(value="commonTaskJob")
public class CommonTaskJob{
	 
	@Autowired
	private RedisOperationService redisOperationService;
	@Autowired
	private ChannelContainer channelContainer;
	
	private static long smsOverTime = 60*1000L;
	/**
	 * 
	 */
//	@Override
	public void doTask() {
		Map<String, String> map = redisOperationService.getAllSmsMobileValideCodeTime();
		Set<Entry<String, String>> set = map.entrySet();
		for(Entry<String, String> entry : set){
			String key = entry.getKey();
			String value = entry.getValue();
			String[] tempArr = value.split("_");
			if (System.currentTimeMillis() - Long.valueOf(tempArr[1]) > smsOverTime) {
				redisOperationService.delSmsMobileValideCodeTime(key);
			}
		}
	}
	
}
