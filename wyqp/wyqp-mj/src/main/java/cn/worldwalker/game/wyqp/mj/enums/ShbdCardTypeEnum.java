package cn.worldwalker.game.wyqp.mj.enums;

public enum ShbdCardTypeEnum {
	
	pingHu(1, 1, "平胡"),
	pengPengHu(2, 2, "碰碰胡"),
	daDiaoChe(3, 2, "大吊车"),
	hunYiSe(4, 2, "混一色"),
	qingYiSe(5, 4, "清一色"),
	menQing(6, 2, "门清"),
	paoBaiDa(7, 2, "跑百搭"),
	wuBaiDa(8, 2, "无百搭");
	
	public Integer type;
	public Integer multiple;
	public String desc;
	
	private ShbdCardTypeEnum(Integer type, Integer multiple, String desc){
		this.type = type;
		this.multiple = multiple;
		this.desc = desc;
	}
	
	public static ShbdCardTypeEnum getCardSuitBySuit(Integer type){
		for(ShbdCardTypeEnum cardType : ShbdCardTypeEnum.values()){
			if (cardType.type.equals(type)) {
				return cardType;
			}
		}
		return null;
		
	}
}
