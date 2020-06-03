package cn.edu.fudan.se.multidependency.service.nospring.code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.node.Node;
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
	
	@Override
	protected void addNodesWithContainRelations() {
		LOGGER.info("{} {} addNodesWithContainRelations", this.currentProject.getName(), this.currentProject.getLanguage());
		final String projectPath = currentProject.getPath();
		entityRepo.entityIterator().forEachRemaining(entity -> {
			// 每个entity对应相应的node
			if (entity instanceof PackageEntity) {
				// C++中第命名空间
				Namespace namespace = new Namespace();
				namespace.setName(entity.getQualifiedName());
				namespace.setEntityId(entity.getId().longValue());
				addNode(namespace, currentProject);
			} else if (entity instanceof FileEntity) {
				ProjectFile file = new ProjectFile();
				file.setEntityId(entity.getId().longValue());
				String filePath = entity.getQualifiedName();
				file.setName(FileUtil.extractFileName(filePath));
				filePath = filePath.replace("\\", "/");
				filePath = filePath.substring(filePath.indexOf(projectPath + "/"));
				file.setPath(filePath);
				file.setSuffix(FileUtil.extractSuffix(entity.getQualifiedName()));
				addNode(file, currentProject);
				// 文件所在目录
				String directoryPath = FileUtil.extractDirectoryFromFile(entity.getQualifiedName()) + "/";
				directoryPath = directoryPath.replace("\\", "/");
				directoryPath = directoryPath.substring(directoryPath.indexOf(projectPath + "/"));
				Package pck = this.getNodes().findPackageByDirectoryPath(directoryPath, currentProject);
				if (pck == null) {
					pck = new Package();
					pck.setEntityId(entityRepo.generateId().longValue());
					pck.setName(directoryPath);
					pck.setDirectoryPath(directoryPath);
					addNode(pck, currentProject);
					Contain projectContainsPackage = new Contain(currentProject, pck);
					addRelation(projectContainsPackage);
				}
				Contain containFile = new Contain(pck, file);
				addRelation(containFile);
			} else if (entity instanceof FunctionEntity) {
				Function function = new Function();
				function.setName(entity.getDisplayName());
				function.setEntityId(entity.getId().longValue());
				function.setImpl(entity.getClass() == FunctionEntityImpl.class);
				function.setSimpleName(entity.getRawName().getName());
				addNode(function, currentProject);
			} else if (entity instanceof VarEntity) {
				Variable variable = new Variable();
				variable.setEntityId(entity.getId().longValue());
				variable.setName(entity.getQualifiedName());
				variable.setTypeIdentify(((VarEntity) entity).getRawType().getName());
				variable.setSimpleName(entity.getRawName().getName());
				addNode(variable, currentProject);
			} else if (entity.getClass() == TypeEntity.class) {
				if (this.getNodes().findNodeByEntityIdInProject(entity.getId().longValue(), currentProject) == null) {
					Type type = new Type();
					type.setEntityId(entity.getId().longValue());
					type.setName(entity.getQualifiedName());
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
						type.setName(typeEntity.getQualifiedName());
						addNode(type, currentProject);
					}
					type.setAliasName(entity.getQualifiedName());
				}
			} else {
				// System.out.println("insertNodesWithContainRelations " + entity.getClass() + "
				// " + entity.toString());
			}
		});
		LOGGER.info("{} {} namespace findNodesByNodeTypeInProject", this.currentProject.getName(), this.currentProject.getLanguage());
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
		});
		LOGGER.info("{} {} type findNodesByNodeTypeInProject", this.currentProject.getName(), this.currentProject.getLanguage());
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
		});
		LOGGER.info("{} {} function findNodesByNodeTypeInProject", this.currentProject.getName(), this.currentProject.getLanguage());
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Function, currentProject).forEach((entityId, node) -> {
			Function function = (Function) node;
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(entityId.intValue());
//			System.out.println(functionEntity.getQualifiedName() + " " + functionEntity.getDisplayName() + " " + functionEntity.getRawName().getName());
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
				if (typeEntity != null && this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type,
						typeEntity.getId().longValue(), currentProject) != null) {
					function.addParameterIdentifies(typeEntity.getQualifiedName());
				} else {
					function.addParameterIdentifies(parameterName);
				}
			}
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
						FileIncludeFile fileIncludeFile = new FileIncludeFile(file, includeFile);
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
