package cn.edu.fudan.se.multidependency.repository.relation.clone;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;

@Repository
public interface CloneRepository extends Neo4jRepository<Clone, Long> {
	
	/**
	 * 根据克隆类型找出克隆关系
	 * @param cloneType
	 * @return
	 */
	@Query("match p= ()-[r:" + RelationType.str_CLONE + "]->() where r.cloneType={cloneType} return p")
	public List<Clone> findAllClonesByCloneType(@Param("cloneType") String cloneType);
	
	/**
	 * 根据克隆类型找出包含此克隆类型关系的所有克隆组
	 * @param cloneRelationType
	 * @return
	 */
	@Query("match p= (g:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->()-[r:" + RelationType.str_CLONE + "]->()<-[:" + RelationType.str_CONTAIN + "]-(g) where r.cloneRelationType={cloneRelationType} return g")
	public List<CloneGroup> findGroupsByCloneType(@Param("cloneRelationType") String cloneRelationType);
	
	/**
	 * 根据克隆组的id找出克隆组内的所有克隆关系
	 * @param groupId
	 * @return
	 */
	@Query("match p= (g:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->()-[:" + RelationType.str_CLONE + "]->()<-[:" + RelationType.str_CONTAIN + "]-(g) where id(g)={groupId} return p")
	public List<Clone> findCloneGroupContainClones(@Param("groupId") long groupId);


}
