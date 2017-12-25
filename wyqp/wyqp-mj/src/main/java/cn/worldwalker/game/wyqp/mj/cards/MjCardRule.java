package cn.worldwalker.game.wyqp.mj.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.utils.GameUtil;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;
import cn.worldwalker.game.wyqp.mj.enums.MjOperationEnum;
import cn.worldwalker.game.wyqp.mj.huvalidate.Hulib;


public class MjCardRule {
	
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
		if (!operationStr.equals(existOperationStr)) {
			return false;
		}
		return true;
	}
	/**
	 * 摇色子
	 * @return
	 */
	public static List<Integer> playDices(){
		List<Integer> list = new ArrayList<Integer>();
		list.add(GameUtil.genDice());
		list.add(GameUtil.genDice());
		return list;
	}
	
	public static Integer getRealMoPai(String moPaiAddFlower){
		String[] ar = moPaiAddFlower.split(",");
		int len = ar.length;
		return Integer.valueOf(ar[len - 1]);
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
		if (type == 0) {/**初始化手牌列表时的校验*/
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
			if (checkHu(curPlayer, cardIndex)) {
				map.put(MjOperationEnum.hu.type, "1");
			}
			operations.put(curPlayer.getPlayerId(), map);
		}else if (type == 1) {/**如果是摸牌，则要判断摸牌的人是否可以明杠、暗杠、胡牌**/
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
				map.put(MjOperationEnum.hu.type, "1");
			}
			operations.put(curPlayer.getPlayerId(), map);
		}else{/**如果是出牌，则需要判断出牌人是否可以听胡，并依次判断其他的玩家是否可以吃、碰、明杠、胡**/
			/**听牌校验(只针对当前出牌的玩家，因为需要通知玩家听牌)*/
			if (curPlayer.getIsTingHu() == 0) {
				if (checkTingHu(curPlayer)) {
					TreeMap<Integer, String> map0 = new TreeMap<Integer, String>();
					map0.put(MjOperationEnum.tingHu.type, "1");
					operations.put(curPlayer.getPlayerId(), map0);
				}
			}
			
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
					String chiStr = checkChiPai(nextPlayer, cardIndex);
					if (StringUtils.isNotBlank(chiStr)) {
						map1.put(MjOperationEnum.chi.type, chiStr);
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
				
				/**以胡牌校验*/
				if (checkHu(nextPlayer, cardIndex)) {
					map1.put(MjOperationEnum.hu.type, "0");
				}
				operations.put(nextPlayer.getPlayerId(), map1);
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
	
	public static void main(String[] args) {
		List<Integer> handCardList = new ArrayList<Integer>();
		handCardList.add(34);
		handCardList.add(34);
		handCardList.add(34);
		handCardList.add(34);
		String ss = "34,35,1_34,2_34,36,3_34,37,39,4";
		System.out.println(ss);
		String str = replaceFlowerCards(handCardList, ss);
		
		System.out.println(str);
		System.out.println(JsonUtil.toJson(handCardList));
	}
	/**
	 * 校验摸牌补花的情况
	 * @param player
	 * @param tableRemainderCardList
	 * @return
	 */
	public static String checkMoPaiAddFlower(List<Integer> tableRemainderCardList, MjPlayerInfo player){
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
				pengSb.append(cardIndex).append(",").append(cardIndex).append(",").append(cardIndex);
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
			gangSb.append(cardIndex).append(",").append(cardIndex).append(",").append(cardIndex);
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
			pengStr += cardIndex + "," + cardIndex + "," + cardIndex;
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
				anGangSb.append(i).append(",").append(i).append(",").append(i).append("_");
			}
			if (handCards[i] == 1 && pengCardList.contains(i)) {
				mingGangSb.append(i).append(",").append(i).append(",").append(i).append("_");
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
		if (canHu) {
			return true;
		}
		
		if (size < 13) {
			return false;
		}
		/**如果查表判断不能胡牌，则判断是否是可以胡七对*/
		/**将手牌进行格式化*/
		int[] cards = new int[Hulib.indexLine];
		for(int i = 0; i < size; i++){
			cards[handCardList.get(i)]++;
		}
		int sum = 0;
		for (int i = 0 ; i < Hulib.indexLine ; ++i)
		{
			sum += cards[i] % 2;
		}
		if (sum != 1) {
			return false;
		}
		/**设置当前玩家为已经听胡*/
		player.setIsTingHu(1);
		return true;
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
		/**如果是开始庄家判胡*/
		if (cardIndex == null) {
			isHu = Hulib.getInstance().get_hu_info(handCardList, Hulib.invalidCardInex, Hulib.invalidCardInex);
		}else{
			isHu = Hulib.getInstance().get_hu_info(handCardList, cardIndex, Hulib.invalidCardInex);
		}
		return isHu;
	}
	
	public static void moveCardsFromHandCards(){
		
	}
}
