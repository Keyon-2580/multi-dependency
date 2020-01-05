package cn.edu.fudan.se.multidependency.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import cn.edu.fudan.se.multidependency.utils.DynamicUtil.DynamicFunctionExecutionFromKieker;

public class DynamicUtilTest {

	@Test
	public void test() {
		Map<String, Map<Integer, List<DynamicFunctionExecutionFromKieker>>> result = DynamicUtil.readKiekerFile(new File("src/test/resources/kieker/kieker-test.dat"));
		assertEquals(result.size(), 1);
		int size = 0;
		for(Map<Integer, List<DynamicFunctionExecutionFromKieker>> groups : result.values()) {
			for(List<DynamicFunctionExecutionFromKieker> group : groups.values()) {
				size += group.size();
			}
		}
		assertEquals(19, size);
	}

}
