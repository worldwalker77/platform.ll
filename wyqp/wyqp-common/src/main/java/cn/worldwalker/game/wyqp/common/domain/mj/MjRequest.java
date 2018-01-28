package cn.worldwalker.game.wyqp.common.domain.mj;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import cn.worldwalker.game.wyqp.common.domain.base.BaseRequest;
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class MjRequest extends BaseRequest{
	
	private MjMsg msg = new MjMsg();

	public MjMsg getMsg() {
		return msg;
	}

	public void setMsg(MjMsg msg) {
		this.msg = msg;
	}
	
	
}
