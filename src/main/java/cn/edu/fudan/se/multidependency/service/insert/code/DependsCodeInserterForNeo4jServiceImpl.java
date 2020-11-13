package cn.edu.fudan.se.multidependency.service.insert.code;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.*;
import depends.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.Has;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfig;
import depends.deptypes.DependencyType;
import depends.entity.repo.EntityRepo;

public abstract class DependsCodeInserterForNeo4jServiceImpl extends BasicCodeInserterForNeo4jServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DependsCodeInserterForNeo4jServiceImpl.class);
	
	public DependsCodeInserterForNeo4jServiceImpl(EntityRepo entityRepo, ProjectConfig projectConfig) {
		super(projectConfig);
		this.entityRepo = entityRepo;
		currentEntityId = this.entityRepo.generateId().longValue() + 1;
	}
	
	protected EntityRepo entityRepo;
	
	protected abstract void addNodesWithContainRelations();
	
	protected abstract void addRelations();
	
	@Override
	public void addNodesAndRelations() throws Exception {
		super.addNodesAndRelations();
		addNodesWithContainRelations();
		addRelations();
	}

	protected void addEmptyPackages() {
		Map<String, Package> emptyPackages = new HashMap<>();
		this.getNodes().findPackagesInProject(currentProject).forEach((path, node) -> {
			Package currentPackage = (Package) node;
			String parentDirectoryPath = currentPackage.lastPackageDirectoryPath();
			Package parentPackage = this.getNodes().findPackageByDirectoryPath(parentDirectoryPath, currentProject);
			while(parentPackage == null) {
				if(emptyPackages.get(parentDirectoryPath) != null || ("/").equals(parentDirectoryPath)) {
					break;
				}
				parentPackage = new Package();
				parentPackage.setEntityId(generateEntityId());
				parentPackage.setLanguage(currentPackage.getLanguage());
//				parentPackage.setEntityId(this.entityRepo.generateId().longValue());
				parentPackage.setDirectoryPath(parentDirectoryPath);
				parentPackage.setLines(0);
				parentPackage.setLoc(0);
				if(!currentPackage.getName().contains("/") && currentPackage.getName().contains(".")) {
					parentPackage.setName(currentPackage.getName().substring(0, currentPackage.getName().lastIndexOf(".")));
				} else {
					parentPackage.setName(parentDirectoryPath);
				}
				emptyPackages.put(parentDirectoryPath, parentPackage);
				currentPackage = parentPackage;
				parentDirectoryPath = parentPackage.lastPackageDirectoryPath();
				parentPackage = this.getNodes().findPackageByDirectoryPath(parentDirectoryPath, currentProject);
			}
		});
		for(Package pck : emptyPackages.values()) {
			this.addNode(pck, currentProject);
			this.addRelation(new Contain(currentProject, pck));
		}
		this.getNodes().findPackagesInProject(currentProject).forEach((directoryPath, node) -> {
			Package currentPackage = (Package) node;
			Package parentPackage = this.getNodes().findPackageByDirectoryPath(currentPackage.lastPackageDirectoryPath(), currentProject);
			if(parentPackage != null) {
				addRelation(new Has(parentPackage, currentPackage));
			}
		});
		
	}

	protected void addCommonParentEmptyPackages() {
		Map<String, Package> emptyPackages = new HashMap<>();
		Map<String, Map<String, Package>> childPackages = new HashMap<>();
		this.getNodes().findPackagesInProject(currentProject).forEach((path, node) -> {
			Package currentPackage = (Package) node;
			String parentDirectoryPath = currentPackage.lastPackageDirectoryPath();
			Package parentPackage = this.getNodes().findPackageByDirectoryPath(parentDirectoryPath, currentProject);

			Map<String, Package> leafChildList = childPackages.getOrDefault(currentPackage.getDirectoryPath(), new HashMap<>());
			leafChildList.put(currentPackage.getDirectoryPath(), currentPackage);
			childPackages.put(currentPackage.getDirectoryPath(), leafChildList);

			if(parentPackage != null){
				Map<String, Package> childList = childPackages.getOrDefault(parentPackage.getDirectoryPath(), new HashMap<>());
				childList.put(currentPackage.getDirectoryPath(), currentPackage);
				childPackages.put(parentPackage.getDirectoryPath(), childList);
			}
			while(parentPackage == null) {
				if(("/").equals(parentDirectoryPath)) {
					break;
				}
				if(emptyPackages.get(parentDirectoryPath) != null ) {
					parentPackage = emptyPackages.get(parentDirectoryPath);
					Map<String, Package> childList = childPackages.getOrDefault(parentPackage.getDirectoryPath(), new HashMap<>());
					childList.put(currentPackage.getDirectoryPath(), currentPackage);
					childPackages.put(parentPackage.getDirectoryPath(), childList);
					break;
				}

				parentPackage = new Package();
				parentPackage.setEntityId(generateEntityId());
				parentPackage.setLanguage(currentPackage.getLanguage());
//				parentPackage.setEntityId(this.entityRepo.generateId().longValue());
				parentPackage.setDirectoryPath(parentDirectoryPath);
				parentPackage.setLines(0);
				parentPackage.setLoc(0);
				if(!currentPackage.getName().contains("/") && currentPackage.getName().contains(".")) {
					parentPackage.setName(currentPackage.getName().substring(0, currentPackage.getName().lastIndexOf(".")));
				} else {
					parentPackage.setName(parentDirectoryPath);
				}
				emptyPackages.put(parentDirectoryPath, parentPackage);

				Map<String, Package>  childList = childPackages.getOrDefault(parentPackage.getDirectoryPath(), new HashMap<>());
				childList.put(currentPackage.getDirectoryPath(), currentPackage);
				childPackages.put(parentPackage.getDirectoryPath(), childList);

				currentPackage = parentPackage;
				parentDirectoryPath = parentPackage.lastPackageDirectoryPath();
				parentPackage = this.getNodes().findPackageByDirectoryPath(parentDirectoryPath, currentProject);
			}
		});

		for(Map.Entry<String, Map<String, Package>> entry : childPackages.entrySet()){
			String currentDirectoryPath = entry.getKey();
			Map<String, Package> childPck = entry.getValue();

			Package emptyPck = emptyPackages.get(currentDirectoryPath);
			if(emptyPck !=  null && childPck.size() > 1){
				this.addNode(emptyPck, currentProject);
				this.addRelation(new Contain(currentProject, emptyPck));
			}

			Package currentPck = childPck.get(currentDirectoryPath);
			if(currentPck != null || childPck.size() > 1){
				if (currentPck == null){
					currentPck = emptyPackages.get(currentDirectoryPath);
				}
				if (currentPck == null){
					LOGGER.error("Empty Package Finding Error.");
					continue;
				}

				String parentDirectoryPath = currentPck.lastPackageDirectoryPath();
				if( ("/").equals(parentDirectoryPath)) {
					continue;
				}

				Map<String, Package> parentChild = childPackages.get(parentDirectoryPath);
				if(parentChild == null){
					LOGGER.error("Empty Package Finding Error: " + currentPck.getDirectoryPath() + " -- " + parentDirectoryPath);
					continue;
				}

				Package parentPck = parentChild.get(parentDirectoryPath);
				while (parentPck == null){
					if(("/").equals(parentDirectoryPath)) {
						break;
					}
					Map<String, Package> child = childPackages.get(parentDirectoryPath);
					if(child == null){
						LOGGER.error("Empty Package Finding Error: " + currentPck.getDirectoryPath() + " -- " + parentDirectoryPath);
						continue;
					}
					Package pck = emptyPackages.get(parentDirectoryPath);
					if(child != null && child.size() > 1 && pck != null){
						addRelation(new Has(pck, currentPck));
						break;
					}
					if(pck != null){
						parentDirectoryPath = pck.lastPackageDirectoryPath();
						if(("/").equals(parentDirectoryPath)) {
							break;
						}
						parentChild = childPackages.get(parentDirectoryPath);
						if(parentChild == null){
							LOGGER.error("Empty Package Finding Error: " + currentPck.getDirectoryPath() + " -- " + parentDirectoryPath);
							break;
						}
						parentPck = parentChild.get(parentDirectoryPath);

					}else {
						System.out.println("Empty Package Finding Error, too.");
						break;
					}
				}

				if (parentPck != null){
					addRelation(new Has(parentPck, currentPck));
				}
			}
		}
	}

	protected void extractRelationsFromTypes() {
		Map<Long, ? extends Node> types = this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Type, currentProject);
		types.forEach((id, node) -> {
			Type type = (Type) node;
			// 继承与实现
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(id.intValue());
			Collection<TypeEntity> inherits = typeEntity.getInheritedTypes();
			inherits.forEach(inherit -> {
				Type other = (Type) types.get(inherit.getId().longValue());
				if(other != null) {
					Extends typeExtends = new Extends(type, other);
					addRelation(typeExtends);
				}
			});
			Collection<TypeEntity> imps = typeEntity.getImplementedTypes();
			imps.forEach(imp -> {
				Type other = (Type) types.get(imp.getId().longValue());
				if(other != null) {
					Implements typeImplements = new Implements(type, other);
					addRelation(typeImplements);
				}
			});
			typeEntity.getRelations().forEach(relation -> {
				switch(relation.getType()) {
				case DependencyType.ANNOTATION:
					Type annotationType = (Type) types.get(relation.getEntity().getId().longValue());
					if(annotationType != null) {
						Annotation typeAnnotationType = new Annotation(type, annotationType);
						addRelation(typeAnnotationType);
					}
					break;
				case DependencyType.CONTAIN:
					if(relation.getEntity().getClass() == TypeEntity.class && relation.getEntity().getId() != -1) {
						// 关联的type，即成员变量的类型，此处仅指代类型直接定义的成员变量，不包含通过List<？>、Set<？>等基本数据类型中参数类型（此种情况将在变量的参数类型中处理）
						Type other = (Type) getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, relation.getEntity().getId().longValue(), currentProject);
						if(other != null) {
							Association association = new Association(type, other);
							addRelation(association);
						}
					}
					break;
				case DependencyType.CALL:
					if( relation.getEntity().getClass() == FunctionEntity.class ) {
						Function other = (Function) getNodes().findNodeByEntityIdInProject(NodeLabelType.Function, relation.getEntity().getId().longValue(), currentProject);
						if(other != null) {
							Call call = new Call(type, other);
							addRelation(call);
						}
					}
//					else if(relation.getEntity().getClass() == TypeEntity.class){
//						Type other = (Type) types.get(relation.getEntity().getId().longValue());
//						if(other != null) {
//							Call call = new Call(type, other);
//							addRelation(call);
//						}
//					}
					break;
				case DependencyType.CREATE:
					if(relation.getEntity().getClass() == TypeEntity.class) {
						Type other = (Type) types.get(relation.getEntity().getId().longValue());
						if(other != null) {
							Create create = new Create(type, other);
							addRelation(create);
						}
					} 
					break;
				case DependencyType.CAST:
					Type castType = (Type) types.get(relation.getEntity().getId().longValue());
					if(castType != null) {
						Cast cast = new Cast(type, castType);
						addRelation(cast);
					}
					break;
				case DependencyType.THROW:
					LOGGER.info(typeEntity + " " + relation.getType() + " " + relation.getEntity().getClass() + " " + relation.getEntity());
					break;
				case DependencyType.USE:
					Entity relationEntity = relation.getEntity();
					if(relation.getEntity() instanceof VarEntity) {
						Node relationNode = this.getNodes().findNodeByEntityIdInProject(relationEntity.getId().longValue(), currentProject);
						if(relationNode != null) {
							assert(relationNode instanceof Variable);
							Variable var = (Variable) relationNode;
							if(var.isField()) {
								Access accessField = new Access(type, var);
								addRelation(accessField);
							}
						}
					}else if(relation.getEntity().getClass() == TypeEntity.class && relation.getEntity().getId() != -1){
						Type other = (Type) types.get(relation.getEntity().getId().longValue());
						if(other != null) {
							Use use = new Use(type, other);
							addRelation(use);
						}
					}
					break;
				default:
					break;
				}
			});
		});
	}
	
	protected void extractRelationsFromVariables() {
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Variable, currentProject).forEach((entityId, node) -> {
			Variable variable = (Variable) node;
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId.intValue());
			TypeEntity typeEntity = varEntity.getType();
			if(typeEntity != null && typeEntity.getClass() == TypeEntity.class) {
				Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, typeEntity.getId().longValue(), currentProject);
				if(type != null) {
					VariableType variableIsType = new VariableType(variable, type);
					addRelation(variableIsType);
				}
			}
			for(depends.relations.Relation relation : varEntity.getRelations()) {
				switch(relation.getType()) {
				case DependencyType.PARAMETER:
					Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, relation.getEntity().getId().longValue(), currentProject);
					if(type != null) {
						Parameter variableTypeParameterType = new Parameter(variable, type);
						addRelation(variableTypeParameterType);
					}
					break;
				case DependencyType.USE:
					Type useType = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, relation.getEntity().getId().longValue(), currentProject);
					if(useType != null){
						if(variable.isField()){
							Entity parentTypeEntity = varEntity.getParent();
							while (parentTypeEntity != null && parentTypeEntity.getClass() != TypeEntity.class){
								parentTypeEntity = parentTypeEntity.getParent();
							}
							Type parentType = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, parentTypeEntity.getId().longValue(), currentProject);
							if(parentType != null && parentType != useType){
								Association association = new Association(parentType, useType);
								addRelation(association);
							}
						} else {
							Use use = new Use(variable, useType);
							addRelation(use);
						}
					}
					break;
				case DependencyType.ANNOTATION:
					Type annotationType = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, relation.getEntity().getId().longValue(), currentProject);
					if(annotationType != null) {
						Annotation typeAnnotationType = new Annotation(variable, annotationType);
						addRelation(typeAnnotationType);
					}
					break;
				default:
					break;
				}
			}
		});
	}
	
	protected void extractRelationsFromFunctions() {
		Map<Long, ? extends Node> functions = this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Function, currentProject);
		Map<Long, ? extends Node> types = this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Type, currentProject);
		functions.forEach((id, node) -> {
			Function function = (Function) node;
			String functionFullName = function.getParameters().toString().replace('[', '(').replace(']', ')');
			// 函数调用
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(id.intValue());
			Entity functionParentTypeEntity = functionEntity.getParent();
			Type functionParentType = (Type) types.get(functionParentTypeEntity.getId().longValue());
			functionEntity.getRelations().forEach(relation -> {
				switch(relation.getType()) {
				case DependencyType.CALL:
					if(relation.getEntity() instanceof FunctionEntity) {
						// call其它方法
						Function other = (Function) functions.get(relation.getEntity().getId().longValue());
						if(other != null) {
							Call call = new Call(function, other);
							addRelation(call);
						}
					}
//					else if(relation.getEntity().getClass() == TypeEntity.class){
//						Type other = (Type) types.get(relation.getEntity().getId().longValue());
//						if(other != null) {
//							Call call = new Call(function, other);
//							addRelation(call);
//						}
//					}
					break;
				case DependencyType.CREATE:
					if(relation.getEntity().getClass() == TypeEntity.class) {
						Type other = (Type) types.get(relation.getEntity().getId().longValue());
						if(other != null) {
							Create create = new Create(function, other);
							addRelation(create);
						}
					}
					break;
				case DependencyType.RETURN:
					Type returnType = (Type) types.get(relation.getEntity().getId().longValue());
					if(returnType != null) {
						Return functionReturnType = new Return(function, returnType);
						addRelation(functionReturnType);
					}
					break;
				case DependencyType.PARAMETER:
					Type parameterType = (Type) types.get(relation.getEntity().getId().longValue());
					if(parameterType != null) {
						Parameter functionParameterType = new Parameter(function, parameterType);
						addRelation(functionParameterType);
					}
					break;
				case DependencyType.THROW:
					Type throwType = (Type) types.get(relation.getEntity().getId().longValue());
					if(throwType != null) {
						Throw functionThrowType = new Throw(function, throwType);
						addRelation(functionThrowType);
					}
					break;
				case DependencyType.ANNOTATION:
					Type annotationType = (Type) types.get(relation.getEntity().getId().longValue());
					if(annotationType != null) {
						Annotation functionAnnotationType = new Annotation(function, annotationType);
						addRelation(functionAnnotationType);
					}
					break;
				case DependencyType.CAST:
					Type castType = (Type) types.get(relation.getEntity().getId().longValue());
					if(castType != null) {
						Cast functionCastType = new Cast(function, castType);
						addRelation(functionCastType);
					}
					break;
				case DependencyType.IMPLEMENT:
					Node implementNode = this.getNodes().findNodeByEntityIdInProject(relation.getEntity().getId().longValue(), currentProject);
					if(implementNode != null && implementNode instanceof Function) {
						Function implementFunction = (Function) implementNode;
						Implements functionImplementFunction = new Implements(function, implementFunction);
						addRelation(functionImplementFunction);
					}
					break;
				case DependencyType.IMPLLINK:
					Node impllinkNode = this.getNodes().findNodeByEntityIdInProject(relation.getEntity().getId().longValue(), currentProject);
					if(impllinkNode != null && impllinkNode instanceof Function) {
						Function implLinkFunction = (Function) impllinkNode;
						ImplLink functionImplLinkFunction = new ImplLink(function, implLinkFunction);
						addRelation(functionImplLinkFunction);
					}
					break;
				case DependencyType.USE:
					Entity relationEntity = relation.getEntity();
					if(relation.getEntity() instanceof VarEntity) {
						Node relationNode = this.getNodes().findNodeByEntityIdInProject(relationEntity.getId().longValue(), currentProject);
						if(relationNode != null) {
							assert(relationNode instanceof Variable);
							Variable var = (Variable) relationNode;
							if(var.isField()) {
								Access accessField = new Access(function, var);
								addRelation(accessField);
							}
						}
					} else if(relation.getEntity().getClass() == TypeEntity.class){
						Type useType = (Type) types.get(relation.getEntity().getId().longValue());
						if(useType != null) {
							Use use = new Use(function, useType);
							addRelation(use);
					    }
				    }
					break;
				case DependencyType.CONTAIN:
					Type containType = (Type) types.get(relation.getEntity().getId().longValue());
					if(containType != null) {
						Use use = new Use(function, containType);
						addRelation(use);
					}
					break;
				default:
					LOGGER.info(function.getName() + functionFullName + " " + relation.getType() + " " + relation.getEntity().getClass().toString() + " " + relation.getEntity().getQualifiedName());
					break;
				}
			});
		});
	}
	
	public EntityRepo getEntityRepo() {
		return entityRepo;
	}

	public void setEntityRepo(EntityRepo entityRepo) {
		this.entityRepo = entityRepo;
	}
	
	protected String processIdentifier(CodeNode node) {
		if(node.getIdentifier() != null) {
			return node.getIdentifier();
		}
		Entity entity = entityRepo.getEntity(node.getEntityId().intValue());
		Entity parentEntity = entity.getParent();
		if(parentEntity == null) {
			return "";
		}
		Node parentNode = this.getNodes().findNodeByEntityIdInProject(parentEntity.getId().longValue(), currentProject);
		if(parentNode == null) {
			return "";
		}
		if(parentNode instanceof ProjectFile) {
			String identifier = new StringBuilder().append(((ProjectFile) parentNode).getPath()).append(Constant.CODE_NODE_IDENTIFIER_SUFFIX_FILE)
					.append(node.getIdentifierSimpleName()).append(node.getIdentifierSuffix()).toString();
			node.setIdentifier(identifier);
			return identifier;
		} else if(parentNode instanceof CodeNode) {
			String identifier = new StringBuilder().append(processIdentifier((CodeNode) parentNode))
					.append(node.getIdentifierSimpleName()).append(node.getIdentifierSuffix()).toString();
			node.setIdentifier(identifier);
			return identifier;
		} else {
			return "";
		}
	}

	protected String getTypeIdentifyOfVar(List<GenericName> varArguments){
		String typeIdentify = "";
		typeIdentify += "<";
		for (GenericName arg : varArguments){
			typeIdentify += arg.getName();
			List<GenericName> varArgs = arg.getArguments();
			if (varArgs != null && !varArgs.isEmpty()){
				typeIdentify += getTypeIdentifyOfVar(varArgs);
			}
			typeIdentify += ", ";
		}
		typeIdentify = typeIdentify.substring(0,typeIdentify.length()-2);
		typeIdentify += ">";

		return typeIdentify;
	}
}
