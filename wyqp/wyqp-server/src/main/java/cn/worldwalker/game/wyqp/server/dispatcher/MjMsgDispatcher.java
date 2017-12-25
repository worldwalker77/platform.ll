package cn.worldwalker.game.wyqp.server.dispatcher;

import io.netty.channel.ChannelHandlerContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.worldwalker.game.wyqp.common.domain.base.BaseRequest;
import cn.worldwalker.game.wyqp.common.domain.base.UserInfo;
import cn.worldwalker.game.wyqp.common.enums.MsgTypeEnum;
import cn.worldwalker.game.wyqp.mj.service.MjGameService;
@Service(value="mjMsgDisPatcher")
public class MjMsgDispatcher extends BaseMsgDisPatcher {
	@Autowired
	private MjGameService mjGameService;
	@Override
	public void requestDispatcher(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Integer msgType = request.getMsgType();
		MsgTypeEnum msgTypeEnum= MsgTypeEnum.getMsgTypeEnumByType(msgType);
		switch (msgTypeEnum) {
		case createRoom:
			mjGameService.createRoom(ctx, request, userInfo);
			break;
		case entryRoom:
			mjGameService.entryRoom(ctx, request, userInfo);
			break;
		case ready:
			mjGameService.ready(ctx, request, userInfo);
			break;
		case dissolveRoom:
			mjGameService.dissolveRoom(ctx, request, userInfo);
			break;
		case agreeDissolveRoom:
			mjGameService.agreeDissolveRoom(ctx, request, userInfo);
			break;
		case disagreeDissolveRoom:
			mjGameService.disagreeDissolveRoom(ctx, request, userInfo);
			break;
		case delRoomConfirmBeforeReturnHall:
			mjGameService.delRoomConfirmBeforeReturnHall(ctx, request, userInfo);
			break;
		case queryPlayerInfo:
			mjGameService.queryPlayerInfo(ctx, request, userInfo);
			break;
		case chatMsg:
			mjGameService.chatMsg(ctx, request, userInfo);
			break;
		case userRecord:
			mjGameService.userRecord(ctx, request, userInfo);
			break;
		case chuPai:
			mjGameService.chuPai(ctx, request, userInfo);
			break;
		case chi:
			mjGameService.chi(ctx, request, userInfo);
			break;
		case peng:
			mjGameService.peng(ctx, request, userInfo);
			break;
		case mingGang:
			mjGameService.mingGang(ctx, request, userInfo);
			break;
		case anGang:
			mjGameService.anGang(ctx, request, userInfo);
			break;
		case tingPai:
			mjGameService.tingPai(ctx, request, userInfo);
			break;
		case huPai:
			mjGameService.huPai(ctx, request, userInfo);
			break;
		case pass:
			mjGameService.pass(ctx, request, userInfo);
			break;
		default:
			break;
		}
	}
	
}
