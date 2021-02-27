package cn.edu.fudan.se.multidependency.repository.node;

import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricRepository extends Neo4jRepository<Metric, Long> {

    @Query("MATCH p=(t:Type)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(t) = $typeId RETURN m")
    Metric findTypeMetrics(@Param("typeId") Long typeId);

    @Query("MATCH p=(t:Type)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m")
    List<Metric> findTypeMetrics();

    @Query("MATCH p=(t:ProjectFile)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(t) = $fileId RETURN m")
    Metric findFileMetrics(@Param("fileId") Long fileId);

    @Query("MATCH p=(t:ProjectFile)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m")
    List<Metric> findFileMetrics();

    @Query("MATCH p=(t:Project)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(t) = $projectId RETURN m")
    Metric findProjectMetrics(@Param("projectId") Long projectId);

    @Query("MATCH p=(t:Project)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m")
    List<Metric> findProjectMetrics();

    @Query("MATCH p=(t:Commit)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(t) = $commitId RETURN m")
    Metric findCommitMetrics(@Param("commitId") Long commitId);

    @Query("MATCH p=(t:Commit)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m")
    List<Metric> findCommitMetrics();
}
