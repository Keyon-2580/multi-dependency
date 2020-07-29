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
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.as.ArchitectureSmellDetector;
import cn.edu.fudan.se.multidependency.service.query.as.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.HubLikeComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.ImplicitCrossModuleDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.SimilarComponentsDetector;
import cn.edu.fudan.se.multidependency.service.query.as.UnstableDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.as.UnusedComponentDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.Cycle;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikePackage;
import cn.edu.fudan.se.multidependency.service.query.as.data.LogicCouplingFiles;
import cn.edu.fudan.se.multidependency.service.query.as.data.MultipleASFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.SimilarComponents;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class ArchitectureSmellDetectorImpl implements ArchitectureSmellDetector {
	
	@Autowired
	private CyclicDependencyDetector cycleASDetector;

	@Autowired
	private UnusedComponentDetector unusedComponentDetector;
	
	@Autowired
	private HubLikeComponentDetector hubLikeComponentDetector;
	
	@Autowired
	private ImplicitCrossModuleDependencyDetector icdDependencyDetector;
	
	@Autowired
	private SimilarComponentsDetector similarComponentsDetector;
	
	@Autowired
	private UnstableDependencyDetector unstableDependencyDetector;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Override
	public Map<Project, List<Cycle<Package>>> cyclePackages(boolean withRelation) {
		return cycleASDetector.cyclePackages(withRelation);
	}
	
	@Override
	public Map<Project, List<Cycle<ProjectFile>>> cycleFiles(boolean withRelation) {
		return cycleASDetector.cycleFiles(withRelation);
	}

	@Override
	public Map<Project, List<Package>> unusedPackages() {
		return unusedComponentDetector.unusedPackage();
	}

	@Override
	public Map<Project, List<HubLikePackage>> hubLikePackages() {
		return hubLikeComponentDetector.hubLikePackages();
	}
	
	@Override
	public Map<Project, List<HubLikeFile>> hubLikeFiles() {
		return hubLikeComponentDetector.hubLikeFiles();
	}

	@Override
	public Collection<LogicCouplingFiles> cochangesInDifferentModule(int minCochange) {
		return icdDependencyDetector.cochangesInDifferentModule(minCochange);
	}

	@Override
	public Map<Project, List<UnstableFile>> unstableFiles() {
		return unstableDependencyDetector.unstableFiles();
	}

	@Override
	public Collection<SimilarComponents<ProjectFile>> similarFiles() {
		return similarComponentsDetector.similarFiles();
	}

	@Override
	public Collection<SimilarComponents<Package>> similarPackages() {
		return similarComponentsDetector.similarPackages();
	}

	@Override
	public Map<Project, List<MultipleASFile>> multipleASFiles(int minCoChangeSInLogicCouplingFiles) {
		Map<Project, List<MultipleASFile>> result = new HashMap<>();
		
		Map<ProjectFile, MultipleASFile> map = new HashMap<>();
		Map<Project, List<Cycle<ProjectFile>>> cycleFiles = cycleFiles(false);
		Map<Project, List<HubLikeFile>> hubLikeFiles = hubLikeFiles();
		Map<Project, List<UnstableFile>> unstableFiles = unstableFiles();
		Collection<LogicCouplingFiles> logicCouplingFiles = cochangesInDifferentModule(minCoChangeSInLogicCouplingFiles);
		Collection<SimilarComponents<ProjectFile>> similarFiles = similarFiles();
		
		for(List<Cycle<ProjectFile>> cycleFilesGroup : cycleFiles.values()) {
			for(Cycle<ProjectFile> files : cycleFilesGroup) {
				for(ProjectFile file : files.getComponents()) {
					MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
					mas.setCycle(true);
					map.put(file, mas);
				}
			}
		}
		
		for(List<HubLikeFile> hubLikeFilesGroup : hubLikeFiles.values()) {
			for(HubLikeFile file : hubLikeFilesGroup) {
				MultipleASFile mas = map.getOrDefault(file.getFile(), new MultipleASFile(file.getFile()));
				mas.setHublike(true);
				map.put(file.getFile(), mas);
			}
		}
		
		for(List<UnstableFile> unstableFilesGroup : unstableFiles.values()) {
			for(UnstableFile file : unstableFilesGroup) {
				MultipleASFile mas = map.getOrDefault(file.getFile(), new MultipleASFile(file.getFile()));
				mas.setUnstable(true);
				map.put(file.getFile(), mas);
			}
		}
		
		for(LogicCouplingFiles files : logicCouplingFiles) {
			MultipleASFile mas = map.getOrDefault(files.getFile1(), new MultipleASFile(files.getFile1()));
			mas.setLogicCoupling(true);
			map.put(files.getFile1(), mas);
			mas = map.getOrDefault(files.getFile2(), new MultipleASFile(files.getFile2()));
			mas.setLogicCoupling(true);
			map.put(files.getFile2(), mas);
		}
		
		for(SimilarComponents<ProjectFile> similarFilesGroup : similarFiles) {
			for(ProjectFile file : similarFilesGroup.getComponents()) {
				MultipleASFile mas = map.getOrDefault(file, new MultipleASFile(file));
				mas.setSimilar(true);
				map.put(file, mas);
			}
		}
		
		for(Map.Entry<ProjectFile, MultipleASFile> entry : map.entrySet()) {
			ProjectFile file = entry.getKey();
			MultipleASFile value = entry.getValue();
			Project project = containRelationService.findFileBelongToProject(file);
			value.setProject(project);
			List<MultipleASFile> temp = result.getOrDefault(project, new ArrayList<>());
			temp.add(value);
			result.put(project, temp);
		}
		
		for(Map.Entry<Project, List<MultipleASFile>> entry : result.entrySet()) {
			entry.getValue().sort((m1, m2) -> {
				if(m2.smellCount() == m1.smellCount()) {
					return m2.getFile().getScore() > m1.getFile().getScore() ? 1 : -1;
				}
				return m2.smellCount() - m1.smellCount();
			});
		}
		
		return result;
	}

}
