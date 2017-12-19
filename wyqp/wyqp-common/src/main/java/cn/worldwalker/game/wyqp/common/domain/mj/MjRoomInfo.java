package cn.worldwalker.game.wyqp.common.domain.mj;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;

public class MjRoomInfo extends BaseRoomInfo{
	/**剩余的牌列表*/
	private List<Integer> remainderCardList;
	/**当前出的牌*/
	private Integer curCardIndex;
	/**玩家id-吃、碰、明杠、暗杠、听胡、胡-吃的牌索引字符串，碰的牌索引字符串，明杠的牌索引字符串，暗杠的牌索引字符串，听胡、胡默认值0(真正要胡的时候才去判断胡的牌型)*/
	private LinkedHashMap<Integer, Map<Integer, String>> curPlayerCanDoMap;
	
	public List<Integer> getRemainderCardList() {
		return remainderCardList;
	}
	public void setRemainderCardList(List<Integer> remainderCardList) {
		this.remainderCardList = remainderCardList;
	}
	public Integer getCurCardIndex() {
		return curCardIndex;
	}
	public void setCurCardIndex(Integer curCardIndex) {
		this.curCardIndex = curCardIndex;
	}
	public LinkedHashMap<Integer, Map<Integer, String>> getCurPlayerCanDoMap() {
		return curPlayerCanDoMap;
	}
	public void setCurPlayerCanDoMap(
			LinkedHashMap<Integer, Map<Integer, String>> curPlayerCanDoMap) {
		this.curPlayerCanDoMap = curPlayerCanDoMap;
	}
	
}
