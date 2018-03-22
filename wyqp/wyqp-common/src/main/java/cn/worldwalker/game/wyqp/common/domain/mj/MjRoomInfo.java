package cn.worldwalker.game.wyqp.common.domain.mj;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
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
	private Integer isKaiBao = 1;
	/**当前局是否开宝*/
	private Integer isCurGameKaiBao = 0;
	/**是否荒翻 0：不荒翻 1：荒翻*/
	private Integer isHuangFan = 1;
	/**荒翻次数*/
	private Integer huangFanNum = 0;
	/**当前局是否荒*/
	private Integer isCurGameHuangZhuang = 0;
	/**是否飞苍蝇 0：不飞苍蝇 1：飞苍蝇*/
	private Integer isFeiCangyin = 0;
	
	/**是否可以吃牌 0：不可以 1：可以*/
	private Integer isChiPai = 1;
	/**是否可以听牌 0：不可以 1：可以*/
	private Integer isTingPai = 1;
	/**胡牌底分*/
	private Integer huButtomScore = 0;
	/**每个花的分数*/
	private Integer eachFlowerScore = 0;
	/**封顶*/
	private Integer huScoreLimit = 1000000;
	/**色字*/
	private List<Integer> dices ;
	/**百搭牌索引*/
	private Integer baiDaCardIndex;
	/**痞子牌索引*/
	private Integer piZiCardIndex;
	
	/**无百搭可抓冲*/
	private Integer noBaiDaCanZhuaChong;
	/**无百搭可抢杠*/
	private Integer noBaiDaCanQiangGang;
	/**模式
	 * 如果是百搭则表示花牌数量，8花：只有春夏秋冬梅兰竹菊是花牌，20花表示中发白春夏秋冬梅兰竹菊都是花牌
	 * 如果是清混碰，3表示包三家，5表示包五家
	 * */
	private Integer model;
	/**花牌和非花牌的分界线*/
	private Integer indexLine;
	/**上海清混碰里面有勒子，创建房间的时候可以选择勒子，表示10花还是20花，
	 * 如果底分+花分超过1个勒子就按照1个勒子算，如果没超过就按照底分+花分算，
	 * 如果是特殊牌型，就直接按照牌型对于的勒子数算
	 * 组合牌型按照牌型对于的最大勒子数算
	 * */
	private Integer flowerPerLezi;
	
	public Integer getFlowerPerLezi() {
		return flowerPerLezi;
	}
	public void setFlowerPerLezi(Integer flowerPerLezi) {
		this.flowerPerLezi = flowerPerLezi;
	}
	public Integer getIsTingPai() {
		return isTingPai;
	}
	public void setIsTingPai(Integer isTingPai) {
		this.isTingPai = isTingPai;
	}
	public Integer getIndexLine() {
		return indexLine;
	}
	public void setIndexLine(Integer indexLine) {
		this.indexLine = indexLine;
	}
	public Integer getModel() {
		return model;
	}
	public void setModel(Integer model) {
		this.model = model;
	}
	public Integer getPiZiCardIndex() {
		return piZiCardIndex;
	}
	public void setPiZiCardIndex(Integer piZiCardIndex) {
		this.piZiCardIndex = piZiCardIndex;
	}
	public Integer getNoBaiDaCanZhuaChong() {
		return noBaiDaCanZhuaChong;
	}
	public void setNoBaiDaCanZhuaChong(Integer noBaiDaCanZhuaChong) {
		this.noBaiDaCanZhuaChong = noBaiDaCanZhuaChong;
	}
	public Integer getNoBaiDaCanQiangGang() {
		return noBaiDaCanQiangGang;
	}
	public void setNoBaiDaCanQiangGang(Integer noBaiDaCanQiangGang) {
		this.noBaiDaCanQiangGang = noBaiDaCanQiangGang;
	}
	public Integer getBaiDaCardIndex() {
		return baiDaCardIndex;
	}
	public void setBaiDaCardIndex(Integer baiDaCardIndex) {
		this.baiDaCardIndex = baiDaCardIndex;
	}
	public List<Integer> getDices() {
		return dices;
	}
	public void setDices(List<Integer> dices) {
		this.dices = dices;
	}
	public Integer getIsChiPai() {
		return isChiPai;
	}
	public void setIsChiPai(Integer isChiPai) {
		this.isChiPai = isChiPai;
	}
	public Integer getHuangFanNum() {
		return huangFanNum;
	}
	public void setHuangFanNum(Integer huangFanNum) {
		this.huangFanNum = huangFanNum;
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
	public Integer getIsCurGameKaiBao() {
		return isCurGameKaiBao;
	}
	public void setIsCurGameKaiBao(Integer isCurGameKaiBao) {
		this.isCurGameKaiBao = isCurGameKaiBao;
	}
	public Integer getIsCurGameHuangZhuang() {
		return isCurGameHuangZhuang;
	}
	public void setIsCurGameHuangZhuang(Integer isCurGameHuangZhuang) {
		this.isCurGameHuangZhuang = isCurGameHuangZhuang;
	}
	
}
