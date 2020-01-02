package cn.edu.fudan.se.multidependency.model.node.testcase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;

public class DynamicTestCaseToFunctionDependency {
	
	protected TestCase testCase;
	
	protected Map<Long, Function> functions = new HashMap<>();

	/**
	 * 函数id1
	 * 函数id2
	 * id1对id2的调用
	 */
	protected Map<Long, Map<Long, List<FunctionDynamicCallFunction>>> allFunctionsDependencies = new HashMap<>();

	public void addFunctionDynamicCallFunction(FunctionDynamicCallFunction call) {
		functions.put(call.getStartNodeGraphId(), call.getFunction());
		functions.put(call.getEndNodeGraphId(), call.getCallFunction());
		Map<Long, List<FunctionDynamicCallFunction>> functionDependencies = allFunctionsDependencies.get(call.getStartNodeGraphId());
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

	public Map<Long, Function> getFunctions() {
		return functions;
	}

	public Map<Long, Map<Long, List<FunctionDynamicCallFunction>>> getAllFunctionDependencies() {
		return new HashMap<>(allFunctionsDependencies);
	}

}
