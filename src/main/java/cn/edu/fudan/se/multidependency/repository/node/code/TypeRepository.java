package cn.edu.fudan.se.multidependency.repository.node.code;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Type;

@Repository
public interface TypeRepository extends Neo4jRepository<Type, Long> {
	
	
}
