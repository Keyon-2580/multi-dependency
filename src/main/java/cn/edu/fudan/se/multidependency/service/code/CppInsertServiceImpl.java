package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.code.FileIncludeFile;
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

	public CppInsertServiceImpl(String projectPath, EntityRepo entityRepo, Language language) {
		super(projectPath, entityRepo, language);
	}

	@Override
	protected void addNodesWithContainRelations() throws LanguageErrorException {
		entityRepo.getEntities().forEach(entity -> {
			// 每个entity对应相应的node
			if(entity instanceof PackageEntity) {
				System.out.println("----------------------------------------------------");
				System.out.println("cpp insertNodesWithContainRelations packageEntity");
				Namespace namespace = new Namespace();
				namespace.setNamespaceName(entity.getQualifiedName());
				namespace.setEntityId(entity.getId().longValue());
				addNodeToNodes(namespace, entity.getId().longValue());
			} else if(entity instanceof FileEntity) {
				ProjectFile file = new ProjectFile();
				String filePath = entity.getQualifiedName();
				file.setEntityId(entity.getId().longValue());
				file.setFileName(FileUtils.extractFileName(filePath));
				file.setPath(entity.getQualifiedName());
				file.setSuffix(FileUtils.extractSuffix(entity.getQualifiedName()));
				addNodeToNodes(file, entity.getId().longValue());
				// 文件所在目录
				String directoryPath = FileUtils.extractDirectoryFromFile(filePath);
				Package pck = this.getNodes().findPackageByPackageName(directoryPath);
				if(pck == null) {
					pck = new Package();
					pck.setEntityId(entityRepo.generateId().longValue());
					pck.setPackageName(directoryPath);
					pck.setDirectoryPath(directoryPath);
					addNodeToNodes(pck, pck.getEntityId().longValue());
					Contain projectContainsPackage = new Contain(project, pck);
					addRelation(projectContainsPackage);
				}
				Contain containFile = new Contain(pck, file);
				addRelation(containFile);
			} else if(entity instanceof FunctionEntity) {
				Function function = new Function();
				function.setFunctionName(entity.getQualifiedName());
				function.setEntityId(entity.getId().longValue());
				addNodeToNodes(function, entity.getId().longValue());
			} else if(entity instanceof VarEntity) {
				Variable variable = new Variable();
				variable.setEntityId(entity.getId().longValue());
				variable.setVariableName(entity.getQualifiedName());
				variable.setTypeIdentify(((VarEntity) entity).getRawType().getName());
				addNodeToNodes(variable, entity.getId().longValue());
			} else if(entity.getClass() == TypeEntity.class) {
				if(this.getNodes().findType(entity.getId().longValue()) == null) {
					Type type = new Type();
					type.setEntityId(entity.getId().longValue());
					type.setTypeName(entity.getQualifiedName());
					addNodeToNodes(type, entity.getId().longValue());
				}
			} else if(entity.getClass() == AliasEntity.class) {
				AliasEntity aliasEntity = (AliasEntity) entity;
				TypeEntity typeEntity = aliasEntity.getType();
				if(typeEntity != null && typeEntity.getParent() != null) {
					Type type = this.getNodes().findType(typeEntity.getId().longValue());
					if(type == null) {
						type = new Type();
						type.setEntityId(typeEntity.getId().longValue());
						type.setTypeName(typeEntity.getQualifiedName());
						addNodeToNodes(type, typeEntity.getId().longValue());
					}
					type.setAliasName(entity.getQualifiedName());
				}
			}
			else {
//				System.out.println("insertNodesWithContainRelations " + entity.getClass() + " " + entity.toString());
			}
		});
		this.getNodes().findTypes().forEach((entityId, type) -> {
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = typeEntity.getParent();
			if(parentEntity != null) {
				if(parentEntity instanceof FileEntity) {
					ProjectFile file = this.getNodes().findCodeFile(parentEntity.getId().longValue());
					if(file != null) {
						Contain fileContainType = new Contain(file, type);
						addRelation(fileContainType);
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
		this.getNodes().findFunctions().forEach((entityId, function) -> {
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(entityId.intValue());
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
				// 方法在文件内还是在类内
				if(parentEntity instanceof FileEntity) {
					ProjectFile file = this.getNodes().findCodeFile(parentEntity.getId().longValue());
					Contain fileContainsFunction = new Contain(file, function);
					addRelation(fileContainsFunction);
				} else if(parentEntity.getClass() == TypeEntity.class) {
					Type type = this.getNodes().findType(parentEntity.getId().longValue());
					Contain typeContainsFunction = new Contain(type, function);
					addRelation(typeContainsFunction);
				}
				// 方法的参数
				for(VarEntity varEntity : functionEntity.getParameters()) {
					String parameterName = varEntity.getRawType().getName();
					TypeEntity typeEntity = varEntity.getType();
					if(typeEntity != null && this.getNodes().findType(typeEntity.getId().longValue()) != null) {
						function.addParameterIdentifies(typeEntity.getQualifiedName());
					} else {
						function.addParameterIdentifies(parameterName);
					}
				}
			}
		});
		this.getNodes().findVariables().forEach((entityId, variable) -> {
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = varEntity.getParent();
			if(parentEntity != null) {
				if(parentEntity instanceof FileEntity) {
					ProjectFile file = this.getNodes().findCodeFile(parentEntity.getId().longValue());
					Contain fileContainsVariable = new Contain(file, variable);
					addRelation(fileContainsVariable);
				} else if(parentEntity.getClass() == TypeEntity.class) {
					Type type = this.getNodes().findType(parentEntity.getId().longValue());
					Contain typeContainsVariable = new Contain(type, variable);
					addRelation(typeContainsVariable);
				} else if(parentEntity instanceof FunctionEntity) {
					Function function = this.getNodes().findFunction(parentEntity.getId().longValue());
					Contain functionContainsVariable = new Contain(function, variable);
					addRelation(functionContainsVariable);
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
	
	/**
	 * c中文件的include关系
	 */
	protected void extractRelationsFromFiles() {
		this.getNodes().findFiles().forEach((entityId, file) -> {
			FileEntity fileEntity = (FileEntity) entityRepo.getEntity(entityId.intValue());
			fileEntity.getImportedFiles().forEach(entity -> {
				if(entity instanceof FileEntity) {
					ProjectFile includeFile = this.getNodes().findCodeFile(entity.getId().longValue());
					if(includeFile != null) {
						FileIncludeFile fileIncludeFile = new FileIncludeFile(file, includeFile);
						addRelation(fileIncludeFile);
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
