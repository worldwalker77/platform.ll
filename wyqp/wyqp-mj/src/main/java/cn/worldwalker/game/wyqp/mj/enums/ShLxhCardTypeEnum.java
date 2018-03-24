package cn.worldwalker.game.wyqp.mj.enums;

public enum ShLxhCardTypeEnum {
	
	pingHu(1, 1, "平胡"),
	pengPengHu(2, 2, "碰碰胡"),
	daDiaoChe(3, 2, "大吊车"),
	hunYiSe(4, 2, "混一色"),
	qingYiSe(5, 4, "清一色"),
	menQing(6, 2, "门清"),
	ziYiSe(2, 16, "字一色"),
	luanFengXiang(3, 8, "乱风向");
	
	public Integer type;
	/**这里表示勒子数*/
	public Integer multiple;
	public String desc;
	
	private ShLxhCardTypeEnum(Integer type, Integer multiple, String desc){
		this.type = type;
		this.multiple = multiple;
		this.desc = desc;
	}
	
	public static ShLxhCardTypeEnum getCardType(Integer type){
		for(ShLxhCardTypeEnum cardType : ShLxhCardTypeEnum.values()){
			if (cardType.type.equals(type)) {
				return cardType;
			}
		}
		return null;
		
	}
}
