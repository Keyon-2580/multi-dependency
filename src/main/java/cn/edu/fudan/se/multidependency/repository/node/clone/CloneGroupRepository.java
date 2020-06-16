package cn.edu.fudan.se.multidependency.repository.node.clone;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;

@Repository
public interface CloneGroupRepository extends Neo4jRepository<CloneGroup, Long> {

	@Query("match (group:CloneGroup) where group.level={level} return group")
	List<CloneGroup> findCloneGroupsByLevel(@Param("level") String level);
	
	@Query("match (group:CloneGroup) where group.level={level} and group.name={name} return group")
	CloneGroup findCloneGroupsByLevelAndName(@Param("level") String level, @Param("name") String name);
	
}