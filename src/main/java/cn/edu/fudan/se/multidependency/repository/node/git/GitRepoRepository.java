package cn.edu.fudan.se.multidependency.repository.node.git;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface GitRepoRepository extends Neo4jRepository<GitRepository, Long> {
	
    @Query("match p = (git:GitRepository)-[:" + RelationType.str_CONTAIN + "]->(b:Branch)-[:" 
    		+ RelationType.str_CONTAIN + "]->(c:Commit) where id(c)={commitId} return git")
	GitRepository findCommitBelongToGitRepository(@Param("commitId") long commitId);

}
