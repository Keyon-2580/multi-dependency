package cn.edu.fudan.se.multidependency.repository.node;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;

@Repository
public interface ProjectRepository extends Neo4jRepository<Project, Long> {
	
	@Query("match (n)-[r]-() delete r")
    void clearRelation();

    @Query("match (n) delete n")
    void clearNode();
    
	@Query("match (project:Project) where project.name={name} and project.language={language} return project")
	Project queryProjectByNameAndLanguage(@Param("name") String name, @Param("language") String language);
	
	@Query("match (project:Project) where project.language={language} return project")
	List<Project> queryProjectsByLanguage(@Param("language") String language);
	
	@Query("match (n) where id(n) = {id} return n")
	Node queryNodeById(@Param("id") long id);
}
