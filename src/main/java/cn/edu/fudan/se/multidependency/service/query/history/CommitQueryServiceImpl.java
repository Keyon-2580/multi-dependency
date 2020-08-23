package cn.edu.fudan.se.multidependency.service.query.history;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;

@Service
public class CommitQueryServiceImpl implements CommitQueryService {
	
	@Autowired
	private CommitRepository commitRepository;
	
	@Autowired
	private CacheService cache;

	@Override
	public Commit queryCommit(long id) {
		if(cache.findNodeById(id) != null) {
			return (Commit) cache.findNodeById(id);
		}
		Commit result = commitRepository.findById(id).get();
		cache.cacheNodeById(result);
		return result;
	}

	@Override
	public Collection<Commit> queryAllCommits() {
		String key = "queryAllCommits";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<Commit> result = commitRepository.findAllCommits();
		cache.cache(getClass(), key, result);
		return result;
	}

}
