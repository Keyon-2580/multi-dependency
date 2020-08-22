package cn.edu.fudan.se.multidependency.service.query.history;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;

public interface IssueQueryService {

	Collection<Issue> queryAllIssues();
	
	Collection<ProjectFile> queryRelatedFilesOnIssue(Issue issue);
	
	Collection<Issue> queryRelatedIssuesOnFile(ProjectFile file);
	
}
