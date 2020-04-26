package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanCallSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanInstanceOfRestfulAPI;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;

public interface MicroserviceService {
	
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
	
	Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls();
	
	Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns();
	
	boolean isMicroServiceCall(MicroService start, MicroService end);
	
	boolean isMicroServiceDependOn(MicroService start, MicroService end);
	
	Map<MicroService, List<RestfulAPI>> microServiceContainsAPIs();
	
	Iterable<MicroServiceCallMicroService> findAllMicroServiceCallMicroServices();
	
	void deleteAllMicroServiceCallMicroService();
	
	void saveMicroServiceCallMicroService(MicroServiceCallMicroService call);
	
	/**
	 * 微服务包含哪些项目
	 * @param ms
	 * @return
	 */
	Iterable<Project> microServiceContainProjects(MicroService ms);
	
	/**
	 * 微服务调用了哪些三方库
	 * @param microService
	 * @return
	 */
	CallLibrary microServiceCallLibraries(MicroService microService);
	
	
	public List<RestfulAPI> findMicroServiceContainRestfulAPI(MicroService microService);
	

	SpanInstanceOfRestfulAPI findSpanBelongToAPI(Span span);
	
	Map<Span, SpanInstanceOfRestfulAPI> findAllSpanInstanceOfRestfulAPIs();
	

}
