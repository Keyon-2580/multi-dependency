package cn.edu.fudan.se.multidependency.repository.node.code;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Project;

@Repository
public interface ProjectRepository extends Neo4jRepository<Project, Long> {
	
	@Query("match (n)-[r]-() delete r")
    void clearRelation();

    @Query("match (n) delete n")
    void clearNode();
}
