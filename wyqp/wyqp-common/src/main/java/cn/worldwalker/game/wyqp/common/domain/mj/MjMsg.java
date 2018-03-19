package cn.worldwalker.game.wyqp.common.domain.mj;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import cn.worldwalker.game.wyqp.common.domain.base.BaseMsg;
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class MjMsg extends BaseMsg{
	/**麻将类型 1上海敲麻 2上海百搭 3上海拉西胡 4 上海清混碰*/
	private Integer mjType;
	/**牌索引*/
	private Integer cardIndex;
	/**是否开宝 0:不开宝 1：开宝*/
	private Integer isKaiBao;
	/**是否荒翻 0：不荒翻 1：荒翻*/
	private Integer isHuangFan;
	/**是否飞苍蝇 0：不飞苍蝇 1：飞苍蝇*/
	private Integer isFeiCangyin;
	/**是否可以吃牌 0：不可以 1：可以*/
	private Integer isChiPai;
	/**胡牌底分*/
	private Integer huButtomScore;
	/**每个花的分数*/
	private Integer eachFlowerScore;
	/**胡牌封顶分数*/
	private Integer huScoreLimit;
	/**无百搭可抓冲*/
	private Integer noBaiDaCanZhuaChong;
	/**无百搭可抢杠*/
	private Integer noBaiDaCanQiangGang;
	/**模式*/
	private Integer model;
	/**上海清混碰里面有勒子，创建房间的时候可以选择勒子，表示10花还是20花，
	 * 如果底分+花分超过1个勒子就按照1个勒子算，如果没超过就按照底分+花分算，
	 * 如果是特殊牌型，就直接按照牌型对于的勒子数算
	 * 组合牌型按照牌型对于的最大勒子数算
	 * */
	private Integer lezi;
	private String chiCards;
	private String pengCards;
	private String gangCards;
	
	public Integer getLezi() {
		return lezi;
	}
	public void setLezi(Integer lezi) {
		this.lezi = lezi;
	}
	public Integer getModel() {
		return model;
	}
	public void setModel(Integer model) {
		this.model = model;
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
	public Integer getIsChiPai() {
		return isChiPai;
	}
	public void setIsChiPai(Integer isChiPai) {
		this.isChiPai = isChiPai;
	}
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
