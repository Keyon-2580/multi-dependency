package cn.edu.fudan.se.multidependency.repository.node.git;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface CommitRepository extends Neo4jRepository<Commit, Long> {

    @Query("match p = (c:Commit)-[:" + RelationType.str_CONTAIN + "]->(f1:ProjectFile)-[r:" 
    		+ RelationType.str_CO_CHANGE + "]->(f2:ProjectFile)<-[:" + RelationType.str_CONTAIN 
    		+ "]-(c) where id(f1)={file1Id} and id(f2)={file2Id} return c")
	List<Commit> findCommitsInTwoFiles(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);
	
}
