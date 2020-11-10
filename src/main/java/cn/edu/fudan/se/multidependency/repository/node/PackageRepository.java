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
    
	@Query("match (p:Package) where p.directoryPath={directoryPath} and p.language = {language} return p")
	public Package queryPackage(@Param("directoryPath") String directoryPath, @Param("language") String language);
	
	
	@Query("MATCH (pck:Package) where pck.lines > 0\r\n" + 
			"WITH size((pck)-[:" + RelationType.str_CONTAIN + "]->(:ProjectFile)) as nof, \r\n" + 
			"     size((pck)-[:" + RelationType.str_CONTAIN + "*2..4]-(:Function)) as nom,\r\n" + 
			"     size((pck)-[:" + RelationType.str_DEPENDS_ON + "]->()) as fanOut, \r\n" + 
			"     size((pck)<-[:" + RelationType.str_DEPENDS_ON + "]-()) as fanIn,\r\n" + 
			"     pck.loc as loc,\r\n" + 
			"     pck.lines as lines,\r\n " + 
			"     pck\r\n" + 
			"RETURN pck, loc, lines, nof, nom, fanIn, fanOut order by(pck.directoryPath) desc;")
	public List<PackageMetrics> calculatePackageMetrics();

	@Query("MATCH (pck:Package) where id(pck) = {packageId}\r\n" +
			"WITH size((pck)-[:" + RelationType.str_CONTAIN + "]->(:ProjectFile)) as nof, \r\n" +
			"     size((pck)-[:" + RelationType.str_CONTAIN + "*2..4]-(:Function)) as nom,\r\n" +
			"     size((pck)-[:" + RelationType.str_DEPENDS_ON + "]->()) as fanOut, \r\n" +
			"     size((pck)<-[:" + RelationType.str_DEPENDS_ON + "]-()) as fanIn,\r\n" +
			"     pck.loc as loc,\r\n" +
			"     pck.lines as lines,\r\n " +
			"     pck\r\n" +
			"RETURN pck, loc, lines, nof, nom, fanIn, fanOut order by(pck.directoryPath) desc;")
	public PackageMetrics calculatePackageMetrics(@Param("packageId") long packageId);
	
	@Query("match (pck:Package)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) with pck, sum(file.loc) as loc set pck.loc = loc;")
	public void setPackageLoc();
	
	@Query("match (pck:Package)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) with pck, sum(file.endLine) as lines set pck.lines = lines;")
	public void setPackageLines();
	
	@Query("match (pck:Package) where not (pck)-[:CONTAIN]->(:ProjectFile) set pck.loc = 0, pck.lines = 0;")
	public void setEmptyPackageLocAndLines();
	
	/**
	 * 目录下有多少子包
	 * @param directoryPath
	 * @return
	 */
	@Query("match (n:Package) where n.language = {language} and n.directoryPath =~ ({directoryPath} + \"[^/]*/\") return n")
	public Collection<Package> findPackageContainSubPackages(@Param("directoryPath") String directoryPath, @Param("language")String language);
}
