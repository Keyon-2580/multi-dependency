package cn.edu.fudan.se.multidependency.service;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Label;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.utils.FileUtil;

public class BatchInserterService implements Closeable {
	private BatchInserterService() {}
	private static BatchInserterService instance = new BatchInserterService();
	public static BatchInserterService getInstance() {
		return instance;
	}
	
	private BatchInserter inserter = null;
	
    private Map<NodeLabelType, Label> mapLabels = new HashMap<>();
    
	public void init(String databasePath, boolean initDatabase) throws Exception {
		File directory = new File(databasePath);
		if(initDatabase) {
			FileUtil.delFile(directory);
		}
		inserter = BatchInserters.inserter(directory);
	    for(NodeLabelType nodeType : NodeLabelType.values()) {
	    	Label label = Label.label(nodeType.toString());
	    	mapLabels.put(nodeType, label);
	    	String index = nodeType.indexName();
	    	if(!StringUtils.isBlank(index)) {
	    		// 创建索引
	    		inserter.createDeferredSchemaIndex(label).on(index).create();
	    	}
	    }
	}
	
	public Long insertNode(Node node) {
		node.setId(inserter.createNode(node.getProperties(), mapLabels.get(node.getNodeType())));
		return node.getId();
	}
	
	public Long insertRelation(Relation relation) {
		relation.setId(inserter.createRelationship(relation.getStartNodeGraphId(), relation.getEndNodeGraphId(), relation.getRelationType(), relation.getProperties()));
		return relation.getId();
	}

	/**
	 * 获取节点属性
	 * @param id
	 * @return
	 */
	public Map<String, Object> getNodeProperties(Long id) {
		return inserter.getNodeProperties(id);
	}
	
	/**
	 * 获取关系属性
	 * @param id
	 * @return
	 */
	public Map<String, Object> getRelationshipProperties(Long id) {
		return inserter.getRelationshipProperties(id);
	}
	
	/**
	 * 获取某个节点的所有关系的id
	 * @param nodeId
	 * @return
	 */
	public List<Long> getRelationshipIds(Long nodeId) {
		List<Long> result = new ArrayList<>();
		for(Long id : inserter.getRelationshipIds(nodeId)) {
			result.add(id);
		}
		return result;
		
	}
	
	@Override
	public void close() {
		if(inserter != null) {
			inserter.shutdown();
		}
	}
	
	public boolean nodeExists(Long id) {
		return id == null ? false : inserter.nodeExists(id);
	}
	
	public boolean relationExists(Long id) {
		return id == null ? false : (inserter.getRelationshipById(id) != null);
	}
	
	public void insertNodes(Nodes allNodes) {
		allNodes.getAllNodes().forEach((nodeType, nodes) -> {
			nodes.forEach(node -> {
				if(!nodeExists(node.getId())) {
					insertNode(node);
				}
			});
		});
	}
	
	public void insertRelations(Relations allRelations) {
		allRelations.getAllRelations().forEach((relationType, relations) -> {
			relations.forEach(relation -> {
				insertRelation(relation);
			});
		});
	}
	
}
