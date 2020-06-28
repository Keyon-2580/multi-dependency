package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;

public interface BasicCloneQueryService {

	/**
	 * 根据克隆类型找出所有该类型的克隆关系
	 * @param cloneType
	 * @return
	 */
	Collection<Clone> findClonesByCloneType(CloneRelationType cloneType);
	
	/**
	 * 找出包含某克隆类型关系的克隆组
	 * @param cloneType
	 * @return
	 */
	Collection<CloneGroup> findGroupsContainCloneTypeRelation(CloneRelationType cloneType);
	
	Collection<Clone> findGroupContainCloneRelations(CloneGroup group);
	
	CloneGroup queryCloneGroup(long id);
	
	CloneGroup queryCloneGroup(String name);
	
	/**
	 * 查询一个克隆组包含的所有方法克隆关系
	 * @param group
	 * @return
	 */
	/*Collection<Clone> queryGroupContainFunctionClones(CloneGroup group);*/
	
	/**
	 * 查询一个克隆组包含的所有文件克隆关系
	 * @param group
	 * @return
	 */
	/*Collection<Clone> queryGroupContainFileClones(CloneGroup group);*/
	
}
