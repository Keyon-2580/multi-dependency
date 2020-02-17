package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FeatureExecuteTrace;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanCallSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.repository.node.microservice.jaeger.MicroServiceRepository;
import cn.edu.fudan.se.multidependency.repository.node.microservice.jaeger.SpanRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FeatureExecuteTraceRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.jaeger.MicroServiceCreateSpanRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.jaeger.SpanCallSpanRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.jaeger.SpanStartWithFunctionRepository;

@Service
public class JaegerServiceImpl implements JaegerService {

	@Autowired
	private FeatureExecuteTraceRepository featureExecuteTraceRepository;
	
	@Autowired
	private SpanCallSpanRepository spanCallSpanRepository;
	
	@Autowired
	private ContainRepository containRepository;
	
	@Autowired
	private MicroServiceCreateSpanRepository projectCreateSpanRepository;
	
	@Autowired
	private MicroServiceRepository microServiceRepository;
	
	@Autowired
	private SpanStartWithFunctionRepository spanStartWithFunctionRepository;
	
	@Autowired
	private SpanRepository spanRepository;
	
	@Override
	public Trace findTraceByFeature(Feature feature) {
		return featureExecuteTraceRepository.findTraceByFeatureId(feature.getFeatureId());
	}

	@Override
	public List<Span> findSpansByTrace(Trace trace) {
		List<Span> spans = containRepository.findSpansByTraceId(trace.getTraceId());
		spans.sort(new Comparator<Span>() {
			@Override
			public int compare(Span o1, Span o2) {
				return o1.getOrder().compareTo(o2.getOrder());
			}
		});
		return spans;
	}

	@Override
	public List<SpanCallSpan> findSpanCallSpans(Span span) {
		return spanCallSpanRepository.findSpanCallSpansBySpanId(span.getSpanId());
	}

	@Override
	public MicroServiceCreateSpan findMicroServiceCreateSpan(Span span) {
		return projectCreateSpanRepository.findProjectCreateSpan(span.getSpanId());
	}

	@Override
	public List<FeatureExecuteTrace> findAllFeatureExecuteTraces() {
		return featureExecuteTraceRepository.findAllFeatureExecuteTrace();
	}

	@Override
	public FeatureExecuteTrace findFeatureExecuteTraceByFeature(Feature feature) {
		return featureExecuteTraceRepository.findExecuteTraceByFeatureId(feature.getFeatureId());
	}

	@Override
	public Map<String, MicroService> findAllMicroService() {
		Map<String, MicroService> result = new HashMap<>();
		microServiceRepository.findAll().forEach(ms -> {
			result.put(ms.getName(), ms);
		});
		return result;
	}

	@Override
	public MicroService findMicroServiceById(Long id) {
		return microServiceRepository.findById(id).get();
	}

	@Override
	public SpanCallSpan findSpanCallSpanById(Long id) {
		return spanCallSpanRepository.findById(id).get();
	}

	@Override
	public SpanStartWithFunction findSpanStartWithFunctionByTraceIdAndSpanId(String requestTraceId,
			String requestSpanId) {
		System.out.println(requestTraceId + " " + requestSpanId);
		return spanStartWithFunctionRepository.findSpanStartWIthFunctionByTraceIdAndSpanId(requestTraceId, requestSpanId);
	}

	@Override
	public Span findSpanById(Long id) {
		return spanRepository.findById(id).get();
	}

	@Override
	public List<Span> findSpansByMicroserviceIdAndTraceId(Long id, String traceId) {
		return null;
	}

}
