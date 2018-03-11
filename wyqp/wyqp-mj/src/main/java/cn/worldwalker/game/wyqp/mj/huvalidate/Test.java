package cn.worldwalker.game.wyqp.mj.huvalidate;

import java.util.ArrayList;
import java.util.List;


public class Test //extends TestCase
{
	public Test()
	{
		TableMgr.getInstance().load();
	}
	
	public void test()
	{
		
	}
	public static void main(String[] args) {
		Test t = new Test();
		t.testOne();
	}
	public void testOne()
	{
		int guiIndex = 100;
		int[] cards = { 
			0, 0, 0, 1, 1, 1, 0, 0, 0, /* 0-8表示1-9万 */ 
			1, 1, 1, 0, 0, 0, 0, 0, 0, /* 9-17表示1-9筒 */
			2, 0, 0, 0, 0, 0, 0, 0, 0, /* 18-26表示1-9条 */
			4, 2, 0, 0, 0, 0, 0 ,//27-33表示东南西北中发白
			0, 0, 0, 0, 0, 0, 0 ,0//34-41表示春夏秋冬梅兰竹菊
		};
//
//		System.out.println("测试1种,癞子:" + guiIndex);
//		Program.print_cards(cards);
//		System.out.println(Hulib.getInstance().get_hu_info(cards, 34, guiIndex));
		List<Integer> list = new ArrayList<Integer>();
		list.add(3);
		list.add(4);
		list.add(5);
//		list.add(9);
//		list.add(10);
//		list.add(11);
//		list.add(18);
//		list.add(18);
//		list.add(18);
//		list.add(27);
//		list.add(27);
//		list.add(27);
//		list.add(32);
//		list.add(32);
		System.out.println(Hulib.getInstance().get_hu_info(list, 100, 100,34));
	}
}
