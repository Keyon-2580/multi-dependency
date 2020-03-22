package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;

@Service
public class TestCaseCoverageService {
	
	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;
	
	public void findFunctionCallFunctionNotDynamicCalled() {
		System.out.println("findFunctionCallFunctionNotDynamicCalled");
		// 所有静态调用
		Map<Function, List<FunctionCallFunction>> staticCalls = staticAnalyseService.findAllFunctionCallRelationsGroupByCaller();
		
		// 没有被动态调用的静态调用
		Map<Function, List<FunctionCallFunction>> notDynamicCalls 
			= dynamicAnalyseService.findFunctionCallFunctionNotDynamicCalled(null);
		Iterable<TestCase> testcases = dynamicAnalyseService.findAllTestCases();
//		Map<Function, List<FunctionCallFunction>> notDynamicCalls 
//		= dynamicAnalyseService.findFunctionCallFunctionNotDynamicCalled(true, testcases);
	
//		0.8917326149183364
//		0.6334459459459459
//		for(Function caller : staticCalls.keySet()) {
//			List<FunctionCallFunction> staticCall = staticCalls.get(caller);
//			List<FunctionCallFunction> notDynamicCall = notDynamicCalls.get(caller);
////			System.out.println(caller.getFunctionName() + " " + notDynamicCall);
//		}
		
		for(Function caller : notDynamicCalls.keySet()) {
			List<FunctionCallFunction> calls = notDynamicCalls.get(caller);
			for(FunctionCallFunction call : calls) {
//				System.out.println(call.getFunction().getFunctionName() + " " + call.getCallFunction().getFunctionName());
			}
		}
		
		FunctionCallPropertion propertion = new FunctionCallPropertion(staticCalls, notDynamicCalls);
		
		System.out.println(propertion.propertionOfNotDynamicCalls());
		
	}
	
	public FunctionCallPropertion findFunctionCallFunctionNotDynamicCalled(TestCase... testCases) {
		List<TestCase> list = new ArrayList<>();
		for(TestCase testCase : testCases) {
			list.add(testCase);
		}
		return findFunctionCallFunctionNotDynamicCalled(list);
	}
	
	public FunctionCallPropertion findFunctionCallFunctionNotDynamicCalled(List<TestCase> testCases) {
		System.out.println("findFunctionCallFunctionNotDynamicCalled");
		// 所有静态调用
		Map<Function, List<FunctionCallFunction>> staticCalls = staticAnalyseService.findAllFunctionCallRelationsGroupByCaller();
		// 没有被动态调用的静态调用
		Map<Function, List<FunctionCallFunction>> notDynamicCalls 
			= dynamicAnalyseService.findFunctionCallFunctionNotDynamicCalled(testCases);
	
		FunctionCallPropertion propertion = new FunctionCallPropertion(staticCalls, notDynamicCalls);

		System.out.println(propertion.propertionOfNotDynamicCalls());
		return propertion;
	}

}
