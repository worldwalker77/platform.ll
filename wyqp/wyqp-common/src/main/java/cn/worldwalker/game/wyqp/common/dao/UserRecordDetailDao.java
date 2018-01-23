package cn.worldwalker.game.wyqp.common.dao;

import java.util.List;

import cn.worldwalker.game.wyqp.common.domain.base.UserRecordModel;

public interface UserRecordDetailDao {
	
	public long insertRecordDetail(UserRecordModel model);
	
	public long batchInsertRecordDetail(List<UserRecordModel> modelList);
	
	public List<UserRecordModel> getUserRecordDetail(UserRecordModel model);
	
}
