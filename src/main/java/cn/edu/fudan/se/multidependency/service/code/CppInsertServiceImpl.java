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
import cn.edu.fudan.se.multidependency.model.relation.code.FileIncludeFile;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionContainsVariable;
import cn.edu.fudan.se.multidependency.model.relation.code.PackageContainsFile;
import cn.edu.fudan.se.multidependency.model.relation.code.ProjectContainsPackage;
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
	protected void addNodesWithContainRelations() throws LanguageErrorException {
		entityRepo.getEntities().forEach(entity -> {
			// 每个entity对应相应的node
			if(entity instanceof PackageEntity) {
//				System.out.println("----------------------------------------------------");
//				System.out.println("cpp insertNodesWithContainRelations packageEntity");
				Namespace namespace = new Namespace();
				namespace.setNamespaceName(entity.getQualifiedName());
				namespace.setEntityId(entity.getId());
				addNodeToNodes(namespace, entity.getId());
			} else if(entity instanceof FileEntity) {
				ProjectFile file = new ProjectFile();
				String fileName = entity.getQualifiedName();
				file.setEntityId(entity.getId());
				file.setFileName(fileName);
				file.setPath(entity.getQualifiedName());
				file.setSuffix(FileUtils.extractSuffix(entity.getQualifiedName()));
				addNodeToNodes(file, entity.getId());
				// 文件所在目录
				String packageName = FileUtils.findDirectoryFromFile(fileName);
				Package pck = this.nodes.findPackageByPackageName(packageName);
				if(pck == null) {
					pck = new Package();
					pck.setEntityId(entityRepo.generateId());
					pck.setPackageName(packageName);
					pck.setDirectory(true);
					addNodeToNodes(pck, pck.getEntityId());
					ProjectContainsPackage projectContainsPackage = new ProjectContainsPackage(project, pck);
					insertRelationToRelations(projectContainsPackage);
				}
				PackageContainsFile containFile = new PackageContainsFile(pck, file);
				insertRelationToRelations(containFile);
			} else if(entity instanceof FunctionEntity) {
				Function function = new Function();
				function.setFunctionName(entity.getQualifiedName());
				function.setEntityId(entity.getId());
				addNodeToNodes(function, entity.getId());
			} else if(entity instanceof VarEntity) {
				Variable variable = new Variable();
				variable.setEntityId(entity.getId());
				variable.setVariableName(entity.getQualifiedName());
				variable.setTypeIdentify(((VarEntity) entity).getRawType().getName());
				addNodeToNodes(variable, entity.getId());
			} else if(entity.getClass() == TypeEntity.class) {
				if(nodes.findType(entity.getId()) == null) {
					Type type = new Type();
					type.setEntityId(entity.getId());
					type.setTypeName(entity.getQualifiedName());
					addNodeToNodes(type, entity.getId());
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
						addNodeToNodes(type, typeEntity.getId());
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
			if(parentEntity != null) {
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
			if(parentEntity != null) {
				while(parentEntity.getClass() != TypeEntity.class && !(parentEntity instanceof FileEntity)) {
					parentEntity = parentEntity.getParent();
					if(parentEntity == null) {
						break;
					}
				}
				if(parentEntity == null) {
					return;
				}
				if(parentEntity instanceof FileEntity) {
					ProjectFile file = this.nodes.findCodeFile(parentEntity.getId());
					FileContainsFunction fileContainsFunction = new FileContainsFunction(file, function);
					insertRelationToRelations(fileContainsFunction);
				} else if(parentEntity.getClass() == TypeEntity.class) {
					Type type = this.nodes.findType(parentEntity.getId());
					TypeContainsFunction typeContainsFunction = new TypeContainsFunction(type, function);
					insertRelationToRelations(typeContainsFunction);
				}
				for(VarEntity varEntity : functionEntity.getParameters()) {
					String parameterName = varEntity.getRawType().getName();
					TypeEntity typeEntity = varEntity.getType();
					if(typeEntity != null && nodes.findType(typeEntity.getId()) != null) {
						function.addParameterIdentifies(typeEntity.getQualifiedName());
					} else {
						function.addParameterIdentifies(parameterName);
					}
				}
			}
		});
		this.nodes.findVariables().forEach((entityId, variable) -> {
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId);
			Entity parentEntity = varEntity.getParent();
			if(parentEntity != null) {
				if(parentEntity instanceof FileEntity) {
					ProjectFile file = this.nodes.findCodeFile(parentEntity.getId());
					FileContainsVariable fileContainsVariable = new FileContainsVariable(file, variable);
					insertRelationToRelations(fileContainsVariable);
				} else if(parentEntity.getClass() == TypeEntity.class) {
					Type type = this.nodes.findType(parentEntity.getId());
					TypeContainsVariable typeContainsVariable = new TypeContainsVariable(type, variable);
					insertRelationToRelations(typeContainsVariable);
				} else if(parentEntity instanceof FunctionEntity) {
					Function function = this.nodes.findFunction(parentEntity.getId());
					FunctionContainsVariable functionContainsVariable = new FunctionContainsVariable(function, variable);
					insertRelationToRelations(functionContainsVariable);
				} else {
				}
			}
		});
	}

	@Override
	protected void addRelations() throws LanguageErrorException {
		extractRelationsFromTypes();
		extractRelationsFromFunctions();
		extractRelationsFromVariables();		
		extractRelationsFromFiles();
//		extractRelationsFromDependsType();
	}
	
	protected void extractRelationsFromFiles() {
		nodes.findFiles().forEach((entityId, file) -> {
			FileEntity fileEntity = (FileEntity) entityRepo.getEntity(entityId);
			fileEntity.getImportedFiles().forEach(entity -> {
				if(entity instanceof FileEntity) {
					ProjectFile includeFile = nodes.findCodeFile(entity.getId());
					if(includeFile != null) {
						FileIncludeFile fileIncludeFile = new FileIncludeFile(file, includeFile);
						insertRelationToRelations(fileIncludeFile);
					}
				} else {
					System.out.println("getImprotedFiles: " + entity.getClass());
				}
			});
			fileEntity.getImportedTypes().forEach(entity -> {
				System.out.println("getImportedTypes: " + entity.getClass());
			});
			
		});
	}
}
