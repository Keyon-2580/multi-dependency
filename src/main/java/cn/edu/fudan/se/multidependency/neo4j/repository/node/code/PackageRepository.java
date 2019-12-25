package cn.edu.fudan.se.multidependency.neo4j.repository.node.code;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.code.Package;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Type;

@Repository
public interface PackageRepository extends Neo4jRepository<Package, Long> {
    
    @Query("match (a:Package)-[r1:PACKAGE_CONTAIN_FILE]-(b:File)-[r2:FILE_CONTAIN_TYPE]-(c:Type) where id(a) = {0} return c")
    List<Type> findTypesByPackage(Long id);

}
