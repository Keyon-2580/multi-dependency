package cn.edu.fudan.se.multidependency.repository.relation;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
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

	@Query("match (p:Package)-[r:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) where id(file) = {fileId} return p")
	Package findFileBelongPackageByFileId(@Param("fileId") long fileId);

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

	@Query("match p=(pck:Package)-[r:" + RelationType.str_DEPENDS_ON + "]->(:Package) where id(pck)={id} return p")
	List<DependsOn> findPackageDependsOn(@Param("id") long packageId);

	@Query("match p=(:Package)-[r:" + RelationType.str_DEPENDS_ON + "]->(pck:Package) where id(pck)={id} return p")
	List<DependsOn> findPackageDependedOnBy(@Param("id") long packageId);

	@Query("match p=(file1:ProjectFile)-[:" + RelationType.str_DEPENDS_ON + "]->(file3:ProjectFile)<-[:" + RelationType.str_DEPENDS_ON + "]-(file2:ProjectFile) where id(file1)={id1} and id(file2)={id2} return file3")
	List<ProjectFile> findFilesCommonDependsOn(@Param("id1") long fileId1,@Param("id2") long fileId2);

	@Query("match p=(file1:ProjectFile)<-[:" + RelationType.str_DEPENDS_ON + "]-(file3:ProjectFile)-[:" + RelationType.str_DEPENDS_ON + "]->(file2:ProjectFile) where id(file1)={id1} and id(file2)={id2} return file3")
	List<ProjectFile> findFilesCommonDependedOnBy(@Param("id1") long fileId1,@Param("id2") long fileId2);
	/**
	 * 两个文件之间的dependsOn
	 * @param file1Id
	 * @param file2Id
	 * @return
	 */
	@Query("match p= (f1:ProjectFile)-[:" + RelationType.str_DEPENDS_ON + "]-(f2:ProjectFile) where id(f1) = {file1Id} and id(f2) = {file2Id} return p")
	List<DependsOn> findDependsOnInFiles(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);

	@Query("match p= (f1:ProjectFile)-[:" + RelationType.str_DEPENDS_ON + "]->(f2:ProjectFile) where id(f1) = {file1Id} and id(f2) = {file2Id} return p")
	DependsOn findSureDependsOnInFiles(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);


	static final String TYPE_LEFT = "match p=(t1:Type)-[:CONTAIN*0..]->()-[r:";
	static final String TYPE_MIDDLE = "]->()<-[:CONTAIN*0..]-(t2:Type) where t1<>t2 " +
			"create (t1)-[:DEPENDS_ON{dependsOnType : \"";
	static final String TYPE_MIDDLE2 = "\", times : ";
	static final String TYPE_RIGHT = "}]->(t2);";
	
	@Query(TYPE_LEFT + RelationType.str_CALL + TYPE_MIDDLE + RelationType.str_CALL +
			TYPE_MIDDLE2 + "1" + TYPE_RIGHT)
	void createDependsOnWithCallInTypes();
	@Query(TYPE_LEFT + RelationType.str_CREATE + TYPE_MIDDLE + RelationType.str_CREATE +
			TYPE_MIDDLE2 + "1" + TYPE_RIGHT)
	void createDependsOnWithCreateInTypes();
	@Query(TYPE_LEFT + RelationType.str_CAST + TYPE_MIDDLE + RelationType.str_CAST +
			TYPE_MIDDLE2 + "1" + TYPE_RIGHT)
	void createDependsOnWithCastInTypes();
	@Query(TYPE_LEFT + RelationType.str_THROW + TYPE_MIDDLE + RelationType.str_THROW +
			TYPE_MIDDLE2 + "1" + TYPE_RIGHT)
	void createDependsOnWithThrowInTypes();
	@Query(TYPE_LEFT + RelationType.str_PARAMETER + TYPE_MIDDLE + RelationType.str_PARAMETER +
			TYPE_MIDDLE2 + "1" + TYPE_RIGHT)
	void createDependsOnWithParameterInTypes();
	@Query(TYPE_LEFT + RelationType.str_USE + TYPE_MIDDLE + RelationType.str_USE +
			TYPE_MIDDLE2 + "1" + TYPE_RIGHT)
	void createDependsOnWithUseTypeInTypes();
	@Query(TYPE_LEFT + RelationType.str_ACCESS + TYPE_MIDDLE + RelationType.str_ACCESS +
			TYPE_MIDDLE2 + "1" + TYPE_RIGHT)
	void createDependsOnWithAccessInTypes();
	@Query(TYPE_LEFT + RelationType.str_ANNOTATION + TYPE_MIDDLE + RelationType.str_ANNOTATION +
			TYPE_MIDDLE2 + "1" + TYPE_RIGHT)
	void createDependsOnWithAnnotationInTypes();

	@Query("match (t1:Type)-[r:DEPENDS_ON]->(t2:Type) with t1,t2,count(r) as times create (t1)-[:DEPENDS_ON{times : times}]->(t2)")
	void createDependsOnWithTimesInTypes();
	@Query("match (:Type)-[r:DEPENDS_ON]->() where r.times is null delete r;")
	void deleteNullTimesDependsOnInTypes();

	final String FILE_LEFT = "match (f1:ProjectFile)-[:CONTAIN*0..]->()-[r: ";
	final String FILE_MIDDLE = "]->()<-[:CONTAIN*0..]-(f2:ProjectFile) where f1 <> f2 " +
			"create (f1)-[:DEPENDS_ON{dependsOnType : \"";
	final String FILE_MIDDLE2 = "\", times : ";
	final String FILE_RIGHT = "}]->(f2);";

	@Query(FILE_LEFT + RelationType.str_IMPORT + FILE_MIDDLE + RelationType.str_IMPORT +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithImportInFiles();
	@Query(FILE_LEFT + RelationType.str_INCLUDE + FILE_MIDDLE + RelationType.str_INCLUDE +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithIncludeInFiles();
	@Query(FILE_LEFT + RelationType.str_EXTENDS + FILE_MIDDLE + RelationType.str_EXTENDS +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithExtendsInFiles();
	@Query(FILE_LEFT + RelationType.str_IMPLEMENTS + FILE_MIDDLE + RelationType.str_IMPLEMENTS +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithImplementsInFiles();
//	@Query(FILE_LEFT + RelationType.str_DEPENDENCY + FILE_MIDDLE + RelationType.str_DEPENDENCY +
//			FILE_MIDDLE2 + "r.times" + FILE_RIGHT)
//	void createDependsOnWithDependencyInFiles();
	@Query(FILE_LEFT + RelationType.str_ASSOCIATION + FILE_MIDDLE + RelationType.str_ASSOCIATION +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithAssociationInFiles();
	@Query(FILE_LEFT + RelationType.str_ANNOTATION + FILE_MIDDLE + RelationType.str_ANNOTATION +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithAnnotationInFiles();
	@Query(FILE_LEFT + RelationType.str_CALL + FILE_MIDDLE + RelationType.str_CALL +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithCallInFiles();
	@Query(FILE_LEFT + RelationType.str_IMPLLINK + FILE_MIDDLE + RelationType.str_IMPLLINK +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithImpllinkInFiles();
	@Query(FILE_LEFT + RelationType.str_IMPLEMENTS + FILE_MIDDLE + RelationType.str_IMPLEMENTS_C +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithFunctionImplementsInFiles();
	@Query(FILE_LEFT + RelationType.str_CREATE + FILE_MIDDLE + RelationType.str_CREATE +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithCreateInFiles();
	@Query(FILE_LEFT + RelationType.str_CAST + FILE_MIDDLE + RelationType.str_CAST +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithCastInFiles();
	@Query(FILE_LEFT + RelationType.str_THROW + FILE_MIDDLE + RelationType.str_THROW +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithThrowInFiles();
	@Query(FILE_LEFT + RelationType.str_PARAMETER + FILE_MIDDLE + RelationType.str_PARAMETER +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithParameterInFiles();
	@Query(FILE_LEFT + RelationType.str_RETURN + FILE_MIDDLE + RelationType.str_RETURN +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithReturnInFiles();
	@Query(FILE_LEFT + RelationType.str_USE + FILE_MIDDLE + RelationType.str_USE +
			FILE_MIDDLE2 + "1" + FILE_RIGHT)
	void createDependsOnWithUseTypeInFiles();
//	@Query(FILE_LEFT + RelationType.str_ACCESS + FILE_MIDDLE + RelationType.str_ACCESS +
//			FILE_MIDDLE2 + "1" + FILE_RIGHT)
//	void createDependsOnWithAccessInFiles();


//	@Query("match (f1:ProjectFile)-[r:DEPENDS]->(f2:ProjectFile) " +
//			"with f1,f2, r.dependsType, sum(r.times) as times" +
//			"reduce(dependsType = \"\", tt in collect(r.dependsType) | dependsType + (\"__\" + tt)) as dependsOnType " +
//			"create (f1)-[:DEPENDS{times : times, dependsOnType : dependsOnType}]->(f2);")
//	void createDependsWithTimesInFiles();

	@Query("match (:ProjectFile)-[r:DEPENDS_ON]->(:ProjectFile) " +
			"where r.dependsOnIntensity is null " +
			"delete r;")
	void deleteNullAggregationDependsOnInFiles();

	@Query("match (p1:Package)-[:CONTAIN]->(:ProjectFile)-[r:DEPENDS_ON]->(:ProjectFile)<-[:CONTAIN]-(p2:Package) " +
			"where p1<>p2 " +
			"create (p1)-[:DEPENDS_ON{dependsOnType : r.dependsOnType, times : r.times}]->(p2);")
	void createDependsOnInPackages();

	@Query("match (:Package)-[r:DEPENDS_ON]->(:Package) " +
			"where r.dependsOnIntensity is null " +
			"delete r;")
	void deleteNullAggregationDependsOnInPackages();

	@Query("match ()-[r:DEPENDS_ON]->() where r.dependsOnType = {dependsOnType} delete r;")
	void deleteDependsOnByRelationType(String dependsOnType);

}
