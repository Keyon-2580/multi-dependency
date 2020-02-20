package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanCallSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanStartWithFunction;

public interface JaegerService {
	
	MicroService findMicroServiceById(Long id);
	
	Map<String, MicroService> findAllMicroService();
	
	Trace findTraceByFeature(Feature feature);
	
	List<Span> findSpansByTrace(Trace trace);
	
	List<SpanCallSpan> findSpanCallSpans(Span span);
	
	MicroServiceCreateSpan findMicroServiceCreateSpan(Span span);
	
	SpanCallSpan findSpanCallSpanById(Long id);

	SpanStartWithFunction findSpanStartWithFunctionByTraceIdAndSpanId(String requestTraceId, String requestSpanId);
	
	Span findSpanById(Long id);

	List<Span> findSpansByMicroserviceAndTraceId(MicroService ms, String traceId);

	Trace findTraceByTraceId(String traceId);

	Trace findTraceById(Long id);
}
