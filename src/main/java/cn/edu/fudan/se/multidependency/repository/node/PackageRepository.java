package cn.edu.fudan.se.multidependency.repository.node;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;

@Repository
public interface PackageRepository extends Neo4jRepository<Package, Long> {
    
	@Query("match (p:Package) where p.directoryPath={directoryPath} return p")
	public Package queryPackage(@Param("directoryPath") String directoryPath);
	
}
