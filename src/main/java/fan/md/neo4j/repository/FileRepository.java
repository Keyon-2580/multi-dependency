package fan.md.neo4j.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import fan.md.model.entity.code.CodeFile;

@Repository
public interface FileRepository extends Neo4jRepository<CodeFile, Long> {


}
