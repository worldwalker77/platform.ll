package cn.worldwalker.game.wyqp.mj.enums;

public enum MjTypeEnum {
	
	shangHaiQiaoMa(1, "上海敲麻"),
	shangHaiBaiDa(2, "上海百搭"),
	shangHaiQingHunPeng(3, "上海清混碰"),
	shangHaiLaXiHu(4, "上海拉西胡");
	
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
