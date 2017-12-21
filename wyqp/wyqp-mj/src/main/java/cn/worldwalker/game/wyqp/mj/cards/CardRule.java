package cn.worldwalker.game.wyqp.mj.cards;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;


public class CardRule {
	
	public static LinkedHashMap<Integer, Map<Integer, String>> calPlayerOperations(List<MjPlayerInfo> list, Integer cardIndex, Integer playerId, boolean isMoPai){
		LinkedHashMap<Integer, Map<Integer, String>> operations = new LinkedHashMap<Integer, Map<Integer,String>>();
		if (isMoPai) {
			MjPlayerInfo playerInfo = null;
			for(MjPlayerInfo temp : list){
				if (playerId.equals(temp.getPlayerId())) {
					playerInfo = temp;
					break;
				}
			}
			/**minggang**/
			List<Integer> pengCardList = playerInfo.getPengCardList();
			/**angang**/
			List<Integer> handCardList = playerInfo.getHandCardList();
			int haddCardSize = handCardList.size();
			/**hu*/
			
		}
		return null;
	}
}
