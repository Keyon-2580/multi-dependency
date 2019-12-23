package fan.md.service.extract;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import depends.deptypes.DependencyType;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.repo.EntityRepo;
import fan.md.model.Language;
import fan.md.model.node.code.CodeFile;
import fan.md.model.node.code.Function;
import fan.md.model.node.code.Package;
import fan.md.model.node.code.Type;
import fan.md.model.relation.code.FileContainsFunction;
import fan.md.model.relation.code.FileContainsType;
import fan.md.model.relation.code.FunctionCallFunction;
import fan.md.model.relation.code.FunctionReturnType;
import fan.md.model.relation.code.PackageContainsFile;
import fan.md.model.relation.code.TypeContainsFunction;
import fan.md.model.relation.code.TypeExtendsType;
import fan.md.model.relation.code.TypeImplementsType;
import fan.md.neo4j.service.BatchInserterService;

public class InsertServiceImple implements InsertDependsCodeToNeo4j {

	public InsertServiceImple(EntityRepo entityRepo, String databasePath, boolean delete, Language language) {
		super();
		this.entityRepo = entityRepo;
		this.databasePath = databasePath;
		this.delete = delete;
		this.language = language;
	}

	protected EntityRepo entityRepo;
	protected String databasePath;
	protected boolean delete;
	protected Language language;
	
	protected Map<Integer, Package> pcks = new HashMap<>();
	protected Map<Integer, CodeFile> files = new HashMap<>();
	protected Map<Integer, Type> types = new HashMap<>();
	protected Map<Integer, Function> functions = new HashMap<>();
	
	@Override
	public void insertCodeToNeo4jDataBase() throws Exception {
		System.out.println("start to store datas to database");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("开始时间：" + sdf.format(currentTime));
		try(BatchInserterService myBatchInserter = BatchInserterService.getInstance()) {
			myBatchInserter.init(databasePath, delete);
			entityRepo.getEntities().forEach(entity -> {
				if(language == Language.cpp && entity instanceof FileEntity) {
					String fileName = entity.getQualifiedName();
					String packageName = null;
					if(fileName.contains("\\")) {
						packageName = fileName.substring(0, fileName.lastIndexOf("\\"));
					} else if(fileName.contains("/")) {
						packageName = fileName.substring(0, fileName.lastIndexOf("/"));
					} else {
						packageName = "default";
					}
					// cpp项目的Package是文件所在的路径
					Package pck = new Package();
					pck.setPackageName(packageName);
					myBatchInserter.insertNode(pck);
					CodeFile file = new CodeFile();
					file.setFileName(entity.getRawName().getName());
					file.setPath(entity.getQualifiedName());
					file.setEntityId(entity.getId());
					myBatchInserter.insertNode(file);
					PackageContainsFile packageContainFile = new PackageContainsFile(pck, file);
					myBatchInserter.insertRelation(packageContainFile);
					entity.getChildren().forEach(fileEntityChild -> {
						if(fileEntityChild instanceof PackageEntity) {
							// 命名空间
						} else if(fileEntityChild instanceof FunctionEntity) {
							// 文件内的函数
							Function function = new Function();
							function.setFunctionName(fileEntityChild.getRawName().getName());
							function.setEntityId(fileEntityChild.getId());
							functions.put(fileEntityChild.getId(), function);
							myBatchInserter.insertNode(function);
							FileContainsFunction containFunction = new FileContainsFunction(file, function);
							myBatchInserter.insertRelation(containFunction);
						}
					});
					List<TypeEntity> typeEntities = ((FileEntity) entity).getDeclaredTypes();
					typeEntities.forEach(typeEntity -> {
						Type type = new Type();
						type.setTypeName(typeEntity.getQualifiedName());
						type.setPackageName(pck.getPackageName());
						type.setEntityId(typeEntity.getId());
						types.put(typeEntity.getId(), type);
						myBatchInserter.insertNode(type);
						FileContainsType fileContainType = new FileContainsType(file, type);
						myBatchInserter.insertRelation(fileContainType);
						typeEntity.getChildren().forEach(typeEntityChild -> {
							if(typeEntityChild instanceof FunctionEntity) {
								Function function = new Function();
								function.setFunctionName(typeEntityChild.getRawName().getName());
								function.setEntityId(typeEntityChild.getId());
								functions.put(typeEntityChild.getId(), function);
								myBatchInserter.insertNode(function);
								TypeContainsFunction containFunction = new TypeContainsFunction(type, function);
								myBatchInserter.insertRelation(containFunction);
							}
						});
					});
				}
				if(language == Language.java && entity instanceof PackageEntity) {
					// Java 从包开始
					Package pck = new Package();
					pck.setPackageName(entity.getQualifiedName());
					pck.setEntityId(entity.getId());
					pcks.put(entity.getId(), pck);
					myBatchInserter.insertNode(pck);
					entity.getChildren().forEach(fileEntity -> {
						if(fileEntity instanceof FileEntity) {
							CodeFile file = new CodeFile();
							file.setFileName(fileEntity.getRawName().getName());
							file.setPath(fileEntity.getQualifiedName());
							file.setEntityId(fileEntity.getId());
							myBatchInserter.insertNode(file);
							PackageContainsFile packageContainFile = new PackageContainsFile(pck, file);
							myBatchInserter.insertRelation(packageContainFile);
							List<TypeEntity> typeEntities = ((FileEntity) fileEntity).getDeclaredTypes();
							typeEntities.forEach(typeEntity -> {
								Type type = new Type();
								type.setTypeName(typeEntity.getQualifiedName());
								type.setPackageName(pck.getPackageName());
								type.setEntityId(typeEntity.getId());
								types.put(typeEntity.getId(), type);
								myBatchInserter.insertNode(type);
								FileContainsType fileContainType = new FileContainsType(file, type);
								myBatchInserter.insertRelation(fileContainType);
								typeEntity.getChildren().forEach(typeEntityChild -> {
									if(typeEntityChild instanceof FunctionEntity) {
										Function function = new Function();
										function.setFunctionName(typeEntityChild.getRawName().getName());
										function.setEntityId(typeEntityChild.getId());
										functions.put(typeEntityChild.getId(), function);
										myBatchInserter.insertNode(function);
										TypeContainsFunction containFunction = new TypeContainsFunction(type, function);
										myBatchInserter.insertRelation(containFunction);
									}
								});
							});
						}
					});
				}
			});
			entityRepo.getEntities().forEach(entity -> {
				entity.getRelations().forEach(relation -> {
				});
			});
			types.forEach((id, type) -> {
				// 继承与实现
				TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(id);
				Collection<TypeEntity> inherits = typeEntity.getInheritedTypes();
				inherits.forEach(inherit -> {
					Type other = types.get(inherit.getId());
					if(other != null) {
						TypeExtendsType typeExtends = new TypeExtendsType(type, other);
						myBatchInserter.insertRelation(typeExtends);
					}
				});
				Collection<TypeEntity> imps = typeEntity.getImplementedTypes();
				imps.forEach(imp -> {
					Type other = types.get(imp.getId());
					if(other != null) {
						TypeImplementsType typeImplements = new TypeImplementsType(type, other);
						myBatchInserter.insertRelation(typeImplements);
					}
				});
			});
			functions.forEach((id, function) -> {
				// 函数调用
				FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(id);
				functionEntity.getRelations().forEach(relation -> {
					if(DependencyType.CALL.equals(relation.getType())) {
						if(relation.getEntity() instanceof FunctionEntity) {
							Function other = functions.get(relation.getEntity().getId());
							if(other != null) {
								FunctionCallFunction call = new FunctionCallFunction(function, other);
								myBatchInserter.insertRelation(call);
							}
						} else {
						}
					}
					if(DependencyType.RETURN.equals(relation.getType())) {
//						System.out.println(functionEntity + "\n " + relation.getEntity().getClass() +  " " + relation.getEntity());
						if(relation.getEntity() instanceof TypeEntity) {
							Type returnType = types.get(relation.getEntity().getId());
							if(returnType != null) {
								FunctionReturnType functionReturnType = new FunctionReturnType(function, returnType);
								myBatchInserter.insertRelation(functionReturnType);
							}
						}
					}
				});
			});
		}
		currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("结束时间：" + sdf.format(currentTime));
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


}
