package cn.worldwalker.game.wyqp.common.enums;

public enum RoomCardConsumeEnum {
	
	nn_roomOwnerPay_10_Games(1, 1, 10, 4, "房主付费,10局4张房卡"),
	nn_roomOwnerPay_20_Games(1, 1, 20, 7, "房主付费,20局7张房卡"),
	nn_AAPay_10_Games(1, 2, 10, 1, "AA制付费,10局每人1张房卡"),
	nn_AAPay_20_Games(1, 2, 20, 2, "AA制付费,20局每人2张房卡"),
	
	mj_roomOwnerPay_8_Games(2, 1, 8, 4, "房主付费8局需4张房卡"),
	mj_roomOwnerPay_16_Games(2, 1, 16, 7, "房主付费16局需7张房卡"),
	mj_roomOwnerPay_24_Games(2, 1, 24, 10, "房主付费24局需10张房卡"),
	mj_AAPay_8_Games(2, 2, 8, 1, "AA制付费10局每人需1张房卡"),
	mj_AAPay_16_Games(2, 2, 16, 2, "AA制付费16局每人需2张房卡"),
	mj_AAPay_24_Games(2, 2, 24, 3, "AA制付费24局每人需3张房卡"),
	
	jh_roomOwnerPay_8_Games(3, 1, 8, 4, "房主付费8局需4张房卡"),
	jh_roomOwnerPay_16_Games(3, 1, 16, 7, "房主付费16局需7张房卡"),
	jh_AAPay_8_Games(3, 2, 8, 1, "AA制付费10局每人需1张房卡"),
	jh_AAPay_16_Games(3, 2, 16, 2, "AA制付费16局每人需2张房卡");
	
	public Integer gameType;
	public Integer payType;
	public Integer totalGames;
	public Integer needRoomCardNum;
	public String desc;
	
	private RoomCardConsumeEnum(Integer gameType, Integer payType, Integer totalGames, Integer needRoomCardNum, String desc){
		this.gameType = gameType;
		this.payType = payType;
		this.totalGames = totalGames;
		this.needRoomCardNum = needRoomCardNum;
		this.desc = desc;
	}
	
	public static RoomCardConsumeEnum getRoomCardConsumeEnum(Integer gameType, Integer payType, Integer totalGames){
		for(RoomCardConsumeEnum consumeEnum : RoomCardConsumeEnum.values()){
			if (consumeEnum.gameType.equals(gameType) && consumeEnum.payType.equals(payType) && consumeEnum.totalGames.equals(totalGames)) {
				return consumeEnum;
			}
		}
		return null;
	}
}
