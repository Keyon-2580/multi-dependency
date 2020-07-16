package cn.edu.fudan.se.multidependency.repository.relation;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.DependOn;

@Repository
public interface DependOnRepository extends Neo4jRepository<DependOn, Long> {

}
