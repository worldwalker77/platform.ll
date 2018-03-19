package cn.worldwalker.game.wyqp.mj.enums;

public enum ShQhpCardTypeEnum {
	
	baHua(1, 4, "八花"),
	ziYiSe(2, 4, "字一色"),
	luanFengXiang(3, 2, "乱风向"),
	qingPeng(4, 2, "清碰"),
	qingYiSe(5, 1, "清一色"),
	hunPeng(6, 1, "混碰"),
	daDiaoChe(7, 1, "大吊车"),
	hunYiSe(8, 0, "混一色"),
	pengPengHu(9, 0, "碰碰胡");
	
	public Integer type;
	/**这里表示勒子数*/
	public Integer multiple;
	public String desc;
	
	private ShQhpCardTypeEnum(Integer type, Integer multiple, String desc){
		this.type = type;
		this.multiple = multiple;
		this.desc = desc;
	}
	
	public static ShQhpCardTypeEnum getCardType(Integer type){
		for(ShQhpCardTypeEnum cardType : ShQhpCardTypeEnum.values()){
			if (cardType.type.equals(type)) {
				return cardType;
			}
		}
		return null;
		
	}
}
