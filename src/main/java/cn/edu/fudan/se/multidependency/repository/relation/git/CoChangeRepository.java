package cn.edu.fudan.se.multidependency.repository.relation.git;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;


@Repository
public interface CoChangeRepository extends Neo4jRepository<CoChange, Long> {

	/**
	 * 找出cochange次数至少为count的cochange关系
	 * @param count
	 * @return
	 */
    @Query("match p= ()-[r:" + RelationType.str_CO_CHANGE + "]->() where r.times >= {count} return p")
    List<CoChange> findGreaterThanCountCoChanges(@Param("count") int count);
    
    /**
     * 找出两个指定文件的cochange关系
     * @param file1Id
     * @param file2Id
     * @return
     */
    @Query("match p= (f1:ProjectFile)-[r:" + RelationType.str_CO_CHANGE + "]->(f2:ProjectFile) where id(f1)={file1Id} and id(f2)={file2Id} return p")
    CoChange findCoChangesBetweenTwoFiles(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);
    
    @Query("match (f1:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f2:ProjectFile) where id(f1) < id(f2) and (c.merge=false or c.merge is null) with f1,f2,count(c) as times where times >= {minCoChangeTimes} create p=(f1)-[:" + RelationType.str_CO_CHANGE 
    		+ "{times:times}]->(f2) with p return count(p)")
    List<CoChange> createCoChanges(@Param("minCoChangeTimes") int minCoChangeTimes);
    
    @Query("match p= (f:ProjectFile)-[r:" + RelationType.str_CO_CHANGE + "]->(:ProjectFile) where id(f)={fileId} return p")
    List<CoChange> cochangesRight(@Param("fileId") long fileId);
    
    @Query("match p= (:ProjectFile)-[r:" + RelationType.str_CO_CHANGE + "]->(f:ProjectFile) where id(f)={fileId} return p")
    List<CoChange> cochangesLeft(@Param("fileId") long fileId);
}
