package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.service.spring.DependencyOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.JaegerService;

@Controller
@RequestMapping("/function")
public class FunctionController {
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;

	@Autowired
	private JaegerService jaegerService;

	@Autowired
	private FeatureOrganizationService organizationService;
	
	@Autowired
	private DependencyOrganizationService dependencyOrganizationService;
	
	private List<Function> test(Function startFunction, List<FunctionDynamicCallFunction> calls, Long depth) {
		List<Function> result = new ArrayList<>();
		List<FunctionDynamicCallFunction> temp = new ArrayList<>();
		
		for(FunctionDynamicCallFunction call : calls) {
			if(call.getFunction().equals(startFunction) && call.getFromDepth().equals(depth)) {
				temp.add(call);
			}
		}
		temp.sort(new Comparator<FunctionDynamicCallFunction>() {
			@Override
			public int compare(FunctionDynamicCallFunction o1, FunctionDynamicCallFunction o2) {
				return o1.getToOrder().compareTo(o2.getToOrder());
			}
		});
		for(FunctionDynamicCallFunction t : temp) {
			result.add(t.getCallFunction());
		}
		return result;
	}
	
	private JSONObject test1(Function f, List<FunctionDynamicCallFunction> calls, Long depth) {
		JSONObject result = new JSONObject();
		result.put("text", f.getFunctionName() + " " + f.getParametersIdentifies());
		JSONArray tagsArray = new JSONArray();
		tagsArray.add("function");
		result.put("tags", tagsArray);
		List<Function> callFunctions = test(f, calls, depth);
		if(callFunctions.size() == 0) {
			return result;
		}
		JSONArray nodes = new JSONArray();
		for(Function callFunction : callFunctions) {
			nodes.add(test1(callFunction, calls, depth + 1));
		}
		result.put("nodes", nodes);
		return result;
	}
	
	@GetMapping("/treeview/span")
	@ResponseBody
	public JSONObject dynamicFunctionToTreeView(@RequestParam("spanGraphId") Long spanGraphId) {
		JSONObject result = new JSONObject();
		try {
			JSONArray functionArray = new JSONArray();
			
			Span span = jaegerService.findSpanById(spanGraphId);
			System.out.println(span);
			SpanStartWithFunction spanStartWithFunction = jaegerService.findSpanStartWithFunctionByTraceIdAndSpanId(span.getTraceId(), span.getSpanId());
			System.out.println(spanStartWithFunction);
			System.out.println(spanStartWithFunction.getFunction());
			List<FunctionDynamicCallFunction> spanFunctionCalls = dynamicAnalyseService.findFunctionCallsByTraceIdAndSpanId(span.getTraceId(), span.getSpanId());
			System.out.println(spanFunctionCalls.size());
			functionArray.add(test1(spanStartWithFunction.getFunction(), spanFunctionCalls, 0L));
			result.put("result", "success");
			result.put("value", functionArray);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@GetMapping("/cytoscape/file")
	@ResponseBody
	@Deprecated
	public JSONObject dynamicFileCallFromMicroServiceInOneTraceToCatoscape(
			@RequestParam("microserviceGraphId") Long microserviceGraphId, 
			@RequestParam(required=false, name="traceId") String traceId,
			@RequestParam(required=false, name="callType") String type) {
		JSONObject result = new JSONObject();
		try {
			System.out.println(microserviceGraphId);
			MicroService ms = jaegerService.findMicroServiceById(microserviceGraphId);
			if(ms == null) {
				throw new Exception("没有id为 " + microserviceGraphId + " 的MicroService");
			}
			List<FunctionDynamicCallFunction> calls = new ArrayList<>();
			if(traceId == null) {
				calls = dynamicAnalyseService.findFunctionDynamicCallsByMicroService(ms);
			} else {
				Trace trace = jaegerService.findTraceByTraceId(traceId);
				calls = dynamicAnalyseService.findFunctionDynamicCallsByTraceAndMicroService(trace, ms);
			}
			dependencyOrganizationService.dynamicCallDependency(calls);
			result.put("result", "success");
			if(type == null || "file".equals(type)) {
				result.put("value", dependencyOrganizationService.fileCallToCytoscape());
			} else if("package".equals(type)) {
				result.put("value", dependencyOrganizationService.directoryCallToCytoscape());
			} else if("function".equals(type)) {
				result.put("value", dependencyOrganizationService.functionCallToCytoscape());
			} else if("fileAndPackage".equals(type)) {
				result.put("value", dependencyOrganizationService.packageAndFileToCytoscape());
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		
		return result;
	}
	
	@GetMapping("/treeview")
	@ResponseBody
	@Deprecated
	public JSONObject toTreeView() {
		JSONObject result = new JSONObject();
		try {
			JSONArray featureArray = new JSONArray();
			List<Feature> features = organizationService.allFeatures();
			for(Feature feature : features) {
				JSONArray featureTags = new JSONArray();
				featureTags.add("feature");
				JSONObject featureJson = new JSONObject();
				featureJson.put("text", feature.getFeatureName());
				featureJson.put("tags", featureTags);
				featureJson.put("href", feature.getFeatureId());
				
				Set<MicroService> relatedMicroServices = organizationService.findRelatedMicroServiceForFeatures(feature);
				JSONArray msArray = new JSONArray();
				for(MicroService ms : relatedMicroServices) {
					JSONObject microservice = new JSONObject();
					microservice.put("text", ms.getName());
					JSONArray microserviceTags = new JSONArray();
					microserviceTags.add("microservice");
					microservice.put("tags", microserviceTags);
					microservice.put("href", ms.getId());
					JSONArray spanArray = new JSONArray();
//					Set<Trace> traces = organizationService.findRelatedTracesForFeature(features);
//					List<Span> spans = organizationService.findMicroServiceCreateSpansInTraces(ms, feature);
//					for(Span span : spans) {
//						JSONObject spanJson = new JSONObject();
//						spanJson.put("text", span.getOperationName());
//						JSONArray spanTags = new JSONArray();
//						spanTags.add("span");
//						spanJson.put("tags", spanTags);
//						spanJson.put("href", span.getId());
//						spanArray.add(spanJson);
//					}
					microservice.put("nodes", spanArray);
					msArray.add(microservice);
				}
				
				featureJson.put("nodes", msArray);
				featureArray.add(featureJson);
			}
			
			result.put("result", "success");
			result.put("value", featureArray);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
}
