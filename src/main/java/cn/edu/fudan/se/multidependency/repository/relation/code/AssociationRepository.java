package cn.edu.fudan.se.multidependency.repository.relation.code;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Association;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssociationRepository extends Neo4jRepository<Association, Long> {

	@Query("match (a:Type)-[:" + RelationType.str_ASSOCIATION + "]->(b:Type) where id(a) = {id} return b")
	List<Type> queryAssociationTypes(@Param("id") long typeId);
}