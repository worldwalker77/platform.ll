package cn.worldwalker.game.wyqp.mj.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;
import cn.worldwalker.game.wyqp.mj.enums.MjHuTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.ShBdCardTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.ShLxhCardTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.ShQhpCardTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.ShQmCardTypeEnum;
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
		Integer huType = player.getHuType();
		Integer huCardIndex = null;
		if (MjHuTypeEnum.zhuaChong.type.equals(huType) || MjHuTypeEnum.qiangGang.type.equals(huType)) {
			huCardIndex =  roomInfo.getLastCardIndex();
		}else if(MjHuTypeEnum.ziMo.type.equals(huType) || MjHuTypeEnum.gangKai.type.equals(huType)){
			huCardIndex = player.getCurMoPaiCardIndex();
		}
		MjTypeEnum mjTypeEnum = MjTypeEnum.getMjTypeEnum(roomInfo.getDetailType());
		/**底花分计算*/
		calButtomAndFlowerScore(player, roomInfo, huCardIndex);
		/**牌型计算*/
		calCardType(roomInfo, player, mjTypeEnum, huCardIndex);
		/**倍数计算*/
		calMultiple(roomInfo, player, mjTypeEnum);
		return null;
	}
	
	public static int[] getHandCards(MjPlayerInfo player, MjRoomInfo roomInfo, Integer huCardIndex){
		List<Integer> handCardList = player.getHandCardList();
		int[] handCards = new int[roomInfo.getIndexLine()];
		for(Integer cardIndex : handCardList){
			handCards[cardIndex]++;
		}
		if (huCardIndex != null) {
			handCards[huCardIndex]++;
		}
		return handCards;
	} 
	
	public static void calCardType(MjRoomInfo roomInfo, MjPlayerInfo player, MjTypeEnum mjTypeEnum, Integer huCardIndex){
		MjTypeEnum mjType = MjTypeEnum.getMjTypeEnum(roomInfo.getDetailType());
		List<Integer> mjCardTypeList = player.getMjCardTypeList();
		switch (mjType) {
		case shangHaiQiaoMa:
			/**门清校验*/
			if (checkMenQing(player, huCardIndex)) {
				mjCardTypeList.add(ShQmCardTypeEnum.menQing.type);
			}
			/**清一色校验*/
			if (checkQingYiSe(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShQmCardTypeEnum.qingYiSe.type);
			}
			/**混一色校验*/
			if (checkHunYiSe(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShQmCardTypeEnum.hunYiSe.type);
			}
			/**大吊车校验*/
			if (checkDaDiaoChe(player, huCardIndex)) {
				mjCardTypeList.add(ShQmCardTypeEnum.daDiaoChe.type);
			}
			/**碰碰胡校验*/
			if (checkPengPengHu(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShQmCardTypeEnum.pengPengHu.type);
			}
			/**特殊牌型都没有，则设置为平胡*/
			if (mjCardTypeList.size() == 0) {
				mjCardTypeList.add(ShQmCardTypeEnum.pingHu.type);
			}
			break;
		case shangHaiBaiDa:
			/**门清校验*/
			if (checkMenQing(player, huCardIndex)) {
				mjCardTypeList.add(ShBdCardTypeEnum.menQing.type);
			}
			/**清一色校验*/
			if (checkQingYiSe(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShBdCardTypeEnum.qingYiSe.type);
			}
			/**混一色校验*/
			if (checkHunYiSe(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShBdCardTypeEnum.hunYiSe.type);
			}
			/**大吊车校验*/
			if (checkDaDiaoChe(player, huCardIndex)) {
				mjCardTypeList.add(ShBdCardTypeEnum.daDiaoChe.type);
			}
			/**碰碰胡校验*/
			if (checkPengPengHu(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShBdCardTypeEnum.pengPengHu.type);
			}
			/**乱风向*/
			if (checkLuanFengXaing(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShBdCardTypeEnum.luanFengXiang.type);
			}
			/**字一色*/
			if (checkZiYiSe(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShBdCardTypeEnum.ziYiSe.type);
			}
			/**特殊牌型都没有，则设置为平胡*/ 
			if (mjCardTypeList.size() == 0) {
				mjCardTypeList.add(ShBdCardTypeEnum.pingHu.type);
			}
			break;
		case shangHaiQingHunPeng:
			/**清一色校验*/
			if (checkQingYiSe(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShQhpCardTypeEnum.qingYiSe.type);
			}
			/**混一色校验*/
			if (checkHunYiSe(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShQhpCardTypeEnum.hunYiSe.type);
			}
			/**大吊车校验*/
			if (checkDaDiaoChe(player, huCardIndex)) {
				mjCardTypeList.add(ShQhpCardTypeEnum.daDiaoChe.type);
			}
			/**碰碰胡校验*/
			if (checkPengPengHu(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShQhpCardTypeEnum.pengPengHu.type);
			}
			/**乱风向*/
			if (checkLuanFengXaing(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShQhpCardTypeEnum.luanFengXiang.type);
			}
			/**字一色*/
			if (checkZiYiSe(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShQhpCardTypeEnum.ziYiSe.type);
			}
			break;
		case shangHaiLaXiHu:
			/**门清校验*/
			if (checkMenQing(player, huCardIndex)) {
				mjCardTypeList.add(ShLxhCardTypeEnum.menQing.type);
			}
			/**清一色校验*/
			if (checkQingYiSe(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShLxhCardTypeEnum.qingYiSe.type);
			}
			/**混一色校验*/
			if (checkHunYiSe(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShLxhCardTypeEnum.hunYiSe.type);
			}
			/**大吊车校验*/
			if (checkDaDiaoChe(player, huCardIndex)) {
				mjCardTypeList.add(ShLxhCardTypeEnum.daDiaoChe.type);
			}
			/**碰碰胡校验*/
			if (checkPengPengHu(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShLxhCardTypeEnum.pengPengHu.type);
			}
			/**乱风向*/
			if (checkLuanFengXaing(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShLxhCardTypeEnum.luanFengXiang.type);
			}
			/**字一色*/
			if (checkZiYiSe(roomInfo, player, huCardIndex)) {
				mjCardTypeList.add(ShLxhCardTypeEnum.ziYiSe.type);
			}
			/**特殊牌型都没有，则设置为平胡*/ 
			if (mjCardTypeList.size() == 0) {
				mjCardTypeList.add(ShLxhCardTypeEnum.pingHu.type);
			}
			break;

		default:
			break;
		}
	}
	
	public static void calMultiple(MjRoomInfo roomInfo, MjPlayerInfo player, MjTypeEnum mjTypeEnum){
		player.setMultiple(1);
		/**荒翻倍数*/
		if (roomInfo.getHuangFanNum() > 0) {
			player.setMultiple(player.getMultiple() * 2);
			roomInfo.setHuangFanNum(roomInfo.getHuangFanNum() - 1);
		}
		/**开宝倍数*/
		if (roomInfo.getIsCurGameKaiBao() > 0) {
			player.setMultiple(player.getMultiple() * 2);
		}
		List<Integer> mjCardTypeList = player.getMjCardTypeList();
		/**牌型组合倍数*/
		switch (mjTypeEnum) {
		case shangHaiQiaoMa:
			/**胡牌类型倍数*/
			player.setMultiple(MjHuTypeEnum.getMjHuTypeEnum(player.getHuType()).multiple);
			for(Integer cardType : mjCardTypeList){
				player.setMultiple(player.getMultiple() * ShQmCardTypeEnum.getCardType(cardType).multiple);
			}
			break;
		case shangHaiBaiDa:
			/**胡牌类型倍数*/
			player.setMultiple(MjHuTypeEnum.getMjHuTypeEnum(player.getHuType()).multiple);
			for(Integer cardType : mjCardTypeList){
				player.setMultiple(player.getMultiple() * ShBdCardTypeEnum.getCardType(cardType).multiple);
			}
			break;	
		case shangHaiQingHunPeng:
			/**清一色+碰碰胡*/
			if (mjCardTypeList.contains(ShQhpCardTypeEnum.qingYiSe.type)
			   &&mjCardTypeList.contains(ShQhpCardTypeEnum.pengPengHu.type)) {
				mjCardTypeList.add(ShQhpCardTypeEnum.qingPeng.type);
				mjCardTypeList.remove(ShQhpCardTypeEnum.qingYiSe.type);
				mjCardTypeList.remove(ShQhpCardTypeEnum.pengPengHu.type);
			}
			/**混一色+碰碰胡*/
			if (mjCardTypeList.contains(ShQhpCardTypeEnum.hunYiSe.type)
			   &&mjCardTypeList.contains(ShQhpCardTypeEnum.pengPengHu.type)) {
				mjCardTypeList.add(ShQhpCardTypeEnum.hunPeng.type);
				mjCardTypeList.remove(ShQhpCardTypeEnum.hunYiSe.type);
				mjCardTypeList.remove(ShQhpCardTypeEnum.pengPengHu.type);
			}
			Collections.sort(mjCardTypeList);
			Integer maxLezi = mjCardTypeList.get(mjCardTypeList.size() - 1);
			/**如果最大牌型的勒子数大于0，则最终结果按照最大勒子数计算*/
			if (maxLezi > 0) {
				player.setButtomAndFlowerScore(maxLezi * roomInfo.getFlowerPerLezi() * roomInfo.getEachFlowerScore());
			}else{/**最大牌型勒子数==0*/
				/**底花分 > 1个勒子分数 则顶为一个勒子分数*/
				if (player.getButtomAndFlowerScore() > roomInfo.getFlowerPerLezi() * roomInfo.getEachFlowerScore()) {
					player.setButtomAndFlowerScore(roomInfo.getFlowerPerLezi() * roomInfo.getEachFlowerScore());
				}
			}
			break;
		case shangHaiLaXiHu:
			/**胡牌类型倍数*/
			player.setMultiple(MjHuTypeEnum.getMjHuTypeEnum(player.getHuType()).multiple);
			for(Integer cardType : mjCardTypeList){
				player.setMultiple(player.getMultiple() * ShQmCardTypeEnum.getCardType(cardType).multiple);
			}
			break;
		default:
			break;
		}
		
	}
	public static void calButtomAndFlowerScore(MjPlayerInfo player, MjRoomInfo roomInfo, Integer huCardIndex){
		/**补花数量*/
		Integer flowerNum = player.getFlowerCardList().size();
		/**风向碰、风暗刻*/
		List<Integer> pengCardList = player.getPengCardList();
		List<Integer> handCardList = player.getHandCardList();
		List<Integer> newHandCardList = new ArrayList<Integer>();
		newHandCardList.addAll(handCardList);
		if (huCardIndex != null) {
			newHandCardList.add(huCardIndex);
		}
		for(int cardIndex = 27; cardIndex < roomInfo.getIndexLine(); cardIndex++){
			if (newHandCardList.contains(Arrays.asList(cardIndex, cardIndex, cardIndex))) {
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
		/**如果是百搭类型则手牌中的百搭牌也算花*/
		if (roomInfo.getDetailType().equals(MjTypeEnum.shangHaiBaiDa.type)) {
			for(Integer temp : newHandCardList){
				if (temp.equals(roomInfo.getBaiDaCardIndex())) {
					flowerNum++;
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
	
	public static boolean checkDaDiaoChe(MjPlayerInfo player,Integer huCardIndex){
		
		if (player.getHandCardList().size() == 1) {
			return true;
		}
		return false;
	}
	public static boolean checkMenQing(MjPlayerInfo player, Integer huCardIndex){
		
		if (player.getChiCardList().size() == 0 && player.getPengCardList().size() == 0 &&player.getMingGangCardList().size() == 0) {
			return true;
		}
		return false;
	}
	
	public static boolean checkQingYiSe(MjRoomInfo roomInfo, MjPlayerInfo player, Integer huCardIndex){
		List<Integer> handCardList = player.getHandCardList();
		List<Integer> newHandCardList = new ArrayList<Integer>();
		newHandCardList.addAll(handCardList);
		if (huCardIndex != null) {
			newHandCardList.add(huCardIndex);
		}
		/**如果是百搭麻将，则去掉百搭后在看是否清一色*/
		if (roomInfo.getBaiDaCardIndex() != null) {
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
			return false;
		}
		if (minCardIndex < 27) {
			if (maxCardIndex/9 != minCardIndex/9) {
				return false;
			}
		}
		return true;
	}
	
	
	public static boolean checkHunYiSe(MjRoomInfo roomInfo, MjPlayerInfo player, Integer huCardIndex){
		List<Integer> handCardList = player.getHandCardList();
		List<Integer> newHandCardList = new ArrayList<Integer>();
		newHandCardList.addAll(handCardList);
		if (huCardIndex != null) {
			newHandCardList.add(huCardIndex);
		}
		/**如果是百搭麻将，则去掉百搭后在看是否混一色*/
		if (roomInfo.getBaiDaCardIndex() != null) {
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
			return false;
		}
		if ((wanNum > 0 && tongNum == 0 && tiaoNum == 0) 
			||(wanNum == 0 && tongNum > 0 && tiaoNum == 0)
			||(wanNum == 0 && tongNum == 0 && tiaoNum > 0)) {
			return true;
		}
		return false;
	}
	
	public static boolean checkPengPengHu(MjRoomInfo roomInfo, MjPlayerInfo player, Integer huCardIndex){
		if (player.getChiCardList().size() > 0) {
			return false;
		}
		int[] handCards = getHandCards(player, roomInfo, huCardIndex);
		int baiDaNum = 0;
		if (roomInfo.getBaiDaCardIndex() != null) {
			baiDaNum = handCards[roomInfo.getBaiDaCardIndex()];
			/**手牌中去掉百搭*/
			handCards[roomInfo.getBaiDaCardIndex()] = 0;
		}
		boolean isPengPengHu = false;
		int cardNum1 = 0;
		int cardNum2 = 0;
		int cardNum3 = 0;
		int cardNum4 = 0;
		for(int i = 0; i < roomInfo.getIndexLine(); i++){
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
		return isPengPengHu;
	}
	
	public static boolean checkPaoBaiDa(MjRoomInfo roomInfo, MjPlayerInfo player, Integer huCardIndex){
		List<Integer> handCardList = player.getHandCardList();
		/**如果手牌中没有百搭则不可能跑百搭*/
		if (!handCardList.contains(roomInfo.getBaiDaCardIndex())) {
			return false;
		}
		if (MjCardRule.checkPaoBaiDa(roomInfo, handCardList)) {
			return true;
		}
		return false;
	}
	public static boolean checkWuBaiDa(MjRoomInfo roomInfo, MjPlayerInfo player, Integer huCardIndex){
		List<Integer> handCardList = player.getHandCardList();
		/**如果手牌中没有百搭并且胡的那张牌也不是百搭*/
		if (!handCardList.contains(roomInfo.getBaiDaCardIndex()) && !roomInfo.getBaiDaCardIndex().equals(huCardIndex)) {
			return true;
		}
		return false;
	}
	public static boolean checkSiBaiDa(MjRoomInfo roomInfo, MjPlayerInfo player, Integer huCardIndex){
		List<Integer> handCardList = player.getHandCardList();
		List<Integer> newHandCardList = new ArrayList<Integer>();
		newHandCardList.addAll(handCardList);
		if (huCardIndex != null) {
			newHandCardList.add(huCardIndex);
		}
		int baiDaNum = 0;
		for(Integer temp : newHandCardList){
			if (temp.equals(roomInfo.getBaiDaCardIndex())) {
				baiDaNum++;
			}
		}
		if (baiDaNum == 4) {
			return true;
		}
		return false;
	}
	public static boolean checkLuanFengXaing(MjRoomInfo roomInfo, MjPlayerInfo player, Integer huCardIndex){
		List<Integer> handCardList = player.getHandCardList();
		Integer gui = Hulib.invalidCardInex;
		if (roomInfo.getBaiDaCardIndex() != null) {
			gui = roomInfo.getBaiDaCardIndex();
		}
		/**如果走正常表校验是可以胡牌的，则说明肯定不是乱风向*/
		if (Hulib.getInstance().get_hu_info(handCardList, huCardIndex, gui,roomInfo.getIndexLine())) {
			return false;
		}
		if (MjCardRule.isAllFeng(roomInfo, player, huCardIndex)) {
			return true;
		}
		return false;
	}
	public static boolean checkZiYiSe(MjRoomInfo roomInfo, MjPlayerInfo player, Integer huCardIndex){
		List<Integer> handCardList = player.getHandCardList();
		Integer gui = Hulib.invalidCardInex;
		if (roomInfo.getBaiDaCardIndex() != null) {
			gui = roomInfo.getBaiDaCardIndex();
		}
		/**如果走正常表校验是不可以胡牌的，则说明肯定不是字一色*/
		if (!Hulib.getInstance().get_hu_info(handCardList, huCardIndex, gui,roomInfo.getIndexLine())) {
			return false;
		}
		if (MjCardRule.isAllFeng(roomInfo, player, huCardIndex)) {
			return true;
		}
		return false;
	}
	
}
