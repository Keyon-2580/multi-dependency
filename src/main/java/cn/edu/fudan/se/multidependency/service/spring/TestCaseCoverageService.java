package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.Graph;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;
import cn.edu.fudan.se.multidependency.utils.GraphUtil;

@Service
public class TestCaseCoverageService {
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;
	
	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	public FunctionCallPropertion findFunctionCallFunctionDynamicCalled(TestCase testCase) {
		return findFunctionCallFunctionDynamicCalled(testCase, null);
	}
	
	public FunctionCallPropertion findFunctionCallFunctionDynamicCalled(TestCase testCase, Project project) {
		List<TestCase> list = new ArrayList<>();
		list.add(testCase);
		return findFunctionCallFunctionDynamicCalled(list, project);
	}
	
	public FunctionCallPropertion findFunctionCallFunctionDynamicCalled(List<TestCase> testCases) {
		return findFunctionCallFunctionDynamicCalled(testCases, null);
	}

	public FunctionCallPropertion findFunctionCallFunctionDynamicCalled(List<TestCase> testCases, Project project) {
		System.out.println("findFunctionCallFunctionDynamicCalled " + testCases.size());
		// 所有静态调用
		Map<Function, List<FunctionCallFunction>> staticCalls = staticAnalyseService.findAllFunctionCallRelationsGroupByCaller();
		// 被动态调用的静态调用
		Map<Function, Map<Function, FunctionCallPropertionDetail>> dynamicCalls = dynamicAnalyseService.findFunctionCallFunctionDynamicCalled(testCases);
		if(project != null) {
			Set<Function> key = new HashSet<>(staticCalls.keySet());
			for(Function f : key) {
				Project fBelongToProject = staticAnalyseService.findFunctionBelongToProject(f);
				if(!fBelongToProject.equals(project)) {
					staticCalls.remove(f);
					dynamicCalls.remove(f);
				}
			}
		}
		
		FunctionCallPropertion propertion = new FunctionCallPropertion(staticCalls, dynamicCalls);
		System.out.println(propertion.coverageOfDynamicCalls());
		return propertion;
	}
	
	public Graph extractSameGraphForMicroServiceCall(TestCase testCase1, TestCase testCase2) {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls1 = featureOrganizationService.findMsCallMsByTestCases(testCase1).getCalls();
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls2 = featureOrganizationService.findMsCallMsByTestCases(testCase2).getCalls();
		Graph graph1 = generageMSCallMSGraph(calls1);
		System.out.println(graph1.toString());
		Graph graph2 = generageMSCallMSGraph(calls2);
		System.out.println(graph2.toString());
		Graph same = GraphUtil.sameSubGraphBetweenGraphsWithSameRelationExcludeRelationProperty(graph1, graph2, RelationType.MICROSERVICE_CALL_MICROSERVICE);
		return same;
	}
	
	private Graph generageMSCallMSGraph(Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls) {
		Graph result = new Graph();
		for(MicroService caller : calls.keySet()) {
			for(MicroService called : calls.get(caller).keySet()) {
				result.addNode(caller, caller.getName());
				result.addNode(called, called.getName());
				MicroServiceCallMicroService call = calls.get(caller).get(called);
				result.addEdge(call);
			}
		}
		return result;
	}
}