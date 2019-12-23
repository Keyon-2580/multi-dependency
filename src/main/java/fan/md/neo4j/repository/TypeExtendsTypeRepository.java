package fan.md.neo4j.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import fan.md.model.entity.code.Type;
import fan.md.model.relation.code.TypeExtendsType;

@Repository
public interface TypeExtendsTypeRepository extends Neo4jRepository<TypeExtendsType, Long> {
	
	@Query("match (a:Type)-[r:TYPE_EXTENDS_TYPE]-(b:Type) where id(a) = {0} return b")
    List<Type> findExtendsTypesByTypeId(Long id);
	
}
