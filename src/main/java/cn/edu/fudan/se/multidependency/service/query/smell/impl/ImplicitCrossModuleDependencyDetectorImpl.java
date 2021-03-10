package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.ImplicitCrossModuleDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.ModuleService;
import cn.edu.fudan.se.multidependency.service.query.smell.data.LogicCouplingComponents;

@Service
public class ImplicitCrossModuleDependencyDetectorImpl implements ImplicitCrossModuleDependencyDetector {
	
	@Autowired
	private CoChangeRepository cochangeRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private ModuleService moduleService;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private PackageRepository packageRepository;

	@Autowired
	private ProjectFileRepository projectFileRepository;

	private int minFileCoChange = 10;
	private int minPackageCoChange = 10;

	@Override
	public Collection<LogicCouplingComponents<ProjectFile>> cochangesInDifferentFile() {
		String key = "cochangesInDifferentFile";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CoChange> cochangesWithOutDependsOn = cochangeRepository.findGreaterThanCountCoChanges(getFileMinCoChange());
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

	@Override
	public Collection<LogicCouplingComponents<Package>> cochangesInDifferentPackage() {
		Collection<LogicCouplingComponents<Package>> result = new ArrayList<>();
		List<Project> projects = projectRepository.queryAllProjects();
		for (Project project : projects) {
			List<Package> packages = packageRepository.getPackageByProjectId(project.getId());
			int length = packages.size();
			for (int i = 0; i < length; i ++) {
				long pckId1 = packages.get(i).getId();
				int fileNumber1 = projectFileRepository.getFilesNumberByPackageId(pckId1);
				for (int j = i + 1; j < length; j ++) {
					long pckId2 = packages.get(j).getId();
					int fileNumber2 = projectFileRepository.getFilesNumberByPackageId(pckId2);
					int fileCoChangeNumber = packageRepository.getCoChangeFileNumberByPackagesId(pckId1, pckId2);
					int fileDependOnNumber = packageRepository.getDependOnFileNumberByPackagesId(pckId1, pckId2);
					CoChange coChange = cochangeRepository.findPackageCoChangeByPackageId(pckId1, pckId2);
					if (coChange != null && coChange.getTimes() >= getPackageMinCoChange() && (fileCoChangeNumber) / (fileNumber1 + fileNumber2 + 0.0) > 0.5 && (coChange.getTimes() / (fileNumber1 + fileNumber2 + 0.0)) >= 1.5 && fileDependOnNumber == 0) {
						result.add(new LogicCouplingComponents<Package>(packages.get(i), packages.get(j), coChange.getTimes()));
					}
				}
			}
		}
		return result;
	}

	@Override
	public void setFileMinCoChange(int minCoChange) {
		this.minFileCoChange = minCoChange;
		cache.remove(getClass());
	}
	
	@Override
	public int getFileMinCoChange() {
		return minFileCoChange;
	}

	@Override
	public void setPackageMinCoChange(int minCoChange) {
		this.minPackageCoChange = minCoChange;
		cache.remove(getClass());
	}

	@Override
	public int getPackageMinCoChange() {
		return minPackageCoChange;
	}
}
