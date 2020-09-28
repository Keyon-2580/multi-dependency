package cn.edu.fudan.se.multidependency.repository.node.git;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.service.query.history.data.GitRepoMetric;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

import java.util.List;

@Repository
public interface GitRepoRepository extends Neo4jRepository<GitRepository, Long> {
	
    @Query("match p = (git:GitRepository)-[:" + RelationType.str_CONTAIN + "]->(b:Branch)-[:" 
    		+ RelationType.str_CONTAIN + "]->(c:Commit) where id(c)={commitId} return git")
	GitRepository findCommitBelongToGitRepository(@Param("commitId") long commitId);

	@Query("match (gitRepo:GitRepository)-[:" + RelationType.str_CONTAIN + "*2]->(commit:Commit) where id(gitRepo)={gitRepoId} return distinct commit;")
	List<Commit> queryCommitsInGitRepo(@Param("gitRepoId") long gitRepoId);
}
