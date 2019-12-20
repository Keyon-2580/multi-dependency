package fan.md.neo4j.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import fan.md.model.relation.code.TypeExtendsType;

@Repository
public interface TypeExtendsTypeRepository extends Neo4jRepository<TypeExtendsType, Long> {

}
