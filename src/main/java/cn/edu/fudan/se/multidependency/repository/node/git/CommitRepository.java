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

    @Query("match p = (c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f1:ProjectFile)-[r:" 
    		+ RelationType.str_CO_CHANGE + "]->(f2:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE 
    		+ "]-(c) where id(f1)={file1Id} and id(f2)={file2Id} return c")
	List<Commit> findCommitsInTwoFiles(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);
	
    @Query("match (project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(commit:Commit) where id(project)={projectId} return distinct commit;")
    List<Commit> queryCommitsInProject(@Param("projectId") long projectId);
    
    @Query("match (c:Commit) where (c)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-() return c order by c.authoredDate desc;")
    List<Commit> queryAllCommits();
    
    @Query("match (c:Commit)-[:" 
    		+ RelationType.str_COMMIT_UPDATE_FILE 
    		+ "]->(file:ProjectFile) where id(file)={fileId} return c order by c.authoredDate desc;")
    List<Commit> queryUpdatedByCommits(@Param("fileId") long fileId);
}
