package cn.worldwalker.game.wyqp.common.result;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import cn.worldwalker.game.wyqp.common.exception.ExceptionEnum;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Result implements Serializable{
	
	private static final long serialVersionUID = 1188275509308264519L;
	
	private int code = 0;
	private String desc;
	private int msgType;
	private int gameType;
	private Object data;
	private Long timeStamp;
	private Long uuid;
	
	public Result(int code, String desc, int msgType){
		this.code = code;
		this.desc = desc;
		this.msgType = msgType;
	}
	
	public Result(int code, String desc, int msgType, int gameType){
		this.code = code;
		this.desc = desc;
		this.msgType = msgType;
		this.gameType = gameType;
	}
	
	public Result(int gameType, int msgType, ExceptionEnum exceptionEnum){
		this.code = exceptionEnum.index;
		this.desc = exceptionEnum.description;
		this.msgType = msgType;
		this.gameType = gameType;
	}
	
	public Result(int gameType, int msgType){
		this.msgType = msgType;
		this.gameType = gameType;
	}
	
	public Result(){
		
	}
	
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public int getMsgType() {
		return msgType;
	}
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	public int getGameType() {
		return gameType;
	}
	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Long getUuid() {
		return uuid;
	}

	public void setUuid(Long uuid) {
		this.uuid = uuid;
	}

}
