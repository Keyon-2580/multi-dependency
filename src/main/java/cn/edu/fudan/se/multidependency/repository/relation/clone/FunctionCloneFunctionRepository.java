package cn.edu.fudan.se.multidependency.repository.relation.clone;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;

@Repository
public interface FunctionCloneFunctionRepository extends Neo4jRepository<FunctionCloneFunction, Long> {

}