package cn.edu.fudan.se.multidependency.repository.node;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.service.spring.as.CyclePackages;
import cn.edu.fudan.se.multidependency.service.spring.metric.PackageMetrics;

@Repository
public interface PackageRepository extends Neo4jRepository<Package, Long> {
    
	@Query("match (p:Package) where p.directoryPath={directoryPath} return p")
	public Package queryPackage(@Param("directoryPath") String directoryPath);
	
	
	@Query("MATCH (pck:Package)-[:CONTAIN]->(file:ProjectFile)\r\n" + 
			"WITH pck, sum(file.endLine) as loc\r\n" + 
			"WITH size((pck)-[:CONTAIN]->(:ProjectFile)) as nof, \r\n" + 
			"     size((pck)-[:CONTAIN*2..4]-(:Function)) as nom,\r\n" + 
			"     size((pck)-[:DEPEND_ON]->()) as fanOut, \r\n" + 
			"     size((pck)<-[:DEPEND_ON]-()) as fanIn,\r\n" + 
			"     loc,\r\n" + 
			"     pck\r\n" + 
			"RETURN pck, loc, nof, nom, fanIn, fanOut order by(pck.directoryPath) desc;")
	public List<PackageMetrics> calculatePackageMetrics();
	
	@Query("CALL algo.scc.stream(\"Package\", \"DEPEND_ON\") " + 
			"YIELD nodeId, partition " + 
			"with partition, collect(algo.getNodeById(nodeId)) AS packages " + 
			"where size(packages) >= 2 " + 
			"return partition, packages " + 
			"ORDER BY size(packages) DESC")
	public List<CyclePackages> cyclePackages();
	
}
