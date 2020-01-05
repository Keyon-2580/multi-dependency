package cn.edu.fudan.se.multidependency.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.utils.JavaDynamicUtil.DynamicFunctionExecutionFromKieker;

public class DynamicUtilTest {
	@Test
	public void testSplitFunctionCall() {
		String sentence = "$2;1578203476880044200;1578203476880043000;-2810437662891048949;5;private void depends.entity.Entity.deduceQualifiedName();depends.entity.TypeEntity;public boolean depends.entity.GenericName.startsWith(java.lang.String);depends.entity.GenericName";
		JavaDynamicUtil.splitFunctionCall(sentence);
		sentence = "$1;1578203476879979400;-2810437662891048949;1;<no-session-id>;DESKTOP-4RF3KHM;-2810437662891048949;-1";
		JavaDynamicUtil.splitFunctionCall(sentence);
		sentence = "$2;1578203476880273900;1578203476880271900;-2810437662891048947;0;static depends.relations.Inferer.<clinit>;depends.relations.Inferer;public static java.lang.Integer java.lang.Integer.valueOf(int);java.lang.Integer";
		JavaDynamicUtil.splitFunctionCall(sentence);
	}

	@Test
	public void test() {
		Map<String, Map<Integer, List<DynamicFunctionExecutionFromKieker>>> result = JavaDynamicUtil.readKiekerExecutionFile(new File("src/test/resources/kieker/kieker-test.dat"));
		assertEquals(result.size(), 1);
		int size = 0;
		for(Map<Integer, List<DynamicFunctionExecutionFromKieker>> groups : result.values()) {
			for(List<DynamicFunctionExecutionFromKieker> group : groups.values()) {
				size += group.size();
			}
		}
		assertEquals(19, size);
	}
	
	@Test
	public void testTestCase() {
		String test = "TestCase success";
		TestCase testCase = JavaDynamicUtil.extractTestCaseFromMarkLine(test);
		assertTrue(testCase.isSuccess());
	}

}
