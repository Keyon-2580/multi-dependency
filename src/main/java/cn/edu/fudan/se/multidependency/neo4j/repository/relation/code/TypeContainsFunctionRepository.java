package cn.edu.fudan.se.multidependency.neo4j.repository.relation.code;

import cn.edu.fudan.se.multidependency.model.relation.code.TypeContainsFunction;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeContainsFunctionRepository extends Neo4jRepository<TypeContainsFunction, Long> {

}
