package cn.edu.fudan.se.multidependency.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class JavaDynamicUtilTest {
	@Test
	public void testFind() {
		List<Long> datas = new ArrayList<>();
//		datas.add(0L);
		datas.add(1L);
		datas.add(3L);
//		datas.add(4L);
		System.out.println(JavaDynamicUtil.find(2L, datas));
	}

}
