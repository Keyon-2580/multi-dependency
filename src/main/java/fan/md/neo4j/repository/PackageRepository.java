package fan.md.neo4j.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import fan.md.model.entity.code.Package;
import fan.md.model.entity.code.Type;

@Repository
public interface PackageRepository extends Neo4jRepository<Package, Long> {
    
    @Query("match (a:Package)-[r1:PACKAGE_CONTAIN_FILE]-(b:File)-[r2:FILE_CONTAIN_TYPE]-(c:Type) where id(a) = {0} return c")
    List<Type> findTypesByPackage(Long id);

}
