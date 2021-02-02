package cn.edu.fudan.se.multidependency.repository.as;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableComponentByInstability;

@Repository
public interface UnstableASRepository extends Neo4jRepository<ProjectFile, Long> {

	@Query("match (project:Project)-[:CONTAIN*2]->(file:ProjectFile) where id(project)=$projectId and file.fanOut >= $fanOut "
			+ "with file match (file)-[:DEPENDS_ON]->(dependsFile:ProjectFile) "
			+ "where dependsFile.instability > file.instability and dependsFile.fanOut >= $fanOut "
			+ "with file, count(dependsFile) as badDependencies, file.fanOut as allDependencies "
			+ "with file, badDependencies, allDependencies "
			+ "where ((badDependencies + 0.0) / allDependencies) >= $ratio "
			+ "return file as component, file.instability as instability, badDependencies, allDependencies")
	public List<UnstableComponentByInstability<ProjectFile>> unstableFilesByInstability(
			@Param("projectId") long projectId, @Param("fanOut") int fanOut, @Param("ratio") double ratio);
	
	@Query("match (project:Project)-[:CONTAIN]->(pck:Package) where id(project)=$projectId and pck.fanOut >= $fanOut "
			+ "with pck match (pck)-[:DEPENDS_ON]->(dependsPck:Package) "
			+ "where dependsPck.instability > pck.instability and dependsPck.fanOut >= $fanOut "
			+ "with pck, count(dependsPck) as badDependencies, pck.fanOut as allDependencies "
			+ "with pck, badDependencies, allDependencies "
			+ "where ((badDependencies + 0.0) / allDependencies) >= $ratio "
			+ "return pck as component, pck.instability as instability, badDependencies, allDependencies")
	public List<UnstableComponentByInstability<Package>> unstablePackagesByInstability(
			@Param("projectId") long projectId, @Param("fanOut") int fanOut, @Param("ratio") double ratio);
	
	@Query("match (project:Project)-[:CONTAIN]->(module:Module) where id(project)=$projectId and module.fanOut >= $fanOut "
			+ "with module match (module)-[:DEPENDS_ON]->(dependsModule:Module) "
			+ "where dependsModule.instability > module.instability and dependsModule.fanOut >= $fanOut "
			+ "with module, count(dependsModule) as badDependencies, module.fanOut as allDependencies "
			+ "with module, badDependencies, allDependencies "
			+ "where ((badDependencies + 0.0) / allDependencies) >= $ratio "
			+ "return module as component, module.instability as instability, badDependencies, allDependencies")
	public List<UnstableComponentByInstability<Module>> unstableModulesByInstability(
			@Param("projectId") long projectId, @Param("fanOut") int fanOut, @Param("ratio") double ratio);
}
