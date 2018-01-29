package cn.worldwalker.game.wyqp.web.job;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.worldwalker.game.wyqp.common.channel.ChannelContainer;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.base.RedisRelaModel;
import cn.worldwalker.game.wyqp.common.domain.jh.JhRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.nn.NnRoomInfo;
import cn.worldwalker.game.wyqp.common.enums.GameTypeEnum;
import cn.worldwalker.game.wyqp.common.manager.CommonManager;
import cn.worldwalker.game.wyqp.common.service.RedisOperationService;
import cn.worldwalker.game.wyqp.common.utils.GameUtil;


public class RoomInfoCleanJob /**extends SingleServerJobByRedis*/ {
	public final static Log log = LogFactory.getLog(RoomInfoCleanJob.class);
	@Autowired
	private RedisOperationService redisOperationService;
	@Autowired
	private ChannelContainer channelContainer;
	@Autowired
	public CommonManager commonManager;
//	@Override
	public void doTask() {
		
		List<RedisRelaModel> list = redisOperationService.getAllRoomIdGameTypeUpdateTime();
		for(RedisRelaModel model : list){
			if (System.currentTimeMillis() - model.getUpdateTime() > 4*60*1000) {
				BaseRoomInfo roomInfo = null;
				if (GameTypeEnum.nn.gameType.equals(model.getGameType()) ) {
					roomInfo = redisOperationService.getRoomInfoByRoomId(model.getRoomId(), NnRoomInfo.class);
				}else if(GameTypeEnum.mj.gameType.equals(model.getGameType()) ){
					roomInfo = redisOperationService.getRoomInfoByRoomId(model.getRoomId(), MjRoomInfo.class);
				}else if(GameTypeEnum.jh.gameType.equals(model.getGameType()) ){
					roomInfo = redisOperationService.getRoomInfoByRoomId(model.getRoomId(), JhRoomInfo.class);
				}
				if (roomInfo == null) {
					redisOperationService.delGameTypeUpdateTimeByRoomId(model.getRoomId());
					return;
				}
				List playerList = roomInfo.getPlayerList();
				redisOperationService.cleanPlayerAndRoomInfo(model.getRoomId(), GameUtil.getPlayerIdStrArr(playerList));
				if (roomInfo.getClubId() != null) {
					redisOperationService.delClubIdRoomId(roomInfo.getClubId(), roomInfo.getRoomId());
				}
				try {
					commonManager.addUserRecord(roomInfo);
				} catch (Exception e) {
					log.error("解散房间时添加记录失败", e);
				}
			}
		}
	}
	
}
