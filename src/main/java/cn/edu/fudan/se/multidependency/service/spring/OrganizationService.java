package cn.edu.fudan.se.multidependency.service.spring;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Factory.to;

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
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FeatureExecuteTrace;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanCallSpan;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Rank.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.Node;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationService {
	private Map<String, MicroService> allMicroService;
	private Map<Feature, FeatureExecuteTrace> featureExecuteTraces;
	private Map<Trace, List<Span>> traceToSpans;
	private Map<Span, List<SpanCallSpan>> spanCallSpans;
	private Map<Span, MicroServiceCreateSpan> spanBelongToMicroService;
	
	public List<Span> findMicroServiceCreateSpansInTraces(MicroService ms, Trace trace) throws Exception {
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
		if(features.length == 1) {
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
		}
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
		List<MicroService> relatedMSs = removeUnuseMS ? findRelatedMicroServiceForFeatures(features) : allMicroServices();
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
		for(Feature feature : features) {
			try {
				Trace trace = this.featureExecuteTraces.get(feature).getTrace();
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
			Set<MicroService> mss = findRelatedMicroServiceForFeature(feature);
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
	
	/**
	 * 某个Feature相关的微服务
	 * @param feature
	 * @return
	 */
	private Set<MicroService> findRelatedMicroServiceForFeature(Feature feature) {
		Trace trace = featureExecuteTraces.get(feature).getTrace();
		return findRelatedMicroServiceForTrace(trace);
	}
	
	public List<MicroService> findRelatedMicroServiceForFeatures(Feature... features) {
		List<MicroService> result = new ArrayList<>();
		for(Feature feature : features) {
			Set<MicroService> mss = findRelatedMicroServiceForFeature(feature);
			for(MicroService ms : mss) {
				if(!result.contains(ms)) {
					result.add(ms);
				}
			}
		}
		return result;
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
		for(Feature feature : featureExecuteTraces.keySet()) {
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
	
	public String featureToSVG(Feature feature) {
		if(feature == null || featureExecuteTraces.get(feature) == null) {
			return null;
		}
		Trace trace = featureExecuteTraces.get(feature).getTrace();
		String svg = renderSVGForTrace(trace, feature.getFeatureName() + " " + trace.getTraceId());
		return svg;
	}
	
	public Map<Feature, String> featureToSVG() {
		Map<Feature, String> result = new HashMap<>();
		for(Feature feature : allFeatures()) {
			result.put(feature, featureToSVG(feature));
		}
		return result;
	}
	
	public String renderSVGForTrace(Trace trace, String graphName) {
		List<LinkSource> linkSources = new ArrayList<>();
		List<Span> spans = traceToSpans.get(trace);
		spans = spans == null ? new ArrayList<>() : spans;
		for(Span span : spans) {
			Node source = node(getSpanName(span)).with(Label.of(getSpanLabel(span)));
			List<SpanCallSpan> callSpans = spanCallSpans.get(span);
			callSpans = callSpans == null ? new ArrayList<>() : callSpans;
			for(SpanCallSpan spanCallSpan : callSpans) {
				Span callSpan = spanCallSpan.getCallSpan();
				Node linkNode = node(getSpanName(callSpan)).with(Label.of(getSpanLabel(callSpan)));
				source = source.link(to(linkNode).with(Label.of(spanCallSpan.getHttpRequestMethod())));
			}
			linkSources.add(source);
		}
		System.out.println(linkSources.size());
		Graph g = graph(graphName).directed().graphAttr().with(Rank.dir(RankDir.LEFT_TO_RIGHT)).with(linkSources);
		return Graphviz.fromGraph(g).render(Format.SVG).toString();
	}
	private static String getSpanName(Span span) {
		return "(" + span.getOrder() + ")\n" + span.getSpanId() + "\n" + span.getServiceName() + "\n" + span.getOperationName();
	}
	private static String getSpanLabel(Span span) {
		return "(" + span.getOrder() + ")\n" + span.getServiceName() + "\n" + span.getOperationName();
	}
	
}
