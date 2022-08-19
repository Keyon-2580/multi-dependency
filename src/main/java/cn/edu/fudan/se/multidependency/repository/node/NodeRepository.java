package cn.edu.fudan.se.multidependency.repository.node;

import cn.edu.fudan.se.multidependency.model.node.Node;
import org.neo4j.graphdb.Label;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface NodeRepository extends Neo4jRepository<Node, Long> {

    @Query("MATCH (n) where id(n)=$nodeId RETURN count(n)<>0;")
    boolean nodeExists(@Param("nodeId") long nodeId);
    @Query("create (n: $labels $prop) return id(n)")
    long insertNode(@Param("labels")String labels, @Param("prop") Map<String, Object> prop);

    @Query("MATCH (n) RETURN count(n)<>0")
    boolean alreadyInserted();

    @Query("create index for(n:CloneGroup) on (n.cloneLevel, n.name)")
    void createCloneGroupIndex();

    @Query("create index for(n:Commit) on (n.commitId)")
    void createCommitIndex();

    @Query("create index for(n:Feature) on (n.name)")
    void createFeatureIndex();

    @Query("create index for(n:MicroService) on (n.name)")
    void createMicroServiceIndex();

    @Query("create index for(n:Package) on (n.directoryPath)")
    void createPackageIndex();

    @Query("create index for(n:Project) on (n.name, n.language)")
    void createProjectIndex();

    @Query("create index for(n:ProjectFile) on (n.path)")
    void createProjectFileIndex();

    @Query("create index for(n:Trace) on (n.traceId)")
    void createTraceIndex();
}
