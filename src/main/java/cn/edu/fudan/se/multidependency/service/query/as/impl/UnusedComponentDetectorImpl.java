package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.repository.as.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.as.UnusedComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class UnusedComponentDetectorImpl implements UnusedComponentDetector {
	
	@Autowired
	private ASRepository asRepository;
	
	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private CacheService cache;

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, List<Package>> unusedPackages() {
		String key = "unusedPackages";
		Map<Long, List<Package>> result = null;
		if(cache.get(this.getClass(), key) != null) {
			return (Map<Long, List<Package>>) cache.get(this.getClass(), key);
		}
		result = new HashMap<>();
		Collection<Package> pcks = asRepository.unusedPackages();
		for(Package pck : pcks) {
			Project project = containRelationService.findPackageBelongToProject(pck);
			List<Package> temp = result.getOrDefault(project, new ArrayList<>());
			temp.add(pck);
			result.put(project.getId(), temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

}
