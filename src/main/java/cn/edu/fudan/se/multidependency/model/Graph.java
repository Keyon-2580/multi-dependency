package cn.edu.fudan.se.multidependency.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;

public class Graph {
	
	private Map<Long, Node> nodes = new HashMap<>();
	
	private Map<Node, String> nodeNames = new HashMap<>();
	
	private Map<Node, Map<Node, List<Relation>>> edges = new HashMap<>();
	
	public String nodeName(Node node) {
		return nodeNames.get(node);
	}
	
	public JSONObject toCytoscape() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		
		for(Node node : allNodes().values()) {
			JSONObject featureData = new JSONObject();
			featureData.put("id", node.getId());
			featureData.put("name", nodeName(node));
			JSONObject featureNode = new JSONObject();
			featureNode.put("data", featureData);
			nodes.add(featureNode);
		}
		for(Node start : this.edges.keySet()) {
			for(Node end : this.edges.get(start).keySet()) {
				for(Relation relation : this.edges.get(start).get(end)) {
					JSONObject containFeature = new JSONObject();
					containFeature.put("id", relation.getId());
					containFeature.put("source", start.getId());
					containFeature.put("target", end.getId());
					
					JSONObject containEdge = new JSONObject();
					containEdge.put("data", containFeature);
					edges.add(containEdge);
				}
			}
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
		
	}
	
	public void addNode(Node node, String name) {
		this.nodes.put(node.getId(), node);
		this.nodeNames.put(node, name);
	}
	
	public boolean addEdge(Relation relation) {
		Node startNode = findNode(relation.getStartNodeGraphId());
		if(startNode == null) {
			return false;
		}
		Map<Node, List<Relation>> startToRelations = edges.getOrDefault(startNode, new HashMap<>());
		Node endNode = findNode(relation.getEndNodeGraphId());
		if(endNode == null) {
			return false;
		}
		List<Relation> relations = startToRelations.getOrDefault(endNode, new ArrayList<>());
		relations.add(relation);
		startToRelations.put(startNode, relations);
		edges.put(endNode, startToRelations);
		return true;
	}
	
	public Map<Long, Node> allNodes() {
		return new HashMap<>(nodes);
	}
	
	public Map<Node, Map<Node, List<Relation>>> allEdges() {
		return new HashMap<>(edges);
	}
	
	public Node findNode(Long nodeId) {
		return nodes.get(nodeId);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(nodes.values().toString()).append("\n");
		for(Node start : allEdges().keySet()) {
			for(Node end : allEdges().get(start).keySet()) {
				for(Relation relation : allEdges().get(start).get(end)) {
					builder.append(relation.getStartNodeGraphId()).append(" -> ").append(relation.getEndNodeGraphId()).append("\n");
				}
			}
		}
		return builder.toString();
	}
	
}
