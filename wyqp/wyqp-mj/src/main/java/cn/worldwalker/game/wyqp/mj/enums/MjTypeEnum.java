package cn.worldwalker.game.wyqp.mj.enums;

public enum MjTypeEnum {
	
	shangHaiQiaoMa(1, "上海敲麻"),
	shangHaiLaXiHu(2, "上海拉西胡"),
	shangHaiQingHunPeng(3, "上海清混碰"),
	shangHaiBaiDa(4, "上海百搭");
	public Integer type;
	public String desc;
	private MjTypeEnum(Integer type, String desc){
		this.type = type;
		this.desc = desc;
	}
	
	public static MjTypeEnum getMjTypeEnum(Integer type){
		for(MjTypeEnum mjTypeEnum : MjTypeEnum.values()){
			if (mjTypeEnum.type.equals(type)) {
				return mjTypeEnum;
			}
		}
		return null;
	}
}
