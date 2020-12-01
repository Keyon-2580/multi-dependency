package cn.edu.fudan.se.multidependency.repository.relation;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.Has;

import java.util.List;

@Repository
public interface HasRepository extends Neo4jRepository<Has, Long> {

    @Query("match (project:Project)-[r:" + RelationType.str_CONTAIN + "]->(pck:Package) where not (pck)<-[:" + RelationType.str_HAS + "]-(:Package) and id(project)={projectId} return pck")
    public List<Package> findProjectHasPackages(@Param("projectId") Long projectId);

    @Query("Match (pck:Package)-[:" + RelationType.str_HAS + "]->(children:Package) where id(pck)={packageId} return children")
    public List<Package> findPackageHasPackages(@Param("packageId") Long packageId);

    @Query("Match (parent:Package)-[:" + RelationType.str_HAS + "]->(pck:Package) where id(pck)={packageId} return parent")
    public Package findPackageInPackage(@Param("packageId") Long packageId);
}
