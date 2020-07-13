package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;

@Service
public class CacheService {

	private final Map<String, ProjectFile> pathToFile = new ConcurrentHashMap<>();
	private final Map<String, Package> directoryToPackage = new ConcurrentHashMap<>();
	private final Map<Long, Node> idToNodeCache = new ConcurrentHashMap<>();
	private final Map<Node, Map<NodeLabelType, Node>> nodeBelongToNodeCache = new ConcurrentHashMap<>();
	
    public void clearCache() {
    	idToNodeCache.clear();
    	nodeBelongToNodeCache.clear();
    	pathToFile.clear();
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
		if(node == null) {
			return null;
		}
		idToNodeCache.put(node.getId(), node);
		if(node instanceof ProjectFile) {
//			this.pathToFile.put(((ProjectFile) node).getPath(), (ProjectFile) node);
			cacheFileByPath(((ProjectFile) node).getPath(), (ProjectFile) node);
		} else if(node instanceof Package) {
			cachePackageByDirectory(((Package) node).getDirectoryPath(), (Package) node);
		}
		return node;
	}
	
	public void cacheFileByPath(String path, ProjectFile file) {
		this.pathToFile.put(path, file);
	}
	
	public ProjectFile findFileByPath(String path) {
		return this.pathToFile.get(path);
	}
	
	public void cachePackageByDirectory(String directory, Package pck) {
		this.directoryToPackage.put(directory, pck);
	}
	
	public Package findPackageByDirectoryPath(String directoryPath) {
		return this.directoryToPackage.get(directoryPath);
	}
	
}
