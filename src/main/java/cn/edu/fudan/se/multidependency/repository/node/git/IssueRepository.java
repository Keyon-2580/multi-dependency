package cn.edu.fudan.se.multidependency.repository.node.git;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.git.Issue;

@Repository
public interface IssueRepository extends Neo4jRepository<Issue, Long> {

	@Query("match (issue:Issue) return issue;")
	List<Issue> queryAllIssues();
	
}
