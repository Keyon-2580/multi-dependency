package cn.edu.fudan.se.multidependency.repository.relation;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface ContainRepository extends Neo4jRepository<Contain, Long> {

	@Query("MATCH (t:Trace{traceId:{traceId}})-[r:" + RelationType.str_CONTAIN + "]->(s:Span) RETURN s")
	public List<Span> findTraceContainSpansByTraceId(@Param("traceId") String traceId);
	
	@Query("MATCH (ms:MicroService)-[r:" + RelationType.str_CONTAIN + "]->(api:RestfulAPI) where id(ms)={id} RETURN api")
	public List<RestfulAPI> findMicroServiceContainRestfulAPI(@Param("id") Long id);
	
	@Query("match (p:Package)-[r" + RelationType.str_CONTAIN + "]->(f:ProjectFile) where id(f)={fileId} return p")
	public Package findFileBelongToPackageByFileId(@Param("fileId") Long id);
	
	@Query("match (ms:MicroService)-[r:" + RelationType.str_CONTAIN + "]->(p:Project) where id(ms)={msId} return p")
	public List<Project> findMicroServiceContainProjects(@Param("msId") Long msId);
	
	@Query("match p = (f1:Feature)-[r:" + RelationType.str_CONTAIN + "]->(f2:Feature) return p")
	public List<Contain> findAllFeatureContainFeatures();
	
	@Query("match (a:Project)-[r:" + RelationType.str_CONTAIN + "*3..4]->(b:Function) where id(b)={functionId} return a")
	public Project findFunctionBelongToProjectByFunctionId(@Param("functionId") Long functionId);
	
	@Query("match (a:Project)-[r:" + RelationType.str_CONTAIN + "*3..4]->(b:Function) where id(a)={projectId} return b")
	public List<Function> findProjectContainFunctionsByProjectId(@Param("projectId") Long projectId);
	
	@Query("match (a:Project)-[r:" + RelationType.str_CONTAIN + "]->(b:Package) where id(a)={projectId} return b")
	public List<Package> findProjectContainPackages(@Param("projectId") Long projectId);
	
	@Query("match (a:Package)-[r:" + RelationType.str_CONTAIN + "]->(b:ProjectFile) where id(a)={packageId} return b")
	public List<ProjectFile> findPackageContainFiles(@Param("packageId") Long packageId);
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "]->(b:Type) where id(a)={fileId} return b")
	public List<Type> findFileContainTypes(@Param("fileId") Long fileId);
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "]->(b:Function) where id(a)={fileId} return b")
	public List<Function> findFileContainFunctions(@Param("fileId") Long fileId);
	
	@Query("match (a:Type)-[r:" + RelationType.str_CONTAIN + "]->(b:Function) where id(a)={typeId} return b")
	public List<Function> findTypeContainFunctions(@Param("typeId") Long typeId);
	
	@Query("match (a:Type)-[r:" + RelationType.str_CONTAIN + "]->(b:Variable) where id(a)={typeId} return b")
	public List<Variable> findTypeContainVariables(@Param("typeId") Long typeId);
	
	@Query("match (a:Function)-[r:" + RelationType.str_CONTAIN + "]->(b:Variable) where id(a)={functionId} return b")
	public List<Variable> findFunctionContainVariables(@Param("functionId") Long functionId);

	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "*1..2]->(b:Function) where id(b)={functionId} return a")
	public ProjectFile findFunctionBelongToFileByFunctionId(@Param("functionId") Long functionId);
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "]->(b:Type) where id(b)={typeId} return a")
	public ProjectFile findTypeBelongToFileByTypeId(@Param("typeId") Long typeId);
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "*1..3]->(b:Variable) where id(b)={variableId} return a")
	public ProjectFile findVariableBelongToFileByVariableId(@Param("variableId") Long variableId);
	
	@Query("match (a:Type)-[r:" + RelationType.str_CONTAIN + "]->(b:Function) where id(b)={functionId} return a")
	public Type findFunctionBelongToTypeByFunctionId(@Param("functionId") Long functionId);
	
	@Query("match (m:MicroService)-[r:" + RelationType.str_CONTAIN + "]->(p:Project) where id(p)={projectId} return m")
	public MicroService findProjectBelongToMicroService(@Param("projectId") Long projectId);
	
	@Query("match (lib:Library)-[r:" + RelationType.str_CONTAIN + "]->(api:LibraryAPI) where id(api)={libraryAPIId) return lib")
	public Library findLibraryAPIBelongToLibrary(@Param("libraryAPIId") Long libraryAPIId);
	
	@Query("match (lib:Library)-[r:" + RelationType.str_CONTAIN + "]->(api:LibraryAPI) where id(lib)={libraryId) return api")
	public List<LibraryAPI> findLibraryContainLibraryAPIs(@Param("libraryId") Long libraryId);
}
