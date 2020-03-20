package cn.edu.fudan.se.multidependency.repository.relation;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.dynamic.FileDependOnFile;

@Repository
public interface FileDependOnFileRepository extends Neo4jRepository<FileDependOnFile, Long> {

}
