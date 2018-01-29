package cn.worldwalker.game.wyqp.common.domain.base;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.std.ToStringSerializer;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class PlayBackModel {
	
	private Long id;
	@JsonSerialize(using= ToStringSerializer.class)
	private Long recordDetailUuid;
	
	private String msg;
	
	private Long operationTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRecordDetailUuid() {
		return recordDetailUuid;
	}

	public void setRecordDetailUuid(Long recordDetailUuid) {
		this.recordDetailUuid = recordDetailUuid;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Long getOperationTime() {
		return operationTime;
	}

	public void setOperationTime(Long operationTime) {
		this.operationTime = operationTime;
	}
	

}
