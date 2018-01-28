package cn.worldwalker.game.wyqp.server.service;

import io.netty.channel.ChannelHandlerContext;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.worldwalker.game.wyqp.common.constant.Constant;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRequest;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.base.ExtensionCodeBindModel;
import cn.worldwalker.game.wyqp.common.domain.base.RedisRelaModel;
import cn.worldwalker.game.wyqp.common.domain.base.SmsResModel;
import cn.worldwalker.game.wyqp.common.domain.base.UserInfo;
import cn.worldwalker.game.wyqp.common.domain.base.UserModel;
import cn.worldwalker.game.wyqp.common.enums.GameTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.MsgTypeEnum;
import cn.worldwalker.game.wyqp.common.exception.BusinessException;
import cn.worldwalker.game.wyqp.common.exception.ExceptionEnum;
import cn.worldwalker.game.wyqp.common.result.Result;
import cn.worldwalker.game.wyqp.common.service.BaseGameService;
import cn.worldwalker.game.wyqp.common.utils.GameUtil;
import cn.worldwalker.game.wyqp.common.utils.HttpClientUtils;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;
import cn.worldwalker.game.wyqp.jh.service.JhGameService;
import cn.worldwalker.game.wyqp.mj.service.MjGameService;
import cn.worldwalker.game.wyqp.nn.service.NnGameService;

@Service(value="commonGameService")
public class CommonGameService extends BaseGameService{
	
	private static final Logger log = Logger.getLogger(CommonGameService.class);
	@Autowired
	private NnGameService nnGameService;
	
	@Autowired
	private MjGameService mjGameService;
	
	@Autowired
	private JhGameService jhGameService;
	
	public void commonEntryRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Integer roomId = request.getMsg().getRoomId();
		RedisRelaModel rrm = redisOperationService.getGameTypeUpdateTimeByRoomId(roomId);
		Integer realGameType = rrm.getGameType();
		/**设置真是的gameType*/
		request.setGameType(realGameType);
		GameTypeEnum gameTypeEnum = GameTypeEnum.getGameTypeEnumByType(realGameType);
		switch (gameTypeEnum) {
			case nn:
				nnGameService.entryRoom(ctx, request, userInfo);
				break;
			case mj:
				mjGameService.entryRoom(ctx, request, userInfo);
				break;
			case jh:
				jhGameService.entryRoom(ctx, request, userInfo);
				break;
			default:
				break;
			}
	}
	
	
	public void commonRefreshRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Integer roomId = userInfo.getRoomId();
		if (roomId == null) {
			channelContainer.sendTextMsgByPlayerIds(new Result(0, MsgTypeEnum.entryHall.msgType), userInfo.getPlayerId());
			return;
		}
		RedisRelaModel rrm = redisOperationService.getGameTypeUpdateTimeByRoomId(roomId);
		/**如果为null，则说明可能是解散房间后，玩家的userInfo里面的roomId没有清空，需要清空掉*/
		if (rrm == null) {
			userInfo.setRoomId(null);
			redisOperationService.setUserInfo(request.getToken(), userInfo);
			channelContainer.sendTextMsgByPlayerIds(new Result(0, MsgTypeEnum.entryHall.msgType), userInfo.getPlayerId());
			return;
		}
		Integer realGameType = rrm.getGameType();
		/**设置真是的gameType*/
		request.setGameType(realGameType);
		GameTypeEnum gameTypeEnum = GameTypeEnum.getGameTypeEnumByType(realGameType);
		switch (gameTypeEnum) {
			case nn:
				nnGameService.refreshRoom(ctx, request, userInfo);
				break;
			case mj:
				mjGameService.refreshRoom(ctx, request, userInfo);
				break;
			case jh:
				jhGameService.refreshRoom(ctx, request, userInfo);
				break;
			default:
				break;
			}
	}
	
	
	@Override
	public BaseRoomInfo doCreateRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		return null;
	}

	@Override
	public BaseRoomInfo doEntryRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		return null;
	}

	@Override
	public List<BaseRoomInfo> doRefreshRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		return null;
	}


	@Override
	public BaseRoomInfo getRoomInfo(ChannelHandlerContext ctx,
			BaseRequest request, UserInfo userInfo) {
		return null;
	}
	
	/**
	 * 发送短信
	 * @param token
	 * @param mobile
	 * @return
	 */
	public Result sendSms(String token, String mobile){
		if (StringUtils.isBlank(token) || StringUtils.isBlank(mobile)) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		UserInfo userInfo = redisOperationService.getUserInfo(token);
		if (userInfo == null) {
			throw new BusinessException(ExceptionEnum.NEED_LOGIN);
		}
		String smsContent = Constant.smsContent;
		String validCode = String.valueOf(GameUtil.genSmsValidCode());
		smsContent = smsContent.replace("CODE", validCode);
		Map<String, String> params = new HashMap<String, String>();
		params.put("account", Constant.smsAppId);
		params.put("password", Constant.smsApiKey);
		params.put("mobile", mobile);
		params.put("content", smsContent);
		params.put("format", "json");
		String httpRes = null;
		/**校验短信验证码是否正确*/
		try {
			httpRes = HttpClientUtils.postForm(Constant.sendSmsUrl, params, null, 10000, 10000);
		}catch (Exception e) {
			log.error(ExceptionEnum.SEND_SMS_ERROR.description, e);
			throw new BusinessException(ExceptionEnum.SEND_SMS_ERROR);
		}
		if (StringUtils.isBlank(httpRes)) {
			log.error("短息接口返回为空");
			throw new BusinessException(ExceptionEnum.SEND_SMS_ERROR);
		}
		
		
		SmsResModel model = JsonUtil.toObject(httpRes, SmsResModel.class);
		if (model.getCode() != 2) {
			log.error(model.getMsg());
			throw new BusinessException(ExceptionEnum.SEND_SMS_ERROR);
		}
		/**将手机号与短信验证码的关系设置到缓存，过期时间60s*/
		redisOperationService.setSmsMobileValideCodeTime(mobile, validCode);
		return new Result();
	}
	
	public static void main(String[] args) {
		String smsContent = Constant.smsContent;
		String validCode = String.valueOf(GameUtil.genSmsValidCode());
		smsContent = smsContent.replace("CODE", validCode);
		Map<String, String> params = new HashMap<String, String>();
		params.put("account", "C52003075");
		params.put("password", "53e37b61166a465381d5e1a2ed4fc7da");
		params.put("mobile", "13006339011");
		params.put("content", smsContent);
		params.put("format", "json");
		String httpRes = null;
		String url = "http://106.ihuyi.cn/webservice/sms.php?method=Submit";
		/**校验短信验证码是否正确*/
		try {
			httpRes = HttpClientUtils.postForm(url, params, null, 10000, 10000);
		}catch (Exception e) {
			log.error(ExceptionEnum.SEND_SMS_ERROR.description, e);
			throw new BusinessException(ExceptionEnum.SEND_SMS_ERROR);
		}
		if (StringUtils.isBlank(httpRes)) {
			log.error("短息接口返回为空");
			throw new BusinessException(ExceptionEnum.SEND_SMS_ERROR);
		}
		System.out.println(httpRes);
		
	}

	/**
	 * 绑定手机号
	 * @param token
	 * @param mobile
	 * @param validCode
	 * @return
	 */
	public Result bindMobile(String token, String mobile, String validCode){
		if (StringUtils.isBlank(token) || StringUtils.isBlank(mobile) || StringUtils.isBlank(validCode)) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		Result result = new Result();
		UserInfo userInfo = redisOperationService.getUserInfo(token);
		if (userInfo == null) {
			throw new BusinessException(ExceptionEnum.NEED_LOGIN);
		}
		String str = redisOperationService.getSmsMobileValideCodeTime(mobile);
		if (StringUtils.isBlank(str)) {
			throw new BusinessException(ExceptionEnum.SMS_CODE_ERROR);
		}
		/**校验验证码*/
		if (!validCode.equals(str.split("_")[0])) {
			throw new BusinessException(ExceptionEnum.SMS_CODE_ERROR);
		}
		/**绑定手机号*/
		UserModel userModel = new UserModel();
		userModel.setPlayerId(userInfo.getPlayerId());
		userModel.setMobile(mobile);
		userModel.setUpdateTime(new Date());
		commonManager.updateUserByPlayerId(userModel);
		/**绑定代理后送15张房卡*/
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("addNum", 1);
		param.put("playerId", userInfo.getPlayerId());
		commonManager.addRoomCard(param);
		userModel = commonManager.getUserById(userInfo.getPlayerId());
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("roomCardNum", userModel.getRoomCardNum());
		data.put("mobile", mobile);
		result.setData(data);
		return result;
	}
	
	
	public Result bindRealNameAndIdNo(String token, String realName, String idNo){
		if (StringUtils.isBlank(token) || StringUtils.isBlank(realName) || StringUtils.isBlank(idNo)) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		Result result = new Result();
		UserInfo userInfo = redisOperationService.getUserInfo(token);
		if (userInfo == null) {
			throw new BusinessException(ExceptionEnum.NEED_LOGIN);
		}
		/**实名认证*/
		UserModel userModel = new UserModel();
		userModel.setPlayerId(userInfo.getPlayerId());
		userModel.setRealName(realName);
		userModel.setIdNo(idNo);
		userModel.setUpdateTime(new Date());
		commonManager.updateUserByPlayerId(userModel);
		return result;
	}
	
	public Result obtainRoomCardByExtensionCode(Integer extensionCode, String token){
		if (StringUtils.isBlank(token) || extensionCode == null) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		Result result = new Result();
		UserInfo userInfo = redisOperationService.getUserInfo(token);
		if (userInfo == null) {
			throw new BusinessException(ExceptionEnum.NEED_LOGIN);
		}
		/**校验激活码是否正确*/
		UserModel model = commonManager.getUserByExtensionCode(extensionCode);
		if (model == null) {
			throw new BusinessException(ExceptionEnum.EXTENSION_CODE_ERROR);
		}
		/**校验当前玩家是否已经绑定了激活码*/
		ExtensionCodeBindModel bindModel = commonManager.getExtensionCodeBindLogByPlayerId(userInfo.getPlayerId());
		if (bindModel != null) {
			throw new BusinessException(ExceptionEnum.HAS_BIND_EXTENSINO_CODE);
		}
		/**插入绑定激活码日志*/
		commonManager.insertExtensionCodeBindLog(extensionCode, userInfo.getPlayerId(), userInfo.getNickName());
		/**绑定激活码的玩家领取房卡*/
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("addNum", 1);
		param.put("playerId", userInfo.getPlayerId());
		commonManager.addRoomCard(param);
		/**推广的玩家获取房卡*/
		param.put("playerId", model.getPlayerId());
		commonManager.addRoomCard(param);
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("roomCardNum", userInfo.getRoomCardNum() + 1);
		result.setData(data);
		return result;
	}
	
}
