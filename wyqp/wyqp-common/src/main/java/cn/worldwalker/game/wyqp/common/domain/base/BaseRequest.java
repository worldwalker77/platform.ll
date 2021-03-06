package cn.worldwalker.game.wyqp.common.domain.base;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class BaseRequest {
	
	private Integer msgType;
	
	private String token;
	
	private Integer gameType;
	
	private Integer detailType;
	
	private BaseMsg msg;
	
	public Integer getDetailType() {
		return detailType;
	}
	public void setDetailType(Integer detailType) {
		this.detailType = detailType;
	}
	public Integer getMsgType() {
		return msgType;
	}
	public void setMsgType(Integer msgType) {
		this.msgType = msgType;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Integer getGameType() {
		return gameType;
	}
	public void setGameType(Integer gameType) {
		this.gameType = gameType;
	}
	public BaseMsg getMsg() {
		return msg;
	}
	public void setMsg(BaseMsg msg) {
		this.msg = msg;
	}
	
}
