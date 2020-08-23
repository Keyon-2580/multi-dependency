package cn.edu.fudan.se.multidependency.service.query.history;

import java.util.Collection;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;

public interface IssueQueryService {
	
	Issue queryIssue(long id);

	Collection<Issue> queryAllIssues();
	
	/**
	 * 有commit提交的issue
	 * @return
	 */
	Collection<Issue> queryIssueAddressedByCommit();
	
	Set<ProjectFile> queryRelatedFilesOnAllIssues();
	
	Collection<ProjectFile> queryRelatedFilesOnIssue(Issue issue);
	
	Collection<Issue> queryRelatedIssuesOnFile(ProjectFile file);
	
	Collection<Commit> queryRelatedCommitsOnIssue(Issue issue);
	
	Collection<Issue> queryIssuesAddressedByCommit(Commit commit);
}
