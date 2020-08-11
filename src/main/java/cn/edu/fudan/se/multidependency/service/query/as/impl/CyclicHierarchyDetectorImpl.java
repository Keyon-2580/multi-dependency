package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.repository.as.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.as.CyclicHierarchyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.CyclicHierarchy;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class CyclicHierarchyDetectorImpl implements CyclicHierarchyDetector {
	
	@Autowired
	private ASRepository asRepository;
	
	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private CacheService cache;
	
	@Override
	public Map<Long, List<CyclicHierarchy>> cyclicHierarchies() {
		String key = "cyclicHierarchies";
		if(cache.get(getClass(), key) != null) {
			return (Map<Long, List<CyclicHierarchy>>) cache.get(getClass(), key);
		}
		List<DependsOn> hierarchyDepends = asRepository.cyclicHierarchyDepends();
		Map<Type, CyclicHierarchy> typeToCycleHierarchies = new HashMap<>();
		for(DependsOn relation : hierarchyDepends) {
			Type superType = (Type) relation.getStartNode();
			CyclicHierarchy cycleHierarchy = typeToCycleHierarchies.getOrDefault(superType, new CyclicHierarchy(superType));
			cycleHierarchy.addDependsOn(relation);
			typeToCycleHierarchies.put(superType, cycleHierarchy);
		}
		
		Map<Long, List<CyclicHierarchy>> result = new HashMap<>();
		for(Map.Entry<Type, CyclicHierarchy> entry : typeToCycleHierarchies.entrySet()) {
			Type superType = entry.getKey();
			CyclicHierarchy cycleHierarchy = entry.getValue();
			Project project = containRelationService.findCodeNodeBelongToProject(superType);
			List<CyclicHierarchy> temp = result.getOrDefault(project, new ArrayList<>());
			temp.add(cycleHierarchy);
			result.put(project.getId(), temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

}
