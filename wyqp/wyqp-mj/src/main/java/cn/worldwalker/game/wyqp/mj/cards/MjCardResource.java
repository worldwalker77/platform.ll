package cn.worldwalker.game.wyqp.mj.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.worldwalker.game.wyqp.common.utils.JsonUtil;

public class MjCardResource {
	
	private static List<Integer> orderCardList = new ArrayList<Integer>();
	static{
		for(int i = 0; i < 42; i++){
			if (i > 33) {
				orderCardList.add(i);
			}else{
				for(int j = 0; j < 4; j++){
					orderCardList.add(i);
				}
			}
		}
	}
	/**
	 * 生成桌牌列表，乱序
	 * @return
	 */
	public static List<Integer> genTableOutOrderCardList(){
		List<Integer> cardList = new ArrayList<Integer>();
		cardList.addAll(orderCardList);
		Collections.shuffle(cardList);
		return cardList;
	}
	/**
	 * 生成玩家手牌列表
	 * @param tableRemainderCardList
	 * @param cardNum 需要生成牌的数量
	 * @return
	 */
	public static List<Integer> genHandCardList(List<Integer> tableRemainderCardList, int cardNum){
		List<Integer> handCardList = new ArrayList<Integer>();
		/**循环摸cardNum张牌*/
		for(int i = 0; i < cardNum; i++){
			int tempCardIndex = tableRemainderCardList.remove(0);
			handCardList.add(tempCardIndex);
		}
		/**排序*/
		Collections.sort(handCardList);
		return handCardList;
	}
	public static Integer mopai(List<Integer> tableRemainderCardList){
		return tableRemainderCardList.remove(0);
	}
	
	public static void main(String[] args) {
		List<Integer> cardList = genTableOutOrderCardList();
		System.out.println(JsonUtil.toJson(genHandCardList(cardList, 13)));
	}
}
