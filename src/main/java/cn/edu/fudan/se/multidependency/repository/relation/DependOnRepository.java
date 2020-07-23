package cn.edu.fudan.se.multidependency.repository.relation;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.DependOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface DependOnRepository extends Neo4jRepository<DependOn, Long> {

	@Query("match p=(:ProjectFile)-[r:" + RelationType.str_DEPEND_ON + "]->(:ProjectFile) return p")
	public List<DependOn> findFileDepends();	
	
	@Query("match p=(:Package)-[r:" + RelationType.str_DEPEND_ON + "]->(:Package) return p")
	public List<DependOn> findPackageDepends();	
	
	@Query("match p=(project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(:ProjectFile)-[r:" + RelationType.str_DEPEND_ON + "]->(:ProjectFile)<-[:" + RelationType.str_CONTAIN + "*2]-(project) where id(project)={id} return p")
	public List<DependOn> findFileDependsInProject(@Param("id") long projectId);	
	
	@Query("match p=(project:Project)-[:" + RelationType.str_CONTAIN + "]->(:Package)-[r:" + RelationType.str_DEPEND_ON + "]->(:Package)<-[:" + RelationType.str_CONTAIN + "]-(project) where id(project)={id} return p")
	public List<DependOn> findPackageDependsInProject(@Param("id") long projectId);	
	
	
}
