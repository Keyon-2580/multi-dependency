package cn.edu.fudan.se.multidependency.repository.relation.git;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitUpdateFile;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommitUpdateFileRepository extends Neo4jRepository<CommitUpdateFile, Long> {

    @Query("match (c:Commit)-[r:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f:ProjectFile) where id(c)={cId} return f")
    List<ProjectFile> findUpdatedFilesByCommitId(@Param("cId") long commitId);
    
    @Query("match p=(c:Commit)-[r:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(:ProjectFile) where id(c)={id} return p")
    List<CommitUpdateFile> findCommitUpdatedFiles(@Param("id") long commitId);
    
    @Query("match (c:Commit) with c, size((c)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(:ProjectFile)) as size set c.commitFilesSize = size return count(c);")
    int setCommitFilesSize();
}
