package cn.edu.fudan.se.multidependency.service.spring;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Factory.to;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Rank.RankDir;
import guru.nidi.graphviz.attribute.Style;
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
	
	public static void mai1n(String[] args) {
		Graph g = graph("example1").directed()
		        .graphAttr().with(Rank.dir(RankDir.LEFT_TO_RIGHT))
		        .with(
		                node("a").with(Color.RED).link(node("b")),
		                node("b").link(to(node("c")).with(Style.DASHED))
		        );
		try {
			Graphviz.fromGraph(g).height(100).render(Format.PNG).toFile(new File("D:\\ex1.png"));
			Graphviz.fromGraph(g).render(Format.SVG).toFile(new File("D:\\ex1.svg"));
			System.out.println(Graphviz.fromGraph(g).render(Format.SVG));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
