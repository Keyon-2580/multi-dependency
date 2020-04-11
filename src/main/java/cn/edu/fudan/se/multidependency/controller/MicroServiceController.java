package cn.edu.fudan.se.multidependency.controller;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;
import cn.edu.fudan.se.multidependency.service.spring.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;

@Controller
@RequestMapping("/microservice")
public class MicroServiceController {
	
	@Autowired
	private MicroserviceService msService;
	
	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@GetMapping(value = {"/", "/index"})
	public String index(HttpServletRequest request) {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls = msService.msCalls();
		
		Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns = msService.msDependOns();
		
		request.setAttribute("calls", msCalls);
		request.setAttribute("depends", msDependOns);
		return "microservice/index";
	}
	
	@GetMapping("/cytoscape")
	@ResponseBody
	public JSONObject getJson() {
		JSONObject result = new JSONObject();
		
		result.put("value", json());
		return result;
	}
	
	public JSONObject json() {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls = msService.msCalls();
		
		Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns = msService.msDependOns();
		
		Collection<MicroService> mses = featureOrganizationService.allMicroServices();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		JSONObject data = new JSONObject();
		data.put("nodes", nodes);
		data.put("edges", edges);
		for(MicroService ms : mses) {
			JSONObject node = new JSONObject();
			node.put("id", ms.getId());
			node.put("name", ms.getName());
			node.put("type", "microservice");
			node.put("length", ms.getName().length() * 10);
			JSONObject temp = new JSONObject();
			temp.put("data", node);
			nodes.add(temp);
		}
		for(MicroService start : msDependOns.keySet()) {
			for(MicroService end : msDependOns.get(start).keySet()) {
				MicroServiceDependOnMicroService depend = msDependOns.get(start).get(end);
				JSONObject edge = new JSONObject();
				edge.put("id", depend.getId());
				edge.put("source", start.getId());
				edge.put("target", end.getId());
				edge.put("type", "dependon");
				JSONObject temp = new JSONObject();
				temp.put("data", edge);
				edges.add(temp);
			}
		}
		for(MicroService start : msCalls.keySet()) {
			for(MicroService end : msCalls.get(start).keySet()) {
				if(!msService.isMicroServiceDependOn(start, end)) {
					MicroServiceCallMicroService call = msCalls.get(start).get(end);
					JSONObject edge = new JSONObject();
					edge.put("id", call.getId());
					edge.put("source", start.getId());
					edge.put("target", end.getId());
					edge.put("type", "call");
					JSONObject temp = new JSONObject();
					temp.put("data", edge);
					edges.add(temp);
				}
			}
		}
		return data;
	}
	
}
