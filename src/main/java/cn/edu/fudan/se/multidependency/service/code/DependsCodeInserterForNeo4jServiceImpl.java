package cn.edu.fudan.se.multidependency.service.code;

import java.util.Collection;
import java.util.Map;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionAnnotationType;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionParameterType;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionReturnType;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionThrowType;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeExtendsType;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeImplementsType;
import cn.edu.fudan.se.multidependency.model.relation.code.VariableIsType;
import depends.deptypes.DependencyType;
import depends.entity.FunctionEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;

public abstract class DependsCodeInserterForNeo4jServiceImpl extends BasicCodeInserterForNeo4jServiceImpl {
	
	public DependsCodeInserterForNeo4jServiceImpl(String projectPath, EntityRepo entityRepo, Language language) {
		super(projectPath, language);
		this.entityRepo = entityRepo;
		setCurrentEntityId(entityRepo.generateId().longValue());
	}

	protected EntityRepo entityRepo;
	
	protected abstract void addNodesWithContainRelations() throws LanguageErrorException;
	
	protected abstract void addRelations() throws LanguageErrorException;
	
	@Override
	public void addNodesAndRelations() {
		try {
			project.setEntityId(entityRepo.generateId().longValue());
			addNodeToNodes(project, project.getEntityId());
			addNodesWithContainRelations();
			addRelations();
		} catch (LanguageErrorException e) {
			e.printStackTrace();
		}
	}
	
	protected void extractRelationsFromTypes() {
		Map<Long, Type> types = this.getNodes().findTypes();
		types.forEach((id, type) -> {
			// 继承与实现
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(id.intValue());
			Collection<TypeEntity> inherits = typeEntity.getInheritedTypes();
			inherits.forEach(inherit -> {
				Type other = types.get(inherit.getId().longValue());
				if(other != null) {
					TypeExtendsType typeExtends = new TypeExtendsType(type, other);
					addRelation(typeExtends);
				}
			});
			Collection<TypeEntity> imps = typeEntity.getImplementedTypes();
			imps.forEach(imp -> {
				Type other = types.get(imp.getId().longValue());
				if(other != null) {
					TypeImplementsType typeImplements = new TypeImplementsType(type, other);
					addRelation(typeImplements);
				}
			});
		});
	}
	
	protected void extractRelationsFromVariables() {
		this.getNodes().findVariables().forEach((entityId, variable) -> {
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId.intValue());
			TypeEntity typeEntity = varEntity.getType();
			if(typeEntity != null && typeEntity.getClass() == TypeEntity.class) {
				Type type = this.getNodes().findType(typeEntity.getId().longValue());
				if(type != null) {
					VariableIsType variableIsType = new VariableIsType(variable, type);
					addRelation(variableIsType);
				}
			} else {
			}
		});
	}
	
	protected void extractRelationsFromFunctions() {
		Map<Long, Function> functions = this.getNodes().findFunctions();
		Map<Long, Type> types = this.getNodes().findTypes();
		functions.forEach((id, function) -> {
			// 函数调用
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(id.intValue());
			functionEntity.getRelations().forEach(relation -> {
				if(DependencyType.CALL.equals(relation.getType())) {
					if(relation.getEntity() instanceof FunctionEntity) {
						// call其它方法
						Function other = functions.get(relation.getEntity().getId().longValue());
						if(other != null) {
							FunctionCallFunction call = new FunctionCallFunction(function, other);
							addRelation(call);
						}
					} else {
						///FIXME
					}
				}
				if(DependencyType.RETURN.equals(relation.getType())) {
					Type returnType = types.get(relation.getEntity().getId().longValue());
					if(returnType != null) {
						FunctionReturnType functionReturnType = new FunctionReturnType(function, returnType);
						addRelation(functionReturnType);
					}
				}
				if(DependencyType.PARAMETER.equals(relation.getType())) {
					Type parameterType = types.get(relation.getEntity().getId().longValue());
					if(parameterType != null) {
						FunctionParameterType functionParameterType = new FunctionParameterType(function, parameterType);
						addRelation(functionParameterType);
					}
				}
				if(DependencyType.THROW.equals(relation.getType())) {
					Type throwType = types.get(relation.getEntity().getId().longValue());
					if(throwType != null) {
						FunctionThrowType functionThrowType = new FunctionThrowType(function, throwType);
						addRelation(functionThrowType);
					}
				}
				if(DependencyType.ANNOTATION.equals(relation.getType())) {
					Type annotationType = types.get(relation.getEntity().getId().longValue());
					if(annotationType != null) {
						FunctionAnnotationType functionAnnotationType = new FunctionAnnotationType(function, annotationType);
						addRelation(functionAnnotationType);
					}
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
