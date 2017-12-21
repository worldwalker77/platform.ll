package cn.worldwalker.game.wyqp.common.domain.mj;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;

public class MjRoomInfo extends BaseRoomInfo{
	/**剩余的牌列表*/
	private List<Integer> tableRemainderCardList;
	/**上一个操作者出的牌*/
	private Integer lastCardIndex;
	/**每个玩家可操作map集合,玩家做了一个操作后，会从这里删除此玩家的可操作权限
	 * 玩家id-1吃、2碰、3明杠、4暗杠、5听胡、6胡-吃的牌索引字符串，碰的牌索引字符串，明杠的牌索引字符串，暗杠的牌索引字符串，听胡、胡默认值0*/
	private LinkedHashMap<Integer, Map<Integer, String>> playerOperationMap = new LinkedHashMap<Integer, Map<Integer,String>>();
	
	public Integer getLastCardIndex() {
		return lastCardIndex;
	}
	public void setLastCardIndex(Integer lastCardIndex) {
		this.lastCardIndex = lastCardIndex;
	}
	public LinkedHashMap<Integer, Map<Integer, String>> getPlayerOperationMap() {
		return playerOperationMap;
	}
	public void setPlayerOperationMap(
			LinkedHashMap<Integer, Map<Integer, String>> playerOperationMap) {
		this.playerOperationMap = playerOperationMap;
	}
	public List<Integer> getTableRemainderCardList() {
		return tableRemainderCardList;
	}
	public void setTableRemainderCardList(List<Integer> tableRemainderCardList) {
		this.tableRemainderCardList = tableRemainderCardList;
	}
	
}
