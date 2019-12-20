package fan.md.neo4j.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import fan.md.model.entity.code.Type;

@Repository
public interface TypeRepository extends Neo4jRepository<Type, Long> {
	
}
