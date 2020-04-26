package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
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
	Collection<Project> microServiceContainProjects(MicroService ms);
	
	MicroService findProjectBelongToMicroService(Project project);
	
	/**
	 * 微服务调用了哪些三方库
	 * @param microService
	 * @return
	 */
	CallLibrary microServiceCallLibraries(MicroService microService);
	
	
	List<RestfulAPI> findMicroServiceContainRestfulAPI(MicroService microService);
	

	SpanInstanceOfRestfulAPI findSpanBelongToAPI(Span span);
	
	Map<Span, SpanInstanceOfRestfulAPI> findAllSpanInstanceOfRestfulAPIs();
	
	/**
	 * 根据函数间的克隆找出项目间的克隆
	 * @param functionClones
	 * @return
	 */
	Iterable<Clone> findMicroServiceClone(Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode);
	
	
	/**
	 * 微服务拥有的方法
	 * @param ms
	 * @return
	 */
	Collection<Function> findMicroServiceContainFunctions(MicroService ms);
	

}
