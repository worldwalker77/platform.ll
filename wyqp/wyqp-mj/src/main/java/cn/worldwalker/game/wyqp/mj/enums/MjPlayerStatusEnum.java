package cn.worldwalker.game.wyqp.mj.enums;

public enum MjPlayerStatusEnum {
	
	notReady(1, "未准备"),
	ready(2, "已准备");
	
	public Integer status;
	public String desc;
	
	private MjPlayerStatusEnum(Integer status, String desc){
		this.status = status;
		this.desc = desc;
	}
}
