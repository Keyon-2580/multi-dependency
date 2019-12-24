package fan.md.neo4j.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import fan.md.model.relation.code.FunctionContainsVariable;

@Repository
public interface FunctionContainsVariableRepository extends Neo4jRepository<FunctionContainsVariable, Long> {

}
