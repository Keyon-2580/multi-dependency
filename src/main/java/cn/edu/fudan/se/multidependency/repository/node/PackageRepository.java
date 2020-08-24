package cn.edu.fudan.se.multidependency.repository.node;

import java.util.Collection;
import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetrics;

@Repository
public interface PackageRepository extends Neo4jRepository<Package, Long> {
    
	@Query("match (p:Package) where p.directoryPath={directoryPath} return p")
	public Package queryPackage(@Param("directoryPath") String directoryPath);
	
	
	@Query("MATCH (pck:Package)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)\r\n" + 
			"WITH pck, sum(file.loc) as loc, sum(file.endLine) as lines\r\n" + 
			"WITH size((pck)-[:" + RelationType.str_CONTAIN + "]->(:ProjectFile)) as nof, \r\n" + 
			"     size((pck)-[:" + RelationType.str_CONTAIN + "*2..4]-(:Function)) as nom,\r\n" + 
			"     size((pck)-[:" + RelationType.str_DEPENDS_ON + "]->()) as fanOut, \r\n" + 
			"     size((pck)<-[:" + RelationType.str_DEPENDS_ON + "]-()) as fanIn,\r\n" + 
			"     loc,\r\n" + 
			"     lines,\r\n " + 
			"     pck\r\n" + 
			"RETURN pck, loc, lines, nof, nom, fanIn, fanOut order by(pck.directoryPath) desc;")
	public List<PackageMetrics> calculatePackageMetrics();
	
	@Query("match (pck:Package)-[:CONTAIN]->(file:ProjectFile) with pck, sum(file.loc) as loc set pck.loc = loc;")
	public void setPackageLoc();
	
	@Query("match (pck:Package)-[:CONTAIN]->(file:ProjectFile) with pck, sum(file.endLine) as lines set pck.lines = lines;")
	public void setPackageLines();
	
	/**
	 * 目录下有多少子包
	 * @param directoryPath
	 * @return
	 */
	@Query("match (n:Package) where n.directoryPath =~ ({directoryPath} + \"[^/]*/\") return n")
	public Collection<Package> findPackageContainSubPackages(@Param("directoryPath") String directoryPath);
}
