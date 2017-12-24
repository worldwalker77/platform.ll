package cn.worldwalker.game.wyqp.mj.enums;


public enum MjRoomStatusEnum {
	
	justBegin(1, "刚开始准备阶段"),
	inGame(2, "小局中"),
	curGameOver(3, "小局结束"),
	totalGameOver(4, "一圈结束");
	
	public Integer status;
	public String desc;
	
	private MjRoomStatusEnum(Integer status, String desc){
		this.status = status;
		this.desc = desc;
	}
	
	public static MjRoomStatusEnum getRoomStatusEnum(Integer status){
		for(MjRoomStatusEnum statusEnum : MjRoomStatusEnum.values()){
			if (statusEnum.status.equals(status)) {
				return statusEnum;
			}
		}
		return null;
	}
}
