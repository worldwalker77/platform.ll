package cn.worldwalker.game.wyqp.mj.service;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import cn.worldwalker.game.wyqp.common.domain.base.BaseMsg;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRequest;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.base.UserInfo;
import cn.worldwalker.game.wyqp.common.domain.base.UserModel;
import cn.worldwalker.game.wyqp.common.domain.mj.MjMsg;
import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.nn.NnPlayerInfo;
import cn.worldwalker.game.wyqp.common.enums.DissolveStatusEnum;
import cn.worldwalker.game.wyqp.common.enums.GameTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.MsgTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.RoomCardOperationEnum;
import cn.worldwalker.game.wyqp.common.enums.RoomStatusEnum;
import cn.worldwalker.game.wyqp.common.exception.BusinessException;
import cn.worldwalker.game.wyqp.common.exception.ExceptionEnum;
import cn.worldwalker.game.wyqp.common.result.Result;
import cn.worldwalker.game.wyqp.common.service.BaseGameService;
import cn.worldwalker.game.wyqp.common.utils.GameUtil;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;
import cn.worldwalker.game.wyqp.mj.cards.MjCardResource;
import cn.worldwalker.game.wyqp.mj.cards.MjCardRule;
import cn.worldwalker.game.wyqp.mj.enums.MjOperationEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjPlayerStatusEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjRoomStatusEnum;
@Service(value="mjGameService")
public class MjGameService extends BaseGameService{

	@Override
	public BaseRoomInfo doCreateRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		MjMsg msg = (MjMsg)request.getMsg();
		MjRoomInfo roomInfo = new MjRoomInfo();
		roomInfo.setRoomBankerId(msg.getPlayerId());
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
		BaseMsg msg = request.getMsg();
		MjRoomInfo roomInfo = redisOperationService.getRoomInfoByRoomId(msg.getRoomId(), MjRoomInfo.class);
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
			MjCardRule.initMjRoom(roomInfo);
			List<Integer> tableRemainderCardList = MjCardResource.genTableOutOrderCardList();
//			List<Integer> tableRemainderCardList = MjCardRule.getTableCardList();
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
				MjCardRule.initMjPlayer(player);
				/**如果是庄家则发14张牌*/
				if (player.getPlayerId().equals(roomInfo.getRoomBankerId())) {
					/**当前说话玩家的手牌缓存，由于没有补花之前的牌需要返回给客户端*/
					List<Integer> handCardListBeforeAddFlower = new ArrayList<Integer>();
					player.setHandCardList(MjCardResource.genHandCardList(tableRemainderCardList, 14));
//					player.setHandCardList(MjCardRule.getHandCardListByIndex(i, true));//测试用
					/**补花之前的牌缓存*/
					handCardListBeforeAddFlower.addAll(player.getHandCardList());
					/**校验手牌补花*/
					String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo.getTableRemainderCardList(), player);
					/**如果手牌中有补花牌，则将补花后的正常牌替换玩家手牌中的花牌*/
					if (StringUtils.isNotBlank(handCardAddFlower)) {
						handCardAddFlower = MjCardRule.replaceFlowerCards(player.getHandCardList(), handCardAddFlower);
					}
					/**计算房间可操作权限*/
					MjCardRule.calculateAllPlayerOperations(roomInfo, null, player.getPlayerId(), 0);
					data.put("handCardList", handCardListBeforeAddFlower);
					if (StringUtils.isNotBlank(handCardAddFlower)) {
						data.put("handCardAddFlower", handCardAddFlower);
					}
					if (MjCardRule.getPlayerHighestPriority(roomInfo, player.getPlayerId()) != null) {
						data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, player.getPlayerId()));
					}
					channelContainer.sendTextMsgByPlayerIds(result, player.getPlayerId());
				}else{/**闲家发13张牌*/
					player.setHandCardList(MjCardResource.genHandCardList(roomInfo.getTableRemainderCardList(), 13));
//					player.setHandCardList(MjCardRule.getHandCardListByIndex(i, false));//测试用
					data.put("handCardList", player.getHandCardList());
					data.remove("handCardAddFlower");
					data.remove("operations");
					channelContainer.sendTextMsgByPlayerIds(result, player.getPlayerId());
				}
			}
			MjPlayerInfo roomBankPlayer = MjCardRule.getPlayerInfoByPlayerId(playerList, roomInfo.getRoomBankerId());
			/**给其他的玩家返回补花数*/
			if (roomBankPlayer.getCurAddFlowerNum() > 0) {
				data.clear();
				data.put("curPlayerId", roomBankPlayer.getPlayerId());
				data.put("addFlowerCount", roomBankPlayer.getCurAddFlowerNum());
				result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, roomBankPlayer.getPlayerId()));
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
		if (!roomInfo.getCurPlayerId().equals(playerId)) {
			throw new BusinessException(ExceptionEnum.IS_NOT_YOUR_TURN);
		}
		roomInfo.setLastPlayerId(playerId);
		roomInfo.setLastCardIndex(msg.getCardIndex());
		MjPlayerInfo player = MjCardRule.getPlayerInfoByPlayerId(playerList, playerId);
		if (player.getCurMoPaiCardIndex() != null) {
			player.getHandCardList().add(player.getCurMoPaiCardIndex());
			player.setCurMoPaiCardIndex(null);
		}
		player.getHandCardList().remove(msg.getCardIndex());
		player.getDiscardCardList().add(msg.getCardIndex());
		/**出牌后手牌进行重新排序*/
		MjCardResource.sortCardList(player.getHandCardList());
		/**计算房间可操作权限*/
		MjCardRule.calculateAllPlayerOperations(roomInfo, msg.getCardIndex(), playerId, 2);
		/**获取当前操作权限的玩家*/
		Integer curPlayerId = MjCardRule.getPlayerHighestPriorityPlayerId(roomInfo);
		/**如果此张出的牌，别的玩家都不需要，则下家摸牌*/
		if (curPlayerId == null) {
			curPlayerId = GameUtil.getNextPlayerId(playerList, playerId);
			/**摸牌并校验补花*/
			MjPlayerInfo curPlayer = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), curPlayerId);
			/**将玩家当前轮补花数设置为0*/
			curPlayer.setCurAddFlowerNum(0);
			String moPaiAddFlower = MjCardRule.checkMoPaiAddFlower(roomInfo.getTableRemainderCardList(), curPlayer);
			String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo.getTableRemainderCardList(), curPlayer);
			if (StringUtils.isNotBlank(handCardAddFlower)) {
				handCardAddFlower = MjCardRule.replaceFlowerCards(curPlayer.getHandCardList(), handCardAddFlower);
			}
			MjCardRule.calculateAllPlayerOperations(roomInfo, MjCardRule.getRealMoPai(moPaiAddFlower), curPlayerId, 1);
			roomInfo.setCurPlayerId(curPlayerId);
			roomInfo.setUpdateTime(new Date());
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			
			/**给其他玩家返回出牌消息及当前说话玩家*/
			Map<String, Object> data = new HashMap<String, Object>();
			result.setData(data);
			data.put("playerId", roomInfo.getLastPlayerId());
			data.put("cardIndex", roomInfo.getLastCardIndex());
			data.put("curPlayerId", curPlayerId);
			result.setMsgType(MsgTypeEnum.chuPai.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
			/**给当前玩家返回摸牌信息*/
			data.clear();
			data.put("playerId", roomInfo.getLastPlayerId());
			data.put("cardIndex", roomInfo.getLastCardIndex());
			data.put("curPlayerId", curPlayerId);
			data.put("moPaiAddFlower", moPaiAddFlower);
			if (StringUtils.isNotBlank(handCardAddFlower)) {
				data.put("handCardAddFlower", handCardAddFlower);
			}
			if (MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId) != null) {
				data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId));
			}
			result.setMsgType(MsgTypeEnum.moPai.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
			
			/**给其他的玩家返回补花数*/
			if (curPlayer.getCurAddFlowerNum() > 0) {
				data.clear();
				data.put("curPlayerId", curPlayerId);
				data.put("addFlowerCount", curPlayer.getCurAddFlowerNum());
				result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
			}
		}else{
			roomInfo.setCurPlayerId(curPlayerId);
			roomInfo.setUpdateTime(new Date());
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			/**给其他玩家返回出牌消息及当前说话玩家*/
			Map<String, Object> data = new HashMap<String, Object>();
			result.setData(data);
			data.put("playerId", playerId);
			data.put("cardIndex", msg.getCardIndex());
			data.put("curPlayerId", curPlayerId);
			result.setMsgType(MsgTypeEnum.chuPai.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
			data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId));
			channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
		}
		
	}
	
	public void chi(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
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
		if (!roomInfo.getCurPlayerId().equals(playerId)) {
			throw new BusinessException(ExceptionEnum.IS_NOT_YOUR_TURN);
		}
		/**校验玩家是否有吃操作权限*/
		if (!MjCardRule.checkCurOperationValid(roomInfo, playerId, MjOperationEnum.chi.type, msg.getChiCards())) {
			throw new BusinessException(ExceptionEnum.NO_AUTHORITY);
		}
		/**将吃的牌从手牌列表中移动到吃牌列表中*/
		MjPlayerInfo player = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), playerId);
		List<Integer> chiCardList = MjCardRule.moveOperationCards(roomInfo, player, MjOperationEnum.chi, msg.getChiCards());
		/**计算剩余手牌列表补花情况*/
		String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo.getTableRemainderCardList(), player);
		if (StringUtils.isNotBlank(handCardAddFlower)) {
			handCardAddFlower = MjCardRule.replaceFlowerCards(player.getHandCardList(), handCardAddFlower);
		}
		/**计算当前玩家剩余可操作权限*/
		MjCardRule.calculateAllPlayerOperations(roomInfo, null, playerId, 0);
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		
		/**给其他玩家返回吃牌消息*/
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		data.put("curPlayerId", playerId);
		data.put("cardIndex", msg.getCardIndex());
		data.put("chiCardList", chiCardList);
		result.setMsgType(MsgTypeEnum.chi.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		/**给当前玩家返回吃牌及补花、其他可操作权限消息*/
		if (StringUtils.isNotBlank(handCardAddFlower)) {
			data.put("handCardAddFlower", handCardAddFlower);
		}
		if (MjCardRule.getPlayerHighestPriority(roomInfo, playerId) != null) {
			data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, playerId));
		}
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
		
		/**如果手牌存在补花，则给其他玩家返回补花数*/
		if (StringUtils.isNotBlank(handCardAddFlower)) {
			data.clear();
			data.put("curPlayerId", playerId);
			data.put("addFlowerCount", player.getCurAddFlowerNum());
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		}
		
	}
	
	public void peng(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
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
		if (!roomInfo.getCurPlayerId().equals(playerId)) {
			throw new BusinessException(ExceptionEnum.IS_NOT_YOUR_TURN);
		}
		/**校验玩家是否有碰操作权限*/
		if (!MjCardRule.checkCurOperationValid(roomInfo, playerId, MjOperationEnum.peng.type, msg.getPengCards())) {
			throw new BusinessException(ExceptionEnum.NO_AUTHORITY);
		}
		/**将碰的牌从手牌列表中移动到碰牌列表中*/
		MjPlayerInfo player = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), playerId);
		List<Integer> pengCardList = MjCardRule.moveOperationCards(roomInfo, player, MjOperationEnum.peng, msg.getPengCards());
		/**将玩家当前轮补花数设置为0*/
		player.setCurAddFlowerNum(0);
		/**计算剩余手牌列表补花情况*/
		String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo.getTableRemainderCardList(), player);
		if (StringUtils.isNotBlank(handCardAddFlower)) {
			handCardAddFlower = MjCardRule.replaceFlowerCards(player.getHandCardList(), handCardAddFlower);
		}
		/**计算当前玩家剩余可操作权限*/
		MjCardRule.calculateAllPlayerOperations(roomInfo, null, playerId, 0);
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		
		/**给其他玩家返回碰牌消息*/
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		data.put("curPlayerId", playerId);
		data.put("cardIndex", msg.getCardIndex());
		data.put("pengCardList", pengCardList);
		result.setMsgType(MsgTypeEnum.peng.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		/**给当前玩家返回碰牌及补花、其他可操作权限消息*/
		if (StringUtils.isNotBlank(handCardAddFlower)) {
			data.put("handCardAddFlower", handCardAddFlower);
		}
		if (MjCardRule.getPlayerHighestPriority(roomInfo, playerId) != null) {
			data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, playerId));
		}
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
		
		/**如果手牌存在补花，则给其他玩家返回补花数*/
		if (StringUtils.isNotBlank(handCardAddFlower)) {
			data.clear();
			data.put("curPlayerId", playerId);
			data.put("addFlowerCount", player.getCurAddFlowerNum());
			result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		}
	}
	
	public void mingGang(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
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
		if (!roomInfo.getCurPlayerId().equals(playerId)) {
			throw new BusinessException(ExceptionEnum.IS_NOT_YOUR_TURN);
		}
		/**校验玩家是否有杠操作权限*/
		if (!MjCardRule.checkCurOperationValid(roomInfo, playerId, MjOperationEnum.mingGang.type, msg.getGangCards())) {
			throw new BusinessException(ExceptionEnum.NO_AUTHORITY);
		}
		/**将杠的牌从手牌列表中移动到杠牌列表中*/
		MjPlayerInfo player = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), playerId);
		List<Integer> mingGangCardList = MjCardRule.moveOperationCards(roomInfo, player, MjOperationEnum.mingGang, msg.getGangCards());
		/**将玩家当前轮补花数设置为0*/
		player.setCurAddFlowerNum(0);
		String moPaiAddFlower = MjCardRule.checkMoPaiAddFlower(roomInfo.getTableRemainderCardList(), player);
		String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo.getTableRemainderCardList(), player);
		if (StringUtils.isNotBlank(handCardAddFlower)) {
			handCardAddFlower = MjCardRule.replaceFlowerCards(player.getHandCardList(), handCardAddFlower);
		}
		MjCardRule.calculateAllPlayerOperations(roomInfo, MjCardRule.getRealMoPai(moPaiAddFlower), playerId, 1);
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		
		/**给当前玩家返回摸牌信息*/
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		data.put("playerId", roomInfo.getLastPlayerId());
		data.put("cardIndex", roomInfo.getLastCardIndex());
		data.put("curPlayerId", playerId);
		data.put("moPaiAddFlower", moPaiAddFlower);
		if (StringUtils.isNotBlank(handCardAddFlower)) {
			data.put("handCardAddFlower", handCardAddFlower);
		}
		if (MjCardRule.getPlayerHighestPriority(roomInfo, playerId) != null) {
			data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, playerId));
		}
		result.setMsgType(MsgTypeEnum.moPai.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
		
		/**给其他玩家返回明杠信息*/
		data.clear();
		data.put("curPlayerId", playerId);
		data.put("cardIndex", mingGangCardList.get(0));
		data.put("mingGangCardList", mingGangCardList);
		result.setMsgType(MsgTypeEnum.mingGang.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		
		/**如果手牌存在补花，则给其他玩家返回补花数*/
		if (player.getCurAddFlowerNum() > 0) {
			data.clear();
			data.put("curPlayerId", playerId);
			data.put("addFlowerCount", player.getCurAddFlowerNum());
			result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		}
	}
	
	public void anGang(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
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
		if (!roomInfo.getCurPlayerId().equals(playerId)) {
			throw new BusinessException(ExceptionEnum.IS_NOT_YOUR_TURN);
		}
		/**校验玩家是否有杠操作权限*/
		if (!MjCardRule.checkCurOperationValid(roomInfo, playerId, MjOperationEnum.anGang.type, msg.getGangCards())) {
			throw new BusinessException(ExceptionEnum.NO_AUTHORITY);
		}
		/**将杠的牌从手牌列表中移动到杠牌列表中*/
		MjPlayerInfo player = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), playerId);
		List<Integer> anGangCardList = MjCardRule.moveOperationCards(roomInfo, player, MjOperationEnum.anGang, msg.getGangCards());
		/**将玩家当前轮补花数设置为0*/
		player.setCurAddFlowerNum(0);
		String moPaiAddFlower = MjCardRule.checkMoPaiAddFlower(roomInfo.getTableRemainderCardList(), player);
//		/**由于暗杠肯定是摸牌后的暗杠，所以下面手牌补花不需要*/
//		String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo.getTableRemainderCardList(), player);
//		if (StringUtils.isNotBlank(handCardAddFlower)) {
//			handCardAddFlower = MjCardRule.replaceFlowerCards(player.getHandCardList(), handCardAddFlower);
//		}
		MjCardRule.calculateAllPlayerOperations(roomInfo, MjCardRule.getRealMoPai(moPaiAddFlower), playerId, 1);
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		
		/**给当前玩家返回摸牌信息*/
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		data.put("curPlayerId", playerId);
		data.put("moPaiAddFlower", moPaiAddFlower);
//		if (StringUtils.isNotBlank(handCardAddFlower)) {
//			data.put("handCardAddFlower", handCardAddFlower);
//		}
		if (MjCardRule.getPlayerHighestPriority(roomInfo, playerId) != null) {
			data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, playerId));
		}
		result.setMsgType(MsgTypeEnum.moPai.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
		
		/**给其他玩家返回明杠信息*/
		data.clear();
		data.put("curPlayerId", playerId);
		data.put("cardIndex", anGangCardList.get(0));
		data.put("anGangCardList", anGangCardList);
		result.setMsgType(MsgTypeEnum.anGang.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		
		/**如果手牌存在补花，则给其他玩家返回补花数*/
		if (player.getCurAddFlowerNum() > 0) {
			data.clear();
			data.put("curPlayerId", playerId);
			data.put("addFlowerCount", player.getCurAddFlowerNum());
			result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		}
	}
	
	public void tingPai(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
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
		if (!roomInfo.getCurPlayerId().equals(playerId)) {
			throw new BusinessException(ExceptionEnum.IS_NOT_YOUR_TURN);
		}
		/**校验玩家是否有杠操作权限*/
		if (!MjCardRule.checkCurOperationValid(roomInfo, playerId, MjOperationEnum.tingHu.type, null)) {
			throw new BusinessException(ExceptionEnum.NO_AUTHORITY);
		}
		MjPlayerInfo player = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), playerId);
		/**设置状态为听牌*/
		player.setIsTingHu(1);
		/**将当前玩家的可操作性权限删除*/
		MjCardRule.delPlayerOperationByPlayerId(roomInfo, playerId);
		
		/**获取剩余玩家可操作权限*/
		Integer curPlayerId = MjCardRule.getPlayerHighestPriorityPlayerId(roomInfo);
		/**如果剩余玩家都没有操作权限了，则下家摸牌*/
		if (curPlayerId == null) {
			curPlayerId = GameUtil.getNextPlayerId(playerList, playerId);
			/**摸牌并校验补花*/
			MjPlayerInfo curPlayer = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), curPlayerId);
			/**将玩家当前轮补花数设置为0*/
			curPlayer.setCurAddFlowerNum(0);
			String moPaiAddFlower = MjCardRule.checkMoPaiAddFlower(roomInfo.getTableRemainderCardList(), curPlayer);
			String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo.getTableRemainderCardList(), curPlayer);
			if (StringUtils.isNotBlank(handCardAddFlower)) {
				handCardAddFlower = MjCardRule.replaceFlowerCards(curPlayer.getHandCardList(), handCardAddFlower);
			}
			MjCardRule.calculateAllPlayerOperations(roomInfo, MjCardRule.getRealMoPai(moPaiAddFlower), curPlayerId, 1);
			roomInfo.setCurPlayerId(curPlayerId);
			roomInfo.setUpdateTime(new Date());
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			
			/**给所有玩家返回听胡消息及当前说话的玩家*/
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("playerId", playerId);
			data.put("curPlayerId", curPlayerId);
			result.setMsgType(MsgTypeEnum.tingPai.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
			/**给当前玩家返回摸牌信息*/
			data.clear();
			data.put("playerId", roomInfo.getLastPlayerId());
			data.put("cardIndex", roomInfo.getLastCardIndex());
			data.put("curPlayerId", curPlayerId);
			data.put("moPaiAddFlower", moPaiAddFlower);
			if (StringUtils.isNotBlank(handCardAddFlower)) {
				data.put("handCardAddFlower", handCardAddFlower);
			}
			if (MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId) != null) {
				data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId));
			}
			result.setMsgType(MsgTypeEnum.moPai.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
			/**给其他的玩家返回补花数*/
			if (curPlayer.getCurAddFlowerNum() > 0) {
				data.clear();
				data.put("curPlayerId", curPlayerId);
				data.put("addFlowerCount", curPlayer.getCurAddFlowerNum());
				result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
			}
		}else{
			roomInfo.setCurPlayerId(curPlayerId);
			roomInfo.setUpdateTime(new Date());
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			/**给所有玩家返回听牌消息及当前说话的玩家*/
			Map<String, Object> data = new HashMap<String, Object>();
			result.setData(data);
			data.put("playerId", playerId);
			data.put("curPlayerId", curPlayerId);
			result.setMsgType(MsgTypeEnum.tingPai.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
			data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId));
			channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
		}
	}
	
	
	public void pass(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
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
		if (!roomInfo.getCurPlayerId().equals(playerId)) {
			throw new BusinessException(ExceptionEnum.IS_NOT_YOUR_TURN);
		}
		MjPlayerInfo player = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), playerId);
		/**设置状态为听牌*/
		player.setIsTingHu(1);
		/**将当前玩家的可操作性权限删除*/
		MjCardRule.delPlayerOperationByPlayerId(roomInfo, playerId);
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		/**如果是pass摸牌、吃、碰、杠后的可操作性权限，则当前说话的玩家还是当前玩家*/
		if (MjCardRule.isHandCard3n2(player)) {
			data.put("curPlayerId", playerId);
			result.setMsgType(MsgTypeEnum.pass.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
		}else{/**如果是pass别人打出的牌*/
			/**获取下个可操作性玩家的可操作权限*/
			Integer curPlayerId = MjCardRule.getPlayerHighestPriorityPlayerId(roomInfo);
			/**如果剩余玩家都没有操作权限了，则下家摸牌*/
			if (curPlayerId == null) {
				curPlayerId = GameUtil.getNextPlayerId(playerList, playerId);
				/**摸牌并校验补花*/
				MjPlayerInfo curPlayer = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), curPlayerId);
				/**将玩家当前轮补花数设置为0*/
				curPlayer.setCurAddFlowerNum(0);
				String moPaiAddFlower = MjCardRule.checkMoPaiAddFlower(roomInfo.getTableRemainderCardList(), curPlayer);
				String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo.getTableRemainderCardList(), curPlayer);
				if (StringUtils.isNotBlank(handCardAddFlower)) {
					handCardAddFlower = MjCardRule.replaceFlowerCards(curPlayer.getHandCardList(), handCardAddFlower);
				}
				MjCardRule.calculateAllPlayerOperations(roomInfo, MjCardRule.getRealMoPai(moPaiAddFlower), curPlayerId, 1);
				roomInfo.setCurPlayerId(curPlayerId);
				roomInfo.setUpdateTime(new Date());
				redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
				
				/**给所有玩家返回pass消息及当前说话的玩家*/
				data.clear();
				data.put("curPlayerId", curPlayerId);
				result.setMsgType(MsgTypeEnum.pass.msgType);
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
				/**给当前玩家返回摸牌信息*/
				data.clear();
				data.put("curPlayerId", curPlayerId);
				data.put("moPaiAddFlower", moPaiAddFlower);
				if (StringUtils.isNotBlank(handCardAddFlower)) {
					data.put("handCardAddFlower", handCardAddFlower);
				}
				if (MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId) != null) {
					data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId));
				}
				result.setMsgType(MsgTypeEnum.moPai.msgType);
				channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
				/**给其他的玩家返回补花数*/
				if (curPlayer.getCurAddFlowerNum() > 0) {
					data.clear();
					data.put("curPlayerId", curPlayerId);
					data.put("addFlowerCount", curPlayer.getCurAddFlowerNum());
					result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
					channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
				}
			}else{
				roomInfo.setCurPlayerId(curPlayerId);
				roomInfo.setUpdateTime(new Date());
				redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
				/**给所有玩家返回pass消息及当前说话的玩家*/
				data.clear();
				data.put("playerId", playerId);
				data.put("curPlayerId", curPlayerId);
				result.setMsgType(MsgTypeEnum.pass.msgType);
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
				data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId));
				channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
			}
		}
	}
	
	public void huPai(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
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
		if (!roomInfo.getCurPlayerId().equals(playerId)) {
			throw new BusinessException(ExceptionEnum.IS_NOT_YOUR_TURN);
		}
		/**校验玩家是否有胡操作权限*/
		if (!MjCardRule.checkCurOperationValid(roomInfo, playerId, MjOperationEnum.hu.type, null)) {
			throw new BusinessException(ExceptionEnum.NO_AUTHORITY);
		}
		
		/**添加胡牌玩家map*/
		String playerHuType = MjCardRule.getPlayerHighestPriority(roomInfo, playerId).get(MjOperationEnum.hu.type);
		roomInfo.getHuPlayerMap().put(playerId, playerHuType);
		
		/**将当前玩家的可操作性权限删除*/
		MjCardRule.delPlayerOperationByPlayerId(roomInfo, playerId);
		MjPlayerInfo player = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), playerId);
		/**获取剩余玩家最高可操作性权限*/
		Integer curPlayerId = MjCardRule.getPlayerHighestPriorityPlayerId(roomInfo);
		if (curPlayerId == null) {
			curPlayerId = playerId;
			calculateScoreAndRoomBanker(roomInfo);
		}else{
			TreeMap<Integer, String> curPlayerOperations = MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId);
			/**如果没有其他玩家可以胡牌，则本局结束，开始结算*/
			if (curPlayerOperations == null || curPlayerOperations.size() == 0 || !curPlayerOperations.containsKey(MjOperationEnum.hu.type)) {
				curPlayerId = playerId;
				calculateScoreAndRoomBanker(roomInfo);
			}else{/**如果有其他玩家可以胡牌，则需要通知其他玩家胡牌*/
				roomInfo.setCurPlayerId(curPlayerId);
				roomInfo.setUpdateTime(new Date());
				redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
				data.put("huCardList", player.getHandCardList());
				data.put("playerId", playerId);
				if (playerHuType.startsWith("0")) {/**别人点炮的胡*/
					data.put("huCardIndex", roomInfo.getLastCardIndex());
					data.put("huType", 0);
					data.put("dianPaoPlayerId", roomInfo.getLastPlayerId());
				}else if(playerHuType.startsWith("1")){/**自摸胡*/
					data.put("huCardIndex", player.getCurMoPaiCardIndex());
					data.put("huType", 1);
				}else if(playerHuType.startsWith("2")){/**天胡*/
					data.put("huCardIndex", roomInfo.getLastCardIndex());
					data.put("huType", 2);
				}else if(playerHuType.startsWith("3")){/**抢杠胡3_111111_3,玩家111111明杠3，被抢杠了*/
					String[] temp = playerHuType.split("_");
					data.put("huCardIndex", roomInfo.getLastCardIndex());
					data.put("huType", 3);
					data.put("dianPaoPlayerId", temp[1]);
				}else{
					throw new BusinessException(ExceptionEnum.HU_TYPE_ERROR);
				}
				/**当前说话者id*/
				data.put("curPlayerId", curPlayerId);
				result.setMsgType(MsgTypeEnum.huPai.msgType);
				/**给下一个可胡牌玩家之外的其他玩家返回信息*/
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
				TreeMap<Integer, String> operations = new TreeMap<Integer, String>();
				operations.put(MjOperationEnum.hu.type, "0");
				/**当前说话者可操作权限，胡牌*/
				data.put("operations", operations);
				/**给下一个可胡牌玩家返回信息*/
				channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
			}
		}
		
	}
	
	private void calculateScoreAndRoomBanker(MjRoomInfo roomInfo){
		List<MjPlayerInfo> playerList = roomInfo.getPlayerList();
		/**找出庄家*/
		Map<Integer, String> huPlayerMap = roomInfo.getHuPlayerMap();
		if (huPlayerMap.size() == 1) {
			Set<Integer> keySet = huPlayerMap.keySet();
			for(Integer roomBankerId : keySet){
				roomInfo.setRoomBankerId(roomBankerId);
				break;
			}
		}else{
			roomInfo.setRoomBankerId(roomInfo.getLastPlayerId());
		}
		/**将其他玩家的牌依次与庄家进行比较，计算各自得当前局分及总得分，最大牌型，并计算下一次庄家是谁*/
		for(MjPlayerInfo player : playerList){
			/**计算各自的最大牌型*/
			if (player.getCardType() > player.getMaxCardType()) {
				player.setMaxCardType(player.getCardType());
			}
		}
		
		/**设置房间的总赢家及当前赢家*/
		Integer totalWinnerId = playerList.get(0).getPlayerId();
		Integer curWinnerId = playerList.get(0).getPlayerId();
		Integer maxTotalScore = playerList.get(0).getTotalScore();
		Integer maxCurScore = playerList.get(0).getCurScore();
		for(MjPlayerInfo player : playerList){
			Integer tempTotalScore = player.getTotalScore();
			Integer tempCurScore = player.getCurScore();
			if (tempTotalScore > maxTotalScore) {
				maxTotalScore = tempTotalScore;
				totalWinnerId = player.getPlayerId();
			}
			if (tempCurScore > maxCurScore) {
				maxCurScore = tempCurScore;
				curWinnerId = player.getPlayerId();
			}
		}
		roomInfo.setTotalWinnerId(totalWinnerId);
		roomInfo.setCurWinnerId(curWinnerId);
		/**如果当前局数小于总局数，则设置为当前局结束*/
		if (roomInfo.getCurGame() < roomInfo.getTotalGames()) {
			roomInfo.setStatus(MjRoomStatusEnum.curGameOver.status);
		}else{/**如果当前局数等于总局数，则设置为一圈结束*/
			roomInfo.setStatus(MjRoomStatusEnum.totalGameOver.status);
			try {
				commonManager.addUserRecord(roomInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/**如果是第一局结束，则扣除房卡;扣除房卡异常不影响游戏进行，会将异常数据放入redis中，由定时任务进行补偿扣除*/
		if (roomInfo.getCurGame() == 1) {
			if (redisOperationService.isLoginFuseOpen()) {
				log.info("扣除房卡开始===============");
				try {
					List<Integer> palyerIdList = commonManager.deductRoomCard(roomInfo, RoomCardOperationEnum.consumeCard);
					log.info("palyerIdList:" + JsonUtil.toJson(palyerIdList));
					for(Integer playerId : palyerIdList){
						UserModel userM = commonManager.getUserById(playerId);
						roomCardNumUpdate(userM.getRoomCardNum(), playerId);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				log.info("扣除房卡结束===============");
			}
		}else{/**如果不是第一局，则只扣除观察者的房卡 add by liujinfengnew*/
			if (redisOperationService.isLoginFuseOpen()) {
				log.info("扣除房卡开始===============");
				try {
					List<Integer> palyerIdList = commonManager.deductRoomCardForObserver(roomInfo, RoomCardOperationEnum.consumeCard);
					log.info("palyerIdList:" + JsonUtil.toJson(palyerIdList));
					for(Integer playerId : palyerIdList){
						UserModel userM = commonManager.getUserById(playerId);
						roomCardNumUpdate(userM.getRoomCardNum(), playerId);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				log.info("扣除房卡结束===============");
			}
		}
		
	}
	

	public static void main(String[] args) {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(1);
		list.add(2);
		list.add(2);
		list.add(3);
		list.add(3);
		list.remove(1);
		System.out.println(JsonUtil.toJson(list));
	}
	
	@Override
	public List<BaseRoomInfo> doRefreshRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		return null;
	}

	@Override
	public BaseRoomInfo getRoomInfo(ChannelHandlerContext ctx,
			BaseRequest request, UserInfo userInfo) {
		Integer roomId = userInfo.getRoomId();
		MjRoomInfo roomInfo = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
		return roomInfo;
	}


}
