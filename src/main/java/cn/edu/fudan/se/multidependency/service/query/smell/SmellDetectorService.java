package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.smell.ModuleRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.smell.data.Cycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SmellDetectorService {
	
	@Autowired
	private ModuleRepository moduleRepository;
	
	@Autowired
	private CacheService cache;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ContainRepository containRepository;


	@Autowired
	private CyclicDependencyDetector cyclicDependencyDetector;

	@Autowired
	private SmellRepository smellRepository;

	public void createCloneSmells(){
        smellRepository.deleteSmellContainRelations(SmellType.CLONE);
        smellRepository.deleteSmellHasMetricRelation(SmellType.CLONE);
        smellRepository.deleteSmells(SmellType.CLONE);

		smellRepository.createCloneSmells();
		smellRepository.createCloneSmellContains();
		smellRepository.setSmellProject();
	}

	public void createCycleDependencySmells(){
		smellRepository.deleteSmellContainRelations(SmellType.CYCLIC_DEPENDENCY);
		smellRepository.deleteSmellHasMetricRelation(SmellType.CYCLIC_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.CYCLIC_DEPENDENCY);
		Map<Long, Map<Integer, Cycle<ProjectFile>>> fileCyclicDependencies = new HashMap<>(cyclicDependencyDetector.fileCycles());
		String name = "file_cycle_";
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();
		for (Map.Entry<Long, Map<Integer, Cycle<ProjectFile>>> fileCyclicDependency : fileCyclicDependencies.entrySet()){
			long projectId = fileCyclicDependency.getKey();
			Project project = (Project)projectRepository.queryNodeById(projectId);
			Map<Integer, Cycle<ProjectFile>> fileCycles = fileCyclicDependency.getValue();
			for (Map.Entry<Integer, Cycle<ProjectFile>> fileCycle : fileCycles.entrySet()){
				List<ProjectFile> files = fileCycle.getValue().getComponents();
				Smell smell = new Smell();
				smell.setName(name + fileCycle.getKey().toString());
				smell.setSize(files.size());
				smell.setLanguage(files.get(0).getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.CYCLIC_DEPENDENCY);
				smell.setLevel(SmellLevel.FILE);
//				Smell smell = smellRepository.createFileCyclicDependencySmell(, files.get(0).getLanguage(), projectId);
				smells.add(smell);
				for (ProjectFile file : files) {
					Contain contain = new Contain(smell,file);
					smellContains.add(contain);
//					smellRepository.createFileCyclicDependencySmellContains(smell.getId(), file.getId());
				}
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
	}
}
