package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FeatureExecuteTrace;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanCallSpan;
import cn.edu.fudan.se.multidependency.service.spring.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.JaegerService;

@Controller
@RequestMapping("/feature")
public class FeatureController {
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;
	
	@Autowired
	private JaegerService jaegerService;

	@GetMapping("/all")
	@ResponseBody
	public List<Feature> findAllFeatures() {
		return dynamicAnalyseService.findAllFeatures();
	}
	
	@GetMapping("/test")
	@ResponseBody
	public void test() {
		organize();
	}
	
	@Bean
	public void organize() {
		System.out.println(jaegerService.findAllFeatureExecuteTraces());
		/*System.out.println(dynamicAnalyseService.findAllFeatures());
		List<Feature> features = dynamicAnalyseService.findFeaturesByFeatureId(1, 2);
		System.out.println(features);
		// 找出feature执行的trace
		Map<Feature, FeatureExecuteTrace> featureToTrace = new HashMap<>();
		for(Feature feature : features) {
			FeatureExecuteTrace execute = jaegerService.findFeatureExecuteTraceByFeature(feature);
			System.out.println(execute);
			featureToTrace.put(feature, execute);
		}
		System.out.println(featureToTrace);
		// 找出trace对应的spans
		Map<Trace, List<Span>> traceToSpans = new HashMap<>();
		// 找出span调用哪些span
		Map<Span, List<SpanCallSpan>> spanCallSpans = new HashMap<>();
		// 找出span属于哪个project
		Map<Span, MicroServiceCreateSpan> spanToProject = new HashMap<>();*/
		
//		for(FeatureExecuteTrace featureExecuteTrace : featureToTrace.values()) {
//			Trace trace = featureExecuteTrace.getTrace();
//			System.out.println(trace.getTraceId());
//			List<Span> spans = jaegerService.findSpansByTrace(trace);
//			System.out.println(spans);
//			traceToSpans.put(trace, spans);
			/*for(Span span : spans) {
				List<SpanCallSpan> callSpans = jaegerService.findSpanCallSpans(span);
				System.out.println(callSpans);
				spanCallSpans.put(span, callSpans);
				ProjectCreateSpan project = jaegerService.findProjectCreateSpan(span);
				spanToProject.put(span, project);
			}*/
//		}
//		System.exit(0);
	}
}
