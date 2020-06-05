package cn.edu.fudan.se.multidependency.repository.node.code;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Project;

@Repository
public interface ProjectRepository extends Neo4jRepository<Project, Long> {
	
	@Query("match (n)-[r]-() delete r")
    void clearRelation();

    @Query("match (n) delete n")
    void clearNode();
    
	@Query("match (project:Project) where project.name={name} and project.language={language} return f")
	Project findProjectByNameAndLanguage(@Param("name") String name, @Param("language") String language);
}
