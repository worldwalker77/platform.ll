package cn.worldwalker.game.wyqp.mj.service;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.tiles.xmlDefinition.I18nFactorySet;
import org.springframework.stereotype.Service;

import cn.worldwalker.game.wyqp.common.domain.base.BaseRequest;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.base.UserInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjMsg;
import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.nn.NnRoomInfo;
import cn.worldwalker.game.wyqp.common.enums.DissolveStatusEnum;
import cn.worldwalker.game.wyqp.common.enums.GameTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.MsgTypeEnum;
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
			roomInfo.setUpdateTime(new Date());
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			
			/**给当前玩家返回摸牌信息*/
			Map<String, Object> data = new HashMap<String, Object>();
			result.setData(data);
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
			/**给其他玩家返回出牌消息及当前说话玩家*/
			data.clear();
			data.put("playerId", roomInfo.getLastPlayerId());
			data.put("cardIndex", roomInfo.getLastCardIndex());
			data.put("curPlayerId", curPlayerId);
			result.setMsgType(MsgTypeEnum.chuPai.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
			/**给其他的玩家返回补花数*/
			if (curPlayer.getCurAddFlowerNum() > 0) {
				data.clear();
				data.put("curPlayerId", curPlayerId);
				data.put("addFlowerCount", curPlayer.getCurAddFlowerNum());
				result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
			}
		}else{
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
