package cn.edu.fudan.se.multidependency.neo4j.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.code.VariableIsType;

@Repository
public interface VariableIsTypeRepository extends Neo4jRepository<VariableIsType, Long>{

}
