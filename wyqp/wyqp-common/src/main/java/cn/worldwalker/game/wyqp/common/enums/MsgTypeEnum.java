package cn.worldwalker.game.wyqp.common.enums;

public enum MsgTypeEnum {
	
	/**公共消息类型 1-99*/
	entryHall(1, "进入大厅"),
	userRecord(2, "玩家战绩"),
	userFeedback(3, "用户反馈"),
	notice(4, "公告通知"),
	createRoom(5, "创建房间"),
	entryRoom(6, "进入房间"),
	ready(7, "点击准备"),
	dealCards(8, "发牌消息"),
	dissolveRoom(9, "解散房间"),
	agreeDissolveRoom(10, "同意解散房间"),
	disagreeDissolveRoom(11, "不同意解散房间"),
	successDissolveRoom(12, "成功解散房间通知"),
	refreshRoom(13, "刷新房间信息"),
	offlineNotice(14, "通知玩家离线"),
	onlineNotice(15, "通知玩家上线"),
	delRoomConfirmBeforeReturnHall(16, "客户端返回大厅时的通知消息"),
	queryPlayerInfo(17, "查看玩家信息"),
	dissolveRoomCausedByOffline(18, "玩家离线超过4分钟，解散房间"),
	chatMsg(19, "聊天消息"),
	heartBeat(20, "心跳检测"),
	roomCardNumUpdate(21, "房卡数更新"),
	sendEmoticon(22, "给某个玩家发送特殊的表情符号"),
	syncPlayerLocation(23, "同步玩家地理位置信息"),
	curSettlement(24, "小结算"),
	totalSettlement(25, "大结算"),
	productList(26, "房卡商品列表"),
	bindProxy(27, "绑定代理"),
	checkBindProxy(28, "校验是否绑定代理"),
	unifiedOrder(29, "微信预支付前统一下单接口"),
//	notReadyTimer(30, "未准备玩家计时器"),
	userRecordDetail(31, "玩家战绩详情"),
	playBack(32, "回放"),
	joinClub(33, "申请加入俱乐部"),
	entryClub(34, "进入俱乐部"),
	exitClub(35, "退出进入的俱乐部"),
	getClubMembers(36, "查询俱乐部玩家列表"),
	getClubRooms(37, "查询俱乐部房间列表"),
	
	
	/**斗牛特有100-199*/
	readyRobBanker(100, "准备抢庄"),
	robBanker(101, "抢庄"),
	robBankerOverTime(102, "抢庄超过10s时间限制"),
	readyStake(103, "准备压分"),
	stakeScore(104, "压分"),
	showCard(105, "亮牌"),
	
	/**麻将特有200-299*/
	initRoom(208, "初始化房间基本数据"),//回放的时候才用到
	initHandCards(209, "游戏开始返回玩家牌列表"),
	moPai(210, "返回摸牌"),
	chuPai(211, "出牌"),
	chi(212, "吃牌"),
	peng(213, "碰牌"),
	mingGang(214, "明杠牌"),
	anGang(215, "暗杠牌"),
	tingPai(216, "听牌"),
	huPai(217, "胡牌"),
	addFlowerNotice(218, "补花通知"),
	pass(219, "过"),
	remaindCardNum(220, "通知玩家剩余桌牌数"),
	
	/**金花特有300-399*/
	stake(300, "压分"),
	autoCardsCompare(301, "到达跟注数量上限的自动比牌"),
	manualCardsCompare(302, "局中发起比牌"),
	watchCards(303, "看牌"),
	discardCards(304, "弃牌"),
	notReadyTimer(305, "未准备玩家计时器");
	
	public int msgType;
	public String desc;
	
	private MsgTypeEnum(int msgType, String desc){
		this.msgType = msgType;
		this.desc = desc;
	}
	
	public static MsgTypeEnum getMsgTypeEnumByType(int msgType){
		for(MsgTypeEnum msgTypeEnum : MsgTypeEnum.values()){
			if (msgType == msgTypeEnum.msgType) {
				return msgTypeEnum;
			}
		}
		return null;
	}
	
}
