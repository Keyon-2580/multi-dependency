package cn.edu.fudan.se.multidependency.service.nospring.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportVariable;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.ProjectConfigUtil.ProjectConfig;
import depends.entity.Entity;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;
import depends.relations.Inferer;

public class JavaInsertServiceImpl extends DependsCodeInserterForNeo4jServiceImpl {

	public JavaInsertServiceImpl(EntityRepo entityRepo, ProjectConfig projectConfig) {
		super(entityRepo, projectConfig);
	}

	@Override
	protected void addNodesWithContainRelations() {
		final String projectPath = currentProject.getPath();
		entityRepo.entityIterator().forEachRemaining(entity -> {
			// 每个entity对应相应的node
			if(entity instanceof PackageEntity) {
			} else if(entity instanceof FileEntity) {
				ProjectFile file = new ProjectFile();
				file.setEntityId(entity.getId().longValue());
				String filePath = entity.getQualifiedName();
				file.setName(FileUtil.extractFileName(filePath));
				filePath = filePath.replace("\\", "/");
				filePath = filePath.substring(filePath.indexOf(projectPath + "/"));
				file.setPath(filePath);
				file.setSuffix(FileUtil.extractSuffix(entity.getQualifiedName()));
				addNode(file, currentProject);
			} else if(entity instanceof FunctionEntity) {
				Function function = new Function();
				function.setImpl(true);
				String functionName = entity.getQualifiedName();
				if(functionName.contains(".")) {
					String[] functionNameSplit = functionName.split("\\.");
					if(functionNameSplit.length >= 2) {
						function.setContrustor(functionNameSplit[functionNameSplit.length - 1].equals(functionNameSplit[functionNameSplit.length - 2]));
					}
				}
				function.setSimpleName(entity.getRawName().getName());
				function.setName(functionName);
				function.setEntityId(entity.getId().longValue());
				addNode(function, currentProject);
			} else if(entity instanceof VarEntity) {
				Variable variable = new Variable();
				variable.setEntityId(entity.getId().longValue());
				variable.setTypeIdentify(((VarEntity) entity).getRawType().getName());
				variable.setName(entity.getQualifiedName());
				variable.setSimpleName(entity.getRawName().getName());
				addNode(variable, currentProject);
			} else if(entity.getClass() == TypeEntity.class) {
				Type type = new Type();
				type.setEntityId(entity.getId().longValue());
				type.setName(entity.getQualifiedName());
				addNode(type, currentProject);
			}
		});
		// Package到File的包含关系
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.ProjectFile, currentProject).forEach((entityId, node) -> {
			// 目录
			ProjectFile codeFile = (ProjectFile) node;
			FileEntity fileEntity = (FileEntity) entityRepo.getEntity(entityId.intValue());
			// 文件所在目录
			String directoryPath = FileUtil.extractDirectoryFromFile(fileEntity.getQualifiedName()) + "/";
			directoryPath = directoryPath.replace("\\", "/");
			directoryPath = directoryPath.substring(directoryPath.indexOf(projectPath + "/"));
			Package pck = this.getNodes().findPackageByDirectoryPath(directoryPath, currentProject);
			if (pck == null) {
				pck = new Package();
				pck.setDirectoryPath(directoryPath);
				Entity parentEntity = fileEntity.getParent();
				if(parentEntity == null) {
					// 该java文件没有显式声明packge，为default包
					pck.setEntityId(entityRepo.generateId().longValue());
					pck.setName(Package.JAVA_PACKAGE_DEFAULT);
				} else {
					pck.setEntityId(parentEntity.getId().longValue());
					pck.setName(parentEntity.getQualifiedName());
				}
//				System.out.println(directoryPath);
				addNode(pck, currentProject);
				Contain projectContainsPackage = new Contain(currentProject, pck);
				addRelation(projectContainsPackage);
			}
			
			Entity parentEntity = fileEntity.getParent();
			if(parentEntity == null) {
				// 该java文件没有显式声明packge，为default包
				pck.setEntityId(entityRepo.generateId().longValue());
				pck.setName(Package.JAVA_PACKAGE_DEFAULT);
			} else {
				pck.setEntityId(parentEntity.getId().longValue());
				pck.setName(parentEntity.getQualifiedName());
			}
			Contain packageContainFile = new Contain(pck, codeFile);
			addRelation(packageContainFile);
			
			setTypeByteCodeName(fileEntity);
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Type, currentProject).forEach((entityId, node) -> {
			Type type = (Type) node;
			Entity typeEntity = entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = typeEntity.getParent();
			while(!(parentEntity instanceof FileEntity)) {
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			Contain fileContainsType = new Contain(parentNode, node);
			addRelation(fileContainsType);
//			System.out.println(type.getTypeName() + " " + typeEntityName.get(entityId.intValue()));
			type.setName(typeEntityName.get(entityId.intValue()));
			type.setAliasName(typeEntityName.get(entityId.intValue()));
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Function, currentProject).forEach((entityId, node) -> {
			Function function = (Function) node;
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = functionEntity.getParent();
			while(parentEntity.getClass() != TypeEntity.class) {
				// 方法内的匿名类的方法的parentEntity是该方法
//				System.out.println(parentEntity.getClass() + " " + parentEntity.getQualifiedName());
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			Contain typeContainsFunction = new Contain(parentNode, function);
			addRelation(typeContainsFunction);
			for(VarEntity varEntity : functionEntity.getParameters()) {
				String parameterName = varEntity.getRawType().getName();
				TypeEntity typeEntity = varEntity.getType();
				// 方法的参数
				if(typeEntity != null 
						&& Inferer.externalType != typeEntity
						&& Inferer.buildInType != typeEntity
						&& Inferer.genericParameterType != typeEntity
						&& this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, parentEntity.getId().longValue(), currentProject) != null) {
					function.addParameterIdentifies(typeEntity.getQualifiedName());
				} else {
					function.addParameterIdentifies(parameterName);
				}
			}

//			System.out.println(functionEntity.getQualifiedName() + " " + parentEntity.getClass());
			Type type = (Type) findNodeByEntityIdInProject(parentEntity);
//			System.out.println(type.getTypeName());
			String newFunctionName = null;
			if(function.isContrustor()) {
				newFunctionName = type.getName();
			} else {
				newFunctionName = type.getName() + "." + function.getSimpleName();
			}
			function.setName(newFunctionName);
//			System.out.println("new-Function-Name: " + newFunctionName);
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Variable, currentProject).forEach((entityId, node) -> {
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = varEntity.getParent();
			while (parentEntity != null && !(parentEntity instanceof FunctionEntity || parentEntity.getClass() == TypeEntity.class)) {
				/// FIXME 内部类的情况暂不考虑
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			Contain contain = new Contain(parentNode, node);
			addRelation(contain);
		});
	}
	
	private Map<Integer, String> typeEntityName = new HashMap<>();
	
	private void setTypeByteCodeName(FileEntity fileEntity) {
		for(TypeEntity e : fileEntity.getDeclaredTypes()) {
			String name = test(e, fileEntity);
			if(name != null) {
				typeEntityName.put(e.getId(), name);
			} else {
				typeEntityName.put(e.getId(), e.getQualifiedName());
			}
		}
	}
	
	private int countIndex(FileEntity fileEntity, TypeEntity type) {
		int index = 0;
		for(TypeEntity e : fileEntity.getDeclaredTypes()) {
			if(e.getQualifiedName().equals(type.getQualifiedName())) {
				index++;
			}
			if(e.getId().equals(type.getId())) {
				break;
			}
		}
		return index;
	}
	
	private String test(TypeEntity typeEntity, FileEntity fileEntity) {
		Entity parent = typeEntity.getParent();
		if(parent instanceof FileEntity) {
			return typeEntity.getQualifiedName();
		}
		if(parent.getClass() == TypeEntity.class) {
			int index = countIndex(fileEntity, typeEntity);
			return test((TypeEntity) parent, fileEntity) + "$" + index + typeEntity.getRawName().getName();
		}
		if(parent instanceof FunctionEntity) {
			parent = parent.getParent();
			return test((TypeEntity) parent, fileEntity) + "$" + countIndex(fileEntity, (TypeEntity) parent) + typeEntity.getRawName().getName();
		}
		return null;
	}
	
	@Override
	protected void addRelations() {
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
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.ProjectFile, currentProject).forEach((entityId, node) -> {
			ProjectFile file = (ProjectFile) node;
			FileEntity fileEntity = (FileEntity) entityRepo.getEntity(entityId.intValue());
			fileEntity.getImportedTypes().forEach(entity -> {
				if(entity instanceof FunctionEntity) {
					Function function = (Function) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Function, entity.getId().longValue(), currentProject);
					if(function != null) {
						FileImportFunction fileImportFunction = new FileImportFunction(file, function);
						addRelation(fileImportFunction);
					}
				} else if(entity instanceof VarEntity) {
					Variable variable = (Variable) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Variable, entity.getId().longValue(), currentProject);
					if(variable != null) {
						FileImportVariable fileImportVariable = new FileImportVariable(file, variable);
						addRelation(fileImportVariable);
					}
				} else if(entity.getClass() == TypeEntity.class) {
					Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, entity.getId().longValue(), currentProject);
					if(type != null) {
						FileImportType fileImportType = new FileImportType(file, type);
						addRelation(fileImportType);
					}
				} else {
					// MultiDeclareEntities
//					System.out.println("extractRelationsFromFiles() " + fileEntity + " " + entity.getClass() + " " + entity.toString());
				}
			});
		});
	}
}
