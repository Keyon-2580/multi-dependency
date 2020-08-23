package cn.edu.fudan.se.multidependency.service.query.history;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;

public interface CommitQueryService {

	Commit queryCommit(long id);
	
	Collection<Commit> queryAllCommits();
}
