package cn.edu.fudan.se.multidependency.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.service.spring.DynamicAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.JaegerService;
import cn.edu.fudan.se.multidependency.service.spring.OrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.SpanWithFunctions;

@Controller
@RequestMapping("/function")
public class FunctionController {
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;

	@Autowired
	private JaegerService jaegerService;

	@Autowired
	private OrganizationService organizationService;
	
	@GetMapping("/index")
	public String index(HttpServletRequest request) {		
		request.setAttribute("features", organizationService.allFeatures());
		return "function";
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
			SpanWithFunctions spanWithFunctions = new SpanWithFunctions(spanStartWithFunction, spanFunctionCalls);
			
			
			result.put("result", "success");
			result.put("value", functionArray);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	
	@GetMapping("/treeview")
	@ResponseBody
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
				
				List<MicroService> relatedMicroServices = organizationService.findRelatedMicroServiceForFeatures(feature);
				JSONArray msArray = new JSONArray();
				for(MicroService ms : relatedMicroServices) {
					JSONObject microservice = new JSONObject();
					microservice.put("text", ms.getName());
					JSONArray microserviceTags = new JSONArray();
					microserviceTags.add("microservice");
					microservice.put("tags", microserviceTags);
					JSONArray spanArray = new JSONArray();
					List<Span> spans = organizationService.findMicroServiceCreateSpansInTraces(ms, feature);
					for(Span span : spans) {
						JSONObject spanJson = new JSONObject();
						spanJson.put("text", span.getOperationName());
						JSONArray spanTags = new JSONArray();
						spanTags.add("span");
						spanJson.put("tags", spanTags);
						spanJson.put("href", span.getId());
						spanArray.add(spanJson);
					}
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
