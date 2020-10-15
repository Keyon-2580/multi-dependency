package cn.edu.fudan.se.multidependency.repository.relation.clone;

import org.springframework.data.repository.query.Param;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;

import java.util.List;

@Repository
public interface ModuleCloneRepository extends Neo4jRepository<ModuleClone, Long> {
    @Query("match p=()-[r:" + RelationType.str_MODULE_CLONE + "]->() return count(p)")
    int getNumberOfModuleClone();

    @Query("match (p1:Package), (p2:Package) " +
            "where id(p1) = {pck1Id} and id(p2) = {pck2Id} " +
            "create (p1)-[:" + RelationType.str_MODULE_CLONE + "{clonePairs:{clonePairs}, allNodesInNode1:{allNodesInNode1}, allNodesInNode2:{allNodesInNode2}, nodesInNode1:{nodesInNode1}, nodesInNode2:{nodesInNode2}}]->(p2)")
    List<ModuleClone> createModuleClone(@Param("pck1Id") long pck1Id, @Param("pck2Id") long pck2Id, @Param("clonePairs") int clonePairs, @Param("allNodesInNode1") int allNodesInNode1, @Param("allNodesInNode2") int allNodesInNode2, @Param("nodesInNode1") int nodesInNode1, @Param("nodesInNode2") int nodesInNode2);

    @Query("match p= (p1:Package)-[r:" + RelationType.str_MODULE_CLONE + "]->(p2:Package) where id(p1)={pck1Id} and id(p2)={pck2Id} return p")
    ModuleClone findModuleCloneBetweenTwoPackages(@Param("pck1Id") long pck1Id, @Param("pck2Id") long pck2Id);

    @Query("match p= ()-[r:" + RelationType.str_MODULE_CLONE + "]->() return p")
    List<ModuleClone> getAllModuleClone();

    @Query("match p = (p1:Package) - [:" + RelationType.str_MODULE_CLONE + "] -> (p2:Package), " +
            "(p1) -[:CONTAIN] -> (f1:ProjectFile) <-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]- (c:Commit) - [:" + RelationType.str_COMMIT_UPDATE_FILE + "]-> (f2:ProjectFile) <- [:CONTAIN] - (p2) \n" +
            "where size( (f1) -[:CLONE] - (f2) ) > 0 " +
            "with p, count(distinct c) as moduleCloneCoChangeTimes " +
            "where moduleCloneCoChangeTimes >= {minCoChangeTimes} " +
            "foreach (r in relationships(p)  |  set r.moduleCloneCochangeTimes = moduleCloneCoChangeTimes ) " +
            "with p return p")
    List<ModuleClone> setModuleCloneCochangeTimes(@Param("minCoChangeTimes") int minCoChangeTimes);
}
