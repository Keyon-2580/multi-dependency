package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanCallSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.repository.node.microservice.jaeger.MicroServiceRepository;
import cn.edu.fudan.se.multidependency.repository.node.microservice.jaeger.SpanRepository;
import cn.edu.fudan.se.multidependency.repository.node.microservice.jaeger.TraceRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.jaeger.MicroServiceCreateSpanRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.jaeger.SpanCallSpanRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.jaeger.SpanStartWithFunctionRepository;

@Service
public class JaegerServiceImpl implements JaegerService {

	
	@Autowired
	private SpanCallSpanRepository spanCallSpanRepository;
	
	@Autowired
	private ContainRepository containRepository;
	
	@Autowired
	private MicroServiceCreateSpanRepository microserviceCreateSpanRepository;
	
	@Autowired
	private MicroServiceRepository microServiceRepository;
	
	@Autowired
	private SpanStartWithFunctionRepository spanStartWithFunctionRepository;
	
	@Autowired
	private SpanRepository spanRepository;
	
	@Autowired
	private TraceRepository traceRepository;
	
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
		return microserviceCreateSpanRepository.findMicroServiceCreateSpan(span.getSpanId());
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
		return spanStartWithFunctionRepository.findSpanStartWIthFunctionByTraceIdAndSpanId(requestTraceId, requestSpanId);
	}

	@Override
	public Span findSpanById(Long id) {
		return spanRepository.findById(id).get();
	}

	@Override
	public List<Span> findSpansByMicroserviceAndTraceId(MicroService ms, String traceId) {
		List<MicroServiceCreateSpan> createSpans = microserviceCreateSpanRepository.findMicroServiceCreateSpansInTrace(ms.getId(), traceId);
		List<Span> result = new ArrayList<>();
		for(MicroServiceCreateSpan createSpan : createSpans) {
			result.add(createSpan.getSpan());
		}
		return result;
	}

	@Override
	public Trace findTraceByTraceId(String traceId) {
		return traceRepository.findTraceByTraceId(traceId);
	}

	@Override
	public Trace findTraceByFeature(Feature feature) {
		return null;
	}

	@Override
	public Trace findTraceById(Long id) {
		return traceRepository.findById(id).get();
	}

}
