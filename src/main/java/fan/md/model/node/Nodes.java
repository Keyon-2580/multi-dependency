package fan.md.model.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Nodes {

	private Map<NodeType, Map<Integer, Node>> allNodes = new HashMap<>();
	
	public void clear() {
		allNodes.clear();
	}
	
	public void clear(NodeType nodeType) {
		Map<Integer, Node> nodes = allNodes.get(nodeType);
		if(nodes != null) {
			nodes.clear();
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param id 不一定是节点的Id
	 */
	public void insertNode(Node node, Integer id) {
		Map<Integer, Node> nodes = allNodes.get(node.getNodeType());
		nodes = nodes == null ? new HashMap<>() : nodes;
		nodes.put(id, node);
		allNodes.put(node.getNodeType(), nodes);
	}
	
	public Node findNode(Integer entityId) {
		for(Map<Integer, Node> nodes : allNodes.values()) {
			Node node = nodes.get(entityId);
			if(nodes.get(entityId) != null) {
				return node;
			}
		}
		return null;
	}
	
	public Node findNode(Integer entityId, NodeType nodeType) {
		Map<Integer, Node> nodes = allNodes.get(nodeType);
		if(nodes == null) {
			return null;
		}
		return nodes.get(entityId);
	}
	
	public Collection<? extends Node> findNodesCollection(NodeType nodeType) {
		Map<Integer, Node> nodes = allNodes.get(nodeType);
		return nodes == null ? new ArrayList<>() : nodes.values();
	}
	
	public Map<Integer, ? extends Node> findNodesMap(NodeType nodeType) {
		Map<Integer, ? extends Node> nodes = allNodes.get(nodeType);
		return nodes == null ? new HashMap<>() : nodes;
	}
}
