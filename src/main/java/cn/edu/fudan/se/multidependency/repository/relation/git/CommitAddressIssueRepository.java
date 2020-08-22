package cn.edu.fudan.se.multidependency.repository.relation.git;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitAddressIssue;

@Repository
public interface CommitAddressIssueRepository extends Neo4jRepository<CommitAddressIssue, Long> {

	@Query("match p= (:commit)-[r:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]->(:Issue) return p")
	List<CommitAddressIssue> queryAllCommitAddressIssues();
	
	@Query("match (issue:Issue) where id(issue)={id} with issue match (issue)<-[:" 
			+ RelationType.str_COMMIT_ADDRESS_ISSUE 
			+ "]-(commit:Commit)-[:" 
			+ RelationType.str_COMMIT_UPDATE_FILE 
			+ "]->(file:ProjectFile) return file")
	List<ProjectFile> queryRelatedFilesOnIssue(@Param("id") long issueId);
}
