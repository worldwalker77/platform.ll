package cn.worldwalker.game.wyqp.common.backend;

import java.math.BigDecimal;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.util.StringUtils;
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class GameModel {
	/**代理id*/
	private Integer proxyId;
	/**游戏昵称*/
	private String nickName;
	/**游戏id*/
	private Integer playerId;
	/**手机号*/
	private String mobilePhone;
	/**微信号*/
	private String wechatNum;
	/**微信号*/
	private String realName;
	/**提现前金额*/
	private double beforeWithdrawalAmount;
	/**提现金额*/
	private double withdrawalAmount;
	/**总提成*/
	private String extractAmount;
	/**账户余额*/
	private String remainderAmount;
	/**累计收益*/
	private String totalIncome;
	/**密码*/
	private String password;
	
	private String wxPayPrice;
	/**时间*/
	private String createTime;
	
	private Integer roomCardNum;
	
	private Integer totalRoomCardNum;
	/**概率控制*/
	private Integer winProbability;
	
	private Integer clubId;
	
	private Integer status;
	
	private Integer type;
	
	private String clubName;
	
	private String clubOwnerWord;
	
	private Integer onlineStatus;
	
	private String headImgUrl;
	
	public Integer getTotalRoomCardNum() {
		return totalRoomCardNum;
	}
	public void setTotalRoomCardNum(Integer totalRoomCardNum) {
		this.totalRoomCardNum = totalRoomCardNum;
	}
	public String getHeadImgUrl() {
		return headImgUrl;
	}
	public void setHeadImgUrl(String headImgUrl) {
		this.headImgUrl = headImgUrl;
	}
	public Integer getOnlineStatus() {
		return onlineStatus;
	}
	public void setOnlineStatus(Integer onlineStatus) {
		this.onlineStatus = onlineStatus;
	}
	public String getClubName() {
		return clubName;
	}
	public void setClubName(String clubName) {
		this.clubName = clubName;
	}
	public String getClubOwnerWord() {
		return clubOwnerWord;
	}
	public void setClubOwnerWord(String clubOwnerWord) {
		this.clubOwnerWord = clubOwnerWord;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getClubId() {
		return clubId;
	}
	public void setClubId(Integer clubId) {
		this.clubId = clubId;
	}
	public Integer getWinProbability() {
		return winProbability;
	}
	public void setWinProbability(Integer winProbability) {
		this.winProbability = winProbability;
	}
	public Integer getPlayerId() {
		return playerId;
	}
	public void setPlayerId(Integer playerId) {
		this.playerId = playerId;
	}
	public String getMobilePhone() {
		return mobilePhone;
	}
	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getWechatNum() {
		return wechatNum;
	}
	public void setWechatNum(String wechatNum) {
		this.wechatNum = wechatNum;
	}
	public Integer getProxyId() {
		return proxyId;
	}
	public void setProxyId(Integer proxyId) {
		this.proxyId = proxyId;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public double getBeforeWithdrawalAmount() {
		return beforeWithdrawalAmount;
	}
	public void setBeforeWithdrawalAmount(double beforeWithdrawalAmount) {
		this.beforeWithdrawalAmount = beforeWithdrawalAmount;
	}
	public double getWithdrawalAmount() {
		return withdrawalAmount;
	}
	public void setWithdrawalAmount(double withdrawalAmount) {
		this.withdrawalAmount = withdrawalAmount;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getCreateTime() {
		if (StringUtils.isEmpty(createTime)) {
			return null;
		}
		return createTime.substring(0, createTime.length() - 2);
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getWxPayPrice() {
		if (StringUtils.isEmpty(wxPayPrice)) {
			return null;
		}
		return BigDecimal.valueOf(Long.valueOf(wxPayPrice)).divide(new BigDecimal(100)).toString();
	}
	public void setWxPayPrice(String wxPayPrice) {
		this.wxPayPrice = wxPayPrice;
	}
	public String getTotalIncome() {
		if (StringUtils.isEmpty(totalIncome)) {
			return null;
		}
		return BigDecimal.valueOf(Long.valueOf(totalIncome)).divide(new BigDecimal(100)).toString();
	}
	public void setTotalIncome(String totalIncome) {
		this.totalIncome = totalIncome;
	}
	public String getExtractAmount() {
		if (StringUtils.isEmpty(extractAmount)) {
			return null;
		}
		return BigDecimal.valueOf(Long.valueOf(extractAmount)).divide(new BigDecimal(100)).toString();
	}
	public void setExtractAmount(String extractAmount) {
		this.extractAmount = extractAmount;
	}
	public String getRemainderAmount() {
		if (StringUtils.isEmpty(remainderAmount)) {
			return null;
		}
		return BigDecimal.valueOf(Long.valueOf(remainderAmount)).divide(new BigDecimal(100)).toString();
	}
	public void setRemainderAmount(String remainderAmount) {
		this.remainderAmount = remainderAmount;
	}
	public Integer getRoomCardNum() {
		return roomCardNum;
	}
	public void setRoomCardNum(Integer roomCardNum) {
		this.roomCardNum = roomCardNum;
	}
	
}
