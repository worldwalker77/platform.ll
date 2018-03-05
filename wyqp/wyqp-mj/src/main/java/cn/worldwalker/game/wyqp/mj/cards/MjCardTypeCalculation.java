package cn.worldwalker.game.wyqp.mj.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;
import cn.worldwalker.game.wyqp.mj.enums.MjHuTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.QmCardTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.ShbdCardTypeEnum;
import cn.worldwalker.game.wyqp.mj.huvalidate.Hulib;

public class MjCardTypeCalculation {
	
	public static void main(String[] args) {
		
		MjRoomInfo roomInfo = new MjRoomInfo();
		roomInfo.setHuButtomScore(5);
		roomInfo.setEachFlowerScore(5);
		roomInfo.setIsFeiCangyin(1);
		roomInfo.setIsKaiBao(1);
		roomInfo.setIsHuangFan(1);
		roomInfo.setIsCurGameKaiBao(1);
		MjPlayerInfo player = new MjPlayerInfo();
		player.setHuType(MjHuTypeEnum.gangKai.type);
		player.setCurMoPaiCardIndex(4);
		player.setFeiCangYingCardIndex(4);
		List<Integer> handCardList = Arrays.asList(4,4,16,16);
		List<Integer> chiCardList = Arrays.asList();
		List<Integer> pengCardList = Arrays.asList(19,19,19,24,24,24);
		List<Integer> mingGangCardList = Arrays.asList(7,7,7,7);
		List<Integer> anGangCardList = Arrays.asList();
		List<Integer> flowerCardList = Arrays.asList(31,31,31,31,33);
		player.setHandCardList(handCardList);
		player.setChiCardList(chiCardList);
		player.setPengCardList(pengCardList);
		player.setMingGangCardList(mingGangCardList);
		player.setAnGangCardList(anGangCardList);
		player.setFlowerCardList(flowerCardList);
		calButtomFlowerScoreAndCardTypeAndMultiple(player, roomInfo);
		System.out.println(JsonUtil.toJson(player.getMjCardTypeList()));
		System.out.println("底花分：" + player.getButtomAndFlowerScore());
		System.out.println("倍数：" + player.getMultiple());
	}
	
	/**
	 * 计算底花分、牌型、倍数
	 * @param player
	 * @param roomInfo
	 * @return
	 */
	public static Integer calButtomFlowerScoreAndCardTypeAndMultiple(MjPlayerInfo player, MjRoomInfo roomInfo){
		int[] handCards = getHandCards(player, roomInfo);
		MjTypeEnum mjTypeEnum = MjTypeEnum.getMjTypeEnum(roomInfo.getDetailType());
		switch (mjTypeEnum) {
			case shangHaiQiaoMa:
				calButtomAndFlowerScore(player, roomInfo);
				calShangHaiQiaoMaCardTypeAndMultiple(roomInfo, player, mjTypeEnum, handCards);
				break;
	
			default:
				break;
		}
		return null;
	}
	
	public static int[] getHandCards(MjPlayerInfo player, MjRoomInfo roomInfo){
		List<Integer> handCardList = player.getHandCardList();
		int[] handCards = new int[Hulib.indexLine];
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
	
	public static void calShangHaiQiaoMaCardTypeAndMultiple(MjRoomInfo roomInfo, MjPlayerInfo player, MjTypeEnum mjTypeEnum, int[] handCards){
		
		/****************牌型计算*****************************/
		/**门清校验*/
		checkMenQing(player, mjTypeEnum, handCards);
		/**清一色校验*/
		checkQingYiSe(roomInfo, player, mjTypeEnum, handCards);
		/**混一色校验*/
		checkHunYiSe(roomInfo, player, mjTypeEnum, handCards);
		/**大吊车校验*/
		checkDaDiaoChe(player, mjTypeEnum, handCards);
		/**碰碰胡校验*/
		checkPengPengHu(roomInfo, player, mjTypeEnum, handCards);
		List<Integer> mjCardTypeList = player.getMjCardTypeList();
		/**特殊牌型都没有，则设置为平胡*/
		if (mjCardTypeList.size() == 0) {
			mjCardTypeList.add(QmCardTypeEnum.pingHu.type);
		}
		
		/********************倍数计算*********************/
		/**胡牌类型倍数*/
		player.setMultiple(MjHuTypeEnum.getMjHuTypeEnum(player.getHuType()).multiple);
		/**荒翻倍数*/
		if (roomInfo.getHuangFanNum() > 0) {
			player.setMultiple(player.getMultiple() * 2);
			roomInfo.setHuangFanNum(roomInfo.getHuangFanNum() - 1);
		}
		/**开宝倍数*/
		if (roomInfo.getIsCurGameKaiBao() > 0) {
			player.setMultiple(player.getMultiple() * 2);
		}
		/**牌型组合倍数*/
		for(Integer cardType : mjCardTypeList){
			player.setMultiple(player.getMultiple() * QmCardTypeEnum.getCardSuitBySuit(cardType).multiple);
		}
	}
	public static void calButtomAndFlowerScore(MjPlayerInfo player, MjRoomInfo roomInfo){
		/**补花数量*/
		Integer flowerNum = player.getFlowerCardList().size();
		/**风向碰、风暗刻*/
		List<Integer> pengCardList = player.getPengCardList();
		List<Integer> handCardList = player.getHandCardList();
		for(int cardIndex = 27; cardIndex <= 30; cardIndex++){
			if (handCardList.contains(Arrays.asList(cardIndex, cardIndex, cardIndex))) {
				flowerNum++;
			}
			if (pengCardList.contains(cardIndex)) {
				flowerNum++;
			}
		}
		/**明杠、风向明杠*/
		List<Integer> mingGangCardList = player.getMingGangCardList();
		if (mingGangCardList.size() > 0) {
			int mingGangNum = mingGangCardList.size()/4;
			for(int i = 0; i < mingGangNum; i++){
				int tempCardIndex = mingGangCardList.get(i*4);
				/**非风向明杠*/
				if (tempCardIndex < 27) {
					flowerNum++;
				}else{/**风向明杠*/
					flowerNum += 2;
				}
			}
		}
		/**暗杠、风向暗杠*/
		List<Integer> anGangCardList = player.getAnGangCardList();
		if (anGangCardList.size() > 0) {
			int anGangNum = anGangCardList.size()/4;
			for(int i = 0; i < anGangNum; i++){
				int tempCardIndex = anGangCardList.get(i*4);
				/**非风向暗杠*/
				if (tempCardIndex < 27) {
					flowerNum += 2;
				}else{/**风向暗杠*/
					flowerNum += 3;
				}
			}
		}
		/**无花果*/
		if (flowerNum == 0) {
			flowerNum = 10;
		}
		/**飞苍蝇*/
		if (roomInfo.getIsFeiCangyin() > 0) {
			Integer feiCangYingCardIndex = player.getFeiCangYingCardIndex();
			/**如果是风牌，则算5花*/
			if (feiCangYingCardIndex > 26) {
				flowerNum += 5;
			}else{
				flowerNum += feiCangYingCardIndex%9;
			}
		}
		/**计算底和花分*/
		player.setButtomAndFlowerScore(flowerNum*roomInfo.getEachFlowerScore() + roomInfo.getHuButtomScore());
	}
	
	public static void checkDaDiaoChe(MjPlayerInfo player, MjTypeEnum mjTypeEnum, int[] handCards){
		
		if (player.getHandCardList().size() == 1) {
			switch (mjTypeEnum) {
				case shangHaiQiaoMa:
					player.getMjCardTypeList().add(QmCardTypeEnum.daDiaoChe.type);
					break;
		
				default:
					break;
			}
		}
	}
	public static void checkMenQing(MjPlayerInfo player, MjTypeEnum mjTypeEnum, int[] handCards){
		
		if (player.getChiCardList().size() == 0 && player.getPengCardList().size() == 0 &&player.getMingGangCardList().size() == 0) {
			switch (mjTypeEnum) {
				case shangHaiQiaoMa:
					player.getMjCardTypeList().add(QmCardTypeEnum.menQing.type);
					break;
				default:
					break;
			}
		}
	}
	
	public static void checkQingYiSe(MjRoomInfo roomInfo, MjPlayerInfo player, MjTypeEnum mjTypeEnum, int[] handCards){
		List<Integer> handCardList = player.getHandCardList();
		List<Integer> newHandCardList = new ArrayList<Integer>();
		newHandCardList.addAll(handCardList);
		/**如果是百搭麻将，则去掉百搭后在看是否清一色*/
		if (MjTypeEnum.shangHaiBaiDa.type.equals(roomInfo.getDetailType())) {
			Iterator<Integer> sListIterator = newHandCardList.iterator(); 
			while(sListIterator.hasNext()){ 
				Integer e = sListIterator.next(); 
			  if(e.equals(roomInfo.getBaiDaCardIndex())){ 
			  sListIterator.remove(); 
			  } 
			}
		}
		Integer maxCardIndex = newHandCardList.get(0);
		Integer minCardIndex = newHandCardList.get(0);
		for(Integer cardIndex : newHandCardList){
			if (cardIndex > maxCardIndex) {
				maxCardIndex = cardIndex;
			}
			if (cardIndex < minCardIndex) {
				minCardIndex = cardIndex;
			}
		}
		
		List<Integer> chiCardList = player.getChiCardList();
		if (chiCardList.size() > 0) {
			for(Integer cardIndex : chiCardList){
				if (cardIndex > maxCardIndex) {
					maxCardIndex = cardIndex;
				}
				if (cardIndex < minCardIndex) {
					minCardIndex = cardIndex;
				}
			}
		}
		
		List<Integer> pengCardList = player.getPengCardList();
		if (pengCardList.size() > 0) {
			for(Integer cardIndex : pengCardList){
				if (cardIndex > maxCardIndex) {
					maxCardIndex = cardIndex;
				}
				if (cardIndex < minCardIndex) {
					minCardIndex = cardIndex;
				}
			}
		}
		
		List<Integer> mingGangCardList = player.getMingGangCardList();
		if (mingGangCardList.size() > 0) {
			for(Integer cardIndex : mingGangCardList){
				if (cardIndex > maxCardIndex) {
					maxCardIndex = cardIndex;
				}
				if (cardIndex < minCardIndex) {
					minCardIndex = cardIndex;
				}
			}
		}
		
		List<Integer> anGangCardList = player.getAnGangCardList();
		if (anGangCardList.size() > 0) {
			for(Integer cardIndex : anGangCardList){
				if (cardIndex > maxCardIndex) {
					maxCardIndex = cardIndex;
				}
				if (cardIndex < minCardIndex) {
					minCardIndex = cardIndex;
				}
			}
		}
		if (maxCardIndex - minCardIndex > 8) {
			return;
		}
		if (minCardIndex < 27) {
			if (maxCardIndex/9 != minCardIndex/9) {
				return;
			}
		}
		switch (mjTypeEnum) {
			case shangHaiQiaoMa:
				player.getMjCardTypeList().add(QmCardTypeEnum.qingYiSe.type);
				break;
			case shangHaiBaiDa:
				player.getMjCardTypeList().add(ShbdCardTypeEnum.qingYiSe.type);
				break;
	
			default:
				break;
		}
	}
	
	
	public static void checkHunYiSe(MjRoomInfo roomInfo, MjPlayerInfo player, MjTypeEnum mjTypeEnum, int[] handCards){
		List<Integer> handCardList = player.getHandCardList();
		List<Integer> newHandCardList = new ArrayList<Integer>();
		newHandCardList.addAll(handCardList);
		/**如果是百搭麻将，则去掉百搭后在看是否混一色*/
		if (MjTypeEnum.shangHaiBaiDa.type.equals(roomInfo.getDetailType())) {
			Iterator<Integer> sListIterator = newHandCardList.iterator(); 
			while(sListIterator.hasNext()){ 
				Integer e = sListIterator.next(); 
			  if(e.equals(roomInfo.getBaiDaCardIndex())){ 
			  sListIterator.remove(); 
			  } 
			}
		}
		List<Integer> chiCardList = player.getChiCardList();
		List<Integer> pengCardList = player.getPengCardList();
		List<Integer> mingGangCardList = player.getMingGangCardList();
		List<Integer> anGangCardList = player.getAnGangCardList();
		int wanNum = 0;
		int tongNum = 0;
		int tiaoNum = 0;
		int fengNum = 0;
		for(Integer cardInex : newHandCardList){
			if (cardInex >= 0 && cardInex <= 8) {
				wanNum++;
			}else if(cardInex >= 9 && cardInex <= 17){
				tongNum++;
			}else if(cardInex >= 18 && cardInex <= 26){
				tiaoNum++;
			}else{
				fengNum++;
			}
		}
		for(Integer cardInex : chiCardList){
			if (cardInex >= 0 && cardInex <= 8) {
				wanNum++;
			}else if(cardInex >= 9 && cardInex <= 17){
				tongNum++;
			}else if(cardInex >= 18 && cardInex <= 26){
				tiaoNum++;
			}else{
				fengNum++;
			}
		}
		for(Integer cardInex : pengCardList){
			if (cardInex >= 0 && cardInex <= 8) {
				wanNum++;
			}else if(cardInex >= 9 && cardInex <= 17){
				tongNum++;
			}else if(cardInex >= 18 && cardInex <= 26){
				tiaoNum++;
			}else{
				fengNum++;
			}
		}
		for(Integer cardInex : mingGangCardList){
			if (cardInex >= 0 && cardInex <= 8) {
				wanNum++;
			}else if(cardInex >= 9 && cardInex <= 17){
				tongNum++;
			}else if(cardInex >= 18 && cardInex <= 26){
				tiaoNum++;
			}else{
				fengNum++;
			}
		}
		for(Integer cardInex : anGangCardList){
			if (cardInex >= 0 && cardInex <= 8) {
				wanNum++;
			}else if(cardInex >= 9 && cardInex <= 17){
				tongNum++;
			}else if(cardInex >= 18 && cardInex <= 26){
				tiaoNum++;
			}else{
				fengNum++;
			}
		}
		if (fengNum == 0) {
			return;
		}
		if ((wanNum > 0 && tongNum == 0 && tiaoNum == 0) 
			||(wanNum == 0 && tongNum > 0 && tiaoNum == 0)
			||(wanNum == 0 && tongNum == 0 && tiaoNum > 0)) {
			switch (mjTypeEnum) {
			case shangHaiQiaoMa:
				player.getMjCardTypeList().add(QmCardTypeEnum.hunYiSe.type);
				break;
			case shangHaiBaiDa:
				player.getMjCardTypeList().add(ShbdCardTypeEnum.qingYiSe.type);
				break;
			default:
				break;
			}
		}
		
	}
	
	public static void checkPengPengHu(MjRoomInfo roomInfo, MjPlayerInfo player, MjTypeEnum mjTypeEnum, int[] handCards){
		if (player.getChiCardList().size() > 0) {
			return;
		}
		boolean isPengPengHu = false;
		int cardNum1 = 0;
		int cardNum2 = 0;
		int cardNum3 = 0;
		int cardNum4 = 0;
		for(int i = 0; i < Hulib.indexLine; i++){
			if (handCards[i] == 1) {
				cardNum1++;
			}else if (handCards[i] == 2) {
				cardNum2++;
			}else if (handCards[i] == 3) {
				cardNum3++;
			}else if (handCards[i] == 4) {
				cardNum4++;
			}
		}
		int baiDaNum = removeBaidaFromHandCardList(roomInfo, player, mjTypeEnum, handCards);
		switch (baiDaNum) {
			case 0:
				if (cardNum1 == 0 && cardNum2 == 1 && cardNum4 == 0) {
					isPengPengHu = true;
				}
				break;
			case 1:
				if (cardNum1 == 0 && cardNum2 == 2 && cardNum4 == 0) {
					isPengPengHu = true;
				}else if(cardNum1 == 1 && cardNum2 == 0 && cardNum4 == 0){
					isPengPengHu = true;
				}
				break;
			case 2:
				if (cardNum1 == 0 && cardNum2 == 0 && cardNum4 == 0) {
					isPengPengHu = true;
				}else if(cardNum1 == 1 && cardNum2 == 1 && cardNum4 == 0){
					isPengPengHu = true;
				}else if(cardNum1 == 0 && cardNum2 == 3 && cardNum4 == 0){
					isPengPengHu = true;
				}
				break;
			case 3:
				if (cardNum1 == 0 && cardNum2 == 1 && cardNum4 == 0) {
					isPengPengHu = true;
				}else if(cardNum1 == 2 && cardNum2 == 0 && cardNum4 == 0){
					isPengPengHu = true;
				}else if(cardNum1 == 1 && cardNum2 == 2 && cardNum4 == 0){
					isPengPengHu = true;
				}else if(cardNum1 == 0 && cardNum2 == 4 && cardNum4 == 0){
					isPengPengHu = true;
				}
				break;
			case 4:
				if (cardNum1 == 1 && cardNum2 == 0 && cardNum4 == 0) {
					isPengPengHu = true;
				}else if(cardNum1 == 0 && cardNum2 == 2 && cardNum4 == 0){
					isPengPengHu = true;
				}else if(cardNum1 == 2 && cardNum2 == 1 && cardNum4 == 0){
					isPengPengHu = true;
				}else if(cardNum1 == 1 && cardNum2 == 3 && cardNum4 == 0){
					isPengPengHu = true;
				}
				break;
	
			default:
				break;
		}
		if (isPengPengHu) {
			switch (mjTypeEnum) {
			case shangHaiQiaoMa:
				player.getMjCardTypeList().add(QmCardTypeEnum.pengPengHu.type);
				break;
			case shangHaiBaiDa:
				player.getMjCardTypeList().add(ShbdCardTypeEnum.pengPengHu.type);
				break;
			default:
				break;
			}
		}
		
	}
	
	public static int removeBaidaFromHandCardList(MjRoomInfo roomInfo, MjPlayerInfo player, MjTypeEnum mjTypeEnum, int[] handCards){
		int baiDaNum = 0;
		if (mjTypeEnum.equals(MjTypeEnum.shangHaiBaiDa)) {
			List<Integer> handCardList = player.getHandCardList();
			List<Integer> newHandCardList = new ArrayList<Integer>();
			newHandCardList.addAll(handCardList);
			Iterator<Integer> sListIterator = newHandCardList.iterator(); 
			
			while(sListIterator.hasNext()){ 
				Integer e = sListIterator.next(); 
				if(e.equals(roomInfo.getBaiDaCardIndex())){ 
				  sListIterator.remove(); 
				  baiDaNum++;
				} 
			}
		}
		return baiDaNum;
	}
	
	
}
