package fan.md.neo4j.service;

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
import fan.md.model.relation.Relations;
import fan.md.model.relation.code.FileContainType;
import fan.md.model.relation.code.FunctionCallFunction;
import fan.md.model.relation.code.PackageContainFile;
import fan.md.model.relation.code.TypeContainsFunction;
import fan.md.model.relation.code.TypeExtendsType;
import fan.md.model.relation.code.TypeImplementsType;
import fan.md.utils.FileUtils;

public class BatchInserterService {
	
	private BatchInserterService() {
		
	}
	
	private static BatchInserterService instance = new BatchInserterService();
	
	public static BatchInserterService getInstance() {
		return instance;
	}
	
	private BatchInserter inserter = null;
    Label fileLabel = Label.label("File");
    Label functionLabel = Label.label("Function");
    Label packageLabel = Label.label("Package");
    Label typeLabel = Label.label("Type");
    
    Map<String, Object> properties = new HashMap<>();
    
    RelationshipType fileContainType = RelationshipType.withName(Relations.FILE_CONTAIN_TYPE.toString());
    RelationshipType packageContainFile = RelationshipType.withName( "PACKAGE_CONTAIN_FILE" );
    RelationshipType typeContainsFunction = RelationshipType.withName( "TYPE_CONTAINS_FUNCTION" );
    RelationshipType functionCallFunction = RelationshipType.withName( "FUNCTION_CALL_FUNCTION" );
    RelationshipType typeExtendsType = RelationshipType.withName( "TYPE_EXTENDS_TYPE" );
    RelationshipType typeImplementsType = RelationshipType.withName( "TYPE_IMPLEMENTS_TYPE" );
    
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
	
	public Long insertPackage(Package pck) {
		properties.clear();
		properties.put("packageName", pck.getPackageName());
		properties.put("entityId", pck.getEntityId());
		pck.setId(inserter.createNode(properties, packageLabel));
		return pck.getId();
	}
	
	public Long insertRelationFileContainType(FileContainType relation) {
		properties.clear();
		return inserter.createRelationship(relation.getFile().getId(), relation.getType().getId(), fileContainType, properties);
	}
	
	public Long insertRelationPackageContainFile(PackageContainFile relation) {
		properties.clear();
		return inserter.createRelationship(relation.getPck().getId(), relation.getFile().getId(), packageContainFile, properties);
	}
	
	public Long insertRelationFunctionCallFunction(FunctionCallFunction call) {
		properties.clear();
		return inserter.createRelationship(call.getFunction().getId(), call.getCallFunction().getId(), functionCallFunction, properties);
	}
	
	public Long insertRelationTypeContainsFunction(TypeContainsFunction relation) {
		properties.clear();
		return inserter.createRelationship(relation.getType().getId(), relation.getFunction().getId(), typeContainsFunction, properties);
	}
	
	public Long insertRelationTypeExtendsType(TypeExtendsType relation) {
		properties.clear();
		return inserter.createRelationship(relation.getStart().getId(), relation.getEnd().getId(), typeExtendsType, properties);
	}
	
	public Long insertRelationTypeImplementsFunction(TypeImplementsType relation) {
		properties.clear();
		return inserter.createRelationship(relation.getStart().getId(), relation.getEnd().getId(), typeImplementsType, properties);
	}
	
	public void close() {
		if(inserter != null) {
			inserter.shutdown();
		}
	}
	
	
	
}
