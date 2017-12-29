package cn.worldwalker.game.wyqp.common.domain.mj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;

public class MjRoomInfo extends BaseRoomInfo{
	
	private List<MjPlayerInfo> playerList = new ArrayList<MjPlayerInfo>();
	/**剩余的牌列表*/
	private List<Integer> tableRemainderCardList;
	/**上一个操作者出的牌*/
	private Integer lastCardIndex;
	/**每个玩家可操作map集合,玩家做了一个操作后，会从这里删除此玩家的可操作权限
	 * 玩家id-1吃、2碰、3明杠、4暗杠、5听胡、6胡-吃的牌索引字符串，碰的牌索引字符串，明杠的牌索引字符串，暗杠的牌索引字符串，听胡、胡默认值0*/
	private LinkedHashMap<Integer, TreeMap<Integer, String>> playerOperationMap;
	/**是否开宝 0:不开宝 1：开宝*/
	private Integer isKaiBao = 0;
	/**是否荒翻 0：不荒翻 1：荒翻*/
	private Integer isHuangFan = 0;
	/**是否飞苍蝇 0：不飞苍蝇 1：飞苍蝇*/
	private Integer isFeiCangyin = 0;
	/**胡牌底分*/
	private Integer huButtomScore = 0;
	/**每个花的分数*/
	private Integer eachFlowerScore = 0;
	/**封顶*/
	private Integer huScoreLimit = 0;
	/**胡牌玩家map*/
	private Map<Integer, String> huPlayerMap = new HashMap<Integer, String>();
	
	public Map<Integer, String> getHuPlayerMap() {
		return huPlayerMap;
	}
	public void setHuPlayerMap(Map<Integer, String> huPlayerMap) {
		this.huPlayerMap = huPlayerMap;
	}
	public Integer getEachFlowerScore() {
		return eachFlowerScore;
	}
	public void setEachFlowerScore(Integer eachFlowerScore) {
		this.eachFlowerScore = eachFlowerScore;
	}
	public List<MjPlayerInfo> getPlayerList() {
		return playerList;
	}
	public void setPlayerList(List<MjPlayerInfo> playerList) {
		this.playerList = playerList;
	}
	public Integer getIsKaiBao() {
		return isKaiBao;
	}
	public void setIsKaiBao(Integer isKaiBao) {
		this.isKaiBao = isKaiBao;
	}
	public Integer getIsHuangFan() {
		return isHuangFan;
	}
	public void setIsHuangFan(Integer isHuangFan) {
		this.isHuangFan = isHuangFan;
	}
	public Integer getIsFeiCangyin() {
		return isFeiCangyin;
	}
	public void setIsFeiCangyin(Integer isFeiCangyin) {
		this.isFeiCangyin = isFeiCangyin;
	}
	public Integer getLastCardIndex() {
		return lastCardIndex;
	}
	public void setLastCardIndex(Integer lastCardIndex) {
		this.lastCardIndex = lastCardIndex;
	}
	public List<Integer> getTableRemainderCardList() {
		return tableRemainderCardList;
	}
	public void setTableRemainderCardList(List<Integer> tableRemainderCardList) {
		this.tableRemainderCardList = tableRemainderCardList;
	}
	public Integer getHuButtomScore() {
		return huButtomScore;
	}
	public void setHuButtomScore(Integer huButtomScore) {
		this.huButtomScore = huButtomScore;
	}
	public Integer getHuScoreLimit() {
		return huScoreLimit;
	}
	public void setHuScoreLimit(Integer huScoreLimit) {
		this.huScoreLimit = huScoreLimit;
	}
	public LinkedHashMap<Integer, TreeMap<Integer, String>> getPlayerOperationMap() {
		return playerOperationMap;
	}
	public void setPlayerOperationMap(
			LinkedHashMap<Integer, TreeMap<Integer, String>> playerOperationMap) {
		this.playerOperationMap = playerOperationMap;
	}
	
}
