package cn.edu.fudan.se.multidependency.service.query.history;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitAddressIssue;
import cn.edu.fudan.se.multidependency.repository.node.git.IssueRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitAddressIssueRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;

@Service
public class IssueQueryServiceImpl implements IssueQueryService {
	
	@Autowired
	private IssueRepository issueRepository;
	
	@Autowired
	private CommitAddressIssueRepository commitAddressIssueRepository;
	
	@Autowired
	private CacheService cache;

	@Override
	public Collection<Issue> queryAllIssues() {
		String key = "queryAllIssues";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<Issue> result = issueRepository.queryAllIssues();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<ProjectFile> queryRelatedFilesOnIssue(Issue issue) {
		String key = "queryRelatedFilesInIssue_" + issue.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<ProjectFile> result = commitAddressIssueRepository.queryRelatedFilesOnIssue(issue.getId());
		result.sort((f1, f2) -> {
			return f1.getPath().compareTo(f2.getPath());
		});
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<Issue> queryRelatedIssuesOnFile(ProjectFile file) {
		return null;
	}


}
