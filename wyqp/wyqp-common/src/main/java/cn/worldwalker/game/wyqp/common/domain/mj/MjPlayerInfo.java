package cn.worldwalker.game.wyqp.common.domain.mj;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import cn.worldwalker.game.wyqp.common.domain.base.BasePlayerInfo;

public class MjPlayerInfo extends BasePlayerInfo{
	/**玩家手上的牌*/
	private List<Integer> handCardList;
	/**已经吃的牌列表*/
	private List<Integer> chiCardList = new ArrayList<Integer>();
	/**已经碰的牌列表*/
	private List<Integer> pengCardList = new ArrayList<Integer>();
	/**已经明杠的牌列表*/
	private List<Integer> mingGangCardList = new ArrayList<Integer>();
	/**已经暗杠的牌列表*/
	private List<Integer> anGangCardList = new ArrayList<Integer>();
	/**已经补花牌列表*/
	private List<Integer> flowerCardList = new ArrayList<Integer>();
	/**已经打出的牌列表*/
	private List<Integer> discardCardList = new ArrayList<Integer>();
	/**当前摸的牌的牌索引*/
	private Integer curMoPaiCardIndex;
	/**是否听胡*/
	private Integer isTingHu = 0;
	/**当前操作补花数*/
	private Integer curAddFlowerNum = 0;
	/**所有补花数*/
	private Integer totalAddFlowerNum = 0;
	/**是否胡牌*/
	private Integer isHu = 0;
	/**倍数*/
	private Integer multiple = 0;
	/**胡类型 0：别人点炮 1：自摸 2：天胡 3：抢杠胡*/
	private Integer huType = 0;
	/**牌型列表，可能是组合牌型，比如清一色碰碰胡*/
	private List<Integer> mjCardTypeList = new ArrayList<Integer>();
	/**底分和花分*/
	private Integer buttomAndFlowerScore = 0;
	/**断线重连刷新的时候会通过这个接口返回当前玩家的操作权限*/
	private TreeMap<Integer, String> operations;
	/**自摸次数*/
	private Integer ziMoCount = 0;
	/**抓冲次数*/
	private Integer zhuaChongCount = 0;
	/**点炮次数*/
	private Integer dianPaoCount = 0;
	/**飞苍蝇牌索引*/
	private Integer feiCangYingCardIndex;
	
	public Integer getFeiCangYingCardIndex() {
		return feiCangYingCardIndex;
	}
	public void setFeiCangYingCardIndex(Integer feiCangYingCardIndex) {
		this.feiCangYingCardIndex = feiCangYingCardIndex;
	}
	public Integer getZiMoCount() {
		return ziMoCount;
	}
	public void setZiMoCount(Integer ziMoCount) {
		this.ziMoCount = ziMoCount;
	}
	public Integer getZhuaChongCount() {
		return zhuaChongCount;
	}
	public void setZhuaChongCount(Integer zhuaChongCount) {
		this.zhuaChongCount = zhuaChongCount;
	}
	public Integer getDianPaoCount() {
		return dianPaoCount;
	}
	public void setDianPaoCount(Integer dianPaoCount) {
		this.dianPaoCount = dianPaoCount;
	}
	public Integer getTotalAddFlowerNum() {
		return totalAddFlowerNum;
	}
	public void setTotalAddFlowerNum(Integer totalAddFlowerNum) {
		this.totalAddFlowerNum = totalAddFlowerNum;
	}
	public TreeMap<Integer, String> getOperations() {
		return operations;
	}
	public void setOperations(TreeMap<Integer, String> operations) {
		this.operations = operations;
	}
	public Integer getButtomAndFlowerScore() {
		return buttomAndFlowerScore;
	}
	public void setButtomAndFlowerScore(Integer buttomAndFlowerScore) {
		this.buttomAndFlowerScore = buttomAndFlowerScore;
	}
	public List<Integer> getMjCardTypeList() {
		return mjCardTypeList;
	}
	public void setMjCardTypeList(List<Integer> mjCardTypeList) {
		this.mjCardTypeList = mjCardTypeList;
	}
	public Integer getHuType() {
		return huType;
	}
	public void setHuType(Integer huType) {
		this.huType = huType;
	}
	public Integer getIsHu() {
		return isHu;
	}
	public void setIsHu(Integer isHu) {
		this.isHu = isHu;
	}
	public Integer getMultiple() {
		return multiple;
	}
	public void setMultiple(Integer multiple) {
		this.multiple = multiple;
	}
	public Integer getCurAddFlowerNum() {
		return curAddFlowerNum;
	}
	public void setCurAddFlowerNum(Integer curAddFlowerNum) {
		this.curAddFlowerNum = curAddFlowerNum;
	}
	public List<Integer> getHandCardList() {
		return handCardList;
	}
	public void setHandCardList(List<Integer> handCardList) {
		this.handCardList = handCardList;
	}
	public List<Integer> getChiCardList() {
		return chiCardList;
	}
	public void setChiCardList(List<Integer> chiCardList) {
		this.chiCardList = chiCardList;
	}
	public List<Integer> getPengCardList() {
		return pengCardList;
	}
	public void setPengCardList(List<Integer> pengCardList) {
		this.pengCardList = pengCardList;
	}
	public List<Integer> getDiscardCardList() {
		return discardCardList;
	}
	public void setDiscardCardList(List<Integer> discardCardList) {
		this.discardCardList = discardCardList;
	}
	public List<Integer> getMingGangCardList() {
		return mingGangCardList;
	}
	public void setMingGangCardList(List<Integer> mingGangCardList) {
		this.mingGangCardList = mingGangCardList;
	}
	public List<Integer> getAnGangCardList() {
		return anGangCardList;
	}
	public void setAnGangCardList(List<Integer> anGangCardList) {
		this.anGangCardList = anGangCardList;
	}
	public List<Integer> getFlowerCardList() {
		return flowerCardList;
	}
	public void setFlowerCardList(List<Integer> flowerCardList) {
		this.flowerCardList = flowerCardList;
	}
	public Integer getCurMoPaiCardIndex() {
		return curMoPaiCardIndex;
	}
	public void setCurMoPaiCardIndex(Integer curMoPaiCardIndex) {
		this.curMoPaiCardIndex = curMoPaiCardIndex;
	}
	public Integer getIsTingHu() {
		return isTingHu;
	}
	public void setIsTingHu(Integer isTingHu) {
		this.isTingHu = isTingHu;
	}
	
}
