package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.repository.smell.ModuleRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.smell.data.Cycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	private StaticAnalyseService staticAnalyseService;

	@Autowired
	private CyclicDependencyDetector cyclicDependencyDetector;

	@Autowired
	private SmellRepository smellRepository;

	public void createCloneSmells(){
		smellRepository.createCloneSmells();
		smellRepository.createCloneSmellContains();
		smellRepository.setSmellProject();

		Map<Long, Map<Integer, Cycle<ProjectFile>>> fileCyclicDependencies = new HashMap<>(cyclicDependencyDetector.fileCycles());
		String name = "file_cycle_";
		for (Map.Entry<Long, Map<Integer, Cycle<ProjectFile>>> fileCyclicDependency : fileCyclicDependencies.entrySet()){
			long projectId = fileCyclicDependency.getKey();
			Map<Integer, Cycle<ProjectFile>> fileCycles = fileCyclicDependency.getValue();
			for (Map.Entry<Integer, Cycle<ProjectFile>> fileCycle : fileCycles.entrySet()){
				List<ProjectFile> files = fileCycle.getValue().getComponents();
				Smell smell = smellRepository.createFileCyclicDependencySmell(name + fileCycle.getKey().toString(), files.size(), files.get(0).getLanguage(), projectId);
				for (ProjectFile file : files) {
					smellRepository.createFileCyclicDependencySmellContains(smell.getId(), file.getId());
				}
			}
		}
	}
}
