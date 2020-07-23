package cn.edu.fudan.se.multidependency.service.nospring.code;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.structure.Access;
import cn.edu.fudan.se.multidependency.model.relation.structure.Annotation;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;
import cn.edu.fudan.se.multidependency.model.relation.structure.Cast;
import cn.edu.fudan.se.multidependency.model.relation.structure.Create;
import cn.edu.fudan.se.multidependency.model.relation.structure.ImplLink;
import cn.edu.fudan.se.multidependency.model.relation.structure.Implement;
import cn.edu.fudan.se.multidependency.model.relation.structure.Inherits;
import cn.edu.fudan.se.multidependency.model.relation.structure.Parameter;
import cn.edu.fudan.se.multidependency.model.relation.structure.Return;
import cn.edu.fudan.se.multidependency.model.relation.structure.Throw;
import cn.edu.fudan.se.multidependency.model.relation.structure.VariableType;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfig;
import depends.deptypes.DependencyType;
import depends.entity.Entity;
import depends.entity.FunctionEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;

public abstract class DependsCodeInserterForNeo4jServiceImpl extends BasicCodeInserterForNeo4jServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DependsCodeInserterForNeo4jServiceImpl.class);
	
	public DependsCodeInserterForNeo4jServiceImpl(EntityRepo entityRepo, ProjectConfig projectConfig) {
		super(projectConfig);
		this.entityRepo = entityRepo;
		currentEntityId = entityRepo.generateId().longValue() + 1;
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
					Inherits typeExtends = new Inherits(type, other, Inherits.INHERIT_TYPE_EXTENDS);
					addRelation(typeExtends);
				}
			});
			Collection<TypeEntity> imps = typeEntity.getImplementedTypes();
			imps.forEach(imp -> {
				Type other = (Type) types.get(imp.getId().longValue());
				if(other != null) {
					Inherits typeImplements = new Inherits(type, other, Inherits.INHERIT_TYPE_IMPLEMENTS);
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
				case DependencyType.CALL:
					if(relation.getEntity() instanceof FunctionEntity) {
						// call其它方法
						Function other = (Function) getNodes().findNodeByEntityIdInProject(NodeLabelType.Function, relation.getEntity().getId().longValue(), currentProject);
						if(other != null) {
							Call call = new Call(type, other);
							addRelation(call);
						}
					}
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
			Type typeParameter = null;
			for(depends.relations.Relation relation : varEntity.getRelations()) {
				switch(relation.getType()) {
				case DependencyType.PARAMETER:
					Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, relation.getEntity().getId().longValue(), currentProject);
					if(type != null && typeParameter != type) {
						Parameter variableTypeParameterType = new Parameter(variable, type);
						addRelation(variableTypeParameterType);
						typeParameter = type;
					}
					break;
				case DependencyType.ANNOTATION:
					Type annotationType = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, relation.getEntity().getId().longValue(), currentProject);
					if(annotationType != null) {
						Annotation typeAnnotationType = new Annotation(variable, annotationType);
						addRelation(typeAnnotationType);
					}
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
			functionEntity.getRelations().forEach(relation -> {
				switch(relation.getType()) {
				case DependencyType.CONTAIN:
					break;
				case DependencyType.CALL:
					if(relation.getEntity() instanceof FunctionEntity) {
						// call其它方法
						Function other = (Function) functions.get(relation.getEntity().getId().longValue());
						if(other != null) {
							Call call = new Call(function, other);
							addRelation(call);
						}
					}
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
						Implement functionImplementFunction = new Implement(function, implementFunction);
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

}
