package cn.worldwalker.game.wyqp.mj.cards;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.mj.enums.MjOperationEnum;
import cn.worldwalker.game.wyqp.mj.huvalidate.Hulib;


public class CardRule {
	
	/**
	 * 
	 * @param list
	 * @param cardIndex
	 * @param playerId
	 * @param isMoPai
	 * @return
	 */
	public static LinkedHashMap<Integer, Map<Integer, String>> calPlayerOperations(List<MjPlayerInfo> list, Integer cardIndex, Integer playerId, boolean isMoPai){
		LinkedHashMap<Integer, Map<Integer, String>> operations = new LinkedHashMap<Integer, Map<Integer,String>>();
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
		if (isMoPai) {
			/**minggang**/
			List<Integer> pengCardList = curPlayer.getPengCardList();
			/**angang**/
			
			int haddCardSize = handCardList.size();
			/**hu*/
		}else{
			/**如果当前出牌的玩家还没有听胡，但是此时可以听胡，则需要通知*/
			if (curPlayer.getIsTingHu() == 0) {
				if (checkTingHu(curPlayer)) {
					Map<Integer, String> map0 = new HashMap<Integer, String>();
					map0.put(MjOperationEnum.tingHu.type, "1");
					operations.put(curPlayer.getPlayerId(), map0);
				}
			}
			
			/**按顺序依次计算出剩余三个玩家可操作权限*/
			for(int i = 1; i <= 3; i++){
				MjPlayerInfo nextPlayer = list.get((curPlayerIndex + i)%4);
				handCardList = nextPlayer.getHandCardList();
				Map<Integer, String> map1 = new HashMap<Integer, String>();
				/**判断玩家是否可以吃牌（只对出牌玩家的下家计算,只有没听胡的玩家才可以吃牌）*/
				if (i == 1) {
					String chiStr = checkChiPai(nextPlayer, cardIndex);
					if (StringUtils.isNotBlank(chiStr)) {
						map1.put(MjOperationEnum.chi.type, chiStr);
					}
				}
				/**判断玩家是否可以碰牌*/
				
				
				/**判断玩家是否可以杠牌*/
				
				
				/**判断玩家是否可以胡牌*/
				if (checkTingHu(nextPlayer)) {
					if (Hulib.getInstance().get_hu_info(handCardList, cardIndex, Hulib.invalidCardInex)) {
						map1.put(MjOperationEnum.hu.type, "1");
					}
				}
			}
		}
		return null;
	}
	/**
	 * 校验是否可以吃牌，如果可以吃牌则返回吃牌的那些牌，没听牌的情况下才可以吃牌
	 * @param player
	 * @param cardIndex
	 * @return
	 */
	public static String checkChiPai(MjPlayerInfo player, Integer cardIndex){
		String chiStr = null;
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
					chiStr =  after1 + "," + after2;
				}
			}else if(cardValue == 1){
				pre1 = cardType*9 + cardValue - 1;
				after1 = cardType*9 + cardValue + 1;
				after2 = cardType*9 + cardValue + 2;
				if (handCardList.contains(pre1) && handCardList.contains(after1) && handCardList.contains(after2)) {
					chiStr =  pre1 + "," + after1 + "_" + after1 + "," + after2;
				}else if(handCardList.contains(pre1) && handCardList.contains(after1)){
					chiStr =  pre1 + "," + after1;
				}else if(handCardList.contains(after1) && handCardList.contains(after2)){
					chiStr =  after1 + "," + after2;
				}
				
			}else if(cardValue == 7){
				pre2 = cardType*9 + cardValue - 2;
				pre1 = cardType*9 + cardValue - 1;
				after1 = cardType*9 + cardValue + 1;
				if (handCardList.contains(pre2) && handCardList.contains(pre1) && handCardList.contains(after1)) {
					chiStr =  pre2 + "," + pre1 + "_" + pre1 + "," + after1;
				}else if(handCardList.contains(pre2) && handCardList.contains(pre1)){
					chiStr =  pre2 + "," + pre1;
				}else if(handCardList.contains(pre1) && handCardList.contains(after1)){
					chiStr =  pre1 + "," + after1;
				}
			}else if(cardValue == 8){
				pre2 = cardType*9 + cardValue - 2;
				pre1 = cardType*9 + cardValue - 1;
				if (handCardList.contains(pre2) && handCardList.contains(pre1)) {
					chiStr =  pre2 + "," + pre1;
				}
			}else{
				pre2 = cardType*9 + cardValue - 2;
				pre1 = cardType*9 + cardValue - 1;
				after1 = cardType*9 + cardValue + 1;
				after2 = cardType*9 + cardValue + 2;
				if (handCardList.contains(pre2) && handCardList.contains(pre1) && handCardList.contains(after1) && handCardList.contains(after2)) {
					chiStr =  pre2 + "," + pre1 + "_" + pre1 + "," + after1 + "_" + after1 + "," + after2;
				}else if(handCardList.contains(pre2) && handCardList.contains(pre1) && handCardList.contains(after1)){
					chiStr =  pre2 + "," + pre1 + "_" + pre1 + "," + after1;
				}else if(handCardList.contains(pre1) && handCardList.contains(after1) && handCardList.contains(after2)){
					chiStr =  pre1 + "," + after1 + "_" + after1 + "," + after2;
				}else if(handCardList.contains(pre2) && handCardList.contains(pre1)){
					chiStr =  pre2 + "," + pre1;
				}else if(handCardList.contains(pre1) && handCardList.contains(after1)){
					chiStr =  pre1 + "," + after1;
				}else if(handCardList.contains(after1) && handCardList.contains(after2)){
					chiStr =  after1 + "," + after2;
				}
			}
		}
		return chiStr;
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
}
