package cn.worldwalker.game.wyqp.common.dao;

import java.util.List;

import cn.worldwalker.game.wyqp.common.domain.base.PlayBackModel;

public interface RecordPlayBackDao {
	
	public long insertPlayBack(PlayBackModel model);
	
	public long batchInsertPlayBack(List<PlayBackModel> modelList);
	
	public List<String> getPlayBack(PlayBackModel model);
	
}
