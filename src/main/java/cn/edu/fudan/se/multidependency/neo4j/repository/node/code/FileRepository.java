package cn.edu.fudan.se.multidependency.neo4j.repository.node.code;

import cn.edu.fudan.se.multidependency.model.node.code.CodeFile;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends Neo4jRepository<CodeFile, Long> {


}
