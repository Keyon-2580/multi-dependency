package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
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

	@GetMapping("/all")
	@ResponseBody
	public List<Feature> findAllFeatures() {
		return organizationService.allFeatures();
	}

	@GetMapping("/index1")
	public String index1(HttpServletRequest request) {
		return "feature/index";
	}
	
	@GetMapping(value = {"/", "/index"})
	public String index(HttpServletRequest request) {
		return "feature";
	}
	
	@GetMapping("/testcase/cytoscape")
	@ResponseBody
	public JSONObject executedByTestCasesToCytoscape() {
		System.out.println("/feature/testcase/cytoscape");
		JSONObject result = new JSONObject();
		try {
			result.put("result", "success");
			System.out.println(organizationService.featureExecuteTestCasesToCytoscape());
			result.put("value", organizationService.featureExecuteTestCasesToCytoscape());
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
	}
	
	@GetMapping("/testcase/treeview")
	@ResponseBody
	public JSONObject executedByTestCasesToTreeView() {
		System.out.println("/feature/testcase/treeview");
		JSONObject result = new JSONObject();
		try {
			result.put("result", "success");
			result.put("value", organizationService.featureExecutedByTestCasesToTreeView());
		} catch (Exception e) {
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		}
		return result;
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
			List<Span> spans = jaegerService.findSpansByMicroserviceAndTraceId(ms, traceId);
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

}
