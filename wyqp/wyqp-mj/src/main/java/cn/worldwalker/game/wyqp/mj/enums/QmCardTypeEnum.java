package cn.worldwalker.game.wyqp.mj.enums;

public enum QmCardTypeEnum {
	
	pingHu(1, 1, "平胡"),
	pengPengHu(2, 2, "碰碰胡"),
	daDiaoChe(3, 2, "大吊车"),
	hunYiSe(4, 2, "混一色"),
	qingYiSe(5, 4, "清一色"),
	menQing(6, 2, "门清");
	
	public Integer type;
	public Integer multiple;
	public String desc;
	
	private QmCardTypeEnum(Integer type, Integer multiple, String desc){
		this.type = type;
		this.multiple = multiple;
		this.desc = desc;
	}
	
	public static QmCardTypeEnum getCardType(Integer type){
		for(QmCardTypeEnum cardType : QmCardTypeEnum.values()){
			if (cardType.type.equals(type)) {
				return cardType;
			}
		}
		return null;
		
	}
}
