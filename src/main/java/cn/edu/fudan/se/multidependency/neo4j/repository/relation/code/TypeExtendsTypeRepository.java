package cn.edu.fudan.se.multidependency.neo4j.repository.relation.code;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.relation.code.TypeExtendsType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Type;

@Repository
public interface TypeExtendsTypeRepository extends Neo4jRepository<TypeExtendsType, Long> {
	
	@Query("match (a:Type)-[r:TYPE_EXTENDS_TYPE]-(b:Type) where id(a) = {0} return b")
    List<Type> findExtendsTypesByTypeId(Long id);
	
}
