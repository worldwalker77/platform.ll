package cn.worldwalker.game.wyqp.mj.enums;

public enum MjOperationEnum {
	
	chi(1, "吃"),
	peng(2, "碰"),
	mingGang(3, "明杠"),
	anGang(4, "暗杠"),
	hu(5, "胡"),
	tingHu(6, "听胡");
	public Integer type;
	public String desc;
	
	private MjOperationEnum(Integer type, String desc){
		this.type = type;
		this.desc = desc;
	}
}
