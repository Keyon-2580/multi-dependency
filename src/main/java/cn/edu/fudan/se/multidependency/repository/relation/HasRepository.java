package cn.edu.fudan.se.multidependency.repository.relation;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.Has;

@Repository
public interface HasRepository extends Neo4jRepository<Has, Long> {

}
