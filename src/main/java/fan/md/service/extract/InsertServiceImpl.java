package fan.md.service.extract;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import depends.deptypes.DependencyType;
import depends.entity.FunctionEntity;
import depends.entity.TypeEntity;
import depends.entity.repo.EntityRepo;
import fan.md.exception.LanguageErrorException;
import fan.md.model.Language;
import fan.md.model.node.Project;
import fan.md.model.node.code.CodeFile;
import fan.md.model.node.code.Function;
import fan.md.model.node.code.Package;
import fan.md.model.node.code.Type;
import fan.md.model.relation.code.FunctionCallFunction;
import fan.md.model.relation.code.FunctionParameterType;
import fan.md.model.relation.code.FunctionReturnType;
import fan.md.model.relation.code.TypeExtendsType;
import fan.md.model.relation.code.TypeImplementsType;
import fan.md.neo4j.service.BatchInserterService;

public abstract class InsertServiceImpl implements InsertDependsCodeToNeo4j {

	public InsertServiceImpl(String projectpath, EntityRepo entityRepo, String databasePath, boolean delete, Language language) {
		super();
		this.entityRepo = entityRepo;
		this.databasePath = databasePath;
		this.delete = delete;
		this.language = language;
		this.batchInserterService = BatchInserterService.getInstance();
		this.project = new Project(databasePath, databasePath, language);
	}

	protected EntityRepo entityRepo;
	protected String databasePath;
	protected boolean delete;
	protected Language language;
	
	protected Map<Integer, Package> pcks = new HashMap<>();
	protected Map<Integer, CodeFile> files = new HashMap<>();
	protected Map<Integer, Type> types = new HashMap<>();
	protected Map<Integer, Function> functions = new HashMap<>();
	
	protected BatchInserterService batchInserterService;
	
	protected Project project;
	
	protected abstract void insertNodesWithContainRelations() throws LanguageErrorException;
	
	protected abstract void insertRelations() throws LanguageErrorException;
	
	@Override
	public void insertCodeToNeo4jDataBase() throws Exception {
		System.out.println("start to store datas to database");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("开始时间：" + sdf.format(currentTime));
		batchInserterService.init(databasePath, delete);
		batchInserterService.insertNode(project);
		
		insertNodesWithContainRelations();
		insertRelations();
		
		closeBatchInserter();
		currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("结束时间：" + sdf.format(currentTime));
	}
	
	protected void extractRelationsFromTypes() {
		types.forEach((id, type) -> {
			// 继承与实现
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(id);
			Collection<TypeEntity> inherits = typeEntity.getInheritedTypes();
			inherits.forEach(inherit -> {
				Type other = types.get(inherit.getId());
				if(other != null) {
					TypeExtendsType typeExtends = new TypeExtendsType(type, other);
					batchInserterService.insertRelation(typeExtends);
				}
			});
			Collection<TypeEntity> imps = typeEntity.getImplementedTypes();
			imps.forEach(imp -> {
				Type other = types.get(imp.getId());
				if(other != null) {
					TypeImplementsType typeImplements = new TypeImplementsType(type, other);
					batchInserterService.insertRelation(typeImplements);
				}
			});
		});
	}
	
	protected void extractRelationsFromFunctions() {
		functions.forEach((id, function) -> {
			// 函数调用
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(id);
			functionEntity.getRelations().forEach(relation -> {
				if(DependencyType.CALL.equals(relation.getType())) {
					if(relation.getEntity() instanceof FunctionEntity) {
						Function other = functions.get(relation.getEntity().getId());
						if(other != null) {
							FunctionCallFunction call = new FunctionCallFunction(function, other);
							batchInserterService.insertRelation(call);
						}
					} else {
					}
				}
				if(DependencyType.RETURN.equals(relation.getType())) {
					Type returnType = types.get(relation.getEntity().getId());
					if(returnType != null) {
						FunctionReturnType functionReturnType = new FunctionReturnType(function, returnType);
						batchInserterService.insertRelation(functionReturnType);
					}
				}
				if(DependencyType.PARAMETER.equals(relation.getType())) {
					Type parameterType = types.get(relation.getEntity().getId());
					if(parameterType != null) {
						FunctionParameterType functionParameterType = new FunctionParameterType(function, parameterType);
						batchInserterService.insertRelation(functionParameterType);
					}
				}
			});
		});
	}
	
	@Override
	public EntityRepo getEntityRepo() {
		return entityRepo;
	}

	@Override
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
	
}
