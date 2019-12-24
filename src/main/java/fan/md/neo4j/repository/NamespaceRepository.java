package fan.md.neo4j.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import fan.md.model.node.code.Namespace;

@Repository
public interface NamespaceRepository extends Neo4jRepository<Namespace, Long> {

}
