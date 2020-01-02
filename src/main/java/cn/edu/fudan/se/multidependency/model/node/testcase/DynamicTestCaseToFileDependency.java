package cn.edu.fudan.se.multidependency.model.node.testcase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;

public class DynamicTestCaseToFileDependency {
	
	private TestCase testCase;

	private Map<Long, ProjectFile> projectFiles = new HashMap<>();
	
	/**
	 * 文件id1
	 * 文件id2
	 * id1对id2中存在哪些函数依赖
	 */
	private Map<Long, Map<Long, List<FunctionDynamicCallFunction>>> allFilesDependencies = new HashMap<>();
	
	public void addFunctionDynamicCallFunction(FunctionDynamicCallFunction call, ProjectFile startFile, ProjectFile endFile) {
		projectFiles.put(startFile.getId(), startFile);
		projectFiles.put(endFile.getId(), endFile);
		
		Map<Long, List<FunctionDynamicCallFunction>> functionDependencies = allFilesDependencies.get(call.getStartNodeGraphId());
		functionDependencies = functionDependencies == null ? new HashMap<>() : functionDependencies;
		List<FunctionDynamicCallFunction> calls = functionDependencies.get(call.getEndNodeGraphId());
		calls = calls == null ? new ArrayList<>() : calls;
		if(!calls.contains(call)) {
			calls.add(call);
		}
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}

	public Map<Long, ProjectFile> getProjectFiles() {
		return new HashMap<>(projectFiles);
	}
	
	public Map<Long, Map<Long, List<FunctionDynamicCallFunction>>> getAllFilesDependencies() {
		return allFilesDependencies;
	}

}
