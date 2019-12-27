package cn.edu.fudan.se.multidependency.repository.relation.code;

import cn.edu.fudan.se.multidependency.model.relation.code.TypeContainsVariable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeContainsVariableRepository extends Neo4jRepository<TypeContainsVariable, Long> {

}
