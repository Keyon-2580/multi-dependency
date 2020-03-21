package cn.edu.fudan.se.multidependency.service.nospring.code;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileIncludeFile;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import depends.entity.AliasEntity;
import depends.entity.Entity;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;

public class CppInsertServiceImpl extends DependsCodeInserterForNeo4jServiceImpl {

	public CppInsertServiceImpl(String projectPath, String projectName, EntityRepo entityRepo, Language language,
			boolean isMicroservice, String serviceGroupName) {
		super(projectPath, projectName, entityRepo, language, isMicroservice, serviceGroupName);
	}

	@Override
	protected void addNodesWithContainRelations() throws LanguageErrorException {
		final String projectPath = currentProject.getProjectPath();
		entityRepo.entityIterator().forEachRemaining(entity -> {
			// 每个entity对应相应的node
			if (entity instanceof PackageEntity) {
				System.out.println("----------------------------------------------------");
				System.out.println("cpp insertNodesWithContainRelations packageEntity");
				Namespace namespace = new Namespace();
				namespace.setNamespaceName(entity.getQualifiedName());
				namespace.setEntityId(entity.getId().longValue());
				addNode(namespace, currentProject);
			} else if (entity instanceof FileEntity) {
				ProjectFile file = new ProjectFile();
				file.setEntityId(entity.getId().longValue());
				String filePath = entity.getQualifiedName();
				file.setFileName(FileUtil.extractFileName(filePath));
				filePath = filePath.replace("\\", "/");
				filePath = filePath.substring(filePath.indexOf(projectPath + "/"));
				file.setPath(filePath);
				file.setSuffix(FileUtil.extractSuffix(entity.getQualifiedName()));
				addNode(file, currentProject);
				// 文件所在目录
				String directoryPath = FileUtil.extractDirectoryFromFile(entity.getQualifiedName()) + "/";
				directoryPath = directoryPath.replace("\\", "/");
				directoryPath = directoryPath.substring(directoryPath.indexOf(projectPath + "/"));
				Package pck = this.getNodes().findPackageByPackageName(directoryPath, currentProject);
				if (pck == null) {
					pck = new Package();
					pck.setEntityId(entityRepo.generateId().longValue());
					pck.setPackageName(directoryPath);
					pck.setDirectoryPath(directoryPath);
					addNode(pck, currentProject);
					Contain projectContainsPackage = new Contain(currentProject, pck);
					addRelation(projectContainsPackage);
				}
				Contain containFile = new Contain(pck, file);
				addRelation(containFile);
			} else if (entity instanceof FunctionEntity) {
				Function function = new Function();
				String functionName = entity.getQualifiedName();
				function.setFunctionName(functionName);
				function.setEntityId(entity.getId().longValue());
				/*if(functionName.contains(".")) {
					String[] functionNameSplit = functionName.split("\\.");
					if(functionNameSplit.length >= 2) {
						function.setContrustor(functionNameSplit[functionNameSplit.length - 1].equals(functionNameSplit[functionNameSplit.length - 2]));
					}
					function.setSimpleName(functionName.substring(functionName.lastIndexOf(".")));
				} else {
					function.setSimpleName(functionName);
				}*/
				function.setSimpleName(functionName);
				addNode(function, currentProject);
			} else if (entity instanceof VarEntity) {
				Variable variable = new Variable();
				variable.setEntityId(entity.getId().longValue());
				variable.setVariableName(entity.getQualifiedName());
				variable.setTypeIdentify(((VarEntity) entity).getRawType().getName());
				addNode(variable, currentProject);
			} else if (entity.getClass() == TypeEntity.class) {
				if (this.getNodes().findNodeByEntityIdInProject(entity.getId().longValue(), currentProject) == null) {
					Type type = new Type();
					type.setEntityId(entity.getId().longValue());
					type.setTypeName(entity.getQualifiedName());
					addNode(type, currentProject);
				}
			} else if (entity.getClass() == AliasEntity.class) {
				AliasEntity aliasEntity = (AliasEntity) entity;
				TypeEntity typeEntity = aliasEntity.getType();
				if (typeEntity != null && typeEntity.getParent() != null) {
					Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type,
							typeEntity.getId().longValue(), currentProject);
					if (type == null) {
						type = new Type();
						type.setEntityId(typeEntity.getId().longValue());
						type.setTypeName(typeEntity.getQualifiedName());
						addNode(type, currentProject);
					}
					type.setAliasName(entity.getQualifiedName());
				}
			} else {
				// System.out.println("insertNodesWithContainRelations " + entity.getClass() + "
				// " + entity.toString());
			}
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Type, currentProject).forEach((entityId, node) -> {
			Type type = (Type) node;
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = typeEntity.getParent();
			while (!(parentEntity instanceof FileEntity)) {
				/// FIXME 内部类的情况暂不考虑
				/*
				 * if(parentEntity instanceof FunctionEntity) { Function function =
				 * this.nodes.findFunction(parentEntity.getId());
				 * 
				 * } else if(parentEntity.getClass() == TypeEntity.class) {
				 * 
				 * }
				 */
				parentEntity = parentEntity.getParent();
				if (parentEntity == null) {
					System.out.println("parentEntity is null");
					return;
				}
			}
			ProjectFile file = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.ProjectFile,
					parentEntity.getId().longValue(), currentProject);
			type.setInFilePath(file.getPath());
			Contain fileContainsType = new Contain(file, type);
			addRelation(fileContainsType);
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Function, currentProject).forEach((entityId, node) -> {
			Function function = (Function) node;
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = functionEntity.getParent();
			if (parentEntity != null) {
				while (parentEntity.getClass() != TypeEntity.class && !(parentEntity instanceof FileEntity)) {
					parentEntity = parentEntity.getParent();
					if (parentEntity == null) {
						break;
					}
				}
				if (parentEntity == null) {
					return;
				}
				// 方法在文件内还是在类内
				if (parentEntity instanceof FileEntity) {
					ProjectFile file = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.ProjectFile,
							parentEntity.getId().longValue(), currentProject);
					function.setInFilePath(file.getPath());
					Contain fileContainsFunction = new Contain(file, function);
					addRelation(fileContainsFunction);
				} else if (parentEntity.getClass() == TypeEntity.class) {
					Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type,
							parentEntity.getId().longValue(), currentProject);
					function.setInFilePath(type.getInFilePath());
					Contain typeContainsFunction = new Contain(type, function);
					addRelation(typeContainsFunction);
				}
				// 方法的参数
				for (VarEntity varEntity : functionEntity.getParameters()) {
					String parameterName = varEntity.getRawType().getName();
					TypeEntity typeEntity = varEntity.getType();
					if (typeEntity != null && this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type,
							typeEntity.getId().longValue(), currentProject) != null) {
						function.addParameterIdentifies(typeEntity.getQualifiedName());
					} else {
						function.addParameterIdentifies(parameterName);
					}
				}
			}
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Variable, currentProject).forEach((entityId, node) -> {
			Variable variable = (Variable) node;
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = varEntity.getParent();
			if (parentEntity != null) {
				if (parentEntity instanceof FileEntity) {
					ProjectFile file = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.ProjectFile, parentEntity.getId().longValue(), currentProject);
					variable.setInFilePath(file.getPath());
					Contain fileContainsVariable = new Contain(file, variable);
					addRelation(fileContainsVariable);
				} else if (parentEntity.getClass() == TypeEntity.class) {
					Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, parentEntity.getId().longValue(), currentProject);
					variable.setInFilePath(type.getInFilePath());
					Contain typeContainsVariable = new Contain(type, variable);
					addRelation(typeContainsVariable);
				} else if (parentEntity instanceof FunctionEntity) {
					Function function = (Function) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Function, parentEntity.getId().longValue(), currentProject);
					variable.setInFilePath(function.getInFilePath());
					Contain functionContainsVariable = new Contain(function, variable);
					addRelation(functionContainsVariable);
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
		// extractRelationsFromDependsType();
	}

	/**
	 * c中文件的include关系
	 */
	protected void extractRelationsFromFiles() {
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.ProjectFile, currentProject).forEach((entityId, node) -> {
			ProjectFile file = (ProjectFile) node;
			FileEntity fileEntity = (FileEntity) entityRepo.getEntity(entityId.intValue());
			fileEntity.getImportedFiles().forEach(entity -> {
				if (entity instanceof FileEntity) {
					ProjectFile includeFile = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.ProjectFile, entity.getId().longValue(), currentProject);
					if (includeFile != null) {
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
