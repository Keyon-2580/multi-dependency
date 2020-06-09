package cn.edu.fudan.se.multidependency.repository.relation;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
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
	
	// belongto
	
	@Query("match (m:MicroService)-[r:" + RelationType.str_CONTAIN + "]->(p:Project) where id(p)={projectId} return m")
	public MicroService findProjectBelongToMicroService(@Param("projectId") Long projectId);
	
	@Query("match (p:Project)-[r" + RelationType.str_CONTAIN + "]->(pck:Package) where id(pck)={packageId} return p")
	public Project findPackageBelongToProject(@Param("packageId") Long pckId);
	
	@Query("match (p:Package)-[r" + RelationType.str_CONTAIN + "]->(f:ProjectFile) where id(f)={fileId} return p")
	public Package findFileBelongToPackage(@Param("fileId") Long fileId);
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "*1..2]->(b:Type) where id(b)={typeId} return a")
	public ProjectFile findTypeBelongToFile(@Param("typeId") Long typeId);
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "*1..3]->(b:Function) where id(b)={functionId} return a")
	public ProjectFile findFunctionBelongToFile(@Param("functionId") Long functionId);
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "*1..4]->(b:Variable) where id(b)={variableId} return a")
	public ProjectFile findVariableBelongToFile(@Param("variableId") Long variableId);
	
	@Query("match (a:Type)-[r:" + RelationType.str_CONTAIN + "]->(b:Function) where id(b)={functionId} return a")
	public Type findFunctionBelongToType(@Param("functionId") Long functionId);
	
	@Query("match (a:Type)-[r:" + RelationType.str_CONTAIN + "*1..2]->(b:Variable) where id(b)={variableId} return a")
	public Type findVariableBelongToType(@Param("variableId") Long variableId);
	
	@Query("match (lib:Library)-[r:" + RelationType.str_CONTAIN + "]->(api:LibraryAPI) where id(api)={libraryAPIId} return lib")
	public Library findLibraryAPIBelongToLibrary(@Param("libraryAPIId") Long libraryAPIId);

	// contain
	
	@Query("MATCH (t:Trace{traceId:{traceId}})-[r:" + RelationType.str_CONTAIN + "]->(s:Span) RETURN s")
	public List<Span> findTraceContainSpansByTraceId(@Param("traceId") String traceId);
	
	@Query("MATCH (ms:MicroService)-[r:" + RelationType.str_CONTAIN + "]->(api:RestfulAPI) where id(ms)={id} RETURN api")
	public List<RestfulAPI> findMicroServiceContainRestfulAPI(@Param("id") Long id);
	
	@Query("match (ms:MicroService)-[r:" + RelationType.str_CONTAIN + "]->(p:Project) where id(ms)={msId} return p")
	public List<Project> findMicroServiceContainProjects(@Param("msId") Long msId);
	
	@Query("match p = (f1:Feature)-[r:" + RelationType.str_CONTAIN + "]->(f2:Feature) return p")
	public List<Contain> findAllFeatureContainFeatures();
	
	@Query("match (a:Project)-[r:" + RelationType.str_CONTAIN + "]->(b:Package) where id(a)={projectId} return b")
	public List<Package> findProjectContainPackages(@Param("projectId") Long projectId);
	@Query("match (a:Project)-[r:" + RelationType.str_CONTAIN + "*2]->(b:ProjectFile) where id(a)={projectId} return b")
	public List<ProjectFile> findProjectContainFiles(@Param("projectId") Long projectId);
	@Query("match (a:Project)-[r:" + RelationType.str_CONTAIN + "*3..5]->(b:Function) where id(a)={projectId} return b")
	public List<Function> findProjectContainFunctions(@Param("projectId") Long projectId);
	
	@Query("match (a:Package)-[r:" + RelationType.str_CONTAIN + "]->(b:ProjectFile) where id(a)={packageId} return b")
	public List<ProjectFile> findPackageContainFiles(@Param("packageId") Long packageId);
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "]->(b:Namespace) where id(a)={fileId} return b")
	public List<Namespace> findFileDirectlyContainNamespaces(@Param("fileId") Long fileId);
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "]->(b:Type) where id(a)={fileId} return b")
	public List<Type> findFileDirectlyContainTypes(@Param("fileId") Long fileId);
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "]->(b:Function) where id(a)={fileId} return b")
	public List<Function> findFileDirectlyContainFunctions(@Param("fileId") Long fileId);
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "]->(b:Variable) where id(a)={fileId} return b")
	public List<Variable> findFileDirectlyContainVariables(@Param("fileId") Long fileId);

	@Query("match (a:Namespace)-[r:" + RelationType.str_CONTAIN + "]->(b:Type) where id(a)={namespaceId} return b")
	public List<Type> findNamespaceDirectlyContainTypes(@Param("namespaceId") Long namespaceId);
	@Query("match (a:Namespace)-[r:" + RelationType.str_CONTAIN + "]->(b:Function) where id(a)={namespaceId} return b")
	public List<Function> findNamespaceDirectlyContainFunctions(@Param("namespaceId") Long namespaceId);
	@Query("match (a:Namespace)-[r:" + RelationType.str_CONTAIN + "]->(b:Variable) where id(a)={namespaceId} return b")
	public List<Variable> findNamespaceDirectlyContainVariables(@Param("namespaceId") Long namespaceId);
	
	@Query("match (a:Type)-[r:" + RelationType.str_CONTAIN + "]->(b:Function) where id(a)={typeId} return b")
	public List<Function> findTypeDirectlyContainFunctions(@Param("typeId") Long typeId);
	@Query("match (a:Type)-[r:" + RelationType.str_CONTAIN + "]->(b:Variable) where id(a)={typeId} return b")
	public List<Variable> findTypeDirectlyContainFields(@Param("typeId") Long typeId);
	
	@Query("match (a:Function)-[r:" + RelationType.str_CONTAIN + "]->(b:Variable) where id(a)={functionId} return b")
	public List<Variable> findFunctionDirectlyContainVariables(@Param("functionId") Long functionId);

	@Query("match (lib:Library)-[r:" + RelationType.str_CONTAIN + "]->(api:LibraryAPI) where id(lib)={libraryId} return api")
	public List<LibraryAPI> findLibraryContainLibraryAPIs(@Param("libraryId") Long libraryId);

	@Query("match (group:CloneGroup)-[r:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) where id(group)={groupId} return file")
	public List<ProjectFile> findCloneGroupContainFiles(@Param("groupId") long groupId);
	
	@Query("match (group:CloneGroup)-[r:" + RelationType.str_CONTAIN + "]->(function:Function) where id(group)={groupId} return function")
	public List<Function> findCloneGroupContainFunctions(@Param("groupId") long groupId);
	
	@Query("match (group:CloneGroup)-[r:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) where id(file)={fileId} return group")
	public CloneGroup findFileBelongToCloneGroup(@Param("fileId") long fileId);
	
	@Query("match (group:CloneGroup)-[r:" + RelationType.str_CONTAIN + "]->(function:Function) where id(function)={functionId} return group")
	public CloneGroup findFunctionBelongToCloneGroup(@Param("functionId") long functionId);

}
