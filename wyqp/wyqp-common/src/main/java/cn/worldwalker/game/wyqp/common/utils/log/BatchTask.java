package cn.worldwalker.game.wyqp.common.utils.log;

import java.util.List;

public interface BatchTask<T> {
	public void doBatchProcess(List<T> list);
}
