package cn.edu.fudan.se.multidependency.neo4j.service;

import java.io.Closeable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.NodeType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
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
    		mapRelations.put(relationType, RelationshipType.withName(relationType.toString()));
    	}
	}
	
	public Long insertNode(Node node) {
		node.setId(inserter.createNode(node.getProperties(), mapLabels.get(node.getNodeType())));
		return node.getId();
	}
	
	public Long insertRelation(Relation relation) {
		relation.setId(inserter.createRelationship(relation.getStartNodeGraphId(), 
				relation.getEndNodeGraphId(), mapRelations.get(relation.getRelationType()), relation.getProperties()));
		return relation.getId();
	}
	
	@Override
	public void close() {
		if(inserter != null) {
			inserter.shutdown();
		}
	}
	
}
