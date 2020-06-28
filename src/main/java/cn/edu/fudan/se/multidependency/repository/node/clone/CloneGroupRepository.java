package cn.edu.fudan.se.multidependency.repository.node.clone;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;

@Repository
public interface CloneGroupRepository extends Neo4jRepository<CloneGroup, Long> {

	@Query("match (group:CloneGroup) where group.name={name} return group")
	CloneGroup queryCloneGroup(@Param("name") String groupName);
	
}
