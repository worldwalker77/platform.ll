package cn.worldwalker.game.wyqp.mj.enums;

public enum QmCardTypeEnum {
	
	pingHu(1, 1, "平胡"),
	pengPengHu(2, 1, "碰碰胡"),
	daDiaoChe(3, 1, "大吊车"),
	wuHuaGuo(4, 0, "无花果"),
	hunYiSe(5, 1, "混一色"),
	qingYiSe(6, 2, "清一色"),
	menQing(7, 1, "门清");
	
	public Integer type;
	/**番数*/
	public Integer fanNum;
	public String desc;
	
	private QmCardTypeEnum(Integer type, Integer fanNum, String desc){
		this.type = type;
		this.fanNum = fanNum;
		this.desc = desc;
	}
	
	public static QmCardTypeEnum getCardSuitBySuit(Integer type){
		for(QmCardTypeEnum cardType : QmCardTypeEnum.values()){
			if (cardType.type.equals(type)) {
				return cardType;
			}
		}
		return null;
		
	}
}
