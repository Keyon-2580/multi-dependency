package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FeatureExecuteTrace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanCallSpan;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.service.spring.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.JaegerService;
import cn.edu.fudan.se.multidependency.service.spring.OrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.SpanWithFunctions;

@Controller
@RequestMapping("/feature")
public class FeatureController {

	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;

	@Autowired
	private JaegerService jaegerService;

	@Autowired
	private OrganizationService organizationService;

	@Bean
	public OrganizationService organize() {
		// List<Feature> features = dynamicAnalyseService.findFeaturesByFeatureId(1, 2);
		List<Feature> features = dynamicAnalyseService.findAllFeatures();
		// 找出feature执行的trace
		Map<Feature, FeatureExecuteTrace> featureExecuteTraces = new HashMap<>();
		for (Feature feature : features) {
			FeatureExecuteTrace execute = jaegerService.findFeatureExecuteTraceByFeature(feature);
			featureExecuteTraces.put(feature, execute);
		}
		// 找出trace对应的spans
		Map<Trace, List<Span>> traceToSpans = new HashMap<>();
		// 找出span调用哪些span
		Map<Span, List<SpanCallSpan>> spanCallSpans = new HashMap<>();
		// 找出span属于哪个project
		Map<Span, MicroServiceCreateSpan> spanBelongToMicroService = new HashMap<>();

		for (FeatureExecuteTrace featureExecuteTrace : featureExecuteTraces.values()) {
			Trace trace = featureExecuteTrace.getTrace();
			List<Span> spans = jaegerService.findSpansByTrace(trace);
			traceToSpans.put(trace, spans);
			for (Span span : spans) {
				List<SpanCallSpan> callSpans = jaegerService.findSpanCallSpans(span);
				spanCallSpans.put(span, callSpans);
				MicroServiceCreateSpan microServiceCreateSpan = jaegerService.findMicroServiceCreateSpan(span);
				spanBelongToMicroService.put(span, microServiceCreateSpan);
			}
		}
		Map<String, MicroService> allMicroService = jaegerService.findAllMicroService();
		OrganizationService organization = new OrganizationService(allMicroService, featureExecuteTraces, traceToSpans,
				spanCallSpans, spanBelongToMicroService);

		return organization;
	}

	@GetMapping("/all")
	@ResponseBody
	public List<Feature> findAllFeatures() {
		return organizationService.allFeatures();
	}

	@GetMapping("/test")
	@ResponseBody
	public void test() {
		organize();
	}

	@GetMapping("/index")
	public String index(HttpServletRequest request) {
		request.setAttribute("features", organizationService.allFeatures());
		request.setAttribute("microservices", organizationService.allMicroServices());
		return "feature/index";
	}
	
	@GetMapping("/show/microservice/{featureId}")
	@ResponseBody
	public JSONObject toCatoscape(
			@PathVariable(name = "featureId", required = true) String featureId,
			@PathParam(value = "removeUnuseMicroService") Boolean removeUnuseMicroService) {
		JSONObject result = new JSONObject();
		try {
			JSONObject value = null;
			if("all".equals(featureId)) {
				value = organizationService.microServiceToCatoscape(true, organizationService.allFeatures());
			} else {
				List<Feature> features = new ArrayList<>();
				String[] featureIds = featureId.split(",");
				for(String fid : featureIds) {
					Integer temp = Integer.valueOf(fid);
					if(temp == null) {
						throw new Exception("请输入正确的featureId");
					}
					Feature feature = organizationService.findFeatureById(temp);
					if(feature == null) {
						throw new Exception("没有featureId为 " + featureId + " 的Feature");
					}
					features.add(feature);
				}
				value = organizationService.microServiceToCatoscape(true, features);
				result.put("detail", value.getJSONObject("detail"));
				if(features.size() == 1) {
					result.put("traceId", value.getString("traceId"));
				}
			}
			result.put("result", "success");
			result.put("removeUnuseMicroService", removeUnuseMicroService);
			result.put("value", value.getJSONObject("value"));
			result.put("microservice", value.getJSONArray("microservice"));
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@GetMapping("/show/function/microservice")
	@ResponseBody
	public JSONObject findFunctionsForMicroservice(@RequestParam("microserviceId") Long id, @RequestParam("traceId") String traceId) {
		JSONObject result = new JSONObject();
		try {
			System.out.println(id + " " + traceId);
			MicroService ms = jaegerService.findMicroServiceById(id);
			if(ms == null) {
				throw new Exception("没有id为 " + id + " 的MicroService");
			}
			List<Span> spans = jaegerService.findSpansByMicroserviceIdAndTraceId(id, traceId);
			System.out.println(spans.size());
			for(Span span : spans) {
				SpanStartWithFunction spanStartWithFunction = jaegerService.findSpanStartWithFunctionByTraceIdAndSpanId(span.getTraceId(), span.getSpanId());
				if(spanStartWithFunction == null) {
					System.out.println(span.getTraceId() + " " + span.getSpanId() + " " + ms.getName() + " " + " null");
				} else {
					System.out.println(spanStartWithFunction.getFunction());
				}
			}
			Map<String, List<SpanWithFunctions>> spanIdToSpanWithFunctions = new HashMap<>();
			
			
			result.put("result", "success");
			result.put("spans", spans);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
//		System.out.println(ms);
//		SpanStartWithFunction spanStartWithFunction = jaegerService.findSpanStartWithFunctionByTraceIdAndSpanId(span.getTraceId(), span.getSpanId());
//		System.out.println(spanStartWithFunction);
//		System.out.println(spanStartWithFunction.getFunction());
//		List<FunctionDynamicCallFunction> spanFunctionCalls = dynamicAnalyseService.findFunctionCallsByTraceIdAndSpanId(span.getTraceId(), span.getSpanId());
//		SpanWithFunctions spanWithFunctions = new SpanWithFunctions(spanStartWithFunction, spanFunctionCalls);
//		result.put(span.getSpanId(), spanWithFunctions);
		return result;
	}
	
	@GetMapping("/show/function/span/{id}")
	@ResponseBody
	public JSONObject findFunctionsForSpan(@PathVariable("id") Long id) {
		try {
			
			System.out.println(id);
			JSONObject result = new JSONObject();
			Span span = jaegerService.findSpanById(id);
			System.out.println(span);
			SpanStartWithFunction spanStartWithFunction = jaegerService.findSpanStartWithFunctionByTraceIdAndSpanId(span.getTraceId(), span.getSpanId());
			System.out.println(spanStartWithFunction);
			System.out.println(spanStartWithFunction.getFunction());
			List<FunctionDynamicCallFunction> spanFunctionCalls = dynamicAnalyseService.findFunctionCallsByTraceIdAndSpanId(span.getTraceId(), span.getSpanId());
			SpanWithFunctions spanWithFunctions = new SpanWithFunctions(spanStartWithFunction, spanFunctionCalls);
			result.put(span.getSpanId(), spanWithFunctions);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@GetMapping("/show/function/{spanCallSpanId}")
	@ResponseBody
	public JSONObject findFunctionsForSpanCallSpan(@PathVariable("spanCallSpanId") Long spanCallSpanId) {
		System.out.println(spanCallSpanId);
		JSONObject result = new JSONObject();
		SpanCallSpan spanCallSpan = jaegerService.findSpanCallSpanById(spanCallSpanId);
		System.out.println(spanCallSpan);
		SpanStartWithFunction spanStartWithFunction = jaegerService.findSpanStartWithFunctionByTraceIdAndSpanId(spanCallSpan.getSpan().getTraceId(), spanCallSpan.getSpan().getSpanId());
		System.out.println(spanStartWithFunction);
		System.out.println(spanStartWithFunction.getFunction());
		List<FunctionDynamicCallFunction> spanFunctionCalls = dynamicAnalyseService.findFunctionCallsByTraceIdAndSpanId(spanCallSpan.getSpan().getTraceId(), spanCallSpan.getSpan().getSpanId());
		SpanWithFunctions spanWithFunctions = new SpanWithFunctions(spanStartWithFunction, spanFunctionCalls);
		SpanStartWithFunction callSpanStartWithFunction = jaegerService.findSpanStartWithFunctionByTraceIdAndSpanId(spanCallSpan.getCallSpan().getTraceId(), spanCallSpan.getCallSpan().getSpanId());
		List<FunctionDynamicCallFunction> callSpanFunctionCalls = dynamicAnalyseService.findFunctionCallsByTraceIdAndSpanId(spanCallSpan.getCallSpan().getTraceId(), spanCallSpan.getCallSpan().getSpanId());
		SpanWithFunctions callSpanWithFunctions = new SpanWithFunctions(callSpanStartWithFunction, callSpanFunctionCalls);
		return result;
	}
	

	@Deprecated
	@GetMapping("/svg/{featureId}")
	@ResponseBody
	public JSONObject renderSVG(@PathVariable("featureId") Integer featureId) {
		System.out.println(featureId);
		JSONObject result = new JSONObject();
		try {
			Feature feature = organizationService.findFeatureById(featureId);
			String svg = organizationService.featureToSVG(feature);
			result.put("result", "success");
			result.put("svg", svg);
			System.out.println(svg);
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
}
