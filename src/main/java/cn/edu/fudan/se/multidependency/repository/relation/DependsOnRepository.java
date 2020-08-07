package cn.edu.fudan.se.multidependency.repository.relation;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface DependsOnRepository extends Neo4jRepository<DependsOn, Long> {

	@Query("match p=(:ProjectFile)-[r:" + RelationType.str_DEPENDS_ON + "]->(:ProjectFile) return p")
	List<DependsOn> findFileDepends();	
	
	@Query("match p=(:Package)-[r:" + RelationType.str_DEPENDS_ON + "]->(:Package) return p")
	List<DependsOn> findPackageDepends();	
	
	@Query("match p=(project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(:ProjectFile)-[r:" + RelationType.str_DEPENDS_ON + "]->(:ProjectFile)<-[:" + RelationType.str_CONTAIN + "*2]-(project) where id(project)={id} return p")
	List<DependsOn> findFileDependsInProject(@Param("id") long projectId);	
	
	@Query("match p=(project:Project)-[:" + RelationType.str_CONTAIN + "]->(:Package)-[r:" + RelationType.str_DEPENDS_ON + "]->(:Package)<-[:" + RelationType.str_CONTAIN + "]-(project) where id(project)={id} return p")
	List<DependsOn> findPackageDependsInProject(@Param("id") long projectId);	
	
	@Query("match p= (f1:ProjectFile)-[:" + RelationType.str_DEPENDS_ON + "]-(f2:ProjectFile) where id(f1) = {file1Id} and id(f2) = {file2Id} return p")
	List<DependsOn> findDependsOnInFiles(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);
	
	
	static final String LEFT = "match (f1:ProjectFile)-[:CONTAIN*1..]->()-[r:";
	static final String RIGHT = "]->()<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2 where f1 <> f2 create (f1)-[:DEPENDS_ON]->(f2);";
	
	@Query(LEFT + RelationType.str_EXTENDS + RIGHT)
	void createDependsOnWithExtends();
	@Query(LEFT + RelationType.str_IMPLEMENTS + RIGHT)
	void createDependsOnWithImplements();
	@Query(LEFT + RelationType.str_CALL + RIGHT)
	void createDependsOnWithCall();
	@Query(LEFT + RelationType.str_CREATE + RIGHT)
	void createDependsOnWithCreate();
	@Query(LEFT + RelationType.str_CAST + RIGHT)
	void createDependsOnWithCast();
	@Query(LEFT + RelationType.str_THROW + RIGHT)
	void createDependsOnWithThrow();
	@Query(LEFT + RelationType.str_PARAMETER + RIGHT)
	void createDependsOnWithParameter();
	@Query(LEFT + RelationType.str_VARIABLE_TYPE + RIGHT)
	void createDependsOnWithVariableType();
	@Query(LEFT + RelationType.str_ACCESS + RIGHT)
	void createDependsOnWithAccess();
	@Query(LEFT + RelationType.str_IMPLLINK + RIGHT)
	void createDependsOnWithImpllink();
	@Query(LEFT + RelationType.str_ANNOTATION + RIGHT)
	void createDependsOnWithAnnotation();
	@Query("match (f1:ProjectFile)-[r:DEPENDS_ON]->(f2:ProjectFile) with f1,f2,count(r) as times create (f1)-[:DEPENDS_ON{times : times}]->(f2)")
	void createDependsOnWithTimes();
	@Query("match (:ProjectFile)-[r:DEPENDS_ON]->() where r.times is null delete r;")
	void deleteNullTimesDependsOn();
	@Query("match (p1:Package)-[:CONTAIN]->(:ProjectFile)-[:DEPENDS_ON]->(:ProjectFile)<-[:CONTAIN]-(p2:Package) with p1,p2 where p1<>p2 and not (p1)-[:DEPENDS_ON]->(p2) create (p1)-[:DEPENDS_ON]->(p2)")
	void createDependsOnInPackage();
	@Query("match (p1:Package)-[r:DEPENDS_ON]->(p2:Package) with p1,p2,count(r) as times create (p1)-[:DEPENDS_ON{times : times}]->(p2)")
	void addTimesOnDependsOnInPackage();
	@Query("match (:Package)-[r:DEPENDS_ON]->() where r.times is null delete r;")
	void deleteNullTimesDependsOnInPackage();
}
