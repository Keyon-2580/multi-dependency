package cn.edu.fudan.se.multidependency.repository.relation;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface ContainRepository extends Neo4jRepository<Contain, Long> {

	@Query("MATCH (t:Trace{traceId:{traceId}})-[r:" + RelationType.str_CONTAIN + "]->(s:Span) RETURN s")
	public List<Span> findTraceContainSpansByTraceId(@Param("traceId") String traceId);
	
	@Query("match (p:Package)-[r" + RelationType.str_CONTAIN + "]->(f:ProjectFile) where id(f)={fileId} return p")
	public Package findFileBelongToPackageByFileId(@Param("fileId") Long id);
	
	@Query("match (m:MicroService)-[r:" + RelationType.str_CONTAIN + "]->(p:Project) where id(m)={msId} return p")
	public List<Project> findMicroServiceContainProject(@Param("msId") Long microserviceId);
	
	@Query("match p = (f1:Feature)-[r:" + RelationType.str_CONTAIN + "]->(f2:Feature) return p")
	public List<Contain> findAllFeatureContainFeatures();
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "*1..2]->(b:Function) where id(b)={functionId} return a")
	public ProjectFile findFunctionBelongToFileByFunctionId(@Param("functionId") Long functionId);
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "]->(b:Type) where id(b)={typeId} return a")
	public ProjectFile findTypeBelongToFileByTypeId(@Param("typeId") Long typeId);
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "*1..3]->(b:Variable) where id(b)={variableId} return a")
	public ProjectFile findVariableBelongToFileByVariableId(@Param("variableId") Long variableId);
	
	@Query("match (a:Type)-[r:" + RelationType.str_CONTAIN + "]->(b:Function) where id(b)={functionId} return a")
	public Type findFunctionBelongToTypeByFunctionId(@Param("functionId") Long functionId);
}
