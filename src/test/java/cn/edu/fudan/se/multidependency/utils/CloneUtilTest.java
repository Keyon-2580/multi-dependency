package cn.edu.fudan.se.multidependency.utils;

import org.junit.Test;

import cn.edu.fudan.se.multidependency.utils.CloneUtil.CloneResultFromCsv;

public class CloneUtilTest {

	@Test
	public void test() {
		int count = 0;
		try {
			Iterable<CloneResultFromCsv> result = CloneUtil.readCloneResultCsv("src/test/resources/clone/type123_method_result.csv");
			for(CloneResultFromCsv clone : result) {
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			count = 0;
		}
		assert(count > 0);
	}

}
