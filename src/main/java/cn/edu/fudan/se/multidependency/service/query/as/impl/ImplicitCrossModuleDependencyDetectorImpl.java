package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.as.ImplicitCrossModuleDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.ModuleService;
import cn.edu.fudan.se.multidependency.service.query.as.data.LogicCouplingComponents;

@Service
public class ImplicitCrossModuleDependencyDetectorImpl implements ImplicitCrossModuleDependencyDetector {
	
	@Autowired
	private CoChangeRepository cochangeRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private ModuleService moduleService;

	@Override
	public Collection<LogicCouplingComponents<ProjectFile>> cochangesInDifferentModule() {
		String key = "cochangesInDifferentModule";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CoChange> cochangesWithOutDependsOn = cochangeRepository.findGreaterThanCountCoChanges(getMinCoChange());
		List<LogicCouplingComponents<ProjectFile>> result = new ArrayList<>();
		for(CoChange cochange : cochangesWithOutDependsOn) {
			// 两个文件在不同的模块，并且两个文件之间没有依赖关系
			if(moduleService.isInDependence((ProjectFile) cochange.getNode1(), (ProjectFile) cochange.getNode2())) {
				result.add(new LogicCouplingComponents<ProjectFile>((ProjectFile) cochange.getNode1(), (ProjectFile) cochange.getNode2(), cochange.getTimes()));
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}
	
	private int minFileCoChange = 10;
	
	@Override
	public void setMinCoChange(int minCoChange) {
		this.minFileCoChange = minCoChange;
		cache.remove(getClass());
	}
	
	@Override
	public int getMinCoChange() {
		return minFileCoChange;
	}

}
