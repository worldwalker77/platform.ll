package cn.worldwalker.game.wyqp.common.domain.base;

import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonSerialize;
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class UserModel {
	
	private Integer playerId;
	
	private String nickName;
	
	private String headImgUrl;
	
	private Integer userLevel;
	
	private String wxOpenId;
	
	private String unionid;
	
	private Integer roomCardNum;
	
	private String mobile;
	
	private Integer sex;
	
	private String realName;
	
	private String idNo;
	
	private Date createtime;
	
	private Date updateTime;

	private Integer winProbability = 0;
	
	private Integer extensionCode;
	
	public Integer getExtensionCode() {
		return extensionCode;
	}

	public void setExtensionCode(Integer extensionCode) {
		this.extensionCode = extensionCode;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getIdNo() {
		return idNo;
	}

	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
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

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getHeadImgUrl() {
		return headImgUrl;
	}

	public void setHeadImgUrl(String headImgUrl) {
		this.headImgUrl = headImgUrl;
	}

	public Integer getUserLevel() {
		return userLevel;
	}

	public void setUserLevel(Integer userLevel) {
		this.userLevel = userLevel;
	}

	public String getWxOpenId() {
		return wxOpenId;
	}

	public void setWxOpenId(String wxOpenId) {
		this.wxOpenId = wxOpenId;
	}

	public Integer getRoomCardNum() {
		return roomCardNum;
	}

	public void setRoomCardNum(Integer roomCardNum) {
		this.roomCardNum = roomCardNum;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getUnionid() {
		return unionid;
	}

	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}
	
}
