package cn.edu.fudan.se.multidependency.repository.relation.clone;

import org.springframework.data.repository.query.Param;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;

import java.util.List;

@Repository
public interface AggregationCloneRepository extends Neo4jRepository<AggregationClone, Long> {
    @Query("match p=()-[r:" + RelationType.str_AGGREGATION_CLONE + "]->() return count(p)")
    int getNumberOfAggregationClone();

    @Query("match (p1:Package), (p2:Package) " +
            "where id(p1) = {pck1Id} and id(p2) = {pck2Id} " +
            "create (p1)-[:" + RelationType.str_AGGREGATION_CLONE + "{parent1Id:{parent1Id}, parent2Id:{parent2Id}, clonePairs:{clonePairs}, allNodesInNode1:{allNodesInNode1}, allNodesInNode2:{allNodesInNode2}, nodesInNode1:{nodesInNode1}, nodesInNode2:{nodesInNode2}}]->(p2)")
    List<AggregationClone> createAggregationClone(@Param("pck1Id") long pck1Id, @Param("pck2Id") long pck2Id, @Param("parent1Id") long parent1Id, @Param("parent2Id") long parent2Id, @Param("clonePairs") int clonePairs, @Param("allNodesInNode1") int allNodesInNode1, @Param("allNodesInNode2") int allNodesInNode2, @Param("nodesInNode1") int nodesInNode1, @Param("nodesInNode2") int nodesInNode2);

    @Query("match p= (p1:Package)-[r:" + RelationType.str_AGGREGATION_CLONE + "]->(p2:Package) where r.parent1Id={parent1Id} and r.parent2Id={parent2Id} return p")
    List<AggregationClone> findAggregationClone(@Param("parent1Id") long parent1Id, @Param("parent2Id") long parent2Id);

    @Query("match p= ()-[r:" + RelationType.str_AGGREGATION_CLONE + "]->() return p")
    List<AggregationClone> getAllAggregationClone();
}
