package cn.edu.fudan.se.multidependency.service.insert.code;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.structure.Include;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfig;
import depends.entity.AliasEntity;
import depends.entity.Entity;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.FunctionEntityImpl;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;
import depends.relations.Inferer;

/**
 * 
 * 7259
 * 11460
 * @author fan
 *
 */
public class CppInsertServiceImpl extends DependsCodeInserterForNeo4jServiceImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(CppInsertServiceImpl.class);
	
	public CppInsertServiceImpl(EntityRepo entityRepo, ProjectConfig projectConfig) {
		super(entityRepo, projectConfig);
	}
	
	private Namespace process(PackageEntity entity) {
		// C++中的命名空间
		Namespace namespace = new Namespace();
		namespace.setLanguage(Language.cpp.name());
		namespace.setName(entity.getQualifiedName());
		namespace.setEntityId(entity.getId().longValue());
		namespace.setSimpleName(entity.getRawName().getName());
		addNode(namespace, currentProject);
		return namespace;
	}
	
	private ProjectFile process(FileEntity entity) {
		final String projectPath = currentProject.getPath();
		ProjectFile file = new ProjectFile();
		file.setLanguage(Language.cpp.name());
		file.setEntityId(entity.getId().longValue());
		String filePath = entity.getQualifiedName();
		file.setName(FileUtil.extractFileName(filePath));
		filePath = FileUtil.extractFilePath(filePath, projectPath);
		file.setPath(filePath);
		file.setSuffix(FileUtil.extractSuffix(entity.getQualifiedName()));
		file.setEndLine(entity.getStopLine());
		file.setLoc(entity.getLoc()+1);
		addNode(file, currentProject);
		// 文件所在目录
		String directoryPath = FileUtil.extractDirectoryFromFile(entity.getQualifiedName()) + "/";
		directoryPath = FileUtil.extractFilePath(directoryPath, projectPath);
		Package pck = this.getNodes().findPackageByDirectoryPath(directoryPath, currentProject);
		if (pck == null) {
			pck = new Package();
//			pck.setEntityId(entityRepo.generateId().longValue());
			pck.setEntityId(generateEntityId());
			pck.setName(directoryPath);
			pck.setDirectoryPath(directoryPath);
			addNode(pck, currentProject);
			Contain projectContainsPackage = new Contain(currentProject, pck);
			addRelation(projectContainsPackage);
		}
		Contain containFile = new Contain(pck, file);
		addRelation(containFile);
		return file;
	}
	
	private Function process(FunctionEntity entity) {
		Function function = new Function();
		function.setLanguage(Language.cpp.name());
		function.setName(entity.getDisplayName());
		function.setEntityId(entity.getId().longValue());
		function.setImpl(entity.getClass() == FunctionEntityImpl.class);
		function.setSimpleName(entity.getRawName().getName());
		function.setStartLine(entity.getStartLine());
		function.setEndLine(entity.getStopLine());
		addNode(function, currentProject);
		return function;
	}
	
	private Variable process(VarEntity entity) {
		Variable variable = new Variable();
		variable.setLanguage(Language.cpp.name());
		variable.setEntityId(entity.getId().longValue());
		variable.setName(entity.getQualifiedName());
		variable.setTypeIdentify(((VarEntity) entity).getRawType().getName());
		variable.setSimpleName(entity.getRawName().getName());
		addNode(variable, currentProject);
		return variable;
	}
	
	private Type process(TypeEntity entity) {
		Node node = this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, 
				entity.getId().longValue(), currentProject);
		if(node == null) {
			Type type = new Type();
			type.setLanguage(Language.cpp.name());
			type.setEntityId(entity.getId().longValue());
			type.setName(entity.getQualifiedName());
			type.setSimpleName(entity.getRawName().getName());
			type.setStartLine(entity.getStartLine());
			type.setEndLine(entity.getStopLine());
			addNode(type, currentProject);
			return type;
		} else {
			return (Type) node;
		}
	}
	
	private Type process(AliasEntity entity) {
		AliasEntity aliasEntity = (AliasEntity) entity;
		TypeEntity typeEntity = aliasEntity.getType();
		if (typeEntity != null && typeEntity.getParent() != null) {
			Type type = process(typeEntity);
			type.setAliasName(entity.getQualifiedName());
			return type;
		}
		return null;
	}
	
	@Override
	protected void addNodesWithContainRelations() {
		entityRepo.entityIterator().forEachRemaining(entity -> {
			// 每个entity对应相应的node
			if (entity instanceof PackageEntity) {
				process((PackageEntity) entity);
			} else if (entity instanceof FileEntity) {
				process((FileEntity) entity);
			} else if (entity instanceof FunctionEntity) {
				process((FunctionEntity) entity);
			} else if (entity instanceof VarEntity) {
				process((VarEntity) entity);
			} else if (entity.getClass() == TypeEntity.class) {
				process((TypeEntity) entity);
			} else if (entity.getClass() == AliasEntity.class) {
				process((AliasEntity) entity);
			} else {
			}
		});
		
		addEmptyPackages();
		
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Namespace, currentProject).forEach((entityId, node) -> {
			Namespace namespace = (Namespace) node;
			PackageEntity packageEntity = (PackageEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = packageEntity.getParent();
			while (parentEntity != null && !(parentEntity instanceof FileEntity)) {
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			Contain contain = new Contain(parentNode, namespace);
			addRelation(contain);
			processIdentifier(namespace);
//			this.getNodes().addCodeNode(namespace);
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Type, currentProject).forEach((entityId, node) -> {
			Type type = (Type) node;
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = typeEntity.getParent();
			while (parentEntity != null && !(parentEntity instanceof FileEntity || parentEntity instanceof PackageEntity)) {
				/// FIXME 内部类的情况暂不考虑
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			Contain contain = new Contain(parentNode, type);
			addRelation(contain);
			processIdentifier(type);
//			this.getNodes().addCodeNode(type);
			while(!(parentEntity instanceof FileEntity)) {
				// 找出方法所在的文件
				parentEntity = parentEntity.getParent();
			}
			ProjectFile file = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(parentEntity.getId().longValue(), currentProject);
			this.getNodes().putNodeToFileByEndLine(file, type);
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Function, currentProject).forEach((entityId, node) -> {
			Function function = (Function) node;
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = functionEntity.getParent();
			while(parentEntity != null && !(parentEntity.getClass() == TypeEntity.class || parentEntity instanceof FileEntity || parentEntity instanceof PackageEntity)) {
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			Contain contain = new Contain(parentNode, function);
			addRelation(contain);
			// 方法的参数
			for (VarEntity varEntity : functionEntity.getParameters()) {
				String parameterName = varEntity.getRawType().getName();
				TypeEntity typeEntity = varEntity.getType();
				if(!StringUtils.isBlank(varEntity.getTypeIdentifier())) {
					function.addParameterIdentifiers(varEntity.getTypeIdentifier());
				} else {
					if(typeEntity != null 
//						&& Inferer.externalType != typeEntity
							&& Inferer.buildInType != typeEntity
							&& Inferer.genericParameterType != typeEntity
							&& this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, parentEntity.getId().longValue(), currentProject) != null) {
						function.addParameterIdentifiers(typeEntity.getQualifiedName());
					} else {
						function.addParameterIdentifiers(parameterName);
					}
				}
			}
			processIdentifier(function);
//			this.getNodes().addCodeNode(function);
			while(!(parentEntity instanceof FileEntity)) {
				// 找出方法所在的文件
				parentEntity = parentEntity.getParent();
			}
			ProjectFile file = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(parentEntity.getId().longValue(), currentProject);
			this.getNodes().putNodeToFileByEndLine(file, function);
		});
		LOGGER.info("{} {} variable findNodesByNodeTypeInProject", this.currentProject.getName(), this.currentProject.getLanguage());
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Variable, currentProject).forEach((entityId, node) -> {
			Variable variable = (Variable) node;
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = varEntity.getParent();
			while(parentEntity != null 
					&& !(parentEntity.getClass() == TypeEntity.class || parentEntity instanceof FileEntity 
						|| parentEntity instanceof PackageEntity || parentEntity instanceof FunctionEntity)) {
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			if(parentNode instanceof Type) { 
				variable.setField(true);
			}
			Contain contain = new Contain(parentNode, variable);
			addRelation(contain);
			processIdentifier(variable);
//			this.getNodes().addCodeNode(variable);
		});
	}

	@Override
	protected void addRelations() {
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
		LOGGER.info("{} {} file extractRelationsFromFiles", this.currentProject.getName(), this.currentProject.getLanguage());
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.ProjectFile, currentProject).forEach((entityId, node) -> {
			ProjectFile file = (ProjectFile) node;
			FileEntity fileEntity = (FileEntity) entityRepo.getEntity(entityId.intValue());
			fileEntity.getImportedFiles().forEach(entity -> {
				if (entity instanceof FileEntity) {
					ProjectFile includeFile = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.ProjectFile, entity.getId().longValue(), currentProject);
					if (includeFile != null) {
						Include fileIncludeFile = new Include(file, includeFile);
						addRelation(fileIncludeFile);
					}
				} else {
//					System.out.println("getImprotedFiles: " + entity.getClass());
				}
			});
			fileEntity.getImportedTypes().forEach(entity -> {
//				System.out.println("getImportedTypes: " + entity.getClass());
			});

		});
	}
}
