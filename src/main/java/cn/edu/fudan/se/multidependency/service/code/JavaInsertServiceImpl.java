package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.code.FileImportFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.FileImportType;
import cn.edu.fudan.se.multidependency.model.relation.code.FileImportVariable;
import cn.edu.fudan.se.multidependency.utils.FileUtils;
import depends.entity.Entity;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;
import depends.relations.Inferer;

public class JavaInsertServiceImpl extends DependsCodeInserterForNeo4jServiceImpl {

	public JavaInsertServiceImpl(String projectPath, String projectName, EntityRepo entityRepo, Language language,
			boolean isMicroservice, String serviceGroupName) {
		super(projectPath, projectName, entityRepo, language, isMicroservice, serviceGroupName);
	}

	@Override
	protected void addNodesWithContainRelations() throws LanguageErrorException {
		final String projectPath = currentProject.getProjectPath();
		entityRepo.entityIterator().forEachRemaining(entity -> {
			// 每个entity对应相应的node
			if(entity instanceof PackageEntity) {
				Package pck = new Package();
				pck.setPackageName(entity.getQualifiedName());
				pck.setEntityId(entity.getId().longValue());
				addNodeToNodes(pck, entity.getId().longValue(), currentProject);
				Contain projectContainsPackage = new Contain(currentProject, pck);
				addRelation(projectContainsPackage);
			} else if(entity instanceof FileEntity) {
				ProjectFile file = new ProjectFile();
				file.setEntityId(entity.getId().longValue());
				String filePath = entity.getQualifiedName();
				file.setFileName(FileUtils.extractFileName(filePath));
				filePath = filePath.replace("\\", "/");
				filePath = filePath.substring(filePath.indexOf(projectPath + "/"));
				file.setPath(filePath);
				file.setSuffix(FileUtils.extractSuffix(entity.getQualifiedName()));
				addNodeToNodes(file, entity.getId().longValue(), currentProject);
			} else if(entity instanceof FunctionEntity) {
				Function function = new Function();
				String functionName = entity.getQualifiedName();
				if(functionName.contains(".")) {
					String[] functionNameSplit = functionName.split("\\.");
					if(functionNameSplit.length >= 2) {
						function.setContrustor(functionNameSplit[functionNameSplit.length - 1].equals(functionNameSplit[functionNameSplit.length - 2]));
					}
				}
				function.setFunctionName(functionName);
				function.setEntityId(entity.getId().longValue());
				addNodeToNodes(function, entity.getId().longValue(), currentProject);
			} else if(entity instanceof VarEntity) {
				Variable variable = new Variable();
				variable.setEntityId(entity.getId().longValue());
				variable.setTypeIdentify(((VarEntity) entity).getRawType().getName());
				variable.setVariableName(entity.getQualifiedName());
				addNodeToNodes(variable, entity.getId().longValue(), currentProject);
			} else if(entity.getClass() == TypeEntity.class) {
				Type type = new Type();
				type.setEntityId(entity.getId().longValue());
				type.setTypeName(entity.getQualifiedName());
				addNodeToNodes(type, entity.getId().longValue(), currentProject);
			}
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeType.ProjectFile, currentProject).forEach((entityId, node) -> {
			ProjectFile codeFile = (ProjectFile) node;
			Contain packageContainFile = new Contain();
			packageContainFile.setEnd(codeFile);
			// 在java中，文件上面是包，若包不存在，则将该文件的包设为当前目录
			Entity fileEntity = entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = fileEntity.getParent();
			Package pck = null;
			if(parentEntity == null) {
				String filePath = codeFile.getPath();
				String packagePath = FileUtils.extractDirectoryFromFile(filePath) + "/";
				packagePath = packagePath.replace("\\", "/");
				packagePath = packagePath.substring(packagePath.indexOf(projectPath + "/"));
				pck = this.getNodes().findPackageByPackageName(packagePath, currentProject);
				if(pck == null) {
					pck = new Package();
					pck.setEntityId(entityRepo.generateId().longValue());
					pck.setPackageName(packagePath);
					pck.setDirectoryPath(packagePath);
					addNodeToNodes(pck, pck.getEntityId(), currentProject);
					Contain projectContainsPackage = new Contain(currentProject, pck);
					addRelation(projectContainsPackage);
				}
			} else {
				pck = (Package) this.getNodes().findNodeByEntityIdInProject(NodeType.Package, parentEntity.getId().longValue(), currentProject);
				String packagePath = FileUtils.extractDirectoryFromFile(fileEntity.getQualifiedName());
				packagePath = packagePath.replace("\\", "/");
				packagePath = packagePath.substring(packagePath.indexOf(projectPath + "/"));
				pck.setDirectoryPath(packagePath);
			}
			packageContainFile.setStart(pck);
			addRelation(packageContainFile);
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeType.Type, currentProject).forEach((entityId, node) -> {
			Type type = (Type) node;
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = typeEntity.getParent();
			/*Entity currentEntity = typeEntity;
				Node currentNode = type;*/
			while(!(parentEntity instanceof FileEntity)) {
				///FIXME 内部类的情况暂不考虑
				/*if(parentEntity instanceof FunctionEntity) {
						Function function = this.nodes.findFunction(parentEntity.getId());
						
					} else if(parentEntity.getClass() == TypeEntity.class) {
						
					}*/
				parentEntity = parentEntity.getParent();
			}
			ProjectFile file = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(NodeType.ProjectFile, parentEntity.getId().longValue(), currentProject);
			type.setInFilePath(file.getPath());
			Contain fileContainsType = new Contain(file, type);
			addRelation(fileContainsType);
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeType.Function, currentProject).forEach((entityId, node) -> {
			Function function = (Function) node;
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = functionEntity.getParent();
			
			while(parentEntity.getClass() != TypeEntity.class) {
				// 方法内的匿名类的方法的parentEntity是该方法
				parentEntity = parentEntity.getParent();
			}
			Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeType.Type, parentEntity.getId().longValue(), currentProject);
			function.setInFilePath(type.getInFilePath());
			Contain typeContainsFunction = new Contain(type, function);
			addRelation(typeContainsFunction);
			for(VarEntity varEntity : functionEntity.getParameters()) {
				String parameterName = varEntity.getRawType().getName();
				TypeEntity typeEntity = varEntity.getType();
				// 方法的参数
				if(typeEntity != null 
						&& Inferer.externalType != typeEntity
						&& Inferer.buildInType != typeEntity
						&& Inferer.genericParameterType != typeEntity
						&& this.getNodes().findNodeByEntityIdInProject(NodeType.Type, parentEntity.getId().longValue(), currentProject) != null) {
					function.addParameterIdentifies(typeEntity.getQualifiedName());
				} else {
					function.addParameterIdentifies(parameterName);
				}
			}

		});
		this.getNodes().findNodesByNodeTypeInProject(NodeType.Variable, currentProject).forEach((entityId, node) -> {
			Variable variable = (Variable) node;
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = varEntity.getParent();
			if(parentEntity instanceof FunctionEntity) {
				Function function = (Function) this.getNodes().findNodeByEntityIdInProject(NodeType.Function, parentEntity.getId().longValue(), currentProject);
				variable.setInFilePath(function.getInFilePath());
				Contain functionContainsVariable = new Contain(function, variable);
				addRelation(functionContainsVariable);
			} else if(parentEntity.getClass() == TypeEntity.class) {
				Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeType.Type, parentEntity.getId().longValue(), currentProject);
				variable.setInFilePath(type.getInFilePath());
				Contain typeContainsVariable = new Contain(type, variable);
				addRelation(typeContainsVariable);
			} else {
//				System.out.println(varEntity);
//				System.out.println(parentEntity.getClass() + " " + parentEntity);
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
	 * java中文件的import关系
	 */
	protected void extractRelationsFromFiles() {
		this.getNodes().findNodesByNodeTypeInProject(NodeType.ProjectFile, currentProject).forEach((entityId, node) -> {
			ProjectFile file = (ProjectFile) node;
			FileEntity fileEntity = (FileEntity) entityRepo.getEntity(entityId.intValue());
			fileEntity.getImportedTypes().forEach(entity -> {
				if(entity instanceof FunctionEntity) {
					Function function = (Function) this.getNodes().findNodeByEntityIdInProject(NodeType.Function, entity.getId().longValue(), currentProject);
					if(function != null) {
						FileImportFunction fileImportFunction = new FileImportFunction(file, function);
						addRelation(fileImportFunction);
					}
				} else if(entity instanceof VarEntity) {
					Variable variable = (Variable) this.getNodes().findNodeByEntityIdInProject(NodeType.Variable, entity.getId().longValue(), currentProject);
					if(variable != null) {
						FileImportVariable fileImportVariable = new FileImportVariable(file, variable);
						addRelation(fileImportVariable);
					}
				} else if(entity.getClass() == TypeEntity.class) {
					Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeType.Type, entity.getId().longValue(), currentProject);
					if(type != null) {
						FileImportType fileImportType = new FileImportType(file, type);
						addRelation(fileImportType);
					}
				} else {
					// MultiDeclareEntities
					System.out.println("extractRelationsFromFiles() " + fileEntity + " " + entity.getClass() + " " + entity.toString());
				}
			});
		});
	}
}
