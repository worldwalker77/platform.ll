package cn.worldwalker.game.wyqp.mj.service;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import cn.worldwalker.game.wyqp.common.constant.Constant;
import cn.worldwalker.game.wyqp.common.domain.base.BaseMsg;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRequest;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.base.UserInfo;
import cn.worldwalker.game.wyqp.common.domain.base.UserModel;
import cn.worldwalker.game.wyqp.common.domain.mj.MjMsg;
import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.enums.GameTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.MsgTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.OnlineStatusEnum;
import cn.worldwalker.game.wyqp.common.enums.RoomCardOperationEnum;
import cn.worldwalker.game.wyqp.common.enums.RoomStatusEnum;
import cn.worldwalker.game.wyqp.common.exception.BusinessException;
import cn.worldwalker.game.wyqp.common.exception.ExceptionEnum;
import cn.worldwalker.game.wyqp.common.result.Result;
import cn.worldwalker.game.wyqp.common.service.BaseGameService;
import cn.worldwalker.game.wyqp.common.utils.GameUtil;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;
import cn.worldwalker.game.wyqp.common.utils.SnowflakeIdGenerator;
import cn.worldwalker.game.wyqp.common.utils.log.ThreadPoolMgr;
import cn.worldwalker.game.wyqp.mj.cards.MjCardResource;
import cn.worldwalker.game.wyqp.mj.cards.MjCardRule;
import cn.worldwalker.game.wyqp.mj.cards.MjCardTypeCalculation;
import cn.worldwalker.game.wyqp.mj.enums.MjHuTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjOperationEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjPlayerStatusEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjRoomStatusEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjTypeEnum;
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
		roomInfo.setIsChiPai(msg.getIsChiPai());
		if (MjTypeEnum.shangHaiBaiDa.type.equals(request.getDetailType())) {
			roomInfo.setModel(msg.getModel());
			if (msg.getModel()  == 8) {
				roomInfo.setIndexLine(31);
			}else{
				roomInfo.setIndexLine(34);
			}
			roomInfo.setNoBaiDaCanQiangGang(msg.getNoBaiDaCanQiangGang());
			roomInfo.setNoBaiDaCanZhuaChong(msg.getNoBaiDaCanZhuaChong());
		}else{
			roomInfo.setIndexLine(31);
		}
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
		if (!MjRoomStatusEnum.justBegin.status.equals(roomInfo.getStatus())&& !MjRoomStatusEnum.curGameOver.status.equals(roomInfo.getStatus())) {
			throw new BusinessException(ExceptionEnum.ROOM_STATUS_CAN_NOT_READY);
		}
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
		
		if (readyCount == 4) {
			/**计算庄家*/
			calculateRoomBanker(roomInfo);
			MjCardRule.initMjRoom(roomInfo);
			List<Integer> tableRemainderCardList = null;
			if (Constant.isTest == 1) {
				tableRemainderCardList = MjCardRule.getTableCardList();//测试用
			}else{
				tableRemainderCardList = MjCardResource.genTableOutOrderCardList();
			}

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
			List<Integer> dices = new ArrayList<Integer>();
			boolean isKaiBao = MjCardRule.playDices(dices);
			roomInfo.setDices(dices);
			data.put("dices", dices);
			if (isKaiBao) {
				roomInfo.setIsCurGameKaiBao(1);
			}
			List<Integer> handCardListBeforeAddFlower = null;
			String handCardAddFlower = null;
			/**第54张牌是痞子，用于翻癞子*/
			Integer piZiCardIndex = MjCardResource.genPiZiCardInex(tableRemainderCardList,roomInfo.getIndexLine());
			Integer baiDaCardIndex = MjCardResource.genBaiDaCardIndex(piZiCardIndex);
			roomInfo.setPiZiCardIndex(piZiCardIndex);
			roomInfo.setBaiDaCardIndex(baiDaCardIndex);
			/**为每个玩家设置牌*/
			for(int i = 0; i < size; i++ ){
				MjPlayerInfo player = playerList.get(i);
				MjCardRule.initMjPlayer(player);
				/**如果是庄家则发14张牌*/
				if (player.getPlayerId().equals(roomInfo.getRoomBankerId())) {
					/**当前说话玩家的手牌缓存，由于没有补花之前的牌需要返回给客户端*/
					handCardListBeforeAddFlower = new ArrayList<Integer>();
					if (Constant.isTest == 1) {
//						player.setHandCardList(MjCardRule.getHandCardListByIndex(i, true));//测试用
					}else{
						player.setHandCardList(MjCardResource.genHandCardList(tableRemainderCardList, 14));
					}

					/**补花之前的牌缓存*/
					handCardListBeforeAddFlower.addAll(player.getHandCardList());
					/**校验手牌补花*/
					handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo, player);
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
					data.put("piZiCardIndex", piZiCardIndex);
					data.put("baiDaCardIndex", baiDaCardIndex);
					channelContainer.sendTextMsgByPlayerIds(result, player.getPlayerId());
				}else{/**闲家发13张牌*/
					if (Constant.isTest == 1) {
//						player.setHandCardList(MjCardRule.getHandCardListByIndex(i, false));//测试用
					}else{
						player.setHandCardList(MjCardResource.genHandCardList(roomInfo.getTableRemainderCardList(), 13));
					}
					

					data.put("handCardList", player.getHandCardList());
					data.remove("handCardAddFlower");
					data.remove("operations");
					data.put("piZiCardIndex", piZiCardIndex);
					data.put("baiDaCardIndex", baiDaCardIndex);
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
			/**记录回放操作日志*/
			addOperationLog(MsgTypeEnum.initRoom.msgType, null, roomInfo, null, null, null, null);
			addOperationLog(MsgTypeEnum.initHandCards.msgType, null, roomInfo, null, handCardAddFlower, null, handCardListBeforeAddFlower);
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
			String moPaiAddFlower = null;
			try {
				moPaiAddFlower = MjCardRule.checkMoPaiAddFlower(roomInfo, curPlayer);
			} catch (BusinessException e) {
				huangZhuang(roomInfo, roomId);
				return;
			}
			String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo, curPlayer);
			if (StringUtils.isNotBlank(handCardAddFlower)) {
				handCardAddFlower = MjCardRule.replaceFlowerCards(curPlayer.getHandCardList(), handCardAddFlower);
			}
			/**计算摸牌后当前玩家有哪些操作权限*/
			if (moPaiAddFlower.split(",").length > 1) {/**补花后是否可以杠开*/
				MjCardRule.calculateAllPlayerOperations(roomInfo, MjCardRule.getRealMoPai(moPaiAddFlower), curPlayerId, 4);
			}else{
				MjCardRule.calculateAllPlayerOperations(roomInfo, MjCardRule.getRealMoPai(moPaiAddFlower), curPlayerId, 1);
			}
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
			List<Integer> playerIdList = GameUtil.getPlayerIdListWithOutSelf(playerList, curPlayerId);
			playerIdList.remove(playerId);
			channelContainer.sendTextMsgByPlayerIds(result, playerIdList);
			data.put("handCardList", player.getHandCardList());
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
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
			/**给摸牌的玩家返回手牌*/
			curPlayer.getHandCardList().add(MjCardRule.getRealMoPai(moPaiAddFlower));
			data.put("handCardList", curPlayer.getHandCardList());
			channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
			
			/**给其他的玩家返回补花数*/
			if (curPlayer.getCurAddFlowerNum() > 0) {
				data.clear();
				data.put("curPlayerId", curPlayerId);
				data.put("addFlowerCount", curPlayer.getCurAddFlowerNum());
				result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
			}
			/**给所有玩家返回桌面剩余牌张数*/
			noticeAllPlayerRemaindCardNum(roomInfo);
			/**记录回放操作日志*/
			addOperationLog(MsgTypeEnum.chuPai.msgType, (MjMsg)request.getMsg(), roomInfo, null, null, null, null);
			/**记录回放操作日志*/
			addOperationLog(MsgTypeEnum.moPai.msgType, null, roomInfo, null, handCardAddFlower, moPaiAddFlower, null);
		}else{
			roomInfo.setCurPlayerId(curPlayerId);
			roomInfo.setUpdateTime(new Date());
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			/**记录回放操作日志*/
			addOperationLog(MsgTypeEnum.chuPai.msgType, (MjMsg)request.getMsg(), roomInfo, null, null, null, null);
			/**给其他玩家返回出牌消息及当前说话玩家*/
			Map<String, Object> data = new HashMap<String, Object>();
			result.setData(data);
			data.put("playerId", playerId);
			data.put("cardIndex", msg.getCardIndex());
			data.put("curPlayerId", curPlayerId);
			result.setMsgType(MsgTypeEnum.chuPai.msgType);
			List<Integer> playerIdList = GameUtil.getPlayerIdListWithOutSelf(playerList, curPlayerId);
			MjPlayerInfo curPlayer = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), curPlayerId);
			/**如果出牌的玩家和出牌吼后具有操作权限的玩家是同一个，则说明是出牌后听牌操作*/
			if (curPlayerId.equals(playerId)) {
				channelContainer.sendTextMsgByPlayerIds(result, playerIdList);
				data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId));
				data.put("handCardList", curPlayer.getHandCardList());
				channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
			}else{
				playerIdList.remove(playerId);
				channelContainer.sendTextMsgByPlayerIds(result, playerIdList);
				data.put("handCardList", player.getHandCardList());
				channelContainer.sendTextMsgByPlayerIds(result, playerId);
				data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId));
				data.remove("handCardList");
				channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
			}
			
		}
		
	}
	/**
	 * 通知所有玩家当前桌面剩余牌数
	 * @param roomInfo
	 */
	public void noticeAllPlayerRemaindCardNum(MjRoomInfo roomInfo){
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		data.put("remaindCardNum", roomInfo.getTableRemainderCardList().size());
		result.setMsgType(MsgTypeEnum.remaindCardNum.msgType);
		result.setGameType(GameTypeEnum.mj.gameType);
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(roomInfo.getPlayerList()));
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
		String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo, player);
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
		data.put("cardIndex", roomInfo.getLastCardIndex());
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
			result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		}
		
		/**记录回放操作日志*/
		addOperationLog(MsgTypeEnum.chi.msgType, (MjMsg)request.getMsg(), roomInfo, null, null, null, null);
		
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
		String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo, player);
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
		/**记录回放操作日志*/
		addOperationLog(MsgTypeEnum.peng.msgType, (MjMsg)request.getMsg(), roomInfo, null, handCardAddFlower, null, null);
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
		
		/**计算其玩家是否可以抢杠*/
		MjCardRule.calculateAllPlayerOperations(roomInfo, Integer.valueOf(msg.getGangCards()), playerId, 3);
		Integer curPlayerId = MjCardRule.getPlayerHighestPriorityPlayerId(roomInfo);
		/**不为空，则说明其他玩家可以抢杠*/
		if (curPlayerId != null) {
			roomInfo.setCurPlayerId(curPlayerId);
			roomInfo.setUpdateTime(new Date());
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			Map<String, Object> data = new HashMap<String, Object>();
			result.setData(data);
			/**给抢杠玩家以外的玩家返回杠玩家的信息及获取操作权限的玩家*/
			data.put("curPlayerId", curPlayerId);
			data.put("playerId", playerId);
			data.put("cardIndex", mingGangCardList.get(0));
			data.put("mingGangCardList", mingGangCardList);
			result.setMsgType(MsgTypeEnum.mingGang.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
			/***给抢杠的玩家返回可操作权限*/
			data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId));
			channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
			/**记录回放操作日志*/
			addOperationLog(MsgTypeEnum.mingGang.msgType, (MjMsg)request.getMsg(), roomInfo, player, null, null, null);
		}else{/**如果没有其他玩家可以抢杠*/
			/**将玩家当前轮补花数设置为0*/
			player.setCurAddFlowerNum(0);
			String moPaiAddFlower = null;
			try {
				moPaiAddFlower = MjCardRule.checkMoPaiAddFlower(roomInfo, player);
			} catch (BusinessException e) {
				huangZhuang(roomInfo, roomId);
				return;
			}
			String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo, player);
			if (StringUtils.isNotBlank(handCardAddFlower)) {
				/**将手牌中的花牌全部替换为补花后的正常牌*/
				handCardAddFlower = MjCardRule.replaceFlowerCards(player.getHandCardList(), handCardAddFlower);
			}
			/**计算摸牌后当前玩家有哪些操作权限(杠开)*/
			MjCardRule.calculateAllPlayerOperations(roomInfo, MjCardRule.getRealMoPai(moPaiAddFlower), playerId, 4);
			
			roomInfo.setUpdateTime(new Date());
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			
			Map<String, Object> data = new HashMap<String, Object>();
			result.setData(data);
			
			/**给其所有玩家返回明杠信息*/
			data.clear();
			data.put("playerId", roomInfo.getLastPlayerId());
			data.put("cardIndex", roomInfo.getLastCardIndex());
			data.put("curPlayerId", playerId);
			data.put("mingGangCardList", mingGangCardList);
			result.setMsgType(MsgTypeEnum.mingGang.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
			/**记录回放操作日志*/
			addOperationLog(MsgTypeEnum.mingGang.msgType, (MjMsg)request.getMsg(), roomInfo, player, null, null, null);
			/**给杠的玩家返回摸牌*/
			data.clear();
			data.put("curPlayerId", playerId);
			data.put("moPaiAddFlower", moPaiAddFlower);
			if (StringUtils.isNotBlank(handCardAddFlower)) {
				data.put("handCardAddFlower", handCardAddFlower);
			}
			if (MjCardRule.getPlayerHighestPriority(roomInfo, playerId) != null) {
				data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, playerId));
			}
			result.setMsgType(MsgTypeEnum.moPai.msgType);
			/**给摸牌的玩家返回手牌*/
			player.getHandCardList().add(MjCardRule.getRealMoPai(moPaiAddFlower));
			data.put("handCardList", player.getHandCardList());
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			
			
			/**如果手牌存在补花，则给其他玩家返回补花数*/
			if (player.getCurAddFlowerNum() > 0) {
				data.clear();
				data.put("curPlayerId", playerId);
				data.put("addFlowerCount", player.getCurAddFlowerNum());
				result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
			}
			
			/**给所有玩家返回桌面剩余牌张数*/
			noticeAllPlayerRemaindCardNum(roomInfo);
			/**记录回放操作日志*/
			addOperationLog(MsgTypeEnum.moPai.msgType, null, roomInfo, player, handCardAddFlower, moPaiAddFlower, null);
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
		String moPaiAddFlower = null;
		try {
			moPaiAddFlower = MjCardRule.checkMoPaiAddFlower(roomInfo, player);
		} catch (BusinessException e) {
			huangZhuang(roomInfo, roomId);
			return;
		}
//		/**由于暗杠肯定是摸牌后的暗杠，所以下面手牌补花不需要*/
//		String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo.getTableRemainderCardList(), player);
//		if (StringUtils.isNotBlank(handCardAddFlower)) {
//			handCardAddFlower = MjCardRule.replaceFlowerCards(player.getHandCardList(), handCardAddFlower);
//		}
		/**计算摸牌后当前玩家有哪些操作权限*/
		MjCardRule.calculateAllPlayerOperations(roomInfo, MjCardRule.getRealMoPai(moPaiAddFlower), playerId, 4);
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		/**给其他玩家返回暗杠信息*/
		data.clear();
		data.put("curPlayerId", playerId);
		data.put("cardIndex", anGangCardList.get(0));
		data.put("anGangCardList", anGangCardList);
		result.setMsgType(MsgTypeEnum.anGang.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
		/**记录回放操作日志*/
		addOperationLog(MsgTypeEnum.anGang.msgType, (MjMsg)request.getMsg(), roomInfo, player, null, null, null);
		
		/**给当前玩家返回摸牌信息*/
		data.clear();
		data.put("curPlayerId", playerId);
		data.put("moPaiAddFlower", moPaiAddFlower);
//		if (StringUtils.isNotBlank(handCardAddFlower)) {
//			data.put("handCardAddFlower", handCardAddFlower);
//		}
		if (MjCardRule.getPlayerHighestPriority(roomInfo, playerId) != null) {
			data.put("operations", MjCardRule.getPlayerHighestPriority(roomInfo, playerId));
		}
		result.setMsgType(MsgTypeEnum.moPai.msgType);
		/**给摸牌的玩家返回手牌*/
		player.getHandCardList().add(MjCardRule.getRealMoPai(moPaiAddFlower));
		data.put("handCardList", player.getHandCardList());
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
		
		/**如果手牌存在补花，则给其他玩家返回补花数*/
		if (player.getCurAddFlowerNum() > 0) {
			data.clear();
			data.put("curPlayerId", playerId);
			data.put("addFlowerCount", player.getCurAddFlowerNum());
			result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		}
		
		/**给所有玩家返回桌面剩余牌张数*/
		noticeAllPlayerRemaindCardNum(roomInfo);
		
		/**记录回放操作日志*/
		addOperationLog(MsgTypeEnum.moPai.msgType, null, roomInfo, player, null, moPaiAddFlower, null);
	}
	
	public void tingPai(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
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
		/**校验玩家是否有听操作权限*/
		if (!MjCardRule.checkCurOperationValid(roomInfo, playerId, MjOperationEnum.tingHu.type, null)) {
			throw new BusinessException(ExceptionEnum.NO_AUTHORITY);
		}
		MjPlayerInfo player = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), playerId);
		/**设置状态为听牌*/
		player.setIsTingHu(1);
		/**将当前玩家的可操作性权限删除*/
		MjCardRule.delPlayerOperationByPlayerId(roomInfo, playerId);
		/**听牌后操作权限还在自己*/
		roomInfo.setCurPlayerId(playerId);
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		/**给所有玩家返回听牌消息及当前说话的玩家*/
		result.setData(data);
		data.put("playerId", playerId);
		data.put("curPlayerId", playerId);
		result.setMsgType(MsgTypeEnum.tingPai.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
		
		/**记录回放操作日志*/
		addOperationLog(MsgTypeEnum.tingPai.msgType, (MjMsg)request.getMsg(), roomInfo, null, null, null, null);
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
		/**将当前玩家的可操作性权限删除*/
		TreeMap<Integer, String> delOperation = MjCardRule.delPlayerOperationByPlayerId(roomInfo, playerId);
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
				String huStr = delOperation.get(MjOperationEnum.hu.type);
				/**如果pass的是抢杠，则需要给当时杠的玩家返回摸牌*/
				if (StringUtils.isNotBlank(huStr) && huStr.startsWith("3")) {
					curPlayerId = roomInfo.getLastPlayerId();
				}else{/**否则，出牌玩家的下家摸牌*/
					curPlayerId = GameUtil.getNextPlayerId(playerList, roomInfo.getLastPlayerId());
				}
				/**摸牌并校验补花*/
				MjPlayerInfo curPlayer = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), curPlayerId);
				/**将玩家当前轮补花数设置为0*/
				curPlayer.setCurAddFlowerNum(0);
				String moPaiAddFlower = null;
				try {
					moPaiAddFlower = MjCardRule.checkMoPaiAddFlower(roomInfo, curPlayer);
				} catch (BusinessException e) {
					huangZhuang(roomInfo, roomId);
					return;
				}
				String handCardAddFlower = MjCardRule.checkHandCardsAddFlower(roomInfo, curPlayer);
				if (StringUtils.isNotBlank(handCardAddFlower)) {
					handCardAddFlower = MjCardRule.replaceFlowerCards(curPlayer.getHandCardList(), handCardAddFlower);
				}
				/**计算摸牌后当前玩家有哪些操作权限*/
				if (moPaiAddFlower.split(",").length > 1) {/**补花后是否可以杠开*/
					MjCardRule.calculateAllPlayerOperations(roomInfo, MjCardRule.getRealMoPai(moPaiAddFlower), curPlayerId, 4);
				}else{
					MjCardRule.calculateAllPlayerOperations(roomInfo, MjCardRule.getRealMoPai(moPaiAddFlower), curPlayerId, 1);
				}
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
				/**给摸牌的玩家返回手牌*/
				curPlayer.getHandCardList().add(MjCardRule.getRealMoPai(moPaiAddFlower));
				data.put("handCardList", curPlayer.getHandCardList());
				channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
				/**给其他的玩家返回补花数*/
				if (curPlayer.getCurAddFlowerNum() > 0) {
					data.clear();
					data.put("curPlayerId", curPlayerId);
					data.put("addFlowerCount", curPlayer.getCurAddFlowerNum());
					result.setMsgType(MsgTypeEnum.addFlowerNotice.msgType);
					channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
				}
				/**给所有玩家返回桌面剩余牌张数*/
				noticeAllPlayerRemaindCardNum(roomInfo);
				
				/**记录回放操作日志*/
				addOperationLog(MsgTypeEnum.moPai.msgType, null, roomInfo, null, handCardAddFlower, moPaiAddFlower, null);
				
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
		MjPlayerInfo player = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), playerId);
		player.setIsHu(1);
		/**获取胡牌的类型*/
		Integer playerHuTypeInt = Integer.valueOf(MjCardRule.getPlayerHighestPriority(roomInfo, playerId).get(MjOperationEnum.hu.type));
		/**将当前玩家的可操作性权限删除*/
		MjCardRule.delPlayerOperationByPlayerId(roomInfo, playerId);
		
		/**获取剩余玩家最高可操作性权限*/
		Integer curPlayerId = MjCardRule.getPlayerHighestPriorityPlayerId(roomInfo);
		if (curPlayerId == null) {
			if (roomInfo.getIsFeiCangyin() > 0) {
				Integer feiCangYingCardIndex = feiCangYing(player, roomInfo.getTableRemainderCardList());
				data.put("feiCangYingCardIndex", feiCangYingCardIndex);
			}
			curPlayerId = playerId;
			player.setHuType(playerHuTypeInt);
			calculateScore(roomInfo);
			roomInfo.setCurPlayerId(curPlayerId);
			roomInfo.setUpdateTime(new Date());
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			if (roomInfo.getStatus().equals(MjRoomStatusEnum.totalGameOver.status)) {
				result.setMsgType(MsgTypeEnum.totalSettlement.msgType);
			}else{
				result.setMsgType(MsgTypeEnum.curSettlement.msgType);
			}
			data.put("curPlayerId", curPlayerId);
			data.put("huType", playerHuTypeInt);
			if (MjHuTypeEnum.zhuaChong.type.equals(playerHuTypeInt) || MjHuTypeEnum.qiangGang.type.equals(playerHuTypeInt)) {
				data.put("cardIndex", roomInfo.getLastCardIndex());
				data.put("dianPaoPlayerId", roomInfo.getLastPlayerId());
			}else if(MjHuTypeEnum.ziMo.type.equals(playerHuTypeInt) || MjHuTypeEnum.gangKai.type.equals(playerHuTypeInt)){
				data.put("cardIndex", player.getCurMoPaiCardIndex());
			}else if(MjHuTypeEnum.tianHu.type.equals(playerHuTypeInt)){
				
			}else{
				throw new BusinessException(ExceptionEnum.HU_TYPE_ERROR);
			}
			data.put("totalWinnerId", roomInfo.getTotalWinnerId());
			List<Map<String, Object>> newPlayerList = new ArrayList<Map<String,Object>>();
			for(MjPlayerInfo temp : playerList){
				Map<String, Object> newPlayer = new HashMap<String, Object>();
				newPlayer.put("playerId", temp.getPlayerId());
				newPlayer.put("nickName", temp.getNickName());
				newPlayer.put("headImgUrl", temp.getHeadImgUrl());
				newPlayer.put("handCardList", temp.getHandCardList());
				newPlayer.put("chiCardList", temp.getChiCardList());
				newPlayer.put("pengCardList", temp.getPengCardList());
				newPlayer.put("mingGangCardList", temp.getMingGangCardList());
				newPlayer.put("anGangCardList", temp.getAnGangCardList());
				newPlayer.put("curScore", temp.getCurScore());
				newPlayer.put("isHu", temp.getIsHu());
				newPlayer.put("mjCardTypeList", temp.getMjCardTypeList());
				newPlayer.put("huType", temp.getHuType());
				newPlayer.put("buttomAndFlowerScore", temp.getButtomAndFlowerScore());
				newPlayer.put("multiple", temp.getMultiple());
				if (temp.getFeiCangYingCardIndex() != null) {
					newPlayer.put("feiCangYingCardIndex", temp.getFeiCangYingCardIndex());
				}
				if (roomInfo.getStatus().equals(MjRoomStatusEnum.totalGameOver.status)) {
					newPlayer.put("ziMoCount", temp.getZiMoCount());
					newPlayer.put("zhuaChongCount", temp.getZhuaChongCount());
					newPlayer.put("dianPaoCount", temp.getDianPaoCount());
					newPlayer.put("totalScore", temp.getTotalScore());
				}
				newPlayerList.add(newPlayer);
			}
			data.put("playerList", newPlayerList);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
			/**记录回放操作日志*/
			addOperationLog(result.getMsgType(), null, roomInfo, player, null, null, null);
		}else{
			TreeMap<Integer, String> curPlayerOperations = MjCardRule.getPlayerHighestPriority(roomInfo, curPlayerId);
			/**如果没有其他玩家可以胡牌，则本局结束，开始结算*/
			if (curPlayerOperations == null || curPlayerOperations.size() == 0 || !curPlayerOperations.containsKey(MjOperationEnum.hu.type)) {
				
				if (roomInfo.getIsFeiCangyin() > 0) {
					Integer feiCangYingCardIndex = feiCangYing(player, roomInfo.getTableRemainderCardList());
					data.put("feiCangYingCardIndex", feiCangYingCardIndex);
				}
				curPlayerId = playerId;
				player.setHuType(playerHuTypeInt);
				calculateScore(roomInfo);
				roomInfo.setCurPlayerId(curPlayerId);
				roomInfo.setUpdateTime(new Date());
				redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
				
				if (roomInfo.getStatus().equals(MjRoomStatusEnum.totalGameOver.status)) {
					result.setMsgType(MsgTypeEnum.totalSettlement.msgType);
				}else{
					result.setMsgType(MsgTypeEnum.curSettlement.msgType);
				}
				data.put("curPlayerId", curPlayerId);
				data.put("huType", playerHuTypeInt);
				
				if (MjHuTypeEnum.zhuaChong.type.equals(playerHuTypeInt) || MjHuTypeEnum.qiangGang.type.equals(playerHuTypeInt)) {
					data.put("cardIndex", roomInfo.getLastCardIndex());
					data.put("dianPaoPlayerId", roomInfo.getLastPlayerId());
				}else if(MjHuTypeEnum.ziMo.type.equals(playerHuTypeInt) || MjHuTypeEnum.gangKai.type.equals(playerHuTypeInt)){
					data.put("cardIndex", player.getCurMoPaiCardIndex());
				}else if(MjHuTypeEnum.tianHu.type.equals(playerHuTypeInt)){
					
				}else{
					throw new BusinessException(ExceptionEnum.HU_TYPE_ERROR);
				}
				data.put("totalWinnerId", roomInfo.getTotalWinnerId());
				List<Map<String, Object>> newPlayerList = new ArrayList<Map<String,Object>>();
				for(MjPlayerInfo temp : playerList){
					Map<String, Object> newPlayer = new HashMap<String, Object>();
					newPlayer.put("playerId", temp.getPlayerId());
					newPlayer.put("nickName", temp.getNickName());
					newPlayer.put("headImgUrl", temp.getHeadImgUrl());
					newPlayer.put("handCardList", temp.getHandCardList());
					newPlayer.put("chiCardList", temp.getChiCardList());
					newPlayer.put("pengCardList", temp.getPengCardList());
					newPlayer.put("mingGangCardList", temp.getMingGangCardList());
					newPlayer.put("anGangCardList", temp.getAnGangCardList());
					newPlayer.put("curScore", temp.getCurScore());
					newPlayer.put("isHu", temp.getIsHu());
					newPlayer.put("mjCardTypeList", temp.getMjCardTypeList());
					newPlayer.put("huType", temp.getHuType());
					newPlayer.put("buttomAndFlowerScore", temp.getButtomAndFlowerScore());
					newPlayer.put("multiple", temp.getMultiple());
					if (temp.getFeiCangYingCardIndex() != null) {
						newPlayer.put("feiCangYingCardIndex", temp.getFeiCangYingCardIndex());
					}
					if (roomInfo.getStatus().equals(MjRoomStatusEnum.totalGameOver.status)) {
						newPlayer.put("ziMoCount", temp.getZiMoCount());
						newPlayer.put("zhuaChongCount", temp.getZhuaChongCount());
						newPlayer.put("dianPaoCount", temp.getDianPaoCount());
						newPlayer.put("totalScore", temp.getTotalScore());
					}
					newPlayerList.add(newPlayer);
				}
				data.put("playerList", newPlayerList);
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
				/**记录回放操作日志*/
				addOperationLog(result.getMsgType(), null, roomInfo, player, null, null, null);
			}else{/**如果有其他玩家可以胡牌，则需要通知其他玩家胡牌*/
				if (roomInfo.getIsFeiCangyin() > 0) {
					Integer feiCangYingCardIndex = feiCangYing(player, roomInfo.getTableRemainderCardList());
					data.put("feiCangYingCardIndex", feiCangYingCardIndex);
				}
				
				player.setHuType(playerHuTypeInt);
				roomInfo.setCurPlayerId(curPlayerId);
				roomInfo.setUpdateTime(new Date());
				redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
				
				data.put("handCardList", player.getHandCardList());
				data.put("playerId", playerId);
				data.put("huType", playerHuTypeInt);
				
				if (MjHuTypeEnum.zhuaChong.type.equals(playerHuTypeInt) || MjHuTypeEnum.qiangGang.type.equals(playerHuTypeInt)) {
					data.put("cardIndex", roomInfo.getLastCardIndex());
					data.put("dianPaoPlayerId", roomInfo.getLastPlayerId());
				}else if(MjHuTypeEnum.ziMo.type.equals(playerHuTypeInt) || MjHuTypeEnum.gangKai.type.equals(playerHuTypeInt)){
					data.put("cardIndex", player.getCurMoPaiCardIndex());
				}else if(MjHuTypeEnum.tianHu.type.equals(playerHuTypeInt)){
					
				}else{
					throw new BusinessException(ExceptionEnum.HU_TYPE_ERROR);
				}
				/**当前说话者id*/
				data.put("curPlayerId", curPlayerId);
				result.setMsgType(MsgTypeEnum.huPai.msgType);
				/**给下一个可胡牌玩家之外的其他玩家返回信息*/
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, curPlayerId));
				TreeMap<Integer, String> operations = new TreeMap<Integer, String>();
				operations.put(MjOperationEnum.hu.type, String.valueOf(MjHuTypeEnum.zhuaChong.type));
				/**当前说话者可操作权限，胡牌*/
				data.put("operations", operations);
				/**给下一个可胡牌玩家返回信息*/
				channelContainer.sendTextMsgByPlayerIds(result, curPlayerId);
				
				/**记录回放操作日志*/
				addOperationLog(MsgTypeEnum.huPai.msgType, null, roomInfo, player, null, null, null);
			}
		}
	}
	
	public Integer feiCangYing(MjPlayerInfo player, List<Integer> tableRemainderCardList){
		Integer feiCangYingCardIndex = null;
		if (tableRemainderCardList.size() > 0) {
			feiCangYingCardIndex = tableRemainderCardList.remove(0);
		}else{
			feiCangYingCardIndex = GameUtil.genFeiCangYingCardIndex();
		}
		player.setFeiCangYingCardIndex(feiCangYingCardIndex);
		return feiCangYingCardIndex;
		
	}
	/**荒庄处理*/
	public void huangZhuang(MjRoomInfo roomInfo, Integer roomId){
		roomInfo.setIsCurGameHuangZhuang(1);
		calculateScore(roomInfo);
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		Result result = new Result();
		result.setGameType(GameTypeEnum.mj.gameType);
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		if (roomInfo.getStatus().equals(MjRoomStatusEnum.totalGameOver.status)) {
			result.setMsgType(MsgTypeEnum.totalSettlement.msgType);
		}else{
			result.setMsgType(MsgTypeEnum.curSettlement.msgType);
		}
		data.put("isCurGameHuangZhuang", 1);
		data.put("totalWinnerId", roomInfo.getTotalWinnerId());
		List<MjPlayerInfo> playerList =  roomInfo.getPlayerList();
		List<Map<String, Object>> newPlayerList = new ArrayList<Map<String,Object>>();
		for(MjPlayerInfo temp : playerList){
			Map<String, Object> newPlayer = new HashMap<String, Object>();
			newPlayer.put("playerId", temp.getPlayerId());
			newPlayer.put("nickName", temp.getNickName());
			newPlayer.put("headImgUrl", temp.getHeadImgUrl());
			newPlayer.put("handCardList", temp.getHandCardList());
			newPlayer.put("chiCardList", temp.getChiCardList());
			newPlayer.put("pengCardList", temp.getPengCardList());
			newPlayer.put("mingGangCardList", temp.getMingGangCardList());
			newPlayer.put("anGangCardList", temp.getAnGangCardList());
			newPlayer.put("curScore", temp.getCurScore());
			newPlayer.put("isHu", temp.getIsHu());
			if (roomInfo.getStatus().equals(MjRoomStatusEnum.totalGameOver.status)) {
				newPlayer.put("ziMoCount", temp.getZiMoCount());
				newPlayer.put("zhuaChongCount", temp.getZhuaChongCount());
				newPlayer.put("dianPaoCount", temp.getDianPaoCount());
			}
			newPlayerList.add(newPlayer);
		}
		data.put("playerList", newPlayerList);
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
		
		
	}
	/**
	 * 计算庄家
	 * @param roomInfo
	 */
	private void calculateRoomBanker(MjRoomInfo roomInfo){
		List<MjPlayerInfo> playerList = roomInfo.getPlayerList();
		List<Integer> huPlayerIdList = new ArrayList<Integer>();
		/**将其他玩家的牌依次与庄家进行比较，计算各自得当前局分及总得分，最大牌型，并计算下一次庄家是谁*/
		for(MjPlayerInfo player : playerList){
			/**计算各自的最大牌型*/
			if (player.getCardType() > player.getMaxCardType()) {
				player.setMaxCardType(player.getCardType());
			}
			if (player.getIsHu() == 1) {
				huPlayerIdList.add(player.getPlayerId());
			}
		}
		if (huPlayerIdList.size() == 0) {/**可能是开局或者荒庄*/
			
		}else if(huPlayerIdList.size() == 1){
			roomInfo.setRoomBankerId(huPlayerIdList.get(0));
		}else{
			roomInfo.setRoomBankerId(roomInfo.getLastPlayerId());
		}
	}
	/**
	 * 牌局结束，计算得分
	 * @param roomInfo
	 */
	private void calculateScore(MjRoomInfo roomInfo){
//		log.info("第" + roomInfo.getCurGame() + "局结算前roomInfo:" + JsonUtil.toJson(roomInfo));
		/**如果当前局没有荒*/
		if (roomInfo.getIsCurGameHuangZhuang() == 0) {
			List<MjPlayerInfo> playerList = roomInfo.getPlayerList();
			List<MjPlayerInfo> huPlayerList = new ArrayList<MjPlayerInfo>();
			for(MjPlayerInfo player : playerList){
				if (player.getIsHu() == 1) {
					MjCardTypeCalculation.calButtomFlowerScoreAndCardTypeAndMultiple(player, roomInfo);
					huPlayerList.add(player);
				}
			}
			MjPlayerInfo huPlayer = null;
			MjPlayerInfo dianPaoPlayer = null;
			/**如果只有一个人胡牌*/
			if (huPlayerList.size() == 1) {
				huPlayer = huPlayerList.get(0);
				Integer tempScore = huPlayer.getButtomAndFlowerScore()*huPlayer.getMultiple();
				if (tempScore > roomInfo.getHuScoreLimit()) {
					tempScore = roomInfo.getHuScoreLimit();
				}
				Integer huType = huPlayer.getHuType();
				if (MjHuTypeEnum.zhuaChong.type.equals(huType) || MjHuTypeEnum.qiangGang.type.equals(huType)) {
					huPlayer.setCurScore(tempScore);
					huPlayer.setZhuaChongCount(huPlayer.getZhuaChongCount() + 1);
					dianPaoPlayer = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), roomInfo.getLastPlayerId());
					dianPaoPlayer.setCurScore(0 - tempScore);
					dianPaoPlayer.setDianPaoCount(dianPaoPlayer.getDianPaoCount() + 1);
				}else if(MjHuTypeEnum.ziMo.type.equals(huType) || MjHuTypeEnum.gangKai.type.equals(huType)){
					huPlayer.setCurScore(tempScore*3);
					huPlayer.setZiMoCount(huPlayer.getZiMoCount() + 1);
					for(MjPlayerInfo player : playerList){
						if (!player.getPlayerId().equals(huPlayer.getPlayerId())) {
							player.setCurScore(0 - tempScore);
						}
					}
				}
			}else if(huPlayerList.size() > 1 && huPlayerList.size() < 4){/**如果是一炮多响*/
				dianPaoPlayer = MjCardRule.getPlayerInfoByPlayerId(roomInfo.getPlayerList(), roomInfo.getLastPlayerId());
				for(MjPlayerInfo player : huPlayerList){
					Integer tempScore = player.getButtomAndFlowerScore()*player.getMultiple();
					if (tempScore > roomInfo.getHuScoreLimit()) {
						tempScore = roomInfo.getHuScoreLimit();
					}
					player.setCurScore(tempScore);
					player.setZhuaChongCount(player.getZhuaChongCount() + 1);
					dianPaoPlayer.setCurScore(dianPaoPlayer.getCurScore() - tempScore);
					dianPaoPlayer.setDianPaoCount(dianPaoPlayer.getDianPaoCount() + 1);
				}
			}else{
				throw new BusinessException(ExceptionEnum.HU_PLAYER_NUM_ERROR);
			}
			/**计算每个玩家总得分及设置房间的总赢家*/
			Integer totalWinnerId = playerList.get(0).getPlayerId();
			Integer maxTotalScore = playerList.get(0).getTotalScore();
			for(MjPlayerInfo player : playerList){
				player.setTotalScore(player.getTotalScore() + player.getCurScore());
				Integer tempTotalScore = player.getTotalScore();
				if (tempTotalScore > maxTotalScore) {
					maxTotalScore = tempTotalScore;
					totalWinnerId = player.getPlayerId();
				}
			}
			roomInfo.setTotalWinnerId(totalWinnerId);
		}
		
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
		/**记录每一局得分*/
		commonManager.addUserRecordDetail(roomInfo);
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
		
//		log.info("第" + roomInfo.getCurGame() + "局结算后roomInfo:" + JsonUtil.toJson(roomInfo));
	}
	

	public static void main(String[] args) {
		MjRoomInfo roomInfo = new MjRoomInfo();
		roomInfo.setHuButtomScore(2);
		roomInfo.setEachFlowerScore(1);
		roomInfo.setIsFeiCangyin(1);
		roomInfo.setIsKaiBao(1);
		roomInfo.setIsHuangFan(1);
		roomInfo.setIsCurGameKaiBao(0);
		roomInfo.setLastCardIndex(0);
		roomInfo.setLastPlayerId(824613);
		MjPlayerInfo player = new MjPlayerInfo();
		roomInfo.getPlayerList().add(player);
		player.setPlayerId(585234);
		player.setIsHu(1);
		player.setHuType(MjHuTypeEnum.zhuaChong.type);
		player.setCurMoPaiCardIndex(null);
		player.setFeiCangYingCardIndex(5);
		List<Integer> handCardList = Arrays.asList(0,0,6,6);
		List<Integer> chiCardList = Arrays.asList();
		List<Integer> pengCardList = Arrays.asList(30,30,30,18,18,18,25,25,25);
		List<Integer> mingGangCardList = Arrays.asList();
		List<Integer> anGangCardList = Arrays.asList();
		List<Integer> flowerCardList = Arrays.asList(39,32,31);
		player.setHandCardList(handCardList);
		player.setChiCardList(chiCardList);
		player.setPengCardList(pengCardList);
		player.setMingGangCardList(mingGangCardList);
		player.setAnGangCardList(anGangCardList);
		player.setFlowerCardList(flowerCardList);
		
		MjPlayerInfo player1 = new MjPlayerInfo();
		player1.setPlayerId(824613);
		player1.setIsHu(0);
		player1.setCurScore(0);
		player1.setTotalScore(0);
		player1.setDianPaoCount(0);
		player1.setZhuaChongCount(0);
		player1.setZiMoCount(0);
		MjPlayerInfo player2 = new MjPlayerInfo();
		player2.setPlayerId(714638);
		player2.setIsHu(0);
		player2.setCurScore(0);
		player2.setTotalScore(0);
		player2.setDianPaoCount(0);
		player2.setZhuaChongCount(0);
		player2.setZiMoCount(0);
		MjPlayerInfo player3 = new MjPlayerInfo();
		player3.setPlayerId(393774);
		player3.setIsHu(0);
		player3.setCurScore(0);
		player3.setTotalScore(0);
		player3.setDianPaoCount(0);
		player3.setZhuaChongCount(0);
		player3.setZiMoCount(0);
		roomInfo.getPlayerList().add(player1);
		roomInfo.getPlayerList().add(player2);
		roomInfo.getPlayerList().add(player3);
//		calculateScore(roomInfo);
		System.out.println(JsonUtil.toJson(player.getMjCardTypeList()));
		System.out.println(JsonUtil.toJson(roomInfo));
		System.out.println("底花分：" + player.getButtomAndFlowerScore());
		System.out.println("倍数：" + player.getMultiple());
	}
	
	@Override
	public List<BaseRoomInfo> doRefreshRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		List<BaseRoomInfo> roomInfoList = new ArrayList<BaseRoomInfo>();
		Integer playerId = userInfo.getPlayerId();
		Integer roomId = userInfo.getRoomId();
		MjRoomInfo roomInfo = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
		MjRoomInfo newRoomInfo = new MjRoomInfo();
		roomInfoList.add(roomInfo);
		roomInfoList.add(newRoomInfo);
		List<MjPlayerInfo> playerList = roomInfo.getPlayerList();
		/**根据不同的房间及玩家状态设置返回房间信息*/
		MjRoomStatusEnum roomStatusEnum = MjRoomStatusEnum.getRoomStatusEnum(roomInfo.getStatus());
		newRoomInfo.setGameType(roomInfo.getGameType());
		newRoomInfo.setStatus(roomStatusEnum.status);
		newRoomInfo.setRoomId(roomId);
		newRoomInfo.setRoomOwnerId(roomInfo.getRoomOwnerId());
		newRoomInfo.setRoomBankerId(roomInfo.getRoomBankerId());
		newRoomInfo.setHuButtomScore(roomInfo.getHuButtomScore());
		newRoomInfo.setEachFlowerScore(roomInfo.getEachFlowerScore());
		newRoomInfo.setIsKaiBao(roomInfo.getIsKaiBao());
		newRoomInfo.setIsHuangFan(roomInfo.getIsHuangFan());
		newRoomInfo.setIsFeiCangyin(roomInfo.getIsFeiCangyin());
		newRoomInfo.setIsChiPai(roomInfo.getIsChiPai());
		newRoomInfo.setHuScoreLimit(roomInfo.getHuScoreLimit());
		newRoomInfo.setTotalGames(roomInfo.getTotalGames());
		newRoomInfo.setCurGame(roomInfo.getCurGame());
		newRoomInfo.setPayType(roomInfo.getPayType());
		newRoomInfo.setCurPlayerId(roomInfo.getCurPlayerId());
		newRoomInfo.setLastPlayerId(roomInfo.getLastPlayerId());
		newRoomInfo.setLastCardIndex(roomInfo.getLastCardIndex());
		newRoomInfo.setIsCurGameHuangZhuang(roomInfo.getIsCurGameHuangZhuang());
		newRoomInfo.setIsCurGameKaiBao(roomInfo.getIsCurGameKaiBao());
		newRoomInfo.setDices(roomInfo.getDices());
		newRoomInfo.setTotalWinnerId(roomInfo.getTotalWinnerId());
		for(MjPlayerInfo player : playerList){
			MjPlayerInfo newPlayer = new MjPlayerInfo();
			newRoomInfo.getPlayerList().add(newPlayer);
			newPlayer.setPlayerId(player.getPlayerId());
			newPlayer.setNickName(player.getNickName());
			newPlayer.setHeadImgUrl(player.getHeadImgUrl());
			newPlayer.setOrder(player.getOrder());
			newPlayer.setRoomCardNum(player.getRoomCardNum());
			newPlayer.setLevel(player.getLevel());
			newPlayer.setStatus(player.getStatus());
			newPlayer.setTotalScore(player.getTotalScore());
			if (playerId.equals(player.getPlayerId())) {
				newPlayer.setOnlineStatus(OnlineStatusEnum.online.status);
			}else{
				newPlayer.setOnlineStatus(player.getOnlineStatus());
			}
			newPlayer.setChiCardList(player.getChiCardList());
			newPlayer.setPengCardList(player.getPengCardList());
			newPlayer.setMingGangCardList(player.getMingGangCardList());
			newPlayer.setAnGangCardList(player.getAnGangCardList());
			newPlayer.setDiscardCardList(player.getDiscardCardList());
			newPlayer.setTotalAddFlowerNum(player.getFlowerCardList().size());
			switch (roomStatusEnum) {
				case justBegin:
					break;
				case inGame:
					newPlayer.setIsTingHu(player.getIsTingHu());
					if (playerId.equals(player.getPlayerId())) {
						newPlayer.setHandCardList(player.getHandCardList());
						if (playerId.equals(roomInfo.getCurPlayerId())) {
							newPlayer.setOperations(MjCardRule.getPlayerHighestPriority(roomInfo, playerId));
							newPlayer.setCurMoPaiCardIndex(player.getCurMoPaiCardIndex());
						}
					}
					break;
				case curGameOver:
					newPlayer.setHandCardList(player.getHandCardList());
					newPlayer.setIsHu(player.getIsHu());
					newPlayer.setHuType(player.getHuType());
					newPlayer.setMjCardTypeList(player.getMjCardTypeList());
					newPlayer.setCurScore(player.getCurScore());
					newPlayer.setMultiple(player.getMultiple());
					newPlayer.setButtomAndFlowerScore(player.getButtomAndFlowerScore());
					break;
				case totalGameOver:
					newPlayer.setHandCardList(player.getHandCardList());
					newPlayer.setIsHu(player.getIsHu());
					newPlayer.setHuType(player.getHuType());
					newPlayer.setMjCardTypeList(player.getMjCardTypeList());
					newPlayer.setCurScore(player.getCurScore());
					newPlayer.setMultiple(player.getMultiple());
					newPlayer.setButtomAndFlowerScore(player.getButtomAndFlowerScore());
					newPlayer.setWinTimes(player.getWinTimes());
					newPlayer.setLoseTimes(player.getLoseTimes());
					break;
				default:
					break;
			}
		}
		
		return roomInfoList;
	}

	@Override
	public BaseRoomInfo getRoomInfo(ChannelHandlerContext ctx,
			BaseRequest request, UserInfo userInfo) {
		Integer roomId = userInfo.getRoomId();
		MjRoomInfo roomInfo = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
		return roomInfo;
	}

	public void addOperationLog(Integer msgType, MjMsg msg, MjRoomInfo roomInfo, MjPlayerInfo oPlayer,
			String handCardAddFlower, String moPaiAddFlower, List<Integer> beforeAddFlowerHandCardList){
		try {
			List<MjPlayerInfo> playerList = roomInfo.getPlayerList();
			Result result = new Result();
			Map<String, Object> data = new HashMap<String, Object>();
			result.setData(data);
			result.setGameType(GameTypeEnum.mj.gameType);
			result.setMsgType(msgType);
			result.setTimeStamp(SnowflakeIdGenerator.idWorker.nextId());
			result.setUuid(roomInfo.getCurGameUuid());
			MsgTypeEnum msgTypeEnum = MsgTypeEnum.getMsgTypeEnumByType(msgType);
			switch (msgTypeEnum) {
			case initRoom:
				data.put("dices", roomInfo.getDices());
				data.put("isCurGameKaiBao", roomInfo.getIsCurGameKaiBao());
				data.put("huangFanNum", roomInfo.getHuangFanNum());
				data.put("roomBankerId", roomInfo.getRoomBankerId());
				data.put("roomOwnerId", roomInfo.getRoomOwnerId());
				List<Map<String, Object>> playerMapList0 = new ArrayList<Map<String,Object>>();
				data.put("playerList", playerMapList0);
				for(MjPlayerInfo player : playerList){
					Map<String, Object> playerMap = new HashMap<String, Object>();
					playerMap.put("playerId", player.getPlayerId());
					playerMap.put("nickName", player.getNickName());
					playerMap.put("headImgUrl", player.getHeadImgUrl());
					playerMap.put("order", player.getOrder());
					playerMapList0.add(playerMap);
				}
				break;
			case initHandCards:
				data.put("curPlayerId", roomInfo.getRoomBankerId());
				List<Map<String, Object>> playerMapList = new ArrayList<Map<String,Object>>();
				data.put("playerList", playerMapList);
				for(MjPlayerInfo player : playerList){
					Map<String, Object> playerMap = new HashMap<String, Object>();
					playerMap.put("playerId", player.getPlayerId());
					if (player.getPlayerId().equals(roomInfo.getRoomBankerId())) {
						playerMap.put("handCardList", beforeAddFlowerHandCardList);
						playerMap.put("handCardAddFlower", handCardAddFlower);
					}else{
						playerMap.put("handCardList", player.getHandCardList());
					}
					playerMapList.add(playerMap);
				}
				break;
			case chuPai:
				data.put("playerId", msg.getPlayerId());
				data.put("cardIndex", msg.getCardIndex());
				data.put("curPlayerId", roomInfo.getCurPlayerId());
				break;
			case moPai:
				data.put("playerId", roomInfo.getCurPlayerId());
				data.put("moPaiAddFlower", moPaiAddFlower);
				data.put("handCardAddFlower", handCardAddFlower);
				data.put("curPlayerId", roomInfo.getCurPlayerId());
				break;
			case chi:
				data.put("playerId", roomInfo.getLastPlayerId());
				data.put("cardIndex", roomInfo.getLastCardIndex());
				data.put("chiCardList", msg.getChiCards().split(","));
				data.put("curPlayerId", roomInfo.getCurPlayerId());
				data.put("handCardAddFlower", handCardAddFlower);
				break;
			case peng:
				data.put("playerId", roomInfo.getLastPlayerId());
				data.put("cardIndex", roomInfo.getLastCardIndex());
				data.put("curPlayerId", roomInfo.getCurPlayerId());
				data.put("handCardAddFlower", handCardAddFlower);
				break;
			case mingGang:
				/**如果是摸牌后的明杠*/
				if (MjCardRule.isHandCard3n2(oPlayer)) {
					data.put("playerId", oPlayer.getPlayerId());
				}else{
					data.put("playerId", roomInfo.getLastPlayerId());
				}
				data.put("cardIndex", msg.getGangCards());
				data.put("curPlayerId", roomInfo.getCurPlayerId());
				data.put("handCardAddFlower", handCardAddFlower);
				break;
			case anGang:
				data.put("cardIndex", msg.getGangCards());
				data.put("curPlayerId", roomInfo.getCurPlayerId());
				data.put("handCardAddFlower", handCardAddFlower);
				break;
			case tingPai:
				data.put("playerId", msg.getPlayerId());
				data.put("curPlayerId", roomInfo.getCurPlayerId());
				break;
			case huPai:/**一炮多响才通过胡牌接口返回，非一炮多响则通过结算接口*/
				data.put("huType", oPlayer.getHuType());
				data.put("playerId", oPlayer.getPlayerId());
				if (MjHuTypeEnum.zhuaChong.type.equals(oPlayer.getHuType()) || MjHuTypeEnum.qiangGang.type.equals(oPlayer.getHuType())) {
					data.put("cardIndex", roomInfo.getLastCardIndex());
					data.put("dianPaoPlayerId", roomInfo.getLastPlayerId());
				}else if(MjHuTypeEnum.ziMo.type.equals(oPlayer.getHuType()) || MjHuTypeEnum.gangKai.type.equals(oPlayer.getHuType())){
					data.put("cardIndex", oPlayer.getCurMoPaiCardIndex());
				}
				data.put("feiCangYingCardIndex", oPlayer.getFeiCangYingCardIndex());
				data.put("curPlayerId", roomInfo.getCurPlayerId());
				break;
			case curSettlement:
				data.put("curPlayerId", oPlayer.getPlayerId());
				data.put("huType", oPlayer.getHuType());
				if (MjHuTypeEnum.zhuaChong.type.equals(oPlayer.getHuType()) || MjHuTypeEnum.qiangGang.type.equals(oPlayer.getHuType())) {
					data.put("cardIndex", roomInfo.getLastCardIndex());
					data.put("dianPaoPlayerId", roomInfo.getLastPlayerId());
				}else if(MjHuTypeEnum.ziMo.type.equals(oPlayer.getHuType()) || MjHuTypeEnum.gangKai.type.equals(oPlayer.getHuType())){
					data.put("cardIndex", oPlayer.getCurMoPaiCardIndex());
				}
				data.put("feiCangYingCardIndex", oPlayer.getFeiCangYingCardIndex());
				List<Map<String, Object>> csPlayerList = new ArrayList<Map<String,Object>>();
				data.put("playerList", csPlayerList);
				for(MjPlayerInfo player : playerList){
					Map<String, Object> newPlayer = new HashMap<String, Object>();
					newPlayer.put("curScore", player.getCurScore());
					newPlayer.put("isHu", player.getIsHu());
					newPlayer.put("mjCardTypeList", player.getMjCardTypeList());
					newPlayer.put("huType", player.getHuType());
					newPlayer.put("buttomAndFlowerScore", player.getButtomAndFlowerScore());
					newPlayer.put("multiple", player.getMultiple());
					newPlayer.put("totalScore", player.getTotalScore());
					csPlayerList.add(newPlayer);
				}
				break;
			case totalSettlement:
				data.put("curPlayerId", oPlayer.getPlayerId());
				data.put("huType", oPlayer.getHuType());
				if (MjHuTypeEnum.zhuaChong.type.equals(oPlayer.getHuType()) || MjHuTypeEnum.qiangGang.type.equals(oPlayer.getHuType())) {
					data.put("cardIndex", roomInfo.getLastCardIndex());
					data.put("dianPaoPlayerId", roomInfo.getLastPlayerId());
				}else if(MjHuTypeEnum.ziMo.type.equals(oPlayer.getHuType()) || MjHuTypeEnum.gangKai.type.equals(oPlayer.getHuType())){
					data.put("cardIndex", oPlayer.getCurMoPaiCardIndex());
				}
				data.put("feiCangYingCardIndex", oPlayer.getFeiCangYingCardIndex());
				List<Map<String, Object>> newPlayerList = new ArrayList<Map<String,Object>>();
				data.put("playerList", newPlayerList);
				data.put("totalWinnerId", roomInfo.getTotalWinnerId());
				for(MjPlayerInfo player : playerList){
					Map<String, Object> newPlayer = new HashMap<String, Object>();
					newPlayer.put("curScore", player.getCurScore());
					newPlayer.put("isHu", player.getIsHu());
					newPlayer.put("mjCardTypeList", player.getMjCardTypeList());
					newPlayer.put("huType", player.getHuType());
					newPlayer.put("buttomAndFlowerScore", player.getButtomAndFlowerScore());
					newPlayer.put("multiple", player.getMultiple());
					newPlayer.put("ziMoCount", player.getZiMoCount());
					newPlayer.put("zhuaChongCount", player.getZhuaChongCount());
					newPlayer.put("dianPaoCount", player.getDianPaoCount());
					newPlayer.put("totalScore", player.getTotalScore());
					newPlayerList.add(newPlayer);
				}
				break;
			default:
				break;
			}
			ThreadPoolMgr.getLogDataInsertProcessor().processAwait(result);
		} catch (Exception e) {
			log.error("记录回放日志异常, msgType:" + msgType + ",msg:" + JsonUtil.toJson(msg) + ",oPlayer:" + JsonUtil.toJson(oPlayer) + ",roomInf:" + JsonUtil.toJson(roomInfo), e);
		}
	}
}
