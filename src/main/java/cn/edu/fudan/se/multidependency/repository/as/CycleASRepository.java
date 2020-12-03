package cn.edu.fudan.se.multidependency.repository.as;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.query.as.data.CycleComponents;

@Repository
public interface CycleASRepository extends Neo4jRepository<ProjectFile, Long> {

	@Query("CALL gds.alpha.scc.stream({" +
			"nodeProjection: \'Package\', " +
			"relationshipProjection: \'" + RelationType.str_DEPENDS_ON + "\'}) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS components " +
			"where size(components) >= 2 " + 
			"return partition, components " + 
			"ORDER BY size(components) DESC")
	public List<CycleComponents<Package>> packageCycles();
	
	@Query("CALL gds.alpha.scc.stream({" +
			"nodeProjection: \'Module\', " +
			"relationshipProjection: \'" + RelationType.str_DEPENDS_ON + "\'}) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS components " +
			"where size(components) >= 2 " + 
			"return partition, components " + 
			"ORDER BY size(components) DESC")
	public List<CycleComponents<Module>> moduleCycles();
	
	@Query("CALL gds.alpha.scc.stream({" +
			"nodeProjection:\'ProjectFile\', " +
			"relationshipProjection: \'" + RelationType.str_DEPENDS_ON + "\'}) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS components " +
			"where size(components) >= 2 " + 
			"return partition, components " + 
			"ORDER BY size(components) DESC")
	public List<CycleComponents<ProjectFile>> fileCycles();
	
	@Query("CALL gds.alpha.scc.stream({" +
			"nodeProjection: \'Package\', " +
			"relationshipProjection: \'" + RelationType.str_DEPENDS_ON + "\'}) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS packages " +
			"match result=(a:Package)-[r:" + RelationType.str_DEPENDS_ON + "]->(b:Package) "
					+ "where partition = {partition} and a in packages and b in packages return result")
	public List<DependsOn> cyclePackagesBySCC(@Param("partition") int partition);
	
	@Query("CALL gds.alpha.scc.stream({" +
			"nodeProjection:\'ProjectFile\', " +
			"relationshipProjection: \'" + RelationType.str_DEPENDS_ON + "\'}) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS files " +
			"match result=(a:ProjectFile)-[r:" + RelationType.str_DEPENDS_ON + "]->(b:ProjectFile) "
					+ "where partition = {partition} and a in files and b in files return result")
	public List<DependsOn> cycleFilesBySCC(@Param("partition") int partition);
	
	@Query("CALL gds.alpha.scc.stream({" +
			"nodeProjection: \'Module\', " +
			"relationshipProjection: \'" + RelationType.str_DEPENDS_ON + "\'}) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS modules " +
			"match result=(a:Module)-[r:" + RelationType.str_DEPENDS_ON + "]->(b:Module) "
					+ "where partition = {partition} and a in modules and b in modules return result")
	public List<DependsOn> cycleModulesBySCC(@Param("partition") int partition);
	
}