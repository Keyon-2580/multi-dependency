package cn.edu.fudan.se.multidependency.repository.as;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeModule;

@Repository
public interface ASRepository extends Neo4jRepository<Project, Long> {
	
	@Query("match (c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(file:ProjectFile) where id(file) = {id} and c.usingForIssue=true return c")
	public List<Commit> findCommitsUsingForIssue(@Param("id") long fileId);
	
	@Query("match (project:Project)-[:CONTAIN*2]->(file:ProjectFile) where id(project)={id} "
			+ "with file, size((file)-[:DEPENDS_ON]->()) as fanOut, size((file)<-[:DEPENDS_ON]-()) as fanIn "
			+ "where fanOut >= {fanOut} and fanIn >= {fanIn} return file, fanIn, fanOut "
			+ "order by fanIn + fanOut desc;")
	public List<HubLikeFile> findHubLikeFiles(@Param("id") long projectId, @Param("fanIn") int fanIn, @Param("fanOut") int fanOut);
	
	@Query("match (project:Project)-[:CONTAIN]->(module:Module) where id(project) = {id} "
			+ "with module, size((module)-[:DEPENDS_ON]->(:Module)) as fanOut, "
			+ "size((module)<-[:DEPENDS_ON]-(:Module)) as fanIn "
			+ "where fanOut >= {fanOut} and fanIn >= {fanIn} return module, fanOut, fanIn;")
	public List<HubLikeModule> findHubLikeModules(@Param("id") long projectId, @Param("fanIn") int fanIn, @Param("fanOut") int fanOut);
	
	@Query("match (p:Package) where not (p)-[:" + RelationType.str_DEPENDS_ON + "]-() return p")
	public List<Package> unusedPackages();

	@Query("match (f:ProjectFile) where not (f)-[:" + RelationType.str_DEPENDS_ON + "]-() return f")
	public List<ProjectFile> unusedFiles();
	
	@Query("match p= (file1:ProjectFile)-[r:" + RelationType.str_CO_CHANGE + "]->(file2:ProjectFile) where r.times >= {count} and not (file1)-[:"
			+ RelationType.str_DEPENDS_ON + "]-(file2) return p")
	public List<CoChange> cochangeFilesWithoutDependsOn(@Param("count") int minCoChangeCount);
	
	@Query("MATCH p=(a:Type)-[r:" + RelationType.str_DEPENDS_ON + "]->(b:Type) where (a)<-[:" + RelationType.str_EXTENDS + "*1..]-(b) or (a)<-[:" + RelationType.str_IMPLEMENTS + "*1..]-(b) RETURN p")
	public List<DependsOn> cyclicHierarchyDepends();
	
	@Query("match (t:Type) with t, "
			+ "size((t)<-[:EXTENDS]-()) as eCount, "
			+ "size((t)<-[:IMPLEMENTS]-()) as iCount, "
			+ "size((t)<-[:DEPENDS_ON]-()) as fanIn "
			+ "where eCount+iCount > 0 and eCount+iCount = fanIn return t")
	public List<Type> unutilizedTypes();
	
	@Query("match (n:Module) with n limit 1 return (count(n) = 1)")
	public boolean existModule();
	
	@Query("match (pck:Package) "
			+ "create (module:Module{name: pck.directoryPath, size: pck.size}) "
			+ "with pck, module "
			+ "match (pck)-[:CONTAIN]->(file:ProjectFile) "
			+ "with module, file "
			+ "create (module)-[:CONTAIN]->(file) return module, count(file);")
	public void createModule();
	
	
	@Query("match (pck1:Package)-[r:DEPENDS_ON]->(pck2:Package) "
			+ "with pck1, r, pck2 match(m1:Module), (m2:Module) "
			+ "where m1.name = pck1.directoryPath and m2.name = pck2.directoryPath "
			+ "create(m1)-[:DEPENDS_ON{times: r.times}]->(m2);")
	public void createModuleDependsOn();
	
	@Query("match (pck1:Package)-[r:HAS]->(pck2:Package) "
			+ "with pck1, r, pck2 match(m1:Module), (m2:Module) "
			+ "where m1.name = pck1.directoryPath and m2.name = pck2.directoryPath "
			+ "create(m1)-[:HAS]->(m2);")
	public void createModuleHas();
	
	@Query("match (project:Project)-[:CONTAIN]->(pck:Package) "
			+ "with project, pck "
			+ "match (module:Module) where module.name = pck.directoryPath "
			+ "create (project)-[:CONTAIN]->(module);")
	public void createProjectContainsModule();
	
	@Query("match (n:Module) with n "
			+ "with "
			+ "size ((:Module) - [:DEPENDS_ON] -> (n)) as fanIn, "
			+ "size ( (n)-[:DEPENDS_ON]->(:Module) ) as fanOut, n "
			+ "set n.fanIn = fanIn, n.fanOut = fanOut "
			+ "with n "
			+ "match (n) where (n.fanIn + n.fanOut) <> 0 "
			+ "set n.instability = n.fanOut / (n.fanIn + n.fanOut + 0.0);")
	public void setModuleInstability();
	
	@Query("match (n:ProjectFile) with n with size ((:ProjectFile) - [:DEPENDS_ON] -> (n)) as fanIn, size ( (n)-[:DEPENDS_ON]->(:ProjectFile) ) as fanOut, n set n.fanIn = fanIn, n.fanOut = fanOut with n match (n) where (n.fanIn + n.fanOut) <> 0 set n.instability = n.fanOut / (n.fanIn + n.fanOut + 0.0);")
	public void setFileInstability();
	
	@Query("match (n:Package) with n with size ((:Package) - [:DEPENDS_ON] -> (n)) as fanIn, size ( (n)-[:DEPENDS_ON]->(:Package) ) as fanOut, n set n.fanIn = fanIn, n.fanOut = fanOut with n match (n) where (n.fanIn + n.fanOut) <> 0 set n.instability = n.fanOut / (n.fanIn + n.fanOut + 0.0);")
	public void setPackageInstability();
}

