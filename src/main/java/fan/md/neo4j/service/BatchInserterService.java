package fan.md.neo4j.service;

import java.io.Closeable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import fan.md.model.entity.code.CodeFile;
import fan.md.model.entity.code.Function;
import fan.md.model.entity.code.Package;
import fan.md.model.entity.code.Type;
import fan.md.model.relation.Relation;
import fan.md.model.relation.RelationType;
import fan.md.utils.FileUtils;

public class BatchInserterService implements Closeable {
	private BatchInserterService() {}
	private static BatchInserterService instance = new BatchInserterService();
	public static BatchInserterService getInstance() {
		return instance;
	}
	
	private BatchInserter inserter = null;
	
    private Label fileLabel = Label.label("File");
    private Label functionLabel = Label.label("Function");
    private Label packageLabel = Label.label("Package");
    private Label typeLabel = Label.label("Type");
    
    private Map<String, Object> properties = new HashMap<>();
    
    private Map<RelationType, RelationshipType> mapRelations = new HashMap<>();
    
	public void init(String databasePath, boolean initDatabase) throws Exception {
		File directory = new File(databasePath);
		if(initDatabase) {
			FileUtils.delFile(directory);
		}
		inserter = BatchInserters.inserter(directory);
	    inserter.createDeferredSchemaIndex( fileLabel ).on( "fileName" ).create();
	    inserter.createDeferredSchemaIndex( fileLabel ).on( "path" ).create();
	    
	    inserter.createDeferredSchemaIndex( functionLabel ).on( "functionName" ).create();
	    
	    inserter.createDeferredSchemaIndex( packageLabel ).on( "packageName" ).create();
	    
	    inserter.createDeferredSchemaIndex( typeLabel ).on( "typeName" ).create();
	    inserter.createDeferredSchemaIndex( typeLabel ).on( "packageName" ).create();
	    
    	for(RelationType relationType : RelationType.values()) {
    		mapRelations.put(relationType, RelationshipType.withName(relationType.toString()));
    	}
	}
	
	public Long insertCodeFile(CodeFile codeFile) {
		properties.clear();
		properties.put("fileName", codeFile.getFileName());
		properties.put("entityId", codeFile.getEntityId());
		properties.put("path", codeFile.getPath());
		codeFile.setId(inserter.createNode(properties, fileLabel));
		return codeFile.getId();
	}
	
	public Long insertType(Type type) {
		properties.clear();
		properties.put("typeName", type.getTypeName());
		properties.put("entityId", type.getEntityId());
		properties.put("packageName", type.getPackageName());
		type.setId(inserter.createNode(properties, typeLabel));
		return type.getId();
	}
	
	public Long insertFunction(Function function) {
		properties.clear();
		properties.put("functionName", function.getFunctionName());
		properties.put("entityId", function.getEntityId());
		function.setId(inserter.createNode(properties, functionLabel));
		return function.getId();
	}
	
	public Long insertPackageForJava(Package pck) {
		properties.clear();
		properties.put("packageName", pck.getPackageName());
		properties.put("entityId", pck.getEntityId());
		pck.setId(inserter.createNode(properties, packageLabel));
		return pck.getId();
	}
	
	public Long insertRelation(Relation relation) {
		relation.setId(inserter.createRelationship(relation.getStartNodeGraphId(), relation.getEndNodeGraphId(), mapRelations.get(relation.getRelationType()), relation.getProperties()));
		return relation.getId();
	}
	
	@Override
	public void close() {
		if(inserter != null) {
			inserter.shutdown();
		}
	}
	
}
