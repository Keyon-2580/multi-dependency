package cn.edu.fudan.se.multidependency.repository.relation.clone;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;

@Repository
public interface FileCloneFileRepository extends Neo4jRepository<FileCloneFile, Long> {
	
}
