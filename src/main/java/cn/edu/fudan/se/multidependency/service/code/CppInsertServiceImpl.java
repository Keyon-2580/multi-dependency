package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Package;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsType;
import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsVariable;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionContainsVariable;
import cn.edu.fudan.se.multidependency.model.relation.code.PackageContainsFile;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeContainsFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeContainsVariable;
import cn.edu.fudan.se.multidependency.utils.FileUtils;
import depends.entity.AliasEntity;
import depends.entity.Entity;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;

public class CppInsertServiceImpl extends DependsCodeInserterForNeo4jServiceImpl {

	public CppInsertServiceImpl(String projectPath, EntityRepo entityRepo, String databasePath, boolean delete,
			Language language) {
		super(projectPath, entityRepo, databasePath, delete, language);
	}

	@Override
	protected void insertNodesWithContainRelations() throws LanguageErrorException {
		entityRepo.getEntities().forEach(entity -> {
			// 每个entity对应相应的node
			if(entity instanceof PackageEntity) {
//				System.out.println("----------------------------------------------------");
//				System.out.println("cpp insertNodesWithContainRelations packageEntity");
				Namespace namespace = new Namespace();
				namespace.setNamespaceName(entity.getQualifiedName());
				namespace.setEntityId(entity.getId());
				insertNodeToNodes(namespace, entity.getId());
			} else if(entity instanceof FileEntity) {
				ProjectFile file = new ProjectFile();
				String fileName = entity.getQualifiedName();
				file.setEntityId(entity.getId());
				file.setFileName(fileName);
				file.setPath(entity.getQualifiedName());
				file.setSuffix(FileUtils.extractSuffix(entity.getQualifiedName()));
				insertNodeToNodes(file, entity.getId());
				// 文件所在目录
				String packageName = FileUtils.findDirectoryFromFile(fileName);
				Package pck = this.nodes.findPackageByPackageName(packageName);
				if(pck == null) {
					pck = new Package();
					pck.setEntityId(entityRepo.generateId());
					pck.setPackageName(packageName);
					pck.setDirectory(true);
					insertNodeToNodes(pck, pck.getEntityId());
				}
				PackageContainsFile containFile = new PackageContainsFile(pck, file);
				insertRelationToRelations(containFile);
			} else if(entity instanceof FunctionEntity) {
				Function function = new Function();
				function.setFunctionName(entity.getQualifiedName());
				function.setEntityId(entity.getId());
				insertNodeToNodes(function, entity.getId());
			} else if(entity instanceof VarEntity) {
				Variable variable = new Variable();
				variable.setEntityId(entity.getId());
				variable.setVariableName(entity.getQualifiedName());
				variable.setTypeIdentify(((VarEntity) entity).getRawType().getName());
				insertNodeToNodes(variable, entity.getId());
			} else if(entity.getClass() == TypeEntity.class) {
				if(nodes.findType(entity.getId()) == null) {
					Type type = new Type();
					type.setEntityId(entity.getId());
					type.setTypeName(entity.getQualifiedName());
					insertNodeToNodes(type, entity.getId());
				}
			} else if(entity.getClass() == AliasEntity.class) {
				AliasEntity aliasEntity = (AliasEntity) entity;
				TypeEntity typeEntity = aliasEntity.getType();
				if(typeEntity != null && typeEntity.getParent() != null) {
					Type type = nodes.findType(typeEntity.getId());
					if(type == null) {
						type = new Type();
						type.setEntityId(typeEntity.getId());
						type.setTypeName(typeEntity.getQualifiedName());
						insertNodeToNodes(type, typeEntity.getId());
					}
					type.setAliasName(entity.getQualifiedName());
				}
			}
			else {
//				System.out.println("insertNodesWithContainRelations " + entity.getClass() + " " + entity.toString());
			}
		});
		this.nodes.findTypes().forEach((entityId, type) -> {
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(entityId);
			Entity parentEntity = typeEntity.getParent();
			if(parentEntity == null) {
//				System.out.println("typeEntity's parent is null");
			} else {
				if(parentEntity instanceof FileEntity) {
					ProjectFile file = this.nodes.findCodeFile(parentEntity.getId());
					if(file != null) {
						FileContainsType fileContainType = new FileContainsType(file, type);
						insertRelationToRelations(fileContainType);
					}
				} else {
					/*while(!(parentEntity instanceof FileEntity)) {
						///FIXME 内部类的情况
						if(parentEntity instanceof FunctionEntity) {
							Function function = this.nodes.findFunction(parentEntity.getId());
							
						} else if(parentEntity.getClass() == TypeEntity.class) {
							
						}
						parentEntity = parentEntity.getParent();
					}*/
//					System.out.println("typeEntity's parent is other Entity " + parentEntity.getClass());
//					System.out.println(typeEntity);
				}
			}
		});
		this.nodes.findFunctions().forEach((entityId, function) -> {
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(entityId);
			Entity parentEntity = functionEntity.getParent();
			if(parentEntity == null) {
//				System.out.println("functionEntity's parent is null");
			} else {
				if(parentEntity instanceof FileEntity) {
//					System.out.println("functionEntity's parent is FileEntity");
					ProjectFile file = this.nodes.findCodeFile(parentEntity.getId());
					FileContainsFunction fileContainsFunction = new FileContainsFunction(file, function);
					insertRelationToRelations(fileContainsFunction);
				} else if(parentEntity.getClass() == TypeEntity.class) {
//					System.out.println("functionEntity's parent is TypeEntity");
					Type type = this.nodes.findType(parentEntity.getId());
					TypeContainsFunction typeContainsFunction = new TypeContainsFunction(type, function);
					insertRelationToRelations(typeContainsFunction);
				} else {
//					System.out.println("functionEntity's parent is other Entity " + parentEntity.getClass());
				}
			}
		});
		this.nodes.findVariables().forEach((entityId, variable) -> {
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId);
			Entity parentEntity = varEntity.getParent();
			if(parentEntity == null) {
//				System.out.println("varEntity's parent is null");
			} else {
				if(parentEntity instanceof FileEntity) {
//					System.out.println("varEntity's parent is FileEntity");
					ProjectFile file = this.nodes.findCodeFile(parentEntity.getId());
					FileContainsVariable fileContainsVariable = new FileContainsVariable(file, variable);
					insertRelationToRelations(fileContainsVariable);
				} else if(parentEntity.getClass() == TypeEntity.class) {
//					System.out.println("varEntity's parent is TypeEntity");
					Type type = this.nodes.findType(parentEntity.getId());
					TypeContainsVariable typeContainsVariable = new TypeContainsVariable(type, variable);
					insertRelationToRelations(typeContainsVariable);
				} else if(parentEntity instanceof FunctionEntity) {
//					System.out.println("varEntity's parent is FunctionEntity");
					Function function = this.nodes.findFunction(parentEntity.getId());
					FunctionContainsVariable functionContainsVariable = new FunctionContainsVariable(function, variable);
					insertRelationToRelations(functionContainsVariable);
				} else {
//					System.out.println("varEntity's parent is other Entity " + parentEntity.getClass());
				}
			}
		});
	}

	@Override
	protected void insertRelations() throws LanguageErrorException {
		extractRelationsFromTypes();
		extractRelationsFromFunctions();
		extractRelationsFromVariables();		
		extractRelationsFromFiles();
	}
	
	protected void extractRelationsFromFiles() {
		nodes.findFiles().forEach((entityId, file) -> {
			FileEntity fileEntity = (FileEntity) entityRepo.getEntity(entityId);
			System.out.println(fileEntity.getQualifiedName());
			System.out.println("getImportedFiles " + fileEntity.getImportedFiles().size());
			System.out.println("getImportedFileInAllLevel " + fileEntity.getImportedFilesInAllLevel().size());
			System.out.println("getImportedNames " + fileEntity.getImportedNames().size());
			System.out.println("getImportedRelationEntities" + fileEntity.getImportedRelationEntities().size());
			System.out.println("getImportedTypes " + fileEntity.getImportedTypes().size());
		});
	}
}
