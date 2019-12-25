package cn.edu.fudan.se.multidependency.neo4j.repository.relation.code;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsFunction;

@Repository
public interface FileContainFunctionRepository extends Neo4jRepository<FileContainsFunction, Long> {

}
