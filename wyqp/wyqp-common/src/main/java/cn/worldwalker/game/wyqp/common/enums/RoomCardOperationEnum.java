package cn.worldwalker.game.wyqp.common.enums;

public enum RoomCardOperationEnum {
	
	buyCard(1, "购买房卡"),
	consumeCard(2, "游戏消费房卡"),
	bindMobile(3, "绑定手机号获取房卡"),
	sendExtensionCode(4, "推广激活码获取房卡"),
	bindExtensionCode(5, "绑定激活码获取房卡"),
	send(6, "赠送房卡"),
	receive(7, "受赠房卡"),
	other(8, "其他");
	
	public Integer type;
	public String desc;
	
	private RoomCardOperationEnum(Integer type, String desc){
		this.type = type;
		this.desc = desc;
	}
	
	public static RoomCardOperationEnum getRoomOperationEnum(Integer type){
		for(RoomCardOperationEnum operationEnum : RoomCardOperationEnum.values()){
			if (operationEnum.type.equals(type)) {
				return operationEnum;
			}
		}
		return null;
	}
}
