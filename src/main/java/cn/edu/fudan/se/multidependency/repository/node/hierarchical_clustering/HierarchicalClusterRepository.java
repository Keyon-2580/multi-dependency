package cn.edu.fudan.se.multidependency.repository.node.hierarchical_clustering;

import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.hierarchical_clustering.HierarchicalCluster;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.hierarchical_clustering.ClusterContain;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HierarchicalClusterRepository extends Neo4jRepository<HierarchicalCluster, Long> {

    @Query("match p=(n:HierarchicalCluster) return n limit 10;")
    List<HierarchicalCluster> findHierarchicalClusterWithLimit();

    @Query("match p=(c:HierarchicalCluster)-[r:" + RelationType.str_CLUSTER_CONTAIN + "]->(f:ProjectFile) where id(f)=$fileId return c;")
    HierarchicalCluster findClusterContainFiles(@Param("fileId") Long fileId);

    @Query("match p=(c:HierarchicalCluster)-[:" + RelationType.str_CLUSTER_CONTAIN + "*]->(f:ProjectFile) where id(f)=$fileId " +
            "and not (:HierarchicalCluster)-[:CLUSTER_CONTAIN]->(c) return c;")
    HierarchicalCluster findTopParentClusterOfFile(@Param("fileId") Long fileId);

    @Query("match p=(c:HierarchicalCluster)-[:" + RelationType.str_CLUSTER_CONTAIN + "*]->(f:ProjectFile) where id(f)=$fileId " +
            "and not (:HierarchicalCluster)-[:CLUSTER_CONTAIN]->(c) return length(p) - 1;")
    Integer findPathLengthFromTopParentClusterToFile(@Param("fileId") Long fileId);


    @Query("match p=(c:HierarchicalCluster)-[:" + RelationType.str_CLUSTER_CONTAIN + "*]->(f:ProjectFile) where " +
            "not (:HierarchicalCluster)-[:CLUSTER_CONTAIN]->(c) return c;")
    List<HierarchicalCluster> findAllTopParentCluster();

    @Query("MATCH p=(n1:HierarchicalCluster)<-[:" + RelationType.str_CLUSTER_CONTAIN + "*]-(:HierarchicalCluster)" +
            "-[:" + RelationType.str_CLUSTER_CONTAIN + "*]->(n2:HierarchicalCluster) " +
            "where id(n1)=$clusterId1 and id(n2)=$clusterId2 RETURN min(length(p));")
    int calDistanceBetweenClusters(@Param("clusterId1") Long clusterId1, @Param("clusterId2") Long clusterId2);

    @Query("MATCH p=(n1:ProjectFile)<-[:" + RelationType.str_CLUSTER_CONTAIN + "*]-(:HierarchicalCluster)" +
            "-[:" + RelationType.str_CLUSTER_CONTAIN + "*]->(n2:ProjectFile) " +
            "where id(n1)=$fileId1 and id(n2)=$fileId2 RETURN min(length(p)) - 2;")
    int calDistanceBetweenFiles(@Param("fileId1") Long fileId1, @Param("fileId2") Long fileId2);
}
