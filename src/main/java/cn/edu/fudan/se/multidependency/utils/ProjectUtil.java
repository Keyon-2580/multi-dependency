package cn.edu.fudan.se.multidependency.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;

public class ProjectUtil {
	
	public static JSONObject toCytoscapeNode(Node node, String type) {
		return toCytoscapeNode(node, node.getName(), type);
	}
	
	public static JSONObject toCytoscapeNode(Node node, String name, String type) {
		return toCytoscapeNode(node.getId(), name, type);
	}
	
	public static JSONObject toCytoscapeNode(Long id, String name, String type) {
		return toCytoscapeNode(id + "", name, type);
	}
	
	public static JSONObject toCytoscapeNode(String id, String name, String type) {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("id", id);
		data.put("name", name);
		result.put("data", data);
		return result;
	}
	
	public static JSONObject relationToEdge(Node start, Node end, String type, String value, boolean autoId) {
		JSONObject edge = new JSONObject();
		JSONObject data = new JSONObject();
		if(autoId) {
			data.put("id", start.getId() + "_" + end.getId());
		}
		data.put("source", start.getId());
		data.put("target", end.getId());
		if(!StringUtils.isBlank(type)) {
			data.put("type", type);
		}
		data.put("value", StringUtils.isBlank(value) ? "" : value);
		edge.put("data", data);
		return edge;
	}
	
	public static boolean isMicroServiceCall(MicroService start, MicroService end, Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls) {
		if(msCalls == null) {
			return false;
		}
		return msCalls.getOrDefault(start, new HashMap<>()).get(end) != null;
	}

	public static boolean isMicroServiceDependOn(MicroService start, MicroService end, Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns) {
		if(msDependOns == null) {
			return false;
		}
		return msDependOns.getOrDefault(start, new HashMap<>()).get(end) != null;
	}
}
