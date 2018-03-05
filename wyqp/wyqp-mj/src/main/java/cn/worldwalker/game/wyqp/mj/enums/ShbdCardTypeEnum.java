package cn.worldwalker.game.wyqp.mj.enums;

public enum ShbdCardTypeEnum {
	
	pingHu(1, 1, "平胡"),
	pengPengHu(2, 2, "碰碰胡"),
	daDiaoChe(3, 2, "大吊车"),
	hunYiSe(4, 2, "混一色"),
	qingYiSe(5, 4, "清一色"),
	menQing(6, 2, "门清"),
	luanFengXiang(7, 4, "乱风向"),
	ziYiSe(8, 8, "字一色"),
	siBaiDa(9, 4, "四百搭"),
	paoBaiDa(10, 2, "跑百搭"),
	wuBaiDa(11, 2, "无百搭");
	
	public Integer type;
	public Integer multiple;
	public String desc;
	
	private ShbdCardTypeEnum(Integer type, Integer multiple, String desc){
		this.type = type;
		this.multiple = multiple;
		this.desc = desc;
	}
	
	public static ShbdCardTypeEnum getCardType(Integer type){
		for(ShbdCardTypeEnum cardType : ShbdCardTypeEnum.values()){
			if (cardType.type.equals(type)) {
				return cardType;
			}
		}
		return null;
		
	}
}
