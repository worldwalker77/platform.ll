package cn.worldwalker.game.wyqp.common.utils.log;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.worldwalker.game.wyqp.common.domain.base.PlayBackModel;
import cn.worldwalker.game.wyqp.common.manager.CommonManager;
import cn.worldwalker.game.wyqp.common.result.Result;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;

@Service
public class LogDataService implements BatchTask<Result> {
	
	public final static Log logger = LogFactory.getLog(LogDataService.class);
	@Autowired
	private CommonManager commonManager;
	@Override
	public void doBatchProcess(List<Result> list) {
		if (!CollectionUtils.isEmpty(list)) {
			List<PlayBackModel> modelList = new ArrayList<PlayBackModel>();
			try {
				for(Result result : list){
					PlayBackModel model = new PlayBackModel();
					model.setRecordDetailUuid(result.getUuid());
					model.setOperationTime(result.getTimeStamp());
					result.setUuid(null);
					result.setTimeStamp(null);
					model.setMsg(JsonUtil.toJson(result));
					modelList.add(model);
				}
				commonManager.batchInsertPlayBack(modelList);
			} catch (Exception e) {
				logger.error("批量插入回放日志异常，list:" + JsonUtil.toJson(list), e);
			}
		}
	}
	
}
