package cn.edu.fudan.se.multidependency.repository.node;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.spring.metric.ProjectMetrics;

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

	@Query("match (project:Project) return project")
	List<Project> queryAllProjects();
	
	@Query("match (n) where id(n) = {id} return n")
	Node queryNodeById(@Param("id") long id);
	
	@Query("MATCH (project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)\r\n" + 
			"WITH project, sum(file.endLine) as loc\r\n" +
			"WITH size((project)-[:" + RelationType.str_CONTAIN + "]->(:Package)) as nop, \r\n" + 
			"     size((project)-[:" + RelationType.str_CONTAIN + "*2]->(:ProjectFile)) as nof,\r\n" + 
			"     size((project)-[:" + RelationType.str_CONTAIN + "*3..5]-(:Function)) as nom,\r\n" + 
			"     loc,\r\n" + 
			"     project\r\n" + 
			"RETURN project, nop, nof, nom, loc order by(project.name) desc;")
	public List<ProjectMetrics> calculateProjectMetrics();
}
