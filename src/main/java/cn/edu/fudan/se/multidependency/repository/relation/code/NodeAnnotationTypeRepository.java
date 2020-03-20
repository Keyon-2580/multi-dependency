package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.NodeAnnotationType;

@Repository
public interface NodeAnnotationTypeRepository extends Neo4jRepository<NodeAnnotationType, Long> {

	@Query("MATCH result=()-[r:" + RelationType.str_NODE_ANNOTATION_TYPE + "]->(type:Type) with type,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3]->(type) where id(project)={projectId} RETURN result")
	List<NodeAnnotationType> findProjectContainNodeAnnotationTypeRelations(@Param("projectId") Long projectId);
}