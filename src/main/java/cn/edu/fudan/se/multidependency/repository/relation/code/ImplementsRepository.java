package cn.edu.fudan.se.multidependency.repository.relation.code;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.structure.Extends;

@Repository
public interface ImplementsRepository extends Neo4jRepository<Extends, Long> {

}
