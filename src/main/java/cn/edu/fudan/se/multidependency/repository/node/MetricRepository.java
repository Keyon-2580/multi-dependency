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

    @Query("MATCH p=(t:Type)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(t) = $typeId RETURN m;")
    Metric findTypeMetric(@Param("typeId") Long typeId);

    @Query("MATCH p=(:Type)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m;")
    List<Metric> findTypeMetric();

    @Query("MATCH p=(file:ProjectFile)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(file) = $fileId RETURN m;")
    Metric findFileMetric(@Param("fileId") Long fileId);

    @Query("MATCH p=(:ProjectFile)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m;")
    List<Metric> findFileMetric();

    @Query("MATCH p=(:ProjectFile)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m limit 10;")
    List<Metric> findFileMetricsWithLimit();

    @Query("MATCH p=(:ProjectFile)-[r:" + RelationType.str_HAS + "]->(m:Metric) delete r, m;")
    void deleteAllFileMetric();

    @Query("MATCH p=(pck:Package)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(pckId) = $pckId RETURN m;")
    Metric findPackageMetric(@Param("pckId") Long pckId);

    @Query("MATCH p=(:Package)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m;")
    List<Metric> findPackageMetric();

    @Query("MATCH p=(:Package)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m limit 10;")
    List<Metric> findPackageMetricsWithLimit();

    @Query("MATCH p=(:Package)-[r:" + RelationType.str_HAS + "]->(m:Metric) delete r, m;")
    void deleteAllPackageMetric();

    @Query("MATCH p=(project:Project)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(project) = $projectId RETURN m")
    Metric findProjectMetric(@Param("projectId") Long projectId);

    @Query("MATCH p=(:Project)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m;")
    List<Metric> findProjectMetric();

    @Query("MATCH p=(:Project)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m limit 10;")
    List<Metric> findProjectMetricsWithLimit();

    @Query("MATCH p=(:Project)-[r:" + RelationType.str_HAS + "]->(m:Metric) delete r, m;")
    void deleteAllProjectMetric();

    @Query("MATCH p=(commit:Commit)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(commit) = $commitId RETURN m;")
    Metric findCommitMetric(@Param("commitId") Long commitId);

    @Query("MATCH p=(:Commit)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m;")
    List<Metric> findCommitMetric();
}
