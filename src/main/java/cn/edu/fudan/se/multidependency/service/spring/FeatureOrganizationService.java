package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanCallSpan;
import edu.emory.mathcs.backport.java.util.Arrays;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FeatureOrganizationService {
	
	private final Map<String, MicroService> allMicroService;
	private final Map<TestCase, List<TestCaseExecuteFeature>> testCaseExecuteFeatures;
	private final Map<Feature, List<TestCaseExecuteFeature>> featureExecutedByTestCases;
	private final Map<Feature, Feature> featureToParentFeature;
	private final Map<TestCase, List<TestCaseRunTrace>> testCaseRunTraces;
	private final Map<Trace, List<Span>> traceToSpans;
	private final Map<Span, List<SpanCallSpan>> spanCallSpans;
	private final Map<Span, MicroServiceCreateSpan> spanBelongToMicroService;
	
	public List<Span> relatedSpan(Iterable<TestCase> testCases) {
		List<Span> result = new ArrayList<>();
		for(TestCase testCase : testCases) {
			List<TestCaseRunTrace> runs = testCaseRunTraces.getOrDefault(testCase, new ArrayList<>());
			for(TestCaseRunTrace run : runs) {
				List<Span> spans = traceToSpans.getOrDefault(run.getTrace(), new ArrayList<>());
				result.addAll(spans);
			}
		}
		return result;
	}
	
	public MicroService spanBelongToMicroservice(Span span) {
		try {
			return spanBelongToMicroService.get(span).getMicroservice();
		} catch (Exception e) {
		}
		return null;
	}
	
	public JSONObject featureExecuteTestCasesToCytoscape() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		
		for(Feature feature : allFeatures()) {
			JSONObject featureData = new JSONObject();
			featureData.put("id", feature.getId());
			featureData.put("name", feature.getFeatureName());
			featureData.put("type", "feature");
			featureData.put("value", feature.getFeatureId() + ":" + feature.getFeatureName());
			JSONObject featureNode = new JSONObject();
			featureNode.put("data", featureData);
			nodes.add(featureNode);
			
			List<TestCaseExecuteFeature> executes = featureExecutedByTestCases.get(feature);
			for(TestCaseExecuteFeature execute : executes) {
				TestCase testcase = execute.getTestCase();
				JSONObject testcaseData = new JSONObject();
				testcaseData.put("id", testcase.getId());
				testcaseData.put("name", testcase.getTestCaseName());
				testcaseData.put("type", "testcase");
				testcaseData.put("value", testcase.getTestCaseId() + ":" + testcase.getTestCaseName());
				JSONObject testcaseNode = new JSONObject();
				testcaseNode.put("data", testcaseData);
				nodes.add(testcaseNode);
				
				JSONObject executeData = new JSONObject();
				executeData.put("id", testcase.getId() + "_" + feature.getId());
				executeData.put("source", testcase.getId());
				executeData.put("target", feature.getId());
				JSONObject executeEdge = new JSONObject();
				executeEdge.put("data", executeData);
				edges.add(executeEdge);
			}
		}
		
		for(Feature feature : featureToParentFeature.keySet()) {
			Feature parentFeature = featureToParentFeature.get(feature);
			JSONObject containFeature = new JSONObject();
			containFeature.put("id", parentFeature.getId() + "_" + feature.getId());
			containFeature.put("source", parentFeature.getId());
			containFeature.put("target", feature.getId());
			containFeature.put("value", "contain");
			JSONObject containEdge = new JSONObject();
			containEdge.put("data", containFeature);
			edges.add(containEdge);
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONArray testcaseExecuteFeaturesToTreeView() {
		JSONArray result = new JSONArray();
		Iterable<TestCase> testcases = allTestCases();
		for(TestCase testcase : testcases) {
			List<TestCaseExecuteFeature> executes = testCaseExecuteFeatures.get(testcase);
			JSONObject testcaseJson = new JSONObject();
			testcaseJson.put("text", testcase.getTestCaseId() + ":" + testcase.getTestCaseName());
			JSONArray tags = new JSONArray();
			tags.add("testcase");
			testcaseJson.put("tags", tags);
			testcaseJson.put("href", testcase.getId());
			
			JSONArray testcaseNodes = new JSONArray();
			for(TestCaseExecuteFeature execute : executes) {
				JSONObject featureJson = new JSONObject();
				featureJson.put("text", execute.getFeature().getFeatureId() + ":" + execute.getFeature().getFeatureName());
				tags = new JSONArray();
				tags.add("feature");
				featureJson.put("tags", tags);
				featureJson.put("href", execute.getFeature().getId());
				testcaseNodes.add(featureJson);
			}
			
			List<TestCaseRunTrace> runs = testCaseRunTraces.get(testcase);
			for(TestCaseRunTrace run : runs) {
				JSONObject traceJson = new JSONObject();
				Trace trace = run.getTrace();
				traceJson.put("text", trace.getTraceId());
				tags = new JSONArray();
				tags.add("trace");
				traceJson.put("tags", tags);
				traceJson.put("href", trace.getId());
				
				JSONArray microservices = new JSONArray();
				for(MicroService ms : findRelatedMicroServiceForTraces(trace)) {
					JSONObject msJson = new JSONObject();
					msJson.put("text", ms.getName());
					tags = new JSONArray();
					tags.add("microservice");
					msJson.put("tags", tags);
					msJson.put("href", ms.getId());
					JSONArray spansArray = new JSONArray();
					for(Span span : findMicroServiceCreateSpansInTraces(ms, trace)) {
						JSONObject spanJson = new JSONObject();
						spanJson.put("text", span.getOperationName());
						tags = new JSONArray();
						tags.add("span");
						tags.add(span.getOrder());
						spanJson.put("tags", tags);
						spanJson.put("href", span.getId());
						spansArray.add(spanJson);
					}
					msJson.put("nodes", spansArray);
					
					microservices.add(msJson);
				}
				traceJson.put("nodes", microservices);
				
				testcaseNodes.add(traceJson);
			}
			
			testcaseJson.put("nodes", testcaseNodes);
			result.add(testcaseJson);
		}
		return result;
	}
	
	public JSONArray featureExecutedByTestCasesToTreeView() {
		JSONArray result = new JSONArray();
		Iterable<Feature> features = allFeatures();
		for(Feature feature : features) {
			List<TestCaseExecuteFeature> executes = featureExecutedByTestCases.get(feature);
			JSONObject featureJson = new JSONObject();
			featureJson.put("text", feature.getFeatureId() + ":" + feature.getFeatureName());
			JSONArray tags = new JSONArray();
			tags.add("feature");
			featureJson.put("tags", tags);
			featureJson.put("href", feature.getId());
			
			JSONArray testCases = new JSONArray();
			for(TestCaseExecuteFeature execute : executes) {
				JSONObject testCaseJson = new JSONObject();
				testCaseJson.put("text", execute.getTestCase().getTestCaseId() + ":" + execute.getTestCase().getTestCaseName());
				tags = new JSONArray();
				tags.add("testcase");
				testCaseJson.put("tags", tags);
				testCaseJson.put("href", execute.getTestCase().getId());
				
				List<TestCaseRunTrace> runs = testCaseRunTraces.get(execute.getTestCase());
				JSONArray traces = new JSONArray();
				for(TestCaseRunTrace run : runs) {
					JSONObject traceJson = new JSONObject();
					Trace trace = run.getTrace();
					traceJson.put("text", trace.getTraceId());
					tags = new JSONArray();
					tags.add("trace");
					traceJson.put("tags", tags);
					traceJson.put("href", trace.getId());
					
					JSONArray microservices = new JSONArray();
					for(MicroService ms : findRelatedMicroServiceForTraces(trace)) {
						JSONObject msJson = new JSONObject();
						msJson.put("text", ms.getName());
						tags = new JSONArray();
						tags.add("microservice");
						msJson.put("tags", tags);
						msJson.put("href", ms.getId());
						JSONArray spansArray = new JSONArray();
						for(Span span : findMicroServiceCreateSpansInTraces(ms, trace)) {
							JSONObject spanJson = new JSONObject();
							spanJson.put("text", span.getOperationName());
							tags = new JSONArray();
							tags.add("span");
							tags.add(span.getOrder());
							spanJson.put("tags", tags);
							spanJson.put("href", span.getId());
							spansArray.add(spanJson);
						}
						msJson.put("nodes", spansArray);
						
						microservices.add(msJson);
					}
					traceJson.put("nodes", microservices);
					
					traces.add(traceJson);
				}
				testCaseJson.put("nodes", traces);
				testCases.add(testCaseJson);
			}
			featureJson.put("nodes", testCases);
			result.add(featureJson);
		}
		
		
		return result;
	}
	
	
	public JSONObject microServiceToCytoscapeUnion(Iterable<TestCase> selectTestCases, Iterable<TestCase> scaleTestCases) {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		// 服务调用服务
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> selectMsCalls = findMsCallMsByTestCases(selectTestCases).getCalls();
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> scaleMsCalls = findMsCallMsByTestCases(scaleTestCases).getCalls();
 		for(MicroService ms : selectMsCalls.keySet()) {
			for(MicroService callMs : selectMsCalls.get(ms).keySet()) {
				MicroServiceCallMicroService msCallMs = selectMsCalls.get(ms).get(callMs);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", ms.getId() + "_" + callMs.getId());
				value.put("value", msCallMs.getTimes());
				value.put("source", ms.getId());
				value.put("target", callMs.getId());
				value.put("type", "selectTestCase");
				edge.put("data", value);
				edges.add(edge);
			}
		}
		for(MicroService ms : scaleMsCalls.keySet()) {
			for(MicroService callMs : scaleMsCalls.get(ms).keySet()) {
				MicroServiceCallMicroService msCallMs = scaleMsCalls.get(ms).get(callMs);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", ms.getId() + "_" + callMs.getId());
				value.put("value", msCallMs.getTimes());
				value.put("source", ms.getId());
				value.put("target", callMs.getId());
				value.put("type", "allTestCase");
				edge.put("data", value);
				edges.add(edge);
			}
		}
		List<MicroService> allMicroServices = allMicroServices();
		Set<MicroService> relatedAllMicroServices = findRelatedMicroServiceForTestCases(scaleTestCases);
		Set<MicroService> relatedSelectMicroServices = findRelatedMicroServiceForTestCases(selectTestCases);
		for(MicroService ms : allMicroServices) {
			JSONObject msJson = new JSONObject();
			JSONObject msDataValue = new JSONObject();
			if(relatedSelectMicroServices.contains(ms)) {
				msDataValue.put("type", "selectMicroService");
			} else if(relatedAllMicroServices.contains(ms)) {
				msDataValue.put("type", "allMicroService");
			} else {
				msDataValue.put("type", "noMicroService");
			}
			msDataValue.put("id", ms.getId());
			msDataValue.put("name", ms.getName());
			msJson.put("data", msDataValue);
			nodes.add(msJson);
		}
		JSONObject value = new JSONObject();
		value.put("nodes", nodes);
		value.put("edges", edges);
		result.put("value", value);
		result.put("microservice", allMicroServices);
		return result;	
	}
	
	public JSONObject microServiceToCytoscapeIntersection(Iterable<TestCase> selectTestCases, Iterable<TestCase> scaleTestCases) {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		// 服务调用服务
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> scaleMsCalls = findMsCallMsByTestCases(scaleTestCases).getCalls();
		List<MicroServiceCallWithEntry> selectMsCallsList = new ArrayList<>();
		for(TestCase testCase : selectTestCases) {
			MicroServiceCallWithEntry selectMsCalls = findMsCallMsByTestCases(testCase);
			selectMsCallsList.add(selectMsCalls);
		}
		MicroServiceCallWithEntry callWithEntry0 = selectMsCallsList.get(0);
		for(MicroService caller : callWithEntry0.getCalls().keySet()) {
			for(MicroService called : callWithEntry0.getCalls().get(caller).keySet()) {
				MicroServiceCallMicroService call = callWithEntry0.getCalls().get(caller).get(called);
				boolean flag = true;
				for(int i = 1; i < selectMsCallsList.size(); i++) {
					MicroServiceCallWithEntry callWithEntry = selectMsCallsList.get(i);
					if(!callWithEntry.containCall(caller, called)) {
						flag = false;
						break;
					}
				}
				if(flag) {
					JSONObject edge = new JSONObject();
					JSONObject value = new JSONObject();
					value.put("id", caller.getId() + "_" + called.getId());
					value.put("source", caller.getId());
					value.put("target", called.getId());
					value.put("type", "selectTestCase");
					edge.put("data", value);
					edges.add(edge);
				}
			}
		}
		
		for(MicroService ms : scaleMsCalls.keySet()) {
			for(MicroService callMs : scaleMsCalls.get(ms).keySet()) {
				MicroServiceCallMicroService msCallMs = scaleMsCalls.get(ms).get(callMs);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", ms.getId() + "_" + callMs.getId());
				value.put("value", msCallMs.getTimes());
				value.put("source", ms.getId());
				value.put("target", callMs.getId());
				value.put("type", "allTestCase");
				edge.put("data", value);
				edges.add(edge);
			}
		}
		List<MicroService> allMicroServices = allMicroServices();
		Set<MicroService> relatedAllMicroServices = findRelatedMicroServiceForTestCases(scaleTestCases);
		Set<MicroService> relatedSelectMicroServices = findRelatedMicroServiceForTestCases(selectTestCases);
		for(MicroService ms : allMicroServices) {
			JSONObject msJson = new JSONObject();
			JSONObject msDataValue = new JSONObject();
			if(relatedSelectMicroServices.contains(ms)) {
				msDataValue.put("type", "selectMicroService");
			} else if(relatedAllMicroServices.contains(ms)) {
				msDataValue.put("type", "allMicroService");
			} else {
				msDataValue.put("type", "noMicroService");
			}
			msDataValue.put("id", ms.getId());
			msDataValue.put("name", ms.getName());
			msJson.put("data", msDataValue);
			nodes.add(msJson);
		}
		JSONObject value = new JSONObject();
		value.put("nodes", nodes);
		value.put("edges", edges);
		result.put("value", value);
		result.put("microservice", allMicroServices);
		return result;	
	}
	
	public JSONObject microServiceToCytoscape(boolean removeUnuseMS, Iterable<Trace> traces) {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		// 服务调用服务
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls = findMsCallMsByTraces(traces).getCalls();
		int count = 0;
		Trace firstTrace = null;
		for(Trace trace : traces) {
			if(count == 0) {
				firstTrace = trace;
			}
			count++;
		}
		if(count == 1) {
			JSONObject msCallMsDetail = new JSONObject();
			for(MicroService ms : msCalls.keySet()) {
				JSONObject info = new JSONObject();
				info.put("from", ms);
				Map<MicroService, MicroServiceCallMicroService> calls = msCalls.get(ms);
				JSONObject toArray = new JSONObject();
				for(MicroService callMs : calls.keySet()) {
					JSONObject to = new JSONObject();
					to.put("to", callMs);
					MicroServiceCallMicroService mcm = calls.get(callMs);
					to.put("times", mcm.getTimes());
					to.put("call", mcm.getSpanCallSpans());
					toArray.put(callMs.getId().toString(), to);
				}
				info.put("tos", toArray);
				msCallMsDetail.put(ms.getId().toString(), info);
			}
			result.put("detail", msCallMsDetail);
			result.put("traceId", firstTrace.getTraceId());
		}
		
		// 显示哪些服务
		for(MicroService ms : msCalls.keySet()) {
			for(MicroService callMs : msCalls.get(ms).keySet()) {
				MicroServiceCallMicroService msCallMs = msCalls.get(ms).get(callMs);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", ms.getId() + "_" + callMs.getId());
				value.put("value", msCallMs.getTimes());
				value.put("source", ms.getId());
				value.put("target", callMs.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		List<MicroService> relatedMSs = removeUnuseMS ? new ArrayList<>(findRelatedMicroServiceForTraces(traces)) : allMicroServices();
		for(MicroService ms : relatedMSs) {
			JSONObject msJson = new JSONObject();
			JSONObject msDataValue = new JSONObject();
			msDataValue.put("id", ms.getId());
			msDataValue.put("name", ms.getName());
			msJson.put("data", msDataValue);
			nodes.add(msJson);
		}
		JSONObject value = new JSONObject();
		value.put("nodes", nodes);
		value.put("edges", edges);
		result.put("value", value);
		result.put("microservice", relatedMSs);
		return result;
	}
	
	public JSONObject microServiceToCytoscape(boolean removeUnuseMS, Trace... traces) {
		return microServiceToCytoscape(removeUnuseMS, Arrays.asList(traces));
	}
	
	/**
	 * 指定TestCase执行的Feature
	 * @param testCase
	 * @return
	 */
	public List<Feature> findTestCaseExecutionFeatures(TestCase testCase) {
		List<Feature> result = new ArrayList<>();
		for(TestCaseExecuteFeature execute : testCaseExecuteFeatures.getOrDefault(testCase, new ArrayList<>())) {
			result.add(execute.getFeature());
		}
		return result;
	}
	
	/**
	 * 指定TestCase相关的Trace
	 * @param testcases
	 * @return
	 */
	public Set<Trace> findRelatedTracesForTestCases(TestCase... testCases) {
		return findRelatedTracesForTestCases(Arrays.asList(testCases));
	}
	
	/**
	 * 指定TestCase相关的Trace
	 * @param testcases
	 * @return
	 */
	public Set<Trace> findRelatedTracesForTestCases(Iterable<TestCase> testcases) {
		Set<Trace> result = new HashSet<>();
		for(TestCase testcase : testcases) {
			List<TestCaseRunTrace> runs = testCaseRunTraces.get(testcase);
			for(TestCaseRunTrace run : runs) {
				result.add(run.getTrace());
			}
		}
		return result;
	}
	
	/**
	 * 指定feature相关的trace
	 * @param features
	 * @return
	 */
	public Set<Trace> findRelatedTracesForFeature(Iterable<Feature> features) {
		Set<Trace> result = new HashSet<>();
		Set<TestCase> testcases = new HashSet<>();
		for(Feature feature : features) {
			List<TestCaseExecuteFeature> tcs = featureExecutedByTestCases.get(feature);
			for(TestCaseExecuteFeature tc : tcs) {
				testcases.add(tc.getTestCase());
			}
		}
		for(TestCase testcase : testcases) {
			List<TestCaseRunTrace> traces = testCaseRunTraces.get(testcase);
			for(TestCaseRunTrace trace : traces) {
				result.add(trace.getTrace());
			}
		}
		return result;
	}
	
	/**
	 * 指定feature相关的trace
	 * @param features
	 * @return
	 */
	public Set<Trace> findRelatedTracesForFeature(Feature... features) {
		return findRelatedTracesForFeature(Arrays.asList(features));
	}
	
	/**
	 * 找出某个微服务在某次Trace中创建的Span
	 * @param ms
	 * @param trace
	 * @return
	 * @throws Exception
	 */
	public List<Span> findMicroServiceCreateSpansInTraces(MicroService ms, Trace trace) {
		List<Span> spans = new ArrayList<>();
		for(Span span : traceToSpans.get(trace)) {
			if(spanBelongToMicroService.get(span).getMicroservice().equals(ms)) {
				spans.add(span);
			}
		}
		return spans;
	}
	
	public MicroServiceCallWithEntry findMsCallMsByTestCases(TestCase... testCases) {
		List<TestCase> list = new ArrayList<>();
		for(TestCase testCase : testCases) {
			list.add(testCase);
		}
		return findMsCallMsByTestCases(list);
	}
	
	/**
	 * 所有测试用例调用的微服务的并集
	 * @param testCases
	 * @return
	 */
	public MicroServiceCallWithEntry findMsCallMsByTestCases(Iterable<TestCase> testCases) {
		List<Trace> traces = new ArrayList<>();
		Map<Trace, TestCase> traceBelongToTestCase = new HashMap<>();
		for(TestCase testCase : testCases) {
			List<TestCaseRunTrace> runs = this.testCaseRunTraces.get(testCase);
			for(TestCaseRunTrace run : runs) {
				traces.add(run.getTrace());
				traceBelongToTestCase.put(run.getTrace(), testCase);
			}
		}
		Map<TestCase, List<MicroService>> testCaseToEntries = new HashMap<>();
		MicroServiceCallWithEntry result = findMsCallMsByTraces(traces);
		for(Trace trace : result.getTraceToEntry().keySet()) {
			MicroService entry = result.getTraceToEntry().get(trace);
			TestCase testCase = traceBelongToTestCase.get(trace);
			List<MicroService> mss = testCaseToEntries.getOrDefault(testCase, new ArrayList<>());
			mss.add(entry);
			testCaseToEntries.put(testCase, mss);
		}
		result.setTestCaseToEntries(testCaseToEntries);
		return result;
	}
	
	/**
	 * 指定trace相关的微服务调用
	 * @param traces
	 * @return
	 */
	public MicroServiceCallWithEntry findMsCallMsByTraces(Trace... traces) {
		try {
			return findMsCallMsByTraces(Arrays.asList(traces));
		} catch (Exception e) {
			return new MicroServiceCallWithEntry();
		}
	}
	
	/**
	 * 指定trace相关的微服务调用
	 * @param traces
	 * @return
	 */
	public MicroServiceCallWithEntry findMsCallMsByTraces(Iterable<Trace> traces) {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls = new HashMap<>();
		Map<Trace, MicroService> traceToEntry = new HashMap<>();
		for(Trace trace : traces) {
			try {
				if(!trace.isMicroServiceTrace()) {
					continue;
				}
				List<Span> spans = traceToSpans.get(trace);
				for(Span span : spans) {
					List<SpanCallSpan> callSpans = spanCallSpans.getOrDefault(span, new ArrayList<>());
					MicroService ms = spanBelongToMicroService.get(span).getMicroservice();
					if(span.isStartSpan()) {
						traceToEntry.put(trace, ms);
					}
					Map<MicroService, MicroServiceCallMicroService> msCallMsTimes = calls.getOrDefault(ms, new HashMap<>());
					for(SpanCallSpan spanCallSpan : callSpans) {
						MicroService callMs = spanBelongToMicroService.get(spanCallSpan.getCallSpan()).getMicroservice();
						MicroServiceCallMicroService msCallMs = msCallMsTimes.getOrDefault(callMs, new MicroServiceCallMicroService(ms, callMs));
						
						msCallMs.addTimes(1);
						msCallMs.addSpanCallSpan(spanCallSpan);
						msCallMsTimes.put(callMs, msCallMs);
					}
					calls.put(ms, msCallMsTimes);
				}
			} catch (Exception e) {
				continue;
			}
		}
		MicroServiceCallWithEntry result = new MicroServiceCallWithEntry();
		result.setCalls(calls);
		result.setTraceToEntry(traceToEntry);
		return result;
	}
	
	/**
	 * 所有feature所对应的相关的微服务
	 * @return
	 */
	public Map<Feature, Set<MicroService>> findAllRelatedMicroServiceSplitByFeature() {
		Map<Feature, Set<MicroService>> result = new HashMap<>();
		for(Feature feature : allFeatures()) {
			Set<MicroService> mss = findRelatedMicroServiceForFeatures(feature);
			result.put(feature, mss);
		}
		return result;
	}
	
	/**
	 * 所有Feature相关的微服务
	 * @return
	 */
	public Set<MicroService> findAllRelatedMicroService() {
		Set<MicroService> result = new HashSet<>();
		for(Set<MicroService> temp : findAllRelatedMicroServiceSplitByFeature().values()) {
			result.addAll(temp);
		}
		return result;
	}
	
	/**
	 * 指定feature相关的微服务
	 * @param features
	 * @return
	 */
	public Set<MicroService> findRelatedMicroServiceForFeatures(Feature... features) {
		Set<Trace> relatedTraces = findRelatedTracesForFeature(features);
		Trace[] traces = new Trace[relatedTraces.size()];
		relatedTraces.toArray(traces);
		return findRelatedMicroServiceForTraces(traces);
	}
	
	/**
	 * 指定测试用例相关的微服务
	 * @param testcases
	 * @return
	 */
	public Set<MicroService> findRelatedMicroServiceForTestCases(Iterable<TestCase> testcases) { 
		Set<MicroService> result = new HashSet<>();
		for(TestCase testcase : testcases) {
			List<TestCaseRunTrace> runs = testCaseRunTraces.get(testcase);
			for(TestCaseRunTrace run : runs) {
				result.addAll(findRelatedMicroServiceForTraces(run.getTrace()));
			}
		}
		return result;
	}
	
	/**
	 * 指定trace相关的微服务
	 * @param traces
	 * @return
	 */
	public Set<MicroService> findRelatedMicroServiceForTraces(Iterable<Trace> traces) {
		Set<MicroService> result = new HashSet<>();
		for(Trace trace : traces) {
			List<Span> containSpans = traceToSpans.get(trace);
			for(Span span : containSpans) {
				MicroServiceCreateSpan create = spanBelongToMicroService.get(span);
				result.add(create.getMicroservice());
			}
		}
		return result;
	}
	
	public Set<MicroService> findRelatedMicroServiceForTraces(Trace... traces) {
		return findRelatedMicroServiceForTraces(Arrays.asList(traces));
	}

	
	/**
	 * 所有微服务
	 * @return
	 */
	public List<MicroService> allMicroServices() {
		List<MicroService> result = new ArrayList<>();
		for(MicroService ms : allMicroService.values()) {
			result.add(ms);
		}
		return result;
	}
	
	/**
	 * 根据微服务名查找微服务
	 * @param name
	 * @return
	 */
	public MicroService findMicroServiceByName(String name) {
		try {
			return allMicroService.get(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 根据featureId（不是graphId）查找Feature
	 * @param featureId
	 * @return
	 */
	public Feature findFeatureById(Integer featureId) {
		try {
			for(Feature feature : allFeatures()) {
				if(feature.getFeatureId().equals(featureId)) {
					return feature;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 所有Feature
	 * @return
	 */
	public Iterable<Feature> allFeatures() {
		List<Feature> result = new ArrayList<>();
		for(Feature feature : featureExecutedByTestCases.keySet()) {
			result.add(feature);
		}
		result.sort(new Comparator<Feature>() {
			@Override
			public int compare(Feature o1, Feature o2) {
				return o1.getFeatureId().compareTo(o2.getFeatureId());
			}
		});
		return result;
	}	
	
	/**
	 * 所有测试用例
	 * @return
	 */
	public Collection<TestCase> allTestCases() {
		List<TestCase> result = new ArrayList<>();
		for(TestCase testcase : testCaseExecuteFeatures.keySet()) {
			result.add(testcase);
		}
		result.sort(new Comparator<TestCase>() {
			@Override
			public int compare(TestCase o1, TestCase o2) {
				return o1.getTestCaseId().compareTo(o2.getTestCaseId());
			}
		});
		return result;
	}
	
	/**
	 * 根据testCaseId（不是graphId）查找TestCase，
	 * @param testCaseId
	 * @return
	 */
	public TestCase findTestCase(Integer testCaseId) {
		for(TestCase testCase : allTestCases()) {
			if(testCase.getTestCaseId().equals(testCaseId)) {
				return testCase;
			}
		}
		return null;
	}
	
	/**
	 * 所有测试用例，根据测试用例group进行分组
	 * @return
	 */
	public Map<String, List<TestCase>> allTestCasesGroupByTestCaseGroup() {
		Iterable<TestCase> testCases = allTestCases();
		Map<String, List<TestCase>> groupToTestCases = new HashMap<>();
		for(TestCase testCase : testCases) {
			List<TestCase> group = groupToTestCases.getOrDefault(testCase.getGroup(), new ArrayList<>());
			group.add(testCase);
			groupToTestCases.put(testCase.getGroup(), group);
		}
		return groupToTestCases;
	}
	
	/**
	 * 所有Trace
	 * @return
	 */
	public List<Trace> allTraces() {
		List<Trace> result = new ArrayList<>();
		for(TestCase testCase : testCaseRunTraces.keySet()) {
			for(TestCaseRunTrace trace : testCaseRunTraces.get(testCase)) {
				if(!result.contains(trace.getTrace())) {
					result.add(trace.getTrace());
				}
			}
		}
		return result;
	}

}
