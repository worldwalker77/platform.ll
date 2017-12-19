package cn.worldwalker.game.wyqp.mj.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.worldwalker.game.wyqp.common.utils.JsonUtil;

public class CardResource {
	
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
	
	public static List<Integer> genOutOrderCardList(){
		List<Integer> cardList = new ArrayList<Integer>();
		cardList.addAll(orderCardList);
		Collections.shuffle(cardList);
		return cardList;
	}
	
	public static int[] genHandCards(List<Integer> remainderCardList, int cardNum){
		int[] handCards = new int[]{
				0, 0, 0, 0, 0, 0, 0, 0, 0, /* 0-8表示1-9万 */ 
				0, 0, 0, 0, 0, 0, 0, 0, 0, /* 9-17表示1-9筒 */
				0, 0, 0, 0, 0, 0, 0, 0, 0, /* 18-26表示1-9条 */
				0, 0, 0, 0, 0, 0, 0 ,//27-33表示东南西北中发白
				0, 0, 0, 0, 0, 0, 0 ,0//34-41表示春夏秋冬梅兰竹菊
		};
		/**循环摸cardNum张牌*/
		for(int i = 0; i < cardNum; i++){
			int tempCardIndex = remainderCardList.remove(0);
			handCards[tempCardIndex]++;
		}
		return handCards;
	}
	
	
	public static void main(String[] args) {
		List<Integer> cardList = genOutOrderCardList();
		System.out.println(JsonUtil.toJson(genHandCards(cardList, 13)));
	}
}
