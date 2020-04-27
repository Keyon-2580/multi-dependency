package cn.edu.fudan.se.multidependency.repository.node.code;

import cn.edu.fudan.se.multidependency.model.node.Project;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends Neo4jRepository<Project, Long> {
	
	@Query("match (n)-[r]-() delete r")
    void clearRelation();

    @Query("match (n) delete n")
    void clearNode();

    @Query("Match (p:Project) where p.name={projectName} return p")
    public Project findProjectByProjectName(@Param("projectName") String projectName);

}
