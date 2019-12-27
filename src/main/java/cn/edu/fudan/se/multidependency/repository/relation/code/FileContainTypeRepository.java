package cn.edu.fudan.se.multidependency.repository.relation.code;

import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsType;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileContainTypeRepository extends Neo4jRepository<FileContainsType, Long> {
}

