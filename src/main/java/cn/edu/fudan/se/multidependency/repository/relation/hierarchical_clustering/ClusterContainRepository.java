package cn.edu.fudan.se.multidependency.repository.relation.hierarchical_clustering;

import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.hierarchical_clustering.ClusterContain;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterContainRepository extends Neo4jRepository<ClusterContain, Long> {

    @Query("match p=()-[:" + RelationType.str_CLUSTER_CONTAIN + "]->() return p limit 10;")
    List<ClusterContain> findClusterContainWithLimit();
}
