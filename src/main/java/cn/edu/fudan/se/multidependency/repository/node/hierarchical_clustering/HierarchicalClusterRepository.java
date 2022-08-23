package cn.edu.fudan.se.multidependency.repository.node.hierarchical_clustering;

import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.hierarchical_clustering.HierarchicalCluster;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.hierarchical_clustering.ClusterContain;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HierarchicalClusterRepository extends Neo4jRepository<HierarchicalCluster, Long> {

    @Query("match p=(n:HierarchicalCluster) return n limit 10;")
    List<HierarchicalCluster> findHierarchicalClusterWithLimit();
}
