package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.model.node.Package;
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
import cn.edu.fudan.se.multidependency.service.query.smell.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SmellDetectorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmellDetectorService.class);

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
	private HubLikeComponentDetector hubLikeComponentDetector;

	@Autowired
	private UnstableDependencyDetectorUsingInstability unstableDependencyDetectorUsingInstability;

	@Autowired
	private ImplicitCrossModuleDependencyDetector implicitCrossModuleDependencyDetector;

	@Autowired
	private SimilarComponentsDetector similarComponentsDetector;

	@Autowired
	private SmellRepository smellRepository;

	public void createCloneSmells(boolean isRecreate){
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.CLONE);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Clone Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
        smellRepository.deleteSmellContainRelations(SmellType.CLONE);
        smellRepository.deleteSmellHasMetricRelation(SmellType.CLONE);
        smellRepository.deleteSmells(SmellType.CLONE);

		smellRepository.createCloneSmells();
		smellRepository.createCloneSmellContains();
		smellRepository.setSmellProject();
	}

	public void createCycleDependencySmells(boolean isRecreate){
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.CYCLIC_DEPENDENCY);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Cycle Dependency Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.CYCLIC_DEPENDENCY);
		smellRepository.deleteSmellHasMetricRelation(SmellType.CYCLIC_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.CYCLIC_DEPENDENCY);
		Map<Long, Map<Integer, Cycle<ProjectFile>>> fileCyclicDependencies = new HashMap<>(cyclicDependencyDetector.fileCycles());
		String name = "file_cycle-dependency_";
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();
		for (Map.Entry<Long, Map<Integer, Cycle<ProjectFile>>> fileCyclicDependency : fileCyclicDependencies.entrySet()){
			long projectId = fileCyclicDependency.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			Map<Integer, Cycle<ProjectFile>> fileCycles = fileCyclicDependency.getValue();
			for (Map.Entry<Integer, Cycle<ProjectFile>> fileCycle : fileCycles.entrySet()){
				List<ProjectFile> files = fileCycle.getValue().getComponents();
				Smell smell = new Smell();
				smell.setName(name + fileCycle.getKey().toString());
				smell.setSize(files.size());
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.CYCLIC_DEPENDENCY);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				for (ProjectFile file : files) {
					Contain contain = new Contain(smell, file);
					smellContains.add(contain);
				}
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
	}

	public void createHubLikeDependencySmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.HUBLIKE_DEPENDENCY);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Hub-Like Dependency Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.HUBLIKE_DEPENDENCY);
		smellRepository.deleteSmellHasMetricRelation(SmellType.HUBLIKE_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.HUBLIKE_DEPENDENCY);
		Map<Long, List<FileHubLike>> fileHubLikes = hubLikeComponentDetector.fileHubLikes();
		String name = "file_hub-like-dependency_";
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();
		int index = 1;
		for (Map.Entry<Long, List<FileHubLike>> fileHubLike : fileHubLikes.entrySet()) {
			long projectId = fileHubLike.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			List<FileHubLike> files = fileHubLike.getValue();
			for (FileHubLike file : files) {
				Smell smell = new Smell();
				smell.setName(name + index);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.HUBLIKE_DEPENDENCY);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Contain contain = new Contain(smell, file.getFile());
				smellContains.add(contain);
				index ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
	}

	public void createUnstableDependencySmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.UNSTABLE_DEPENDENCY);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Unstable Dependency Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.UNSTABLE_DEPENDENCY);
		smellRepository.deleteSmellHasMetricRelation(SmellType.UNSTABLE_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.UNSTABLE_DEPENDENCY);
		Map<Long, List<UnstableComponentByInstability<ProjectFile>>> fileUnstables = unstableDependencyDetectorUsingInstability.fileUnstables();
		String name = "file_unstable-dependency_";
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();
		int index = 1;
		for (Map.Entry<Long, List<UnstableComponentByInstability<ProjectFile>>> fileUnstable : fileUnstables.entrySet()) {
			long projectId = fileUnstable.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			List<UnstableComponentByInstability<ProjectFile>> files = fileUnstable.getValue();
			for (UnstableComponentByInstability<ProjectFile> file : files) {
				Smell smell = new Smell();
				smell.setName(name + index);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.UNSTABLE_DEPENDENCY);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Contain contain = new Contain(smell, file.getComponent());
				smellContains.add(contain);
				index ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
	}

	public void createSimilarComponentsSmell(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.SIMILAR_COMPONENTS);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Similar Components Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.SIMILAR_COMPONENTS);
		smellRepository.deleteSmellHasMetricRelation(SmellType.SIMILAR_COMPONENTS);
		smellRepository.deleteSmells(SmellType.SIMILAR_COMPONENTS);
		Collection<SimilarComponents<ProjectFile>> fileSimilars = similarComponentsDetector.fileSimilars();
		String name = "file_similar-components_";
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();
		int index = 1;
		for (SimilarComponents<ProjectFile> fileSimilar : fileSimilars) {
			Package pck1 = containRepository.findFileBelongToPackage(fileSimilar.getNode1().getId());
			Package pck2 = containRepository.findFileBelongToPackage(fileSimilar.getNode2().getId());
			Project project1 = containRepository.findPackageBelongToProject(pck1.getId());
			Project project2 = containRepository.findPackageBelongToProject(pck2.getId());
			Smell smell = new Smell();
			smell.setName(name + index);
			smell.setSize(2);
			if (project1.getId().equals(project2.getId())) {
				smell.setLanguage(project1.getLanguage());
				smell.setProjectId(project1.getId());
				smell.setProjectName(project1.getName());
			}
			else {
				smell.setLanguage(project1.getLanguage());
				smell.setProjectId(project1.getId());
				smell.setProjectName(project1.getName() + "+" + project2.getName());
			}
			smell.setType(SmellType.SIMILAR_COMPONENTS);
			smell.setLevel(SmellLevel.FILE);
			smells.add(smell);
			Contain contain1 = new Contain(smell, fileSimilar.getNode1());
			Contain contain2 = new Contain(smell, fileSimilar.getNode2());
			smellContains.add(contain1);
			smellContains.add(contain2);
			index ++;
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
	}

	public void createLogicalCouplingSmell(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.LOGICAL_COUPLING);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Logical Coupling Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.LOGICAL_COUPLING);
		smellRepository.deleteSmellHasMetricRelation(SmellType.LOGICAL_COUPLING);
		smellRepository.deleteSmells(SmellType.LOGICAL_COUPLING);
		Collection<LogicCouplingComponents<ProjectFile>> fileLogicals = implicitCrossModuleDependencyDetector.cochangesInDifferentModule();
		String name = "file_logical-coupling_";
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();
		int index = 1;
		for (LogicCouplingComponents<ProjectFile> fileLogical : fileLogicals) {
			Package pck = containRepository.findFileBelongToPackage(fileLogical.getNode1().getId());
			Project project = containRepository.findPackageBelongToProject(pck.getId());
			Smell smell = new Smell();
			smell.setName(name + index);
			smell.setSize(2);
			smell.setLanguage(project.getLanguage());
			smell.setProjectId(project.getId());
			smell.setProjectName(project.getName());
			smell.setType(SmellType.LOGICAL_COUPLING);
			smell.setLevel(SmellLevel.FILE);
			smells.add(smell);
			Contain contain1 = new Contain(smell, fileLogical.getNode1());
			Contain contain2 = new Contain(smell, fileLogical.getNode2());
			smellContains.add(contain1);
			smellContains.add(contain2);
			index ++;
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
	}
}
