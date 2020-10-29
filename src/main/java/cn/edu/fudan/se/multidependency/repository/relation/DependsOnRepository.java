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

	@Query("match p=(p1:Package)-[:" + RelationType.str_DEPENDS_ON + "]-(p2:Package) " +
			"where id(p1) = {pckId1} and id(p2) = {pckId2}" +
			"return p;")
	List<DependsOn> findPackageDependsByPackageId(@Param("pckId1") long pckId1, @Param("pckId2") long pckId2);
	
	@Query("match p=(project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(:ProjectFile)-[r:" + RelationType.str_DEPENDS_ON + "]->(:ProjectFile)<-[:" + RelationType.str_CONTAIN + "*2]-(project) where id(project)={id} return p")
	List<DependsOn> findFileDependsInProject(@Param("id") long projectId);	
	
	@Query("match p=(project:Project)-[:" + RelationType.str_CONTAIN + "]->(:Package)-[r:" + RelationType.str_DEPENDS_ON + "]->(:Package)<-[:" + RelationType.str_CONTAIN + "]-(project) where id(project)={id} return p")
	List<DependsOn> findPackageDependsInProject(@Param("id") long projectId);	
	
	@Query("match p=(file:ProjectFile)-[r:" + RelationType.str_DEPENDS_ON + "]->(:ProjectFile) where id(file)={id} return p")
	List<DependsOn> findFileDependsOn(@Param("id") long fileId);
	
	@Query("match p=(:ProjectFile)-[r:" + RelationType.str_DEPENDS_ON + "]->(file:ProjectFile) where id(file)={id} return p")
	List<DependsOn> findFileDependedOnBy(@Param("id") long fileId);
	
	/**
	 * 两个文件之间的dependsOn
	 * @param file1Id
	 * @param file2Id
	 * @return
	 */
	@Query("match p= (f1:ProjectFile)-[:" + RelationType.str_DEPENDS_ON + "]-(f2:ProjectFile) where id(f1) = {file1Id} and id(f2) = {file2Id} return p")
	List<DependsOn> findDependsOnInFiles(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);
	
	//static final String TYPE_LEFT = "match p=(t1:Type)-[:CONTAIN*0..]->()-[r:";
	//static final String TYPE_RIGHT = "]->()<-[:CONTAIN*0..]-(t2:Type) where t1<>t2 create (t1)-[:DEPENDS_ON]->(t2)";
	
	//@Query(TYPE_LEFT + RelationType.str_CALL + TYPE_RIGHT)
	//void createDependsOnWithCallInTypes();
	//@Query(TYPE_LEFT + RelationType.str_CREATE + TYPE_RIGHT)
	//void createDependsOnWithCreateInTypes();
	//@Query(TYPE_LEFT + RelationType.str_CAST + TYPE_RIGHT)
	//void createDependsOnWithCastInTypes();
	//@Query(TYPE_LEFT + RelationType.str_THROW + TYPE_RIGHT)
	//void createDependsOnWithThrowInTypes();
	//@Query(TYPE_LEFT + RelationType.str_PARAMETER + TYPE_RIGHT)
	//void createDependsOnWithParameterInTypes();
	//@Query(TYPE_LEFT + RelationType.str_VARIABLE_TYPE + TYPE_RIGHT)
	//void createDependsOnWithVariableTypeInTypes();
	//@Query(TYPE_LEFT + RelationType.str_ACCESS + TYPE_RIGHT)
	//void createDependsOnWithAccessInTypes();
	//@Query(TYPE_LEFT + RelationType.str_ANNOTATION + TYPE_RIGHT)
	//void createDependsOnWithAnnotationInTypes();
	//@Query("match (t1:Type)-[r:DEPENDS_ON]->(t2:Type) with t1,t2,count(r) as times create (t1)-[:DEPENDS_ON{times : times}]->(t2)")
	//void createDependsOnWithTimesInTypes();
	//@Query("match (:Type)-[r:DEPENDS_ON]->() where r.times is null delete r;")
	//void deleteNullTimesDependsOnInTypes();

	final String FILE_TYPE_LEFT = "match (f1:ProjectFile)-[:CONTAIN*1..]->(:Type)-[r: ";
	final String FILE_TYPE_MIDDLE = "]->(:Type)<-[:CONTAIN*1..]-(f2:ProjectFile) with f1,f2,r where f1 <> f2 " +
			"create (f1)-[:DEPENDS_ON{dependsOnType: \"";
	final String FILE_TYPE_MIDDLE2 = "\", times : ";
	final String File_TYPE_RIGHT = "}]->(f2);";
	
	@Query(FILE_TYPE_LEFT + RelationType.str_EXTENDS + FILE_TYPE_MIDDLE + RelationType.str_EXTENDS +
			FILE_TYPE_MIDDLE2 + "1" + File_TYPE_RIGHT)
	void createDependsOnWithExtendsInFiles();
	@Query(FILE_TYPE_LEFT + RelationType.str_IMPLEMENTS + FILE_TYPE_MIDDLE + RelationType.str_IMPLEMENTS +
			FILE_TYPE_MIDDLE2 + "1" + File_TYPE_RIGHT)
	void createDependsOnWithImplementsInFiles();
	@Query(FILE_TYPE_LEFT + RelationType.str_DEPENDENCY + FILE_TYPE_MIDDLE + RelationType.str_DEPENDENCY +
			FILE_TYPE_MIDDLE2 + "r.times" + File_TYPE_RIGHT)
	void createDependsOnWithDependencyInFiles();
	@Query(FILE_TYPE_LEFT + RelationType.str_ASSOCIATION + FILE_TYPE_MIDDLE + RelationType.str_ASSOCIATION +
			FILE_TYPE_MIDDLE2 + "r.times" + File_TYPE_RIGHT)
	void createDependsOnWithAssociationInFiles();
	@Query(FILE_TYPE_LEFT + RelationType.str_ANNOTATION + FILE_TYPE_MIDDLE + RelationType.str_ASSOCIATION +
			FILE_TYPE_MIDDLE2 + "r.times" + File_TYPE_RIGHT)
	void createDependsOnWithAnnotationInFiles();

	final String FILE_FUNCTION_LEFT = "match (f1:ProjectFile)-[:CONTAIN]->(:Function)-[r: ";
	final String FILE_FUNCTION_MIDDLE = "]->(:Function)<-[:CONTAIN]-(f2:ProjectFile) with f1,f2,r where f1 <> f2 " +
			"create (f1)-[:DEPENDS_ON{dependsOnType: \"";
	final String FILE_FUNCTION_MIDDLE2 = "\", times : ";
	final String File_FUNCTION_RIGHT = "}]->(f2);";
	@Query(FILE_FUNCTION_LEFT + RelationType.str_CALL + FILE_TYPE_MIDDLE + RelationType.str_CALL +
			FILE_FUNCTION_MIDDLE2 + "1" + File_FUNCTION_RIGHT)
	void createDependsOnWithFunctionCallInFiles();
	@Query(FILE_FUNCTION_LEFT + RelationType.str_IMPLLINK + FILE_TYPE_MIDDLE + RelationType.str_IMPLLINK +
			FILE_FUNCTION_MIDDLE2 + "1" + File_FUNCTION_RIGHT)
	void createDependsOnWithFunctionImpllinkInFiles();
	@Query(FILE_FUNCTION_LEFT + RelationType.str_IMPLEMENTS + FILE_TYPE_MIDDLE + RelationType.str_IMPLEMENTS +
			FILE_FUNCTION_MIDDLE2 + "1" + File_FUNCTION_RIGHT)
	void createDependsOnWithFunctionImplementsInFiles();


	//@Query(FILE_LEFT + RelationType.str_CALL + FILE_RIGHT)
	//void createDependsOnWithCallInFiles();
	//@Query(FILE_LEFT + RelationType.str_CREATE + FILE_RIGHT)
	//void createDependsOnWithCreateInFiles();
	//@Query(FILE_LEFT + RelationType.str_CAST + FILE_RIGHT)
	//void createDependsOnWithCastInFiles();
	//@Query(FILE_LEFT + RelationType.str_THROW + FILE_RIGHT)
	//void createDependsOnWithThrowInFiles();
	//@Query(FILE_LEFT + RelationType.str_PARAMETER + FILE_RIGHT)
	//void createDependsOnWithParameterInFiles();
	//@Query(FILE_LEFT + RelationType.str_VARIABLE_TYPE + FILE_RIGHT)
	//void createDependsOnWithVariableTypeInFiles();
	//@Query(FILE_LEFT + RelationType.str_ACCESS + FILE_RIGHT)
	//void createDependsOnWithAccessInFiles();


	@Query("match (f1:ProjectFile)-[r:DEPENDS_ON]->(f2:ProjectFile) " +
			"with f1,f2,sum(r.times) as times, " +
			"reduce(dependsType = \"\", tt in collect(distinct r.dependsOnType) | dependsType + (\"__\" + tt)) as dependsOnType " +
			"create (f1)-[:DEPENDS_ON{times : times, dependsOnType : dependsOnType}]->(f2);")
	void createDependsOnWithTimesInFiles();
	@Query("match (:ProjectFile)-[r:DEPENDS_ON]->(:ProjectFile) " +
			"where not r.dependsOnType starts with \"__\" " +
			"delete r;")
	void deleteNullAggregationDependsOnInFiles();

	@Query("match (p1:Package)-[:CONTAIN]->(:ProjectFile)-[r:DEPENDS_ON]->(:ProjectFile)<-[:CONTAIN]-(p2:Package) " +
			"where p1<>p2 " +
			"with p1,p2,sum(r.times) as times, " +
			"reduce(tmp = \"\", tt in collect(distinct r.dependsOnType) | tmp + tt) as dependsType " +
			"with p1,p2,times," +
			"reduce(tmp = \"\", tt in split(substring(dependsType, 2), \"__\") | " +
			"case when tmp contains tt then tmp else tmp + (\"__\" + tt) end ) as dependsOnType " +
			"create (p1)-[:DEPENDS_ON{times : times, dependsOnType : dependsOnType}]->(p2);")
	void createDependsOnInPackages();

	@Query("match ()-[r:DEPENDS_ON]->() where r.dependsOnType = {dependsOnType} delete r;")
	void deleteDependsOnByRelationType(String dependsOnType);

}
