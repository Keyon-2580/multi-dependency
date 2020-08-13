package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.as.ASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.as.ImplicitCrossModuleDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.LogicCouplingFiles;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;

@Service
public class ImplicitCrossModuleDependencyDetectorImpl implements ImplicitCrossModuleDependencyDetector {
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private GitAnalyseService gitAnalyseService;
	
	@Autowired
	private ASRepository asRepository;
	
	@Autowired
	private CacheService cache;

	@Override
	public Collection<LogicCouplingFiles> cochangesInDifferentModule() {
		String key = "cochangesInDifferentModule";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CoChange> cochangesWithOutDependsOn = asRepository.cochangeFilesWithoutDependsOn(getMinCoChange());
		List<LogicCouplingFiles> result = new ArrayList<>();
		for(CoChange cochange : cochangesWithOutDependsOn) {
			if(staticAnalyseService.isInDifferentModule(cochange.getFile1(), cochange.getFile2())) {
				result.add(new LogicCouplingFiles(cochange.getFile1(), cochange.getFile2(), cochange.getTimes()));
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}
	
	public Collection<LogicCouplingFiles> cochangesInDifferentModuleUsingGitCoChange(int minCochange) {
		Collection<CoChange> allCochanges = gitAnalyseService.calCntOfFileCoChange();
		List<LogicCouplingFiles> result = new ArrayList<>();
		for(CoChange cochange : allCochanges) {
			if(cochange.getTimes() < minCochange) {
				return result;
			}
			if(staticAnalyseService.isInDifferentModule(cochange.getFile1(), cochange.getFile2())
					&& !staticAnalyseService.isDependsOn(cochange.getFile1(), cochange.getFile2())) {
				result.add(new LogicCouplingFiles(cochange.getFile1(), cochange.getFile2(), cochange.getTimes()));
			}
		}
		result.sort((f1, f2) -> {
			return f2.getCochangeTimes() - f1.getCochangeTimes();
		});
		return result;
	}
	
	private int minCoChange = 10;
	
	@Override
	public void setMinCoChange(int minCoChange) {
		this.minCoChange = minCoChange;
		cache.remove(getClass());
	}
	
	@Override
	public int getMinCoChange() {
		return minCoChange;
	}

}
