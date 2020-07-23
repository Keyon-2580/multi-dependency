package cn.edu.fudan.se.multidependency.repository.as;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.DependOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.spring.as.data.CyclePackages;

@Repository
public interface ASRepository extends Neo4jRepository<Project, Long> {
	
	@Query("CALL algo.scc.stream(\"Package\", \"DEPEND_ON\") " + 
			"YIELD nodeId, partition " + 
			"with partition, collect(algo.getNodeById(nodeId)) AS packages " + 
			"where size(packages) >= 2 " + 
			"return partition, packages " + 
			"ORDER BY size(packages) DESC")
	public List<CyclePackages> cyclePackages();
	
	@Query("match (p:Package) where not (p)-[:" + RelationType.str_DEPEND_ON + "]-() return p")
	public List<Package> unusedPackages();

	@Query("CALL algo.scc.stream(\"Package\", \"DEPEND_ON\") " + 
			"YIELD nodeId, partition " + 
			"with partition, collect(algo.getNodeById(nodeId)) AS packages " + 
			"match result=(a:Package)-[r:DEPEND_ON]->(b:Package) where partition = {partition} and a in packages and b in packages return result")
	public List<DependOn> cyclePackagesBySCC(@Param("partition") int partition);
	
	@Query("match result=(a:Package)-[:DEPEND_ON]->(b:Package) where id(a) in {ids} and id(b) in {ids} return result")
	public List<DependOn> cyclePackagesByIds(@Param("ids") long[] ids);}
