package cn.edu.fudan.se.multidependency.utils;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;

public class CytoscapeUtil {
	
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
	
	public static JSONObject relationToEdge(Object startId, Object endId, String type, String value, boolean autoId) {
		JSONObject edge = new JSONObject();
		JSONObject data = new JSONObject();
		if(autoId) {
			data.put("id", startId + "_" + endId);
		}
		data.put("source", startId);
		data.put("target", endId);
		if(!StringUtils.isBlank(type)) {
			data.put("type", type);
		}
		data.put("value", StringUtils.isBlank(value) ? "" : value);
		edge.put("data", data);
		return edge;
	}
	
	public static JSONObject relationToEdge(Node start, Node end, String type, String value, boolean autoId) {
		return relationToEdge(start.getId(), end.getId(), type, value, autoId);
	}
}
