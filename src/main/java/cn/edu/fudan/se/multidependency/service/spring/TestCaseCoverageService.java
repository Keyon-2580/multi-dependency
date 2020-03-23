package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;

@Service
public class TestCaseCoverageService {
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;
	
	public FunctionCallPropertion findFunctionCallFunctionNotDynamicCalled(TestCase testCase) {
		return findFunctionCallFunctionNotDynamicCalled(testCase, null);
	}
	
	public FunctionCallPropertion findFunctionCallFunctionNotDynamicCalled(TestCase testCase, Project project) {
		List<TestCase> list = new ArrayList<>();
		list.add(testCase);
		return findFunctionCallFunctionNotDynamicCalled(list, project);
	}
	
	public FunctionCallPropertion findFunctionCallFunctionNotDynamicCalled(List<TestCase> testCases) {
		return findFunctionCallFunctionNotDynamicCalled(testCases, null);
	}

	public FunctionCallPropertion findFunctionCallFunctionNotDynamicCalled(List<TestCase> testCases, Project project) {
		System.out.println("findFunctionCallFunctionNotDynamicCalled");
		// 所有静态调用
		Map<Function, List<FunctionCallFunction>> staticCalls = staticAnalyseService.findAllFunctionCallRelationsGroupByCaller();
		// 没有被动态调用的静态调用
		Map<Function, List<FunctionCallFunction>> notDynamicCalls = dynamicAnalyseService.findFunctionCallFunctionNotDynamicCalled(testCases);

		if(project != null) {
			Set<Function> key = new HashSet<>(staticCalls.keySet());
			for(Function f : key) {
				Project fBelongToProject = staticAnalyseService.findFunctionBelongToProject(f);
				if(!fBelongToProject.equals(project)) {
					staticCalls.remove(f);
					notDynamicCalls.remove(f);
				}
			}
		}
		
		FunctionCallPropertion propertion = new FunctionCallPropertion(staticCalls, notDynamicCalls);
		System.out.println(propertion.propertionOfNotDynamicCalls());
		return propertion;
	}
}
