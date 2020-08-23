package cn.edu.fudan.se.multidependency.repository.as;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.as.data.CycleComponents;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;

@Repository
public interface ASRepository extends Neo4jRepository<Project, Long> {
	
	@Query("match (project:Project)-[:CONTAIN*2]->(file:ProjectFile) where id(project)={id} "
			+ "with file, size((file)-[:DEPENDS_ON]->()) as fanOut, size((file)<-[:DEPENDS_ON]-()) as fanIn "
			+ "where fanOut >= {fanOut} and fanIn >= {fanIn} return file, fanIn, fanOut "
			+ "order by fanIn + fanOut desc;")
	public List<HubLikeFile> findHubLikeFiles(@Param("id") long projectId, @Param("fanIn") int fanIn, @Param("fanOut") int fanOut);
	
	@Query("CALL algo.scc.stream(\"Package\", \"" + RelationType.str_DEPENDS_ON + "\") " + 
			"YIELD nodeId, partition " + 
			"with partition, collect(algo.getNodeById(nodeId)) AS components " + 
			"where size(components) >= 2 " + 
			"return partition, components " + 
			"ORDER BY size(components) DESC")
	public List<CycleComponents<Package>> packageCycles();
	
	@Query("CALL algo.scc.stream(\"ProjectFile\", \"" + RelationType.str_DEPENDS_ON + "\") " + 
			"YIELD nodeId, partition " + 
			"with partition, collect(algo.getNodeById(nodeId)) AS components " + 
			"where size(components) >= 2 " + 
			"return partition, components " + 
			"ORDER BY size(components) DESC")
	public List<CycleComponents<ProjectFile>> fileCycles();
	
	@Query("match (p:Package) where not (p)-[:" + RelationType.str_DEPENDS_ON + "]-() return p")
	public List<Package> unusedPackages();

	@Query("CALL algo.scc.stream(\"Package\", \"" + RelationType.str_DEPENDS_ON + "\") " + 
			"YIELD nodeId, partition " + 
			"with partition, collect(algo.getNodeById(nodeId)) AS packages " + 
			"match result=(a:Package)-[r:" + RelationType.str_DEPENDS_ON + "]->(b:Package) where partition = {partition} and a in packages and b in packages return result")
	public List<DependsOn> cyclePackagesBySCC(@Param("partition") int partition);
	
	@Query("CALL algo.scc.stream(\"ProjectFile\", \"" + RelationType.str_DEPENDS_ON + "\") " + 
			"YIELD nodeId, partition " + 
			"with partition, collect(algo.getNodeById(nodeId)) AS files " + 
			"match result=(a:ProjectFile)-[r:" + RelationType.str_DEPENDS_ON + "]->(b:ProjectFile) where partition = {partition} and a in files and b in files return result")
	public List<DependsOn> cycleFilesBySCC(@Param("partition") int partition);
	
	
	@Query("match p= (file1:ProjectFile)-[r:" + RelationType.str_CO_CHANGE + "]->(file2:ProjectFile) where r.times >= {count} and not (file1)-[:"
			+ RelationType.str_DEPENDS_ON + "]-(file2) return p")
	public List<CoChange> cochangeFilesWithoutDependsOn(@Param("count") int minCoChangeCount);
	
	@Query("MATCH p=(a:Type)-[r:" + RelationType.str_DEPENDS_ON + "]->(b:Type) where (a)<-[:" + RelationType.str_EXTENDS + "*1..]-(b) or (a)<-[:" + RelationType.str_IMPLEMENTS + "*1..]-(b) RETURN p")
	public List<DependsOn> cyclicHierarchyDepends();
	
}
