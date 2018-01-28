package cn.worldwalker.game.wyqp.common.domain.base;

import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonSerialize;
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ExtensionCodeBindModel {
	
	private Integer playerId;
	
	private String nickName;
	private Date createtime;
	
	private Date updateTime;
	private Date createTime;
	private Integer extensionCode;

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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

	public Integer getExtensionCode() {
		return extensionCode;
	}

	public void setExtensionCode(Integer extensionCode) {
		this.extensionCode = extensionCode;
	}
	
	
}
