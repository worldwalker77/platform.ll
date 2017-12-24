package cn.worldwalker.game.wyqp.mj.service;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import cn.worldwalker.game.wyqp.common.domain.base.BaseRequest;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.base.UserInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjMsg;
import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.enums.DissolveStatusEnum;
import cn.worldwalker.game.wyqp.common.enums.GameTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.MsgTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.RoomStatusEnum;
import cn.worldwalker.game.wyqp.common.exception.BusinessException;
import cn.worldwalker.game.wyqp.common.exception.ExceptionEnum;
import cn.worldwalker.game.wyqp.common.result.Result;
import cn.worldwalker.game.wyqp.common.service.BaseGameService;
import cn.worldwalker.game.wyqp.common.utils.GameUtil;
import cn.worldwalker.game.wyqp.mj.cards.MjCardResource;
import cn.worldwalker.game.wyqp.mj.cards.MjCardRule;
import cn.worldwalker.game.wyqp.mj.enums.MjPlayerStatusEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjRoomStatusEnum;
@Service(value="mjGameService")
public class MjGameService extends BaseGameService{

	@Override
	public BaseRoomInfo doCreateRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		MjMsg msg = (MjMsg)request.getMsg();
		MjRoomInfo roomInfo = new MjRoomInfo();
		roomInfo.setGameType(GameTypeEnum.mj.gameType);
		roomInfo.setIsKaiBao(msg.getIsKaiBao());
		roomInfo.setIsHuangFan(msg.getIsHuangFan());
		roomInfo.setIsFeiCangyin(msg.getIsFeiCangyin());
		roomInfo.setHuButtomScore(msg.getHuButtomScore());
		roomInfo.setEachFlowerScore(msg.getEachFlowerScore());
		roomInfo.setHuScoreLimit(msg.getHuScoreLimit());
		List<MjPlayerInfo> playerList = roomInfo.getPlayerList();
		MjPlayerInfo player = new MjPlayerInfo();
		playerList.add(player);
		return roomInfo;
	}

	@Override
	public BaseRoomInfo doEntryRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		
		MjRoomInfo roomInfo = redisOperationService.getRoomInfoByRoomId(userInfo.getRoomId(), MjRoomInfo.class);
		List<MjPlayerInfo> playerList = roomInfo.getPlayerList();
		MjPlayerInfo playerInfo = new MjPlayerInfo();
		playerList.add(playerInfo);
		return roomInfo;
	}
	
	public void ready(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		result.setGameType(GameTypeEnum.mj.gameType);
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		Integer playerId = userInfo.getPlayerId();
		final Integer roomId = userInfo.getRoomId();
		MjRoomInfo roomInfo = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
		List<MjPlayerInfo> playerList = roomInfo.getPlayerList();
		/**玩家已经准备计数*/
		int readyCount = 0;
		int size = playerList.size();
		for(int i = 0; i < size; i++){
			MjPlayerInfo player = playerList.get(i);
			if (player.getPlayerId().equals(playerId)) {
				/**设置状态为已准备*/
				player.setStatus(MjPlayerStatusEnum.ready.status);
			}
			if (MjPlayerStatusEnum.ready.status.equals(player.getStatus())) {
				readyCount++;
			}
		}
		
		if (readyCount > 1 && readyCount == size) {
			List<Integer> tableRemainderCardList = MjCardResource.genTableOutOrderCardList();
			/**初始化桌牌*/
			roomInfo.setTableRemainderCardList(tableRemainderCardList);
			/**开始发牌时将房间内当前局数+1*/
			roomInfo.setCurGame(roomInfo.getCurGame() + 1);
			/**发牌返回信息*/
			result.setMsgType(MsgTypeEnum.initHandCards.msgType);
			data.put("roomId", roomInfo.getRoomId());
			data.put("roomOwnerId", roomInfo.getRoomOwnerId());
			data.put("roomBankerId", roomInfo.getRoomBankerId());
			/**庄家的第一个说话*/
			data.put("curPlayerId", roomInfo.getRoomBankerId());
			data.put("totalGames", roomInfo.getTotalGames());
			data.put("curGame", roomInfo.getCurGame());
			data.put("dices", MjCardRule.playDices());
			
			/**为每个玩家设置牌*/
			for(int i = 0; i < size; i++ ){
				MjPlayerInfo player = playerList.get(i);
				/**如果是庄家则发14张牌*/
				if (player.getPlayerId().equals(roomInfo.getRoomBankerId())) {
					/**当前说话玩家的手牌缓存，由于没有补花之前的牌需要返回给客户端*/
					List<Integer> handCardListBeforeAddFlower = new ArrayList<Integer>();
					player.setHandCardList(MjCardResource.genHandCardList(tableRemainderCardList, 14));
					/**补花之前的牌缓存*/
					handCardListBeforeAddFlower.addAll(player.getHandCardList());
					/**校验手牌补花*/
					String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo.getTableRemainderCardList(), player);
					/**如果手牌中有补花牌，则将补花后的正常牌替换玩家手牌中的花牌*/
					if (StringUtils.isNotBlank(handCardAddFlower)) {
						handCardAddFlower = MjCardRule.replaceFlowerCards(player.getHandCardList(), handCardAddFlower);
					}
					/**计算房间可操作权限*/
					MjCardRule.calculateAllPlayerOperations(roomInfo, null, playerId, 0);
					data.put("handCardList", handCardListBeforeAddFlower);
					data.put("handCardAddFlower", handCardAddFlower);
					data.put("operations", roomInfo.getPlayerOperationMap().get(playerId));
					channelContainer.sendTextMsgByPlayerIds(result, playerId);
				}else{/**闲家发13张牌*/
					player.setHandCardList(MjCardResource.genHandCardList(roomInfo.getTableRemainderCardList(), 13));
					data.put("handCardList", player.getHandCardList());
					channelContainer.sendTextMsgByPlayerIds(result, playerId);
				}
				/**设置每个玩家的解散房间状态为不同意解散，后面大结算返回大厅的时候回根据此状态判断是否解散房间*/
				player.setDissolveStatus(DissolveStatusEnum.disagree.status);
			}
			/**当前说话玩家的id*/
			roomInfo.setCurPlayerId(roomInfo.getRoomBankerId());
			roomInfo.setStatus(MjRoomStatusEnum.inGame.status);
			roomInfo.setUpdateTime(new Date());
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			return;
		}
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		result.setMsgType(request.getMsgType());
		data.put("playerId", userInfo.getPlayerId());
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
	}
	
	public void chuPai(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		MjMsg msg = (MjMsg)request.getMsg();
		Integer roomId = userInfo.getRoomId();
		Integer playerId = userInfo.getPlayerId();
		MjRoomInfo roomInfo = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
		List<MjPlayerInfo> playerList = roomInfo.getPlayerList();
		if (!GameUtil.isExistPlayerInRoom(msg.getPlayerId(), playerList)) {
			throw new BusinessException(ExceptionEnum.PLAYER_NOT_IN_ROOM);
		}
		/**如果当前房间的状态不是在游戏中，则不处理此请求*/
		if (!RoomStatusEnum.inGame.status.equals(roomInfo.getStatus())) {
			throw new BusinessException(ExceptionEnum.PLAYER_NOT_IN_ROOM);
		}
		/**计算房间可操作权限*/
		MjCardRule.calculateAllPlayerOperations(roomInfo, msg.getCardIndex(), playerId, 2);
		/**获取当前操作权限的玩家*/
		Integer curPlayerId = MjCardRule.getPlayerHighestPriorityPlayerId(roomInfo);
		/**如果此张出的牌，别的玩家都不需要，则下家摸牌*/
		if (curPlayerId == null) {
			curPlayerId = GameUtil.getNextPlayerId(playerList, playerId);
			/**摸牌并校验补花*/
			MjPlayerInfo player = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), playerId);
			String moPaiAddFlower = MjCardRule.checkMoPaiAddFlower(roomInfo.getTableRemainderCardList(), player);
			String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo.getTableRemainderCardList(), player);
			if (StringUtils.isNotBlank(handCardAddFlower)) {
				handCardAddFlower = MjCardRule.replaceFlowerCards(player.getHandCardList(), handCardAddFlower);
			}
			/**给摸牌的玩家返回信息*/
			
		}else{
			
		}
		
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		result.setMsgType(MsgTypeEnum.chuPai.msgType);
		
		data.put("playerId", msg.getPlayerId());
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		channelContainer.sendTextMsgByPlayerIds(result, msg.getPlayerId());
	}


	@Override
	public List<BaseRoomInfo> doRefreshRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		return null;
	}

	@Override
	public BaseRoomInfo getRoomInfo(ChannelHandlerContext ctx,
			BaseRequest request, UserInfo userInfo) {
		return null;
	}


}
