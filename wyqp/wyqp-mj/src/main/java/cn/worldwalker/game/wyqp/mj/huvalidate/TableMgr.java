package cn.worldwalker.game.wyqp.mj.huvalidate;

import cn.worldwalker.game.wyqp.common.constant.Constant;

public class TableMgr {
	public static TableMgr mgr = new TableMgr();
	public SetTable[] m_check_table = new SetTable[9];
	public SetTable[] m_check_eye_table = new SetTable[9];
	public SetTable[] m_check_feng_table = new SetTable[9];
	public SetTable[] m_check_feng_eye_table = new SetTable[9];

	public TableMgr() {
		for (int i = 0; i < 9; ++i) {
			m_check_table[i] = new SetTable();
			m_check_eye_table[i] = new SetTable();
			m_check_feng_table[i] = new SetTable();
			m_check_feng_eye_table[i] = new SetTable();
		}
	}

	public static TableMgr getInstance() {
		return mgr;
	}
	/**
	 * 校验单一花色是否满足胡牌条件
	 * @param key 单一花色手牌key
	 * @param gui_num 鬼牌数量
	 * @param eye 是否带将
	 * @param chi 是否风
	 * @return
	 */
	public boolean check(int key, int gui_num, boolean eye, boolean chi) {
		SetTable tbl;

		if (chi) {
			if (eye) {
				tbl = m_check_eye_table[gui_num];
			} else {
				tbl = m_check_table[gui_num];
			}
		} else {
			if (eye) {
				tbl = m_check_feng_eye_table[gui_num];
			} else {
				tbl = m_check_feng_table[gui_num];
			}
		}

		return tbl.check(key);
	}

	public void add(int key, int gui_num, boolean eye, boolean chi) {
		SetTable tbl;

		if (chi) {
			if (eye) {
				tbl = m_check_eye_table[gui_num];
			} else {
				tbl = m_check_table[gui_num];
			}
		} else {
			if (eye) {
				tbl = m_check_feng_eye_table[gui_num];
			} else {
				tbl = m_check_feng_table[gui_num];
			}
		}

		tbl.add(key);
	}

	public boolean load() {
		for (int i = 0; i < 9; ++i) {
			String path = Constant.mjTablePath + "table_";
			m_check_table[i].load(path + i + ".tbl");
		}

		for (int i = 0; i < 9; ++i) {
			String path = Constant.mjTablePath + "eye_table_";
			m_check_eye_table[i].load(path + i + ".tbl");
		}

		for (int i = 0; i < 9; ++i) {
			String path = Constant.mjTablePath + "feng_table_";
			m_check_feng_table[i].load(path + i + ".tbl");
		}

		for (int i = 0; i < 9; ++i) {
			String path = Constant.mjTablePath + "feng_eye_table_";
			m_check_feng_eye_table[i].load(path + i + ".tbl");
		}
		return true;
	}

	public boolean dump_table() {
		for (int i = 0; i < 9; ++i) {
			String path = Constant.mjTablePath + "table_";
			m_check_table[i].dump(path + i + ".tbl");
		}

		for (int i = 0; i < 9; ++i) {
			String path = Constant.mjTablePath + "eye_table_";
			m_check_eye_table[i].dump(path + i + ".tbl");
		}
		return true;
	}

	public boolean dump_feng_table() {
		for (int i = 0; i < 9; ++i) {
			String path = Constant.mjTablePath + "feng_table_";
			m_check_feng_table[i].dump(path + i + ".tbl");
		}

		for (int i = 0; i < 9; ++i) {
			String path = Constant.mjTablePath + "feng_eye_table_";
			m_check_feng_eye_table[i].dump(path + i + ".tbl");
		}

		return true;
	}
}
