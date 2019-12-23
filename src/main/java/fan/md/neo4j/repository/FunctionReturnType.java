package fan.md.neo4j.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionReturnType extends Neo4jRepository<FunctionReturnType, Long> {

}
