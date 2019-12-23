package fan.md.neo4j.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import fan.md.model.node.Project;

@Repository
public interface ProjectRepository extends Neo4jRepository<Project, Long> {
	
	@Query("match (n)-[r]-() delete r")
    void clearRelation();

    @Query("match (n) delete n")
    void clearNode();

}
