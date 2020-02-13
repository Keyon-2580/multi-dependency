package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FeatureExecuteTrace;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanCallSpan;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Organization {
	private Map<String, MicroService> allMicroService;
	private Map<Feature, FeatureExecuteTrace> featureExecuteTraces;
	private Map<Trace, List<Span>> traceToSpans;
	private Map<Span, List<SpanCallSpan>> spanCallSpans;
	private Map<Span, MicroServiceCreateSpan> spanBelongToMicroService;
	
	public MicroService findMicroServiceByName(String name) {
		try {
			return allMicroService.get(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 找出指定feature相关的微服务
	 * @return
	 */
	public Map<Feature, Set<MicroService>> findAllRelatedMicroServiceSplitByFeature() {
		Map<Feature, Set<MicroService>> result = new HashMap<>();
		for(Feature feature : allFeatures()) {
			Set<MicroService> mss = findRelatedMicroServiceForFeature(feature);
			result.put(feature, mss);
		}
		return result;
	}
	
	public Set<MicroService> findAllRelatedMicroService() {
		Set<MicroService> result = new HashSet<>();
		for(Set<MicroService> temp : findAllRelatedMicroServiceSplitByFeature().values()) {
			result.addAll(temp);
		}
		return result;
	}
	
	
	/**
	 * 一条trace调用到的所有微服务
	 * @param trace
	 * @return
	 */
	private Set<MicroService> findRelatedMicroServiceForTrace(Trace trace) {
		Set<MicroService> result = new HashSet<>();
		List<Span> containSpans = traceToSpans.get(trace);
		for(Span span : containSpans) {
			MicroServiceCreateSpan create = spanBelongToMicroService.get(span);
			result.add(create.getMicroservice());
		}
		return result;
	}
	
	private Set<MicroService> findRelatedMicroServiceForFeature(Feature feature) {
		Trace trace = featureExecuteTraces.get(feature).getTrace();
		return findRelatedMicroServiceForTrace(trace);
	}
	

	public List<MicroService> allMicroServices() {
		List<MicroService> result = new ArrayList<>();
		for(MicroService ms : allMicroService.values()) {
			result.add(ms);
		}
		return result;
	}
	
	public List<Feature> allFeatures() {
		List<Feature> result = new ArrayList<>();
		for(Feature feature : featureExecuteTraces.keySet()) {
			result.add(feature);
		}
		return result;
	}	
	
}
