package cn.worldwalker.game.wyqp.common.domain.mj;

import cn.worldwalker.game.wyqp.common.domain.base.BaseMsg;

public class MjMsg extends BaseMsg{
	/**麻将类型 1上海敲麻 2上海百搭 3上海拉西胡 4 上海清混碰*/
	private Integer mjType;
	/**牌索引*/
	private Integer cardIndex;
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
	/**胡牌封顶分数*/
	private Integer huScoreLimit = 0;
	
	private String chiCards;
	private String pengCards;
	private String gangCards;
	
	public String getChiCards() {
		return chiCards;
	}
	public void setChiCards(String chiCards) {
		this.chiCards = chiCards;
	}
	public String getPengCards() {
		return pengCards;
	}
	public void setPengCards(String pengCards) {
		this.pengCards = pengCards;
	}
	public String getGangCards() {
		return gangCards;
	}
	public void setGangCards(String gangCards) {
		this.gangCards = gangCards;
	}
	public Integer getMjType() {
		return mjType;
	}
	public void setMjType(Integer mjType) {
		this.mjType = mjType;
	}
	public Integer getCardIndex() {
		return cardIndex;
	}
	public void setCardIndex(Integer cardIndex) {
		this.cardIndex = cardIndex;
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
	public Integer getHuButtomScore() {
		return huButtomScore;
	}
	public void setHuButtomScore(Integer huButtomScore) {
		this.huButtomScore = huButtomScore;
	}
	public Integer getEachFlowerScore() {
		return eachFlowerScore;
	}
	public void setEachFlowerScore(Integer eachFlowerScore) {
		this.eachFlowerScore = eachFlowerScore;
	}
	public Integer getHuScoreLimit() {
		return huScoreLimit;
	}
	public void setHuScoreLimit(Integer huScoreLimit) {
		this.huScoreLimit = huScoreLimit;
	}
	
}
