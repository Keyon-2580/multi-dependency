package cn.edu.fudan.se.multidependency.service.code;

import java.util.Collection;
import java.util.Map;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionCastType;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionParameterType;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionReturnType;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionThrowType;
import cn.edu.fudan.se.multidependency.model.relation.code.NodeAnnotationType;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeExtendsType;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeImplementsType;
import cn.edu.fudan.se.multidependency.model.relation.code.VariableIsType;
import cn.edu.fudan.se.multidependency.model.relation.code.VariableTypeParameterType;
import depends.deptypes.DependencyType;
import depends.entity.FunctionEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;

public abstract class DependsCodeInserterForNeo4jServiceImpl extends BasicCodeInserterForNeo4jServiceImpl {
	
	public DependsCodeInserterForNeo4jServiceImpl(
			String projectPath, String projectName, EntityRepo entityRepo, 
			Language language, boolean isMicroservice, String serviceGroupName) {
		super(projectPath, projectName, language, isMicroservice, serviceGroupName);
		this.entityRepo = entityRepo;
		setCurrentEntityId(entityRepo.generateId().longValue());
	}

	protected EntityRepo entityRepo;
	
	protected abstract void addNodesWithContainRelations() throws LanguageErrorException;
	
	protected abstract void addRelations() throws LanguageErrorException;
	
	@Override
	public void addNodesAndRelations() throws Exception {
		super.addNodesAndRelations();
		addNodesWithContainRelations();
		addRelations();
	}
	
	protected void extractRelationsFromTypes() {
		Map<Long, ? extends Node> types = this.getNodes().findNodesByNodeTypeInProject(NodeType.Type, currentProject);
		types.forEach((id, node) -> {
			Type type = (Type) node;
			// 继承与实现
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(id.intValue());
			Collection<TypeEntity> inherits = typeEntity.getInheritedTypes();
			inherits.forEach(inherit -> {
				Type other = (Type) types.get(inherit.getId().longValue());
				if(other != null) {
					TypeExtendsType typeExtends = new TypeExtendsType(type, other);
					addRelation(typeExtends);
				}
			});
			Collection<TypeEntity> imps = typeEntity.getImplementedTypes();
			imps.forEach(imp -> {
				Type other = (Type) types.get(imp.getId().longValue());
				if(other != null) {
					TypeImplementsType typeImplements = new TypeImplementsType(type, other);
					addRelation(typeImplements);
				}
			});
			typeEntity.getRelations().forEach(relation -> {
				switch(relation.getType()) {
				case DependencyType.ANNOTATION:
					Type annotationType = (Type) types.get(relation.getEntity().getId().longValue());
					if(annotationType != null) {
						NodeAnnotationType typeAnnotationType = new NodeAnnotationType(type, annotationType);
						addRelation(typeAnnotationType);
					}
					break;
				case DependencyType.CALL:
//					System.out.println("type call" + typeEntity + " " + relation.getEntity().getClass() + " " + relation.getEntity());
					if(relation.getEntity() instanceof FunctionEntity) {
						// call其它方法
						Function other = (Function) getNodes().findNodeByEntityIdInProject(NodeType.Function, relation.getEntity().getId().longValue(), currentProject);
						if(other != null) {
							TypeCallFunction call = new TypeCallFunction(type, other);
							addRelation(call);
						}
					} 					
					break;
				case DependencyType.CREATE:
//					System.out.println("type create" + typeEntity + " " + relation.getEntity().getClass() + " " + relation.getEntity());
					break;
				}
			});
		});
	}
	
	protected void extractRelationsFromVariables() {
		this.getNodes().findNodesByNodeTypeInProject(NodeType.Variable, currentProject).forEach((entityId, node) -> {
			Variable variable = (Variable) node;
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId.intValue());
			TypeEntity typeEntity = varEntity.getType();
			if(typeEntity != null && typeEntity.getClass() == TypeEntity.class) {
				Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeType.Type, typeEntity.getId().longValue(), currentProject);
				if(type != null) {
					VariableIsType variableIsType = new VariableIsType(variable, type);
					addRelation(variableIsType);
				}
			}
			Type typeParameter = null;
			for(depends.relations.Relation relation : varEntity.getRelations()) {
				switch(relation.getType()) {
				case DependencyType.PARAMETER:
					Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeType.Type, relation.getEntity().getId().longValue(), currentProject);
					if(type != null && typeParameter != type) {
						VariableTypeParameterType variableTypeParameterType = new VariableTypeParameterType();
						variableTypeParameterType.setVariable(variable);
						variableTypeParameterType.setType(type);
						addRelation(variableTypeParameterType);
						typeParameter = type;
					}
					break;
				case DependencyType.ANNOTATION:
					Type annotationType = (Type) this.getNodes().findNodeByEntityIdInProject(NodeType.Type, relation.getEntity().getId().longValue(), currentProject);
					if(annotationType != null) {
						NodeAnnotationType typeAnnotationType = new NodeAnnotationType(variable, annotationType);
						addRelation(typeAnnotationType);
					}
				default:
					break;
				}
			}
		});
	}
	
	protected void extractRelationsFromFunctions() {
		Map<Long, ? extends Node> functions = this.getNodes().findNodesByNodeTypeInProject(NodeType.Function, currentProject);
		Map<Long, ? extends Node> types = this.getNodes().findNodesByNodeTypeInProject(NodeType.Type, currentProject);
		functions.forEach((id, node) -> {
			Function function = (Function) node;
			// 函数调用
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(id.intValue());
//			System.out.println("------------------");
			functionEntity.getRelations().forEach(relation -> {
				switch(relation.getType()) {
				case DependencyType.CALL:
//					System.out.println("function call" + relation.getEntity().getClass() + " " + relation.getEntity());
					if(relation.getEntity() instanceof FunctionEntity) {
						/*if(functionEntity.getQualifiedName().contains("JavaListener.enterConstructorDeclaration")) {
							System.out.println(functionEntity);
							System.out.println(relation.getEntity().getQualifiedName() + " " + ((FunctionEntity) relation.getEntity()).getParameters());
							for(VarEntity varEntity : ((FunctionEntity) relation.getEntity()).getParameters()) {
								System.out.println(varEntity.getType());
							}
						}*/
						// call其它方法
						Function other = (Function) functions.get(relation.getEntity().getId().longValue());
						if(other != null) {
							FunctionCallFunction call = new FunctionCallFunction(function, other);
							addRelation(call);
						}
					} else {
						///FIXME
//						System.out.println("function call" + relation.getEntity().getClass() + " " + relation.getEntity());
					}
					break;
				case DependencyType.CREATE:
//					System.out.println("function create" + relation.getEntity().getClass() + " " + relation.getEntity());
					break;
				case DependencyType.RETURN:
					Type returnType = (Type) types.get(relation.getEntity().getId().longValue());
					if(returnType != null) {
						FunctionReturnType functionReturnType = new FunctionReturnType(function, returnType);
						addRelation(functionReturnType);
					}
					break;
				case DependencyType.PARAMETER:
					Type parameterType = (Type) types.get(relation.getEntity().getId().longValue());
					if(parameterType != null) {
						FunctionParameterType functionParameterType = new FunctionParameterType(function, parameterType);
						addRelation(functionParameterType);
					}
					break;
				case DependencyType.THROW:
					Type throwType = (Type) types.get(relation.getEntity().getId().longValue());
					if(throwType != null) {
						FunctionThrowType functionThrowType = new FunctionThrowType(function, throwType);
						addRelation(functionThrowType);
					}
					break;
				case DependencyType.ANNOTATION:
					Type annotationType = (Type) types.get(relation.getEntity().getId().longValue());
					if(annotationType != null) {
						NodeAnnotationType functionAnnotationType = new NodeAnnotationType(function, annotationType);
						addRelation(functionAnnotationType);
					}
					break;
				case DependencyType.CAST:
					Type castType = (Type) types.get(relation.getEntity().getId().longValue());
					if(castType != null) {
						FunctionCastType functionCastType = new FunctionCastType(function, castType);
						addRelation(functionCastType);
					}
					break;
				default:
					break;
				}
			});
		});
	}
	
	/*protected void extractRelationsFromDependsType() {
		entityRepo.getEntities().forEach(entity -> {
			entity.getRelations().forEach(relation -> {
				switch(relation.getType()) {
				case DependencyType.PARAMETER:
					break;
				case DependencyType.CALL:
					break;
				case DependencyType.CAST:
					break;
				case DependencyType.INHERIT:
					break;
				case DependencyType.CREATE:
					break;
				case DependencyType.IMPLEMENT:
					break;
				case DependencyType.MIXIN:
					break;
				case DependencyType.ANNOTATION:
					break;
				case DependencyType.CONTAIN:
					break;
				case DependencyType.RETURN:
					break;
				case DependencyType.SET:
					break;
				case DependencyType.THROW:
					break;
				case DependencyType.USE:
					break;
				}
			});
		});
	}*/

	public EntityRepo getEntityRepo() {
		return entityRepo;
	}

	public void setEntityRepo(EntityRepo entityRepo) {
		this.entityRepo = entityRepo;
	}

}
