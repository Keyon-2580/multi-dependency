package cn.edu.fudan.se.multidependency.service.code;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.StaticCodeNodes;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionParameterType;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionReturnType;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeExtendsType;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeImplementsType;
import cn.edu.fudan.se.multidependency.model.relation.code.VariableIsType;
import cn.edu.fudan.se.multidependency.service.BatchInserterService;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;
import depends.deptypes.DependencyType;
import depends.entity.EmptyTypeEntity;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;

public abstract class DependsCodeInserterForNeo4jServiceImpl implements InserterForNeo4j {

	public DependsCodeInserterForNeo4jServiceImpl(String projectPath, EntityRepo entityRepo, String databasePath, boolean delete, Language language) {
		super();
		this.entityRepo = entityRepo;
		this.databasePath = databasePath;
		this.delete = delete;
		this.language = language;
		this.batchInserterService = BatchInserterService.getInstance();
		this.nodes = new StaticCodeNodes();
		this.nodes.setProject(new Project(projectPath, projectPath, language));
		this.relations = new Relations();
	}

	protected EntityRepo entityRepo;
	protected String databasePath;
	protected boolean delete;
	protected Language language;
	
	protected StaticCodeNodes nodes;
	protected Relations relations;
	
	protected BatchInserterService batchInserterService;
	
	protected abstract void insertNodesWithContainRelations() throws LanguageErrorException;
	
	protected abstract void insertRelations() throws LanguageErrorException;
	
	protected void insertNodeToNodes(Node node, Integer entityId) {
		this.nodes.insertNode(node, entityId);
	}
	
	protected void insertRelationToRelations(Relation relation) {
		this.relations.insertRelation(relation);
	}
	
	@Override
	public void insertToNeo4jDataBase() throws Exception {
		System.out.println("start to store datas to database");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("开始时间：" + sdf.format(currentTime));
		batchInserterService.init(databasePath, delete);
		batchInserterService.insertNode(this.nodes.getProject());
		
		insertNodesWithContainRelations();
		insertRelations();
		
		// 将节点和关系放入nodes和relations后，统一插入neo4j
		insertToNeo4j();
		
		closeBatchInserter();
		currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("结束时间：" + sdf.format(currentTime));
	}
	
	private void insertToNeo4j() {
		this.batchInserterService.insertNodes(nodes);
		this.batchInserterService.insertRelations(relations);
	}

	protected void extractRelationsFromTypes() {
		Map<Integer, Type> types = this.nodes.findTypes();
		types.forEach((id, type) -> {
			// 继承与实现
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(id);
			Collection<TypeEntity> inherits = typeEntity.getInheritedTypes();
			inherits.forEach(inherit -> {
				Type other = types.get(inherit.getId());
				if(other != null) {
					TypeExtendsType typeExtends = new TypeExtendsType(type, other);
					insertRelationToRelations(typeExtends);
				}
			});
			Collection<TypeEntity> imps = typeEntity.getImplementedTypes();
			imps.forEach(imp -> {
				Type other = types.get(imp.getId());
				if(other != null) {
					TypeImplementsType typeImplements = new TypeImplementsType(type, other);
					insertRelationToRelations(typeImplements);
				}
			});
		});
	}
	
	protected void extractRelationsFromVariables() {
		Map<Integer, Variable> variables = this.nodes.findVariables();
		
		variables.forEach((entityId, variable) -> {
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId);
			TypeEntity typeEntity = varEntity.getType();
			if(typeEntity == null) {
//				System.out.println(varEntity);
			} else {
				if(typeEntity.getClass() == TypeEntity.class) {
					Type type = this.nodes.findType(typeEntity.getId());
					if(type != null) {
						VariableIsType variableIsType = new VariableIsType(variable, type);
						insertRelationToRelations(variableIsType);
					}
				} else {
					if(typeEntity.getClass() == EmptyTypeEntity.class) {
//						System.out.println(typeEntity);
					} else {
//						System.out.println("extractRelationsFromVariables " + typeEntity.getClass());
					}
				}
			}
		});
	}
	
	protected void extractRelationsFromFunctions() {
		Map<Integer, Function> functions = this.nodes.findFunctions();
		Map<Integer, Type> types = this.nodes.findTypes();
		functions.forEach((id, function) -> {
			// 函数调用
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(id);
			functionEntity.getRelations().forEach(relation -> {
				if(DependencyType.CALL.equals(relation.getType())) {
					if(relation.getEntity() instanceof FunctionEntity) {
						// call其它方法
						Function other = functions.get(relation.getEntity().getId());
						if(other != null) {
							FunctionCallFunction call = new FunctionCallFunction(function, other);
							insertRelationToRelations(call);
						}
					} else {
						///FIXME
					}
				}
				if(DependencyType.RETURN.equals(relation.getType())) {
					Type returnType = types.get(relation.getEntity().getId());
					if(returnType != null) {
						FunctionReturnType functionReturnType = new FunctionReturnType(function, returnType);
						insertRelationToRelations(functionReturnType);
					}
				}
				if(DependencyType.PARAMETER.equals(relation.getType())) {
					Type parameterType = types.get(relation.getEntity().getId());
					if(parameterType != null) {
						FunctionParameterType functionParameterType = new FunctionParameterType(function, parameterType);
						insertRelationToRelations(functionParameterType);
					}
				}
			});
		});
	}

	protected abstract void extractRelationsFromFiles();
	
	public EntityRepo getEntityRepo() {
		return entityRepo;
	}

	public void setEntityRepo(EntityRepo entityRepo) {
		this.entityRepo = entityRepo;
	}

	@Override
	public void setDatabasePath(String databasePath) {
		this.databasePath = databasePath;
	}

	@Override
	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	@Override
	public void setLanguage(Language language) {
		this.language = language;
	}

	protected void closeBatchInserter() {
		if(this.batchInserterService != null) {
			this.batchInserterService.close();
		}
	}

	@Override
	public Nodes getNodes() {
		return nodes;
	}

	@Override
	public Relations getRelations() {
		return relations;
	}
	

}
