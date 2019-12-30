package cn.edu.fudan.se.multidependency.service;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.utils.FileUtils;

public class BatchInserterService implements Closeable {
	private BatchInserterService() {}
	private static BatchInserterService instance = new BatchInserterService();
	public static BatchInserterService getInstance() {
		return instance;
	}
	
	private BatchInserter inserter = null;
	
    private Map<RelationType, RelationshipType> mapRelations = new HashMap<>();
    private Map<NodeType, Label> mapLabels = new HashMap<>();
    
	public void init(String databasePath, boolean initDatabase) throws Exception {
		File directory = new File(databasePath);
		if(initDatabase) {
			FileUtils.delFile(directory);
		}
		inserter = BatchInserters.inserter(directory);
	    /*inserter.createDeferredSchemaIndex( fileLabel ).on( "fileName" ).create();
	    inserter.createDeferredSchemaIndex( fileLabel ).on( "path" ).create();
	    inserter.createDeferredSchemaIndex( functionLabel ).on( "functionName" ).create();
	    inserter.createDeferredSchemaIndex( packageLabel ).on( "packageName" ).create();
	    inserter.createDeferredSchemaIndex( typeLabel ).on( "typeName" ).create();
	    inserter.createDeferredSchemaIndex( typeLabel ).on( "packageName" ).create();*/
	    for(NodeType nodeType : NodeType.values()) {
	    	mapLabels.put(nodeType, Label.label(nodeType.toString()));
	    }
    	for(RelationType relationType : RelationType.values()) {
//    		mapRelations.put(relationType, RelationshipType.withName(relationType.toString()));
    		mapRelations.put(relationType, relationType);
    	}
	}
	
	public Long insertNode(Node node) {
		node.setId(inserter.createNode(node.getProperties(), mapLabels.get(node.getNodeType())));
		return node.getId();
	}
	
	public Long insertRelation(Relation relation) {
		relation.setId(inserter.createRelationship(relation.getStartNodeGraphId(), 
				relation.getEndNodeGraphId(), mapRelations.get(relation.getRelationType()), relation.getProperties()));
//		relation.setId(inserter.createRelationship(relation.getStartNodeGraphId(), 
//				relation.getEndNodeGraphId(), relation.getRelationType(), relation.getProperties()));

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
			nodes.forEach((otherId, node) -> {
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
