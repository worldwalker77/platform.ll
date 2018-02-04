package cn.worldwalker.game.wyqp.mj.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.enums.DissolveStatusEnum;
import cn.worldwalker.game.wyqp.common.exception.BusinessException;
import cn.worldwalker.game.wyqp.common.exception.ExceptionEnum;
import cn.worldwalker.game.wyqp.common.utils.GameUtil;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;
import cn.worldwalker.game.wyqp.common.utils.SnowflakeIdGenerator;
import cn.worldwalker.game.wyqp.mj.enums.MjHuTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjOperationEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjPlayerStatusEnum;
import cn.worldwalker.game.wyqp.mj.huvalidate.Hulib;
import cn.worldwalker.game.wyqp.mj.huvalidate.TableMgr;


public class MjCardRule {
	private static Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
	private static List<Integer> tableList = Arrays.asList(
			/**1-9万*/	0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5,6,6,6,6,7,7,7,7,8,8,8,8,
			/**1-9筒*/	9,9,9,9,10,10,10,10,11,11,11,11,12,12,12,12,13,13,13,13,14,14,14,14,15,15,15,15,16,16,16,16,17,17,17,17,
			/**1-9条*/	18,18,18,18,19,19,19,19,20,20,20,20,21,21,21,21,22,22,22,22,23,23,23,23,24,24,24,24,25,25,25,25,26,26,26,26,
		/**东南西北中发白*/	27,27,27,27,28,28,28,28,29,29,29,29,30,30,30,30,31,31,31,31,32,32,32,32,33,33,33,33,
		/**春夏秋冬梅兰竹菊*/	34,35,36,37,38,39,40,41);
	private static List<Integer> list1 = Arrays.asList(0,0,0,1,1,1,2,2,2,3,3,6,4,5);
	private static List<Integer> list2 = Arrays.asList(5,5,5,7,7,8,8,9,9,10,15,17,19);
	private static List<Integer> list3 = Arrays.asList(3,4,6,7,8,9,10,11,12,13,14,17,17);
	private static List<Integer> list4 = Arrays.asList(20,21,17,30,22,34,38,26,27,24,23,25,19);
	static{
		map.put(1, list1);
		map.put(2, list2);
		map.put(3, list3);
		map.put(4, list4);
	}
	public static void setHEveryandCardList(Map<Integer, List<Integer>> amap){
		map.putAll(amap);
		
	}
	public List<Integer> getHandCardList(int bankerOrder, int curOrder){
		List<Integer> list = new ArrayList<Integer>();
		if (bankerOrder == curOrder) {
			list.addAll(map.get(1));
		}else if((bankerOrder + 1)%4 == curOrder){
			list.addAll(map.get(2));
		}else if ((bankerOrder + 2)%4 == curOrder) {
			list.addAll(map.get(3));
		}else if ((bankerOrder + 3)%4 == curOrder) {
			list.addAll(map.get(4));
		}
		return list;
	}
	public static List<Integer> getTableCardList(){
		List<Integer> curTableCardList = new ArrayList<Integer>();
		curTableCardList.addAll(tableList);
		curTableCardList.removeAll(map.get(0));
		curTableCardList.removeAll(map.get(1));
		curTableCardList.removeAll(map.get(2));
		curTableCardList.removeAll(map.get(3));
		return curTableCardList;
	}
	
	public static TreeMap<Integer, String> delPlayerOperationByPlayerId(MjRoomInfo roomInfo, Integer playerId){
		return roomInfo.getPlayerOperationMap().remove(playerId);
	}
	
	/**
	 * 从玩家可操作权限列表里面获取具有最高操作权限的玩家id
	 * 优先级顺序为：听胡>胡>杠、碰>吃
	 * @return
	 */
	public static Integer getPlayerHighestPriorityPlayerId(MjRoomInfo roomInfo){
		LinkedHashMap<Integer, TreeMap<Integer, String>> allPlayerOperations = roomInfo.getPlayerOperationMap();
		if (allPlayerOperations ==  null || allPlayerOperations.isEmpty()) {
			return null;
		}
		Set<Entry<Integer, TreeMap<Integer, String>>> set = allPlayerOperations.entrySet();
		Integer maxPriorityPlayerId = null;
		Integer maxPriorityOperationType = 0;
		for(Entry<Integer, TreeMap<Integer, String>> entry : set){
			Integer playerId = entry.getKey();
			Map<Integer, String> curPlayerOperations = entry.getValue();
			Set<Integer> keySet = curPlayerOperations.keySet();
			Integer operationType = Collections.max(keySet);
			if (operationType > maxPriorityOperationType) {
				maxPriorityOperationType = operationType;
				maxPriorityPlayerId = playerId;
			}
		}
		return maxPriorityPlayerId;
	}
	public static TreeMap<Integer, String> getPlayerHighestPriority(MjRoomInfo roomInfo, Integer playerId){
		LinkedHashMap<Integer, TreeMap<Integer, String>> allPlayerOperations = roomInfo.getPlayerOperationMap();
		if (allPlayerOperations == null || allPlayerOperations.size() == 0) {
			return null;
		}
		return allPlayerOperations.get(playerId);
	}
	
	public static boolean checkCurOperationValid(MjRoomInfo roomInfo, Integer playerId, Integer operationType, String operationStr){
		LinkedHashMap<Integer, TreeMap<Integer, String>> allOperations = roomInfo.getPlayerOperationMap();
		if (allOperations == null || allOperations.size() == 0) {
			return false;
		}
		TreeMap<Integer, String> curOperation = allOperations.get(playerId);
		if (curOperation == null || curOperation.size() == 0) {
			return false;
		}
		String existOperationStr = curOperation.get(operationType);
		if (operationType.equals(MjOperationEnum.hu.type) || operationType.equals(MjOperationEnum.tingHu.type)) {
			if (StringUtils.isBlank(existOperationStr)) {
				return false;
			}
		}else{
			if (!existOperationStr.contains(operationStr)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static void initMjRoom(MjRoomInfo roomInfo){
		roomInfo.setLastCardIndex(null);
		roomInfo.setLastPlayerId(null);
		roomInfo.setIsCurGameKaiBao(0);
		if (roomInfo.getIsCurGameHuangZhuang() == 1) {
			roomInfo.setHuangFanNum(roomInfo.getHuangFanNum() + 1);
			roomInfo.setIsCurGameHuangZhuang(0);
		}
		roomInfo.setCurGameUuid(SnowflakeIdGenerator.idWorker.nextId());
	}
	
	public static void initMjPlayer(MjPlayerInfo playerInfo){
		playerInfo.setCurScore(0);
		playerInfo.setIsTingHu(0);
		playerInfo.setCurMoPaiCardIndex(null);
		playerInfo.setCurAddFlowerNum(0);
		playerInfo.setIsHu(0);
		playerInfo.setHuType(0);
		playerInfo.setMultiple(0);
		playerInfo.setTotalAddFlowerNum(0);
		playerInfo.setButtomAndFlowerScore(0);
		playerInfo.setFeiCangYingCardIndex(null);
		playerInfo.getMjCardTypeList().clear();
		playerInfo.setHandCardList(null);
		playerInfo.getChiCardList().clear();
		playerInfo.getPengCardList().clear();
		playerInfo.getMingGangCardList().clear();
		playerInfo.getAnGangCardList().clear();
		playerInfo.getDiscardCardList().clear();
		playerInfo.getFlowerCardList().clear();
		/**设置每个玩家的解散房间状态为不同意解散，后面大结算返回大厅的时候回根据此状态判断是否解散房间*/
		playerInfo.setDissolveStatus(DissolveStatusEnum.disagree.status);
		playerInfo.setStatus(MjPlayerStatusEnum.notReady.status);
	}
	/**
	 * 摇色子
	 * @return
	 */
	public static boolean playDices(List<Integer> list){
		Integer dice1 = GameUtil.genDice();
		Integer dice2 = GameUtil.genDice();
		list.add(dice1);
		list.add(dice2);
		String temp = dice1 + "" + dice2;
		if ("11".equals(temp) || "44".equals(temp) ||"14".equals(temp) ||"41".equals(temp)) {
			return true;
		}
		return false;
	}
	
	public static Integer getRealMoPai(String moPaiAddFlower){
		String[] ar = moPaiAddFlower.split(",");
		int len = ar.length;
		return Integer.valueOf(ar[len - 1]);
	}
	
	public static boolean isHandCard3n2(MjPlayerInfo player){
		int size = player.getHandCardList().size();
		if (player.getCurMoPaiCardIndex() != null) {
			size += 1;
		}
		if (size%3 == 2) {
			return true;
		}
		return false;
	}
	
	public static List<Integer> moveOperationCards(MjRoomInfo roomInfo, MjPlayerInfo player, MjOperationEnum operationType, String operationStr){
		List<Integer> operationList = new  ArrayList<Integer>();
		String[] strArr = operationStr.split(",");
		int len = strArr.length;
		MjPlayerInfo lastPlayer = getPlayerInfoByPlayerId(roomInfo.getPlayerList(), roomInfo.getLastPlayerId());
		List<Integer> lastPlayerDiscardCardList = lastPlayer.getDiscardCardList();
		List<Integer> handCardList = player.getHandCardList();
		switch (operationType) {
		case chi:
			operationList.add(Integer.valueOf(strArr[0]));
			operationList.add(Integer.valueOf(strArr[1]));
			operationList.add(roomInfo.getLastCardIndex());
			/**将吃牌的两张牌从手牌中移出*/
			handCardList.remove(operationList.get(0));
			handCardList.remove(operationList.get(1));
			Collections.sort(operationList);
			/**吃牌加入吃牌列表*/
			player.getChiCardList().addAll(operationList);
			/**将打出的那张牌移出已经出牌列表*/
			lastPlayerDiscardCardList.remove(lastPlayerDiscardCardList.size() - 1);
			break;
		case peng:
			Integer pengCardIndex = Integer.valueOf(strArr[0]);
			handCardList.remove(pengCardIndex);
			handCardList.remove(pengCardIndex);
			List<Integer> pengCardList = player.getPengCardList();
			pengCardList.add(pengCardIndex);
			pengCardList.add(pengCardIndex);
			pengCardList.add(pengCardIndex);
			lastPlayerDiscardCardList.remove(lastPlayerDiscardCardList.size() - 1);
			
			operationList.add(pengCardIndex);
			operationList.add(pengCardIndex);
			operationList.add(pengCardIndex);
			break;
		case mingGang:
			Integer mingGangCardIndex = Integer.valueOf(strArr[0]);
			List<Integer> mingGangCardList = player.getMingGangCardList();
			/**如果是摸牌后的明杠*/
			if (isHandCard3n2(player)) {
				handCardList.remove(mingGangCardIndex);
				mingGangCardList.add(mingGangCardIndex);
				Collections.sort(mingGangCardList);
			}else{/**如果是别人打的牌的明杠*/
				/**从玩家手牌列表中移出三张杠牌*/
				handCardList.remove(mingGangCardIndex);
				handCardList.remove(mingGangCardIndex);
				handCardList.remove(mingGangCardIndex);
				/**从出牌玩家已经出牌列表中移出此张牌*/
				lastPlayerDiscardCardList.remove(lastPlayerDiscardCardList.size() - 1);
				/**将4张杠牌放入杠牌列表*/
				mingGangCardList.add(mingGangCardIndex);
				mingGangCardList.add(mingGangCardIndex);
				mingGangCardList.add(mingGangCardIndex);
				mingGangCardList.add(mingGangCardIndex);
			}
			operationList.add(mingGangCardIndex);
			operationList.add(mingGangCardIndex);
			operationList.add(mingGangCardIndex);
			operationList.add(mingGangCardIndex);
			break;
		case anGang:
			Integer anGangCardIndex = Integer.valueOf(strArr[0]);
			List<Integer> anGangCardList = player.getMingGangCardList();
			/**将4张杠牌从手牌中移出*/
			handCardList.remove(anGangCardIndex);
			handCardList.remove(anGangCardIndex);
			handCardList.remove(anGangCardIndex);
			handCardList.remove(anGangCardIndex);
			/***将4张杠牌加入暗杠列表*/
			anGangCardList.add(anGangCardIndex);
			anGangCardList.add(anGangCardIndex);
			anGangCardList.add(anGangCardIndex);
			anGangCardList.add(anGangCardIndex);
			
			operationList.add(anGangCardIndex);
			operationList.add(anGangCardIndex);
			operationList.add(anGangCardIndex);
			operationList.add(anGangCardIndex);
			break;

		default:
			break;
		}
		return operationList;
	}
	
	/**
	 * 摸牌或者出牌的时候，依次计算每个玩家的可操作权限
	 * @param list
	 * @param cardIndex
	 * @param playerId
	 * @param isMoPai
	 * @return
	 */
	public static LinkedHashMap<Integer, TreeMap<Integer, String>> calculateAllPlayerOperations(MjRoomInfo roomInfo, Integer cardIndex, Integer playerId, Integer type){
		List<MjPlayerInfo> list = roomInfo.getPlayerList();
		LinkedHashMap<Integer, TreeMap<Integer, String>> operations = new LinkedHashMap<Integer, TreeMap<Integer,String>>();
		/**找出出牌或者摸牌人自己*/
		MjPlayerInfo curPlayer = null;
		int size = list.size();
		int curPlayerIndex = 0;
		for(int i = 0; i < size; i++){
			MjPlayerInfo temp = list.get(i);
			if (playerId.equals(temp.getPlayerId())) {
				curPlayer = temp;
				curPlayerIndex = i;
				break;
			}
		}
		List<Integer> handCardList = curPlayer.getHandCardList();
		if (type == 0) {/**手牌列表校验*/
			/**格式化手牌*/
			int len = handCardList.size();
			int[] cards = new int[Hulib.indexLine];
			for(int j = 0; j < len; j++){
				if (handCardList.get(j) < Hulib.indexLine) {
					cards[handCardList.get(j)]++;
				}
			}
			/**暗杠校验**/
			TreeMap<Integer, String> map = checkHandCardGang(cards, curPlayer.getPengCardList());
			/**胡牌校验*/
			if (checkHu(curPlayer, null)) {
				map.put(MjOperationEnum.hu.type, String.valueOf(MjHuTypeEnum.tianHu.type));
			}
			/**听牌校验*/
			if (curPlayer.getIsTingHu() == 0) {
				Map<Integer, List<String>> chuCardIndexHuCardListMap = new HashMap<Integer, List<String>>();
				if (checkTingHu1(roomInfo, curPlayer, cardIndex, chuCardIndexHuCardListMap)) {
					/**4_4-2,7-1;7_4-2,7-1*/
					map.put(MjOperationEnum.tingHu.type, JsonUtil.toJson(chuCardIndexHuCardListMap));
				}
			}
			if (map.size() > 0) {
				operations.put(curPlayer.getPlayerId(), map);
			}
			
		}else if (type == 1) {/**如果是摸牌，则要判断摸牌的人是否可以明杠、暗杠、胡牌、听牌**/
			TreeMap<Integer, String> map = new TreeMap<Integer, String>();
			/**格式化手牌*/
			int len = handCardList.size();
			int[] cards = new int[Hulib.indexLine];
			for(int j = 0; j < len; j++){
				if (handCardList.get(j) < Hulib.indexLine) {
					cards[handCardList.get(j)]++;
				}
			}
			/**明杠校验**/
			String mingGangStr = checkMingGangByMoPai(curPlayer.getPengCardList(), cardIndex);
			if (StringUtils.isNotBlank(mingGangStr)) {
				map.put(MjOperationEnum.mingGang.type, mingGangStr);
			}
			/**暗杠校验**/
			String anGangStr = checkGang(cards, cardIndex);
			if (StringUtils.isNotBlank(anGangStr)) {
				map.put(MjOperationEnum.anGang.type, anGangStr);
			}
			/**之前放弃杠的牌再次校验**/
			TreeMap<Integer, String> handCardMap = checkHandCardGang(cards, curPlayer.getPengCardList());
			/**将杠合并，如果有*/
			if (StringUtils.isNotBlank(map.get(MjOperationEnum.mingGang.type))&&StringUtils.isNotBlank(handCardMap.get(MjOperationEnum.mingGang.type))) {
				map.put(MjOperationEnum.mingGang.type, map.get(MjOperationEnum.mingGang.type) + "_" + handCardMap.get(MjOperationEnum.mingGang.type));
			}else if(StringUtils.isBlank(map.get(MjOperationEnum.mingGang.type))&&StringUtils.isNotBlank(handCardMap.get(MjOperationEnum.mingGang.type))){
				map.put(MjOperationEnum.mingGang.type, handCardMap.get(MjOperationEnum.mingGang.type));
			}
			if (StringUtils.isNotBlank(map.get(MjOperationEnum.anGang.type))&&StringUtils.isNotBlank(handCardMap.get(MjOperationEnum.anGang.type))) {
				map.put(MjOperationEnum.anGang.type, map.get(MjOperationEnum.anGang.type) + "_" + handCardMap.get(MjOperationEnum.anGang.type));
			}else if(StringUtils.isBlank(map.get(MjOperationEnum.anGang.type))&&StringUtils.isNotBlank(handCardMap.get(MjOperationEnum.anGang.type))){
				map.put(MjOperationEnum.anGang.type, handCardMap.get(MjOperationEnum.anGang.type));
			}
			
			/**胡牌校验*/
			if (checkHu(curPlayer, cardIndex)) {
				map.put(MjOperationEnum.hu.type, String.valueOf(MjHuTypeEnum.ziMo.type));
			}
			/**听牌校验*/
			if (curPlayer.getIsTingHu() == 0) {
				Map<Integer, List<String>> chuCardIndexHuCardListMap = new HashMap<Integer, List<String>>();
				if (checkTingHu1(roomInfo, curPlayer, cardIndex, chuCardIndexHuCardListMap)) {
					/**4_4-2,7-1;7_4-2,7-1*/
					map.put(MjOperationEnum.tingHu.type, JsonUtil.toJson(chuCardIndexHuCardListMap));
				}
			}
			
			/**将摸牌索引设置到玩家信息中*/
			curPlayer.setCurMoPaiCardIndex(cardIndex);
			if (map.size() > 0) {
				operations.put(curPlayer.getPlayerId(), map);
			}
			
		}else if(type == 2){/**如果是出牌，依次判断其他的玩家是否可以吃、碰、明杠、胡**/
			/**听牌校验(只针对当前出牌的玩家，因为需要通知玩家听牌)*/
//			if (curPlayer.getIsTingHu() == 0) {
//				if (checkTingHu(curPlayer)) {
//					TreeMap<Integer, String> map0 = new TreeMap<Integer, String>();
//					map0.put(MjOperationEnum.tingHu.type, "1");
//					operations.put(curPlayer.getPlayerId(), map0);
//				}
//			}
			
			/**按顺序依次计算出剩余三个玩家可操作权限*/
			for(int i = 1; i <= 3; i++){
				MjPlayerInfo nextPlayer = list.get((curPlayerIndex + i)%4);
				handCardList = nextPlayer.getHandCardList();
				TreeMap<Integer, String> map1 = new TreeMap<Integer, String>();
				/**格式化手牌*/
				int len = handCardList.size();
				int[] cards = new int[Hulib.indexLine];
				for(int j = 0; j < len; j++){
					if (handCardList.get(j) < Hulib.indexLine) {
						cards[handCardList.get(j)]++;
					}
				}
				/**吃牌校验（只对出牌玩家的下家计算,只有没听胡的玩家才可以吃牌）*/
				if (i == 1) {
					if (roomInfo.getIsChiPai() > 0) {
						String chiStr = checkChiPai(nextPlayer, cardIndex);
						if (StringUtils.isNotBlank(chiStr)) {
							map1.put(MjOperationEnum.chi.type, chiStr);
						}
					}
				}
				/**碰牌校验*/
				String pengStr = checkPeng(cards, cardIndex, nextPlayer.getIsTingHu());
				if (StringUtils.isNotBlank(pengStr)) {
					map1.put(MjOperationEnum.peng.type, pengStr);
				}
				
				/**明杠牌校验*/
				String mingGangStr = checkGang(cards, cardIndex);
				if (StringUtils.isNotBlank(mingGangStr)) {
					map1.put(MjOperationEnum.mingGang.type, mingGangStr);
				}
				
				/**胡牌校验*/
				if (checkHu(nextPlayer, cardIndex)) {
					map1.put(MjOperationEnum.hu.type, String.valueOf(MjHuTypeEnum.zhuaChong.type));
				}
				if (map1.size() > 0) {
					operations.put(nextPlayer.getPlayerId(), map1);
				}
			}
		}else if(type == 3){/**判断是否可以抢杠*/
			/**按顺序依次计算出剩余三个玩家可操作权限*/
			for(int i = 1; i <= 3; i++){
				MjPlayerInfo nextPlayer = list.get((curPlayerIndex + i)%4);
				handCardList = nextPlayer.getHandCardList();
				TreeMap<Integer, String> map1 = new TreeMap<Integer, String>();
				/**格式化手牌*/
				int len = handCardList.size();
				int[] cards = new int[Hulib.indexLine];
				for(int j = 0; j < len; j++){
					if (handCardList.get(j) < Hulib.indexLine) {
						cards[handCardList.get(j)]++;
					}
				}
				
				/**以胡牌校验*/
				if (checkHu(nextPlayer, cardIndex)) {
					map1.put(MjOperationEnum.hu.type, String.valueOf(MjHuTypeEnum.qiangGang.type));
					roomInfo.setLastCardIndex(cardIndex);
					roomInfo.setLastPlayerId(playerId);
				}
				if (map1.size() > 0) {
					operations.put(nextPlayer.getPlayerId(), map1);
				}
			}
//			roomInfo.getPlayerOperationMap().putAll(operations);
//			return roomInfo.getPlayerOperationMap();
		}else if(type == 4){/**判断是否可以杠开*/
			TreeMap<Integer, String> map = new TreeMap<Integer, String>();
			/**格式化手牌*/
			int len = handCardList.size();
			int[] cards = new int[Hulib.indexLine];
			for(int j = 0; j < len; j++){
				if (handCardList.get(j) < Hulib.indexLine) {
					cards[handCardList.get(j)]++;
				}
			}
			/**明杠校验**/
			String mingGangStr = checkMingGangByMoPai(curPlayer.getPengCardList(), cardIndex);
			if (StringUtils.isNotBlank(mingGangStr)) {
				map.put(MjOperationEnum.mingGang.type, mingGangStr);
			}
			/**暗杠校验**/
			String anGangStr = checkGang(cards, cardIndex);
			if (StringUtils.isNotBlank(anGangStr)) {
				map.put(MjOperationEnum.anGang.type, anGangStr);
			}
			/**之前放弃杠的牌再次校验**/
			TreeMap<Integer, String> handCardMap = checkHandCardGang(cards, curPlayer.getPengCardList());
			/**将杠合并，如果有*/
			if (StringUtils.isNotBlank(map.get(MjOperationEnum.mingGang.type))&&StringUtils.isNotBlank(handCardMap.get(MjOperationEnum.mingGang.type))) {
				map.put(MjOperationEnum.mingGang.type, map.get(MjOperationEnum.mingGang.type) + "_" + handCardMap.get(MjOperationEnum.mingGang.type));
			}else if(StringUtils.isBlank(map.get(MjOperationEnum.mingGang.type))&&StringUtils.isNotBlank(handCardMap.get(MjOperationEnum.mingGang.type))){
				map.put(MjOperationEnum.mingGang.type, handCardMap.get(MjOperationEnum.mingGang.type));
			}
			if (StringUtils.isNotBlank(map.get(MjOperationEnum.anGang.type))&&StringUtils.isNotBlank(handCardMap.get(MjOperationEnum.anGang.type))) {
				map.put(MjOperationEnum.anGang.type, map.get(MjOperationEnum.anGang.type) + "_" + handCardMap.get(MjOperationEnum.anGang.type));
			}else if(StringUtils.isBlank(map.get(MjOperationEnum.anGang.type))&&StringUtils.isNotBlank(handCardMap.get(MjOperationEnum.anGang.type))){
				map.put(MjOperationEnum.anGang.type, handCardMap.get(MjOperationEnum.anGang.type));
			}
			
			/**胡牌校验*/
			if (checkHu(curPlayer, cardIndex)) {
				map.put(MjOperationEnum.hu.type, String.valueOf(MjHuTypeEnum.gangKai.type));
			}
			/**听牌校验*/
			if (curPlayer.getIsTingHu() == 0) {
				Map<Integer, List<String>> chuCardIndexHuCardListMap = new HashMap<Integer, List<String>>();
				if (checkTingHu1(roomInfo, curPlayer, cardIndex, chuCardIndexHuCardListMap)) {
					/**4_4-2,7-1;7_4-2,7-1*/
					map.put(MjOperationEnum.tingHu.type, JsonUtil.toJson(chuCardIndexHuCardListMap));
				}
			}
			/**将摸牌索引设置到玩家信息中*/
			curPlayer.setCurMoPaiCardIndex(cardIndex);
			if (map.size() > 0) {
				operations.put(curPlayer.getPlayerId(), map);
			}
		}
		roomInfo.setPlayerOperationMap(operations);
		return operations;
	}
	/**
	 * 校验手牌列表牌补花情况
	 * @param player
	 * @param tableRemainderCardList
	 * @return
	 */
	public static String checkHandCardsAddFlower(List<Integer> tableRemainderCardList, MjPlayerInfo player){
		List<Integer> handCardList = player.getHandCardList();
		int size = handCardList.size();
		Stack<Integer> handCardflowerCardStack = new Stack<Integer>();
		/**将手牌中已经有的花牌入栈*/
		for(int i = 0; i < size; i++){
			if (handCardList.get(i) >= Hulib.indexLine) {
				handCardflowerCardStack.push(handCardList.get(i));
			}
		}
		String addFlowerPath = addFlowerOperation(handCardflowerCardStack, player, tableRemainderCardList);
		return addFlowerPath;
	}
	
	public static String replaceFlowerCards(List<Integer> handCardList, String addFlowerStr){
		int handCardSize = handCardList.size();
		/**34,35,1_34,35,2*/
		String[] as = addFlowerStr.split("_");
		int arrLen = as.length;
		for(int i = 0; i < arrLen; i++){
			String temp = as[i];
			String[] tempPath = temp.split(",");
			int pathLen = tempPath.length;
			for(int j = 0; j < handCardSize; j++){
				if (handCardList.get(j).equals(Integer.valueOf(tempPath[0]))) {
					handCardList.set(j, Integer.valueOf(tempPath[pathLen - 1]));
					break;
				}
			}
		}
		
		/**返回给客户端的*/
		int maxPathLen = 0;
		for(int i = 0; i < arrLen; i++){
			String temp = as[i];
			String[] tempPath = temp.split(",");
			int pathLen = tempPath.length;
			if (pathLen > maxPathLen) {
				maxPathLen = pathLen;
			}
		}
		StringBuffer sbt = new StringBuffer("");
		for(int i = 0; i < maxPathLen; i++){
			StringBuffer sb = new StringBuffer("");
			for(int j = 0; j < arrLen; j++){
				String temp = as[j];
				String[] tempPath = temp.split(",");
				int pathLen = tempPath.length;
				if (pathLen > i) {
					sb.append(tempPath[i]).append(",");
				}
			}
			sbt.append(sb.substring(0, sb.length()-1)).append("_");
		}
		return sbt.substring(0, sbt.length()-1);
	}
	
	/**
	 * 校验摸牌补花的情况
	 * @param player
	 * @param tableRemainderCardList
	 * @return
	 */
	public static String checkMoPaiAddFlower(List<Integer> tableRemainderCardList, MjPlayerInfo player){
		/**如果桌牌数为0，则结束*/
		if (tableRemainderCardList.size() == 0) {
			throw new BusinessException(ExceptionEnum.NO_MORE_CARD_ERROR);
		}
		Integer tempCard = MjCardResource.mopai(tableRemainderCardList);
		/**如果摸的是非花牌，则直接返回这张牌*/
		if (tempCard  < Hulib.indexLine) {
			return String.valueOf(tempCard);
		}
		/**如果摸到的是花牌，则补花，直到补到非花牌为止*/
		Stack<Integer> moPaiflowerCardStack = new Stack<Integer>();
		moPaiflowerCardStack.push(tempCard);
		String addFlowerPath = addFlowerOperation(moPaiflowerCardStack, player, tableRemainderCardList);
		return addFlowerPath;
	}
	
	public static MjPlayerInfo getPlayerInfoByPlayerId(List<MjPlayerInfo> list, Integer playerId){
		for(MjPlayerInfo player : list){
			if (player.getPlayerId().equals(playerId)) {
				return player;
			}
			
		}
		return null;
	}

	/**
	 * 补花操作
	 * @param flowerCardStack
	 * @param player
	 * @param tableRemainderCardList
	 * @return 补花路径
	 */
	public static String addFlowerOperation(Stack<Integer> flowerCardStack, MjPlayerInfo player, List<Integer> tableRemainderCardList){
		StringBuffer addFlowerSb = new StringBuffer("");
		/**如果有花牌，则不需要补花，直接返回*/
		if (flowerCardStack.isEmpty()) {
			return addFlowerSb.toString();
		}
		/**如果有花牌，则一直补到没有花牌为止*/
		while(true){
			if (flowerCardStack.isEmpty() || tableRemainderCardList.size() == 0) {
				break;
			}
			Integer tempFlower = flowerCardStack.pop();
			addFlowerSb.append(tempFlower).append(",");
			/**补花的牌加入玩家补花牌列表里面*/
			player.getFlowerCardList().add(tempFlower);
			player.setCurAddFlowerNum(player.getCurAddFlowerNum() + 1);
			Integer tempCard = MjCardResource.mopai(tableRemainderCardList);
			/**如果摸到的牌是花牌，则入栈*/
			if (tempCard  >= Hulib.indexLine) {
				flowerCardStack.push(tempCard);
			}else{/**如果不是花牌*/
				/**拼接补花链路*/
				addFlowerSb.append(tempCard).append("_");
			}
		}
		return addFlowerSb.substring(0, addFlowerSb.length() - 1);
	}
	
	/**
	 * 校验是否可以吃牌，如果可以吃牌则返回吃牌的那些牌，没听牌的情况下才可以吃牌
	 * @param player
	 * @param cardIndex
	 * @return
	 */
	public static String checkChiPai(MjPlayerInfo player, Integer cardIndex){
		StringBuffer chiSb = new StringBuffer("");
		if (cardIndex >= 27) {
			return chiSb.toString();
		}
		if (player.getIsTingHu() == 0) {
			List<Integer> handCardList = player.getHandCardList();
			/**获取当前牌索引的前两张牌索引和后两张牌索引*/
			int pre2 = 0;
			int pre1 = 0;
			int after1 = 0;
			int after2 = 0;
			int cardType = cardIndex/9;
			int cardValue = cardIndex%9;
			if (cardValue == 0) {
				after1 = cardType*9 + cardValue + 1;
				after2 = cardType*9 + cardValue + 2;
				if (handCardList.contains(after1) && handCardList.contains(after2)) {
					chiSb.append(after1).append(",").append(after2);
				}
			}else if(cardValue == 1){
				pre1 = cardType*9 + cardValue - 1;
				after1 = cardType*9 + cardValue + 1;
				after2 = cardType*9 + cardValue + 2;
				if (handCardList.contains(pre1) && handCardList.contains(after1) && handCardList.contains(after2)) {
					chiSb.append(pre1).append(",").append(after1)
					.append("_")
					.append(after1).append(",").append(after2);
				}else if(handCardList.contains(pre1) && handCardList.contains(after1)){
					chiSb.append(pre1).append(",").append(after1);
				}else if(handCardList.contains(after1) && handCardList.contains(after2)){
					chiSb.append(after1).append(",").append(after2);
				}
				
			}else if(cardValue == 7){
				pre2 = cardType*9 + cardValue - 2;
				pre1 = cardType*9 + cardValue - 1;
				after1 = cardType*9 + cardValue + 1;
				if (handCardList.contains(pre2) && handCardList.contains(pre1) && handCardList.contains(after1)) {
					chiSb.append(pre2).append(",").append(pre1)
					.append("_")
					.append(pre1).append(",").append(after1);
				}else if(handCardList.contains(pre2) && handCardList.contains(pre1)){
					chiSb.append(pre2).append(",").append(pre1);
				}else if(handCardList.contains(pre1) && handCardList.contains(after1)){
					chiSb.append(pre1).append(",").append(after1);
				}
			}else if(cardValue == 8){
				pre2 = cardType*9 + cardValue - 2;
				pre1 = cardType*9 + cardValue - 1;
				if (handCardList.contains(pre2) && handCardList.contains(pre1)) {
					chiSb.append(pre2).append(",").append(pre1);
				}
			}else{
				pre2 = cardType*9 + cardValue - 2;
				pre1 = cardType*9 + cardValue - 1;
				after1 = cardType*9 + cardValue + 1;
				after2 = cardType*9 + cardValue + 2;
				if (handCardList.contains(pre2) && handCardList.contains(pre1) && handCardList.contains(after1) && handCardList.contains(after2)) {
					chiSb.append(pre2).append(",").append(pre1)
					.append("_")
					.append(pre1).append(",").append(after1)
					.append("_")
					.append(after1).append(",").append(after2);
				}else if(handCardList.contains(pre2) && handCardList.contains(pre1) && handCardList.contains(after1)){
					chiSb.append(pre2).append(",").append(pre1)
					.append("_")
					.append(pre1).append(",").append(after1);
				}else if(handCardList.contains(pre1) && handCardList.contains(after1) && handCardList.contains(after2)){
					chiSb.append(pre1).append(",").append(after1)
					.append("_")
					.append(after1).append(",").append(after2);
				}else if(handCardList.contains(pre2) && handCardList.contains(pre1)){
					chiSb.append(pre2).append(",").append(pre1);
				}else if(handCardList.contains(pre1) && handCardList.contains(after1)){
					chiSb.append(pre1).append(",").append(after1);
				}else if(handCardList.contains(after1) && handCardList.contains(after2)){
					chiSb.append(after1).append(",").append(after2);
				}
			}
		}
		return chiSb.toString();
	}
	/**
	 * 校验玩家手牌是否可以碰
	 * @param cards
	 * @param cardIndex
	 * @param isTingHu
	 * @return
	 */
	public static String checkPeng(int[] handCards, Integer cardIndex, int isTingHu){
		StringBuffer pengSb = new StringBuffer("");
		/**没听胡才可以碰牌*/
		if (isTingHu == 0) {
			if (handCards[cardIndex] == 3 || handCards[cardIndex] == 2) {
				pengSb.append(cardIndex);
			}
		}
		return pengSb.toString();
	}
	/**
	 * 检查手牌列表是否可以杠，明杠还是暗杠根据是摸牌还是出牌来定
	 * @param cards 手牌
	 * @param cardIndex 当前出的牌或者摸的牌
	 * @return
	 */
	public static String checkGang(int[] handCards, Integer cardIndex){
		StringBuffer gangSb = new StringBuffer("");
		if (handCards[cardIndex] == 3) {
			gangSb.append(cardIndex);
		}
		return gangSb.toString();
	}
	/**
	 * 摸牌后检查已经碰的牌里面是否有明杠
	 * @param pengCardList 碰的牌列表
	 * @param cardIndex 当前摸的牌
	 * @return
	 */
	public static String checkMingGangByMoPai(List<Integer> pengCardList, Integer cardIndex){
		String pengStr = "";
		int size = pengCardList.size();
		if (size == 0) {
			return pengStr;
		}
		if (pengCardList.contains(cardIndex)) {
			pengStr += cardIndex;
		}
		return pengStr;
	}
	/**
	 * 除去摸的或者出的牌，检查已有的牌中是否有杠的
	 * @param handCards
	 * @param pengCardList
	 * @return
	 */
	public static TreeMap<Integer, String> checkHandCardGang(int[] handCards, List<Integer> pengCardList){
		int handCardLen = handCards.length;
		StringBuffer anGangSb = new StringBuffer("");
		StringBuffer mingGangSb = new StringBuffer("");
		for(int i = 0; i < handCardLen; i++){
			if (handCards[i] == 4) {
				anGangSb.append(i).append("_");
			}
			if (handCards[i] == 1 && pengCardList.contains(i)) {
				mingGangSb.append(i).append("_");
			}
		}
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();
		String mingGangStr = mingGangSb.toString();
		String anGangStr = anGangSb.toString();
		if (StringUtils.isNotBlank(mingGangStr)) {
			map.put(MjOperationEnum.mingGang.type, mingGangStr.substring(0, mingGangStr.length() - 1));
		}
		if (StringUtils.isNotBlank(anGangStr)) {
			map.put(MjOperationEnum.anGang.type, anGangStr.substring(0, anGangStr.length() - 1));
		}
		
		return map;
	}
	
	/**
	 * 判断当前已经出牌的玩家是否可以听胡
	 * @param handCardList
	 * @return
	 */
	public static boolean checkTingHu(MjPlayerInfo player){
		List<Integer> handCardList = player.getHandCardList();
		/**校验手牌，如果手牌中有31-41的花牌，则不能听牌*/
		int size = handCardList.size();
		for(int i = 0; i < size; i++){
			if (handCardList.get(i) >= Hulib.indexLine) {
				return false;
			}
		}
		/**从0-31牌索引中找出第一张手牌中没有的牌*/
		int notContainIndex = 0;
		for(int i = 0; i < Hulib.indexLine; i++){
			if (!handCardList.contains(i)) {
				notContainIndex = i;
				break;
			}
		}
		/**将上步骤中的牌作为癞子，查表判断是否可以胡牌*/
		boolean canHu = Hulib.getInstance().get_hu_info(handCardList, notContainIndex, notContainIndex);
		return canHu;
	}
	
	/**
	 * 判断当前已经出牌的玩家是否可以听胡
	 * @param handCardList
	 * @return
	 */
	public static boolean checkTingHu1(MjRoomInfo roomInfo, MjPlayerInfo player, Integer cardIndex,Map<Integer, List<String>> chuCardIndexHuCardListMap){
		List<Integer> handCardList = player.getHandCardList();
		/**校验手牌，如果手牌中有31-41的花牌，则不能听牌*/
		int size = handCardList.size();
		for(int i = 0; i < size; i++){
			if (handCardList.get(i) >= Hulib.indexLine) {
				return false;
			}
		}
		List<Integer> tempCardList = new ArrayList<Integer>();
		tempCardList.addAll(handCardList);
		if (cardIndex != null) {
			tempCardList.add(cardIndex);
		}
		/**从0-31牌索引中找出第一张手牌中没有的牌*/
		int notContainIndex = 0;
		for(int i = 0; i < Hulib.indexLine; i++){
			if (!tempCardList.contains(i)) {
				notContainIndex = i;
				break;
			}
		}
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		for(Integer index : tempCardList){
			List<Integer> tempList = new ArrayList<Integer>();
			tempList.addAll(tempCardList);
			tempList.remove(index);
			/**将去掉的牌作为癞子，查表判断是否可以胡牌*/
			boolean canHu = Hulib.getInstance().get_hu_info(tempList, notContainIndex, notContainIndex);
			if (canHu) {
				map.put(index, tempList);
			}
		}
		if (map.size() == 0) {
			return false;
		}
		/**计算桌面上可以看见的牌及当前玩家手上的每张牌数量*/
		Map<Integer, Integer> cardNumMap = new HashMap<Integer, Integer>();
		List<Integer> cardNumList = new ArrayList<Integer>();
//		List<MjPlayerInfo> playerList = roomInfo.getPlayerList();
//	    for(MjPlayerInfo tempPlayer : playerList){
//	    	if (player.getPlayerId().equals(tempPlayer.getPlayerId())) {
//	    		cardNumList.addAll(tempCardList);
//			}
//	    	cardNumList.addAll(tempPlayer.getChiCardList());
//	    	cardNumList.addAll(tempPlayer.getPengCardList());
//	    	cardNumList.addAll(tempPlayer.getMingGangCardList());
//	    	cardNumList.addAll(tempPlayer.getAnGangCardList());
//	    	cardNumList.addAll(tempPlayer.getDiscardCardList());
//	    }
//	    for(Integer tempIndex : cardNumList){
//	    	if (cardNumMap.containsKey(tempIndex)) {
//	    		cardNumMap.put(tempIndex, cardNumMap.get(tempIndex) + 1);
//			}else{
//				cardNumMap.put(tempIndex, 1);
//			}
//	    }
//	    
		Set<Entry<Integer, List<Integer>>> set = map.entrySet();
		for(Entry<Integer, List<Integer>> entry : set){
			Integer key = entry.getKey();
			List<Integer> value = entry.getValue();
			Collections.sort(value);
			/**n%3 == 0表示此花色已经可以胡牌;n%3 > 0则表示此门牌需要支持才能胡牌*/
			List<Integer> wanList = new ArrayList<Integer>();
			List<Integer> tongList = new ArrayList<Integer>();
			List<Integer> tiaoList = new ArrayList<Integer>();
			for(Integer index : value){
				/**万个数*/
				if (index >=0 && index < 9) {
					wanList.add(index);
				}
				/**筒个数*/
				if (index >=9 && index < 18) {
					tongList.add(index);
				}
				/**条个数*/
				if (index >=18 && index < 27) {
					tiaoList.add(index);
				}
			}
			List<Integer> huCardList = checkHuCardList(wanList, value);
			if (CollectionUtils.isEmpty(huCardList)) {
				huCardList = checkHuCardList(tongList, value);
				if (CollectionUtils.isEmpty(huCardList)) {
					huCardList = checkHuCardList(tiaoList, value);
				}
			}
			List<String> huCardAndRemaindNumList = new ArrayList<String>();
			for(Integer huCard : huCardList){
				Integer remainNum = cardNumMap.get(huCard);
				if (remainNum == null) {
					remainNum = 4;
				}else{
					remainNum = 4 - remainNum;
				}
				huCardAndRemaindNumList.add(huCard + "_" + remainNum);
			}
			chuCardIndexHuCardListMap.put(key, huCardAndRemaindNumList);
		}
		return true;
	}
	
	public static void main(String[] args) {
		TableMgr.getInstance().load();
		MjPlayerInfo player = new MjPlayerInfo();
		List<Integer> list = Arrays.asList(4,4,6,9,10,11,13,13,14,15);//13
		player.setHandCardList(list);
		Map<Integer, List<String>> chuCardIndexHuCardListMap = new HashMap<Integer, List<String>>();
		checkTingHu1(null, player, 11, chuCardIndexHuCardListMap);
		System.out.println(JsonUtil.toJson(chuCardIndexHuCardListMap));
		
	}
	
	private static List<Integer> checkHuCardList(List<Integer> cardList, List<Integer> handCardList){
		List<Integer> huCardList = new ArrayList<Integer>();
		int size = cardList.size();
		if (size%3 > 0) {
			int minIndex = cardList.get(0) > 0 ? cardList.get(0) - 1: cardList.get(0);
			int maxIndex = cardList.get(size - 1) < 8 ? cardList.get(size - 1) + 1: cardList.get(size - 1);
			for(int i = minIndex; i <= maxIndex; i++){
				boolean isHu = Hulib.getInstance().get_hu_info(handCardList, i, Hulib.invalidCardInex);
				if (isHu) {
					huCardList.add(i);
				}
			}
		}
		return huCardList;
	}
	/**
	 * 判胡
	 * @param player
	 * @param cardIndex
	 * @return
	 */
	public static boolean checkHu(MjPlayerInfo player, Integer cardIndex){
		boolean isHu = false;
		if (player.getIsTingHu() == 0) {
			return isHu;
		}
		
		List<Integer> handCardList = player.getHandCardList();
		if (handCardList.size() == 14) {
			isHu = Hulib.getInstance().get_hu_info(handCardList, Hulib.invalidCardInex, Hulib.invalidCardInex);
		}else{
			isHu = Hulib.getInstance().get_hu_info(handCardList, cardIndex, Hulib.invalidCardInex);
		}
		return isHu;
	}
	
	public static void moveCardsFromHandCards(){
		
	}
}
