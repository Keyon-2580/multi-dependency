package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;

@Service
public class CacheService {

	private final Map<Long, Node> idToNodeCache = new ConcurrentHashMap<>();
	private final Map<Node, Map<NodeLabelType, Node>> nodeBelongToNodeCache = new ConcurrentHashMap<>();
	
    public void clearCache() {
    	idToNodeCache.clear();
    	nodeBelongToNodeCache.clear();
    }
    
    public void cacheNodeBelongToNode(Node node, Node belongToNode) {
    	Map<NodeLabelType, Node> belongToNodes = nodeBelongToNodeCache.getOrDefault(node, new ConcurrentHashMap<>());
    	belongToNodes.put(belongToNode.getNodeType(), belongToNode);
    	this.nodeBelongToNodeCache.put(node, belongToNodes);
    	cacheNodeById(node);
    	cacheNodeById(belongToNode);
    }
    
    public Node findNodeBelongToNode(Node node, NodeLabelType label) {
    	cacheNodeById(node);
    	Map<NodeLabelType, Node> belongToNodes = nodeBelongToNodeCache.get(node);
    	return belongToNodes == null ? null : belongToNodes.get(label);
    }
	
	public Node findNodeById(long id) {
		return idToNodeCache.get(id);
	}
	
	public Node cacheNodeById(Node node) {
		idToNodeCache.put(node.getId(), node);
		return node;
	}
	
}
