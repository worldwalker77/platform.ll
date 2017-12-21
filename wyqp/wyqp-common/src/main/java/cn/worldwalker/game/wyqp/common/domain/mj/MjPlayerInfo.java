package cn.worldwalker.game.wyqp.common.domain.mj;

import java.util.List;

import cn.worldwalker.game.wyqp.common.domain.base.BasePlayerInfo;

public class MjPlayerInfo extends BasePlayerInfo{
	/**玩家手上的牌*/
	private List<Integer> handCardList;
	/**已经吃的牌列表*/
	private List<Integer> chiCardList;
	/**已经碰的牌列表*/
	private List<Integer> pengCardList;
	/**已经明杠的牌列表*/
	private List<Integer> mingGangCardList;
	/**已经暗杠的牌列表*/
	private List<Integer> anGangCardList;
	/**已经补花牌列表*/
	private List<Integer> flowerCardList;
	/**已经打出的牌列表*/
	private List<Integer> discardCardList;
	/**当前摸的牌的牌索引*/
	private Integer curMoPaiCardIndex;
	/**是否听胡*/
	private Integer isTingHu = 0;
	
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
