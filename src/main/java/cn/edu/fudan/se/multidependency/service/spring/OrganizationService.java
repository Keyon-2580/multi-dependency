package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanCallSpan;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationService {
	
	private final Map<String, MicroService> allMicroService;
	private final Map<TestCase, List<TestCaseExecuteFeature>> testCaseExecuteFeatures;
	private final Map<Feature, List<TestCaseExecuteFeature>> featureExecutedByTestCases;
	private final Map<TestCase, List<TestCaseRunTrace>> testCaseRunTraces;
	private final Map<Trace, List<Span>> traceToSpans;
	private final Map<Span, List<SpanCallSpan>> spanCallSpans;
	private final Map<Span, MicroServiceCreateSpan> spanBelongToMicroService;
	
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
				executeData.put("id", feature.getId() + "_" + testcase.getId());
				executeData.put("source", feature.getId());
				executeData.put("target", testcase.getId());
				JSONObject executeEdge = new JSONObject();
				executeEdge.put("data", executeData);
				edges.add(executeEdge);
			}
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONArray featureExecutedByTestCasesToTreeView() {
		JSONArray result = new JSONArray();
		List<Feature> features = allFeatures();
		for(Feature feature : features) {
			List<TestCaseExecuteFeature> executes = featureExecutedByTestCases.get(feature);
			JSONObject featureJson = new JSONObject();
			featureJson.put("text", feature.getFeatureId() + ":" + feature.getFeatureName());
			JSONArray tags = new JSONArray();
			tags.add("feature");
			featureJson.put("tags", tags);
			
			JSONArray testCases = new JSONArray();
			for(TestCaseExecuteFeature execute : executes) {
				JSONObject testCaseJson = new JSONObject();
				testCaseJson.put("text", execute.getTestCase().getTestCaseId() + ":" + execute.getTestCase().getTestCaseName());
				tags = new JSONArray();
				tags.add("testcase");
				testCaseJson.put("tags", tags);
				
				List<TestCaseRunTrace> runs = testCaseRunTraces.get(execute.getTestCase());
				JSONArray traces = new JSONArray();
				for(TestCaseRunTrace run : runs) {
					JSONObject traceJson = new JSONObject();
					Trace trace = run.getTrace();
					traceJson.put("text", trace.getTraceId());
					tags = new JSONArray();
					tags.add("trace");
					traceJson.put("tags", tags);
					
					JSONArray microservices = new JSONArray();
					for(MicroService ms : findRelatedMicroServiceForTraces(trace)) {
						JSONObject msJson = new JSONObject();
						msJson.put("text", ms.getName());
						tags = new JSONArray();
						tags.add("microservice");
						msJson.put("tags", tags);
						JSONArray spansArray = new JSONArray();
						for(Span span : findMicroServiceCreateSpansInTraces(ms, trace)) {
							JSONObject spanJson = new JSONObject();
							spanJson.put("text", span.getOperationName());
							tags = new JSONArray();
							tags.add("span");
							spanJson.put("tags", tags);
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
	
	/**
	 * feature相关的trace
	 * @param features
	 * @return
	 */
	public Set<Trace> findRelatedTracesForFeature(Feature... features) {
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
	
	public Set<Trace> findRelatedTracesForFeature(List<Feature> features) {
		Feature[] array = new Feature[features.size()];
		features.toArray(array);
		return findRelatedTracesForFeature(array);
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
	
	public JSONObject microServiceToCatoscape(boolean removeUnuseMS, List<Feature> features) {
		Feature[] featureArray = new Feature[features.size()];
		features.toArray(featureArray);
		return microServiceToCatoscape(removeUnuseMS, featureArray);
	}
	
	public JSONObject microServiceToCatoscape(boolean removeUnuseMS, Feature... features) {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		// 服务调用服务
		Map<MicroService, Map<MicroService, MSCallMS>> msCalls = msCalls(features);
		/*if(features.length == 1) {
			JSONObject msCallMsDetail = new JSONObject();
			for(MicroService ms : msCalls.keySet()) {
				JSONObject info = new JSONObject();
				info.put("from", ms);
				Map<MicroService, MSCallMS> calls = msCalls.get(ms);
				JSONObject toArray = new JSONObject();
				for(MicroService callMs : calls.keySet()) {
					JSONObject to = new JSONObject();
					to.put("to", callMs);
					MSCallMS mcm = calls.get(callMs);
					to.put("times", mcm.getTimes());
					to.put("call", mcm.getSpanCallSpans());
					toArray.put(callMs.getId().toString(), to);
				}
				info.put("tos", toArray);
				msCallMsDetail.put(ms.getId().toString(), info);
			}
			result.put("detail", msCallMsDetail);
			FeatureExecuteTrace featureExecuteTrace = featureExecuteTraces.get(features[0]);
			result.put("traceId", featureExecuteTrace.getTrace().getTraceId());
		}*/
		
		// 显示哪些服务
		for(MicroService ms : msCalls.keySet()) {
			for(MicroService callMs : msCalls.get(ms).keySet()) {
				MSCallMS msCallMs = msCalls.get(ms).get(callMs);
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
		List<MicroService> relatedMSs = removeUnuseMS ? new ArrayList<>(findRelatedMicroServiceForFeatures(features)) : allMicroServices();
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
	
	public Map<MicroService, Map<MicroService, MSCallMS>> msCalls(Feature... features) {
		Map<MicroService, Map<MicroService, MSCallMS>> result = new HashMap<>();
		Set<Trace> traces = findRelatedTracesForFeature(features);
			for(Trace trace : traces) {
				try {
					List<Span> spans = traceToSpans.get(trace);
					for(Span span : spans) {
						List<SpanCallSpan> callSpans = spanCallSpans.get(span);
						callSpans = callSpans == null ? new ArrayList<>() : callSpans;
						MicroService ms = spanBelongToMicroService.get(span).getMicroservice();
						Map<MicroService, MSCallMS> msCallMsTimes = result.get(ms);
						msCallMsTimes = msCallMsTimes == null ? new HashMap<>() : msCallMsTimes;
						for(SpanCallSpan spanCallSpan : callSpans) {
							MicroService callMs = spanBelongToMicroService.get(spanCallSpan.getCallSpan()).getMicroservice();
							MSCallMS msCallMs = msCallMsTimes.get(callMs);
							msCallMs = msCallMs == null ? new MSCallMS(ms, callMs) : msCallMs;
							msCallMs.addTimes(1);
							msCallMs.addSpanCallSpan(spanCallSpan);
							msCallMsTimes.put(callMs, msCallMs);
						}
						result.put(ms, msCallMsTimes);
					}
				} catch (Exception e) {
					continue;
				}
			}
		return result;
	}
	
	public MicroService findMicroServiceByName(String name) {
		try {
			return allMicroService.get(name);
		} catch (Exception e) {
			return null;
		}
	}
	
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
	 * 找出organization当前所有的feature相关的微服务
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
	 * 找到当前类内的所有Feature相关的微服务
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
	 * trace相关的微服务
	 * @param traces
	 * @return
	 */
	private Set<MicroService> findRelatedMicroServiceForTraces(Trace... traces) {
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
	
	/**
	 * feature相关的微服务
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
	 * 所有Feature
	 * @return
	 */
	public List<Feature> allFeatures() {
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
	
	public List<TestCase> allTestCases() {
		List<TestCase> result = new ArrayList<>();
		for(TestCase feature : testCaseExecuteFeatures.keySet()) {
			result.add(feature);
		}
		result.sort(new Comparator<TestCase>() {
			@Override
			public int compare(TestCase o1, TestCase o2) {
				return o1.getTestCaseId().compareTo(o2.getTestCaseId());
			}
		});
		return result;
	}

}
