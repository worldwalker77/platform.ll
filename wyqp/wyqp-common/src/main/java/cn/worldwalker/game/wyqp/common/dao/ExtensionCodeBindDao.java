package cn.worldwalker.game.wyqp.common.dao;

import java.util.List;

import cn.worldwalker.game.wyqp.common.domain.base.ExtensionCodeBindModel;

public interface ExtensionCodeBindDao {
	
	 public Integer insertExtensionCodeBindLog(ExtensionCodeBindModel model);
	 
	 public List<ExtensionCodeBindModel> getExtensionCodeBindLogByCondition(ExtensionCodeBindModel model);
	 
	 
}
