package cn.worldwalker.game.wyqp.mj.cards;

import java.util.List;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.mj.enums.MjHuTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjTypeEnum;

public class MjCardTypeCalculation {
	
	public static Integer calCardType(MjPlayerInfo player, MjRoomInfo roomInfo){
		MjTypeEnum mjTypeEnum = MjTypeEnum.getMjTypeEnum(roomInfo.getMjType());
		switch (mjTypeEnum) {
			case shangHaiQiaoMa:
				
				break;
	
			default:
				break;
		}
		return null;
	}
	
	public static int[] getHandCards(MjPlayerInfo player, MjRoomInfo roomInfo){
		List<Integer> handCardList = player.getHandCardList();
		int[] handCards = new int[31];
		for(Integer cardIndex : handCardList){
			handCards[cardIndex]++;
		}
		Integer huType = player.getHuType();
		if (MjHuTypeEnum.zhuaChong.type.equals(huType) || MjHuTypeEnum.qiangGang.type.equals(huType)) {
			handCards[roomInfo.getLastCardIndex()]++;
		}else if(MjHuTypeEnum.ziMo.type.equals(huType) || MjHuTypeEnum.gangKai.type.equals(huType)){
			handCards[player.getCurMoPaiCardIndex()]++;
		}
		return handCards;
	} 
	
	public static Integer calShangHaiQiaoMaCardType(MjPlayerInfo player, MjTypeEnum mjTypeEnum, int[] handCards){
		checkDaDiaoChe(player, mjTypeEnum, handCards);
		return null;
	}
	
	public static void checkDaDiaoChe(MjPlayerInfo player, MjTypeEnum mjTypeEnum, int[] handCards){
		switch (mjTypeEnum) {
			case shangHaiQiaoMa:
				
				break;
	
			default:
				break;
		}
		
	}
	
}
