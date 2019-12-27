package cn.edu.fudan.se.multidependency.neo4j.repository.node;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;

@Repository
public interface FileRepository extends Neo4jRepository<ProjectFile, Long> {


}
