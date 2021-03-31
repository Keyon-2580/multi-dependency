package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.data.*;
import cn.edu.fudan.se.multidependency.service.query.smell.impl.GodComponentDetectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SmellDetectorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmellDetectorService.class);

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
	private GodComponentDetectorImpl godComponentDetector;

	@Autowired
	private UnutilizedAbstractionDetector unutilizedAbstractionDetector;

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
        smellRepository.deleteSmellMetric(SmellType.CLONE);
        smellRepository.deleteSmells(SmellType.CLONE);

		smellRepository.createCloneSmells();
		smellRepository.createCloneSmellContains();
		smellRepository.setCloneSmellProject();
		LOGGER.info("创建Clone Smell节点关系完成");
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
		smellRepository.deleteSmellMetric(SmellType.CYCLIC_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.CYCLIC_DEPENDENCY);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Map<Long, Map<Integer, Cycle<Type>>> typeCyclicDependencies = new HashMap<>(cyclicDependencyDetector.typeCycles());
		String typeSmellName = "type_cycle-dependency_";
		for (Map.Entry<Long, Map<Integer, Cycle<Type>>> typeCyclicDependency : typeCyclicDependencies.entrySet()){
			long projectId = typeCyclicDependency.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			Map<Integer, Cycle<Type>> typeCycles = typeCyclicDependency.getValue();
			for (Map.Entry<Integer, Cycle<Type>> typeCycle : typeCycles.entrySet()){
				List<Type> types = typeCycle.getValue().getComponents();
				Smell smell = new Smell();
				smell.setName(typeSmellName + typeCycle.getKey().toString());
				smell.setSize(types.size());
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.CYCLIC_DEPENDENCY);
				smell.setLevel(SmellLevel.TYPE);
				smells.add(smell);
				for (Type type : types) {
					Contain contain = new Contain(smell, type);
					smellContains.add(contain);
				}
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);

		smells.clear();
		smellContains.clear();
		Map<Long, Map<Integer, Cycle<ProjectFile>>> fileCyclicDependencies = new HashMap<>(cyclicDependencyDetector.fileCycles());
		String fileSmellName = "file_cycle-dependency_";
		for (Map.Entry<Long, Map<Integer, Cycle<ProjectFile>>> fileCyclicDependency : fileCyclicDependencies.entrySet()){
			long projectId = fileCyclicDependency.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			Map<Integer, Cycle<ProjectFile>> fileCycles = fileCyclicDependency.getValue();
			for (Map.Entry<Integer, Cycle<ProjectFile>> fileCycle : fileCycles.entrySet()){
				List<ProjectFile> files = fileCycle.getValue().getComponents();
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileCycle.getKey().toString());
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

		smells.clear();
		smellContains.clear();
		Map<Long, Map<Integer, Cycle<Package>>> packageCyclicDependencies = new HashMap<>(cyclicDependencyDetector.packageCycles());
		String packageSmellName = "package_cycle-dependency_";
		for (Map.Entry<Long, Map<Integer, Cycle<Package>>> packageCyclicDependency : packageCyclicDependencies.entrySet()){
			long projectId = packageCyclicDependency.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			Map<Integer, Cycle<Package>> packageCycles = packageCyclicDependency.getValue();
			for (Map.Entry<Integer, Cycle<Package>> packageCycle : packageCycles.entrySet()){
				List<Package> packages = packageCycle.getValue().getComponents();
				Smell smell = new Smell();
				smell.setName(packageSmellName + packageCycle.getKey().toString());
				smell.setSize(packages.size());
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.CYCLIC_DEPENDENCY);
				smell.setLevel(SmellLevel.PACKAGE);
				smells.add(smell);
				for (Package pck : packages) {
					Contain contain = new Contain(smell, pck);
					smellContains.add(contain);
				}
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Cycle Dependency Smell节点关系完成");
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
		smellRepository.deleteSmellMetric(SmellType.HUBLIKE_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.HUBLIKE_DEPENDENCY);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Map<Long, List<FileHubLike>> fileHubLikes = hubLikeComponentDetector.fileHubLikes();
		String fileSmellName = "file_hub-like-dependency_";
		int fileSmellIndex = 1;
		for (Map.Entry<Long, List<FileHubLike>> fileHubLike : fileHubLikes.entrySet()) {
			long projectId = fileHubLike.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			List<FileHubLike> files = fileHubLike.getValue();
			for (FileHubLike file : files) {
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.HUBLIKE_DEPENDENCY);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Contain contain = new Contain(smell, file.getFile());
				smellContains.add(contain);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);

		smells.clear();
		smellContains.clear();
		Map<Long, List<PackageHubLike>> packageHubLikes = hubLikeComponentDetector.packageHubLikes();
		String packageSmellName = "package_hub-like-dependency_";
		int packageSmellIndex = 1;
		for (Map.Entry<Long, List<PackageHubLike>> packageHubLike : packageHubLikes.entrySet()) {
			long projectId = packageHubLike.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			List<PackageHubLike> packages = packageHubLike.getValue();
			for (PackageHubLike pck : packages) {
				Smell smell = new Smell();
				smell.setName(packageSmellName + packageSmellIndex);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.HUBLIKE_DEPENDENCY);
				smell.setLevel(SmellLevel.PACKAGE);
				smells.add(smell);
				Contain contain = new Contain(smell, pck.getPck());
				smellContains.add(contain);
				packageSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Hub-Like Dependency Smell节点关系完成");
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
		smellRepository.deleteSmellMetric(SmellType.UNSTABLE_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.UNSTABLE_DEPENDENCY);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Map<Long, List<UnstableComponentByInstability<ProjectFile>>> fileUnstables = unstableDependencyDetectorUsingInstability.fileUnstables();
		String fileSmellname = "file_unstable-dependency_";
		int fileSmellIndex = 1;
		for (Map.Entry<Long, List<UnstableComponentByInstability<ProjectFile>>> fileUnstable : fileUnstables.entrySet()) {
			long projectId = fileUnstable.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			List<UnstableComponentByInstability<ProjectFile>> files = fileUnstable.getValue();
			for (UnstableComponentByInstability<ProjectFile> file : files) {
				Smell smell = new Smell();
				smell.setName(fileSmellname + fileSmellIndex);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.UNSTABLE_DEPENDENCY);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Contain contain = new Contain(smell, file.getComponent());
				smellContains.add(contain);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);

		smells.clear();
		smellContains.clear();
		Map<Long, List<UnstableComponentByInstability<Package>>> packageUnstables = unstableDependencyDetectorUsingInstability.packageUnstables();
		String packageSmellname = "package_unstable-dependency_";
		int packageSmellIndex = 1;
		for (Map.Entry<Long, List<UnstableComponentByInstability<Package>>> packageUnstable : packageUnstables.entrySet()) {
			long projectId = packageUnstable.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			List<UnstableComponentByInstability<Package>> packages = packageUnstable.getValue();
			for (UnstableComponentByInstability<Package> pck : packages) {
				Smell smell = new Smell();
				smell.setName(packageSmellname + packageSmellIndex);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.UNSTABLE_DEPENDENCY);
				smell.setLevel(SmellLevel.PACKAGE);
				smells.add(smell);
				Contain contain = new Contain(smell, pck.getComponent());
				smellContains.add(contain);
				packageSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Unstable Dependency Smell节点关系完成");
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
		smellRepository.deleteSmellMetric(SmellType.SIMILAR_COMPONENTS);
		smellRepository.deleteSmells(SmellType.SIMILAR_COMPONENTS);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Collection<SimilarComponents<ProjectFile>> fileSimilars = similarComponentsDetector.fileSimilars();
		String fileSmellName = "file_similar-components_";
		int fileSmellIndex = 1;
		for (SimilarComponents<ProjectFile> fileSimilar : fileSimilars) {
			Package pck1 = containRepository.findFileBelongToPackage(fileSimilar.getNode1().getId());
			Package pck2 = containRepository.findFileBelongToPackage(fileSimilar.getNode2().getId());
			Project project1 = containRepository.findPackageBelongToProject(pck1.getId());
			Project project2 = containRepository.findPackageBelongToProject(pck2.getId());
			Smell smell = new Smell();
			smell.setName(fileSmellName + fileSmellIndex);
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
			fileSmellIndex ++;
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);

		smells.clear();
		smellContains.clear();
		Collection<SimilarComponents<Package>> packageSimilars = similarComponentsDetector.packageSimilars();
		String packageSmellName = "package_similar-components_";
		int packageSmellIndex = 1;
		for (SimilarComponents<Package> packageSimilar : packageSimilars) {
			Package pck1 = packageSimilar.getNode1();
			Package pck2 = packageSimilar.getNode2();
			Project project1 = containRepository.findPackageBelongToProject(pck1.getId());
			Project project2 = containRepository.findPackageBelongToProject(pck2.getId());
			Smell smell = new Smell();
			smell.setName(packageSmellName + packageSmellIndex);
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
			Contain contain1 = new Contain(smell, packageSimilar.getNode1());
			Contain contain2 = new Contain(smell, packageSimilar.getNode2());
			smellContains.add(contain1);
			smellContains.add(contain2);
			packageSmellIndex ++;
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Similar Components Smell节点关系完成");
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
		smellRepository.deleteSmellMetric(SmellType.LOGICAL_COUPLING);
		smellRepository.deleteSmells(SmellType.LOGICAL_COUPLING);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Collection<LogicCouplingComponents<ProjectFile>> fileLogicals = implicitCrossModuleDependencyDetector.cochangesInDifferentFile();
		String fileSmellName = "file_logical-coupling_";
		int fileSmellIndex = 1;
		for (LogicCouplingComponents<ProjectFile> fileLogical : fileLogicals) {
			Package pck = containRepository.findFileBelongToPackage(fileLogical.getNode1().getId());
			Project project = containRepository.findPackageBelongToProject(pck.getId());
			Smell smell = new Smell();
			smell.setName(fileSmellName + fileSmellIndex);
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
			fileSmellIndex ++;
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);

		smells.clear();
		smellContains.clear();
		Collection<LogicCouplingComponents<Package>> packageLogicals = implicitCrossModuleDependencyDetector.cochangesInDifferentPackage();
		String packageSmellName = "package_logical-coupling_";
		int packageSmellIndex = 1;
		for (LogicCouplingComponents<Package> packageLogical : packageLogicals) {
			Project project = containRepository.findPackageBelongToProject(packageLogical.getNode1().getId());
			Smell smell = new Smell();
			smell.setName(packageSmellName + packageSmellIndex);
			smell.setSize(2);
			smell.setLanguage(project.getLanguage());
			smell.setProjectId(project.getId());
			smell.setProjectName(project.getName());
			smell.setType(SmellType.LOGICAL_COUPLING);
			smell.setLevel(SmellLevel.PACKAGE);
			smells.add(smell);
			Contain contain1 = new Contain(smell, packageLogical.getNode1());
			Contain contain2 = new Contain(smell, packageLogical.getNode2());
			smellContains.add(contain1);
			smellContains.add(contain2);
			packageSmellIndex ++;
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Logical Coupling Smell节点关系完成");
	}

	public void createGodComponentSmell(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.GOD_COMPONENT);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在God Component Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.GOD_COMPONENT);
		smellRepository.deleteSmellMetric(SmellType.GOD_COMPONENT);
		smellRepository.deleteSmells(SmellType.GOD_COMPONENT);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

  		Map<Long, List<FileGod>> fileGodComponents = godComponentDetector.fileGodComponents();
		String fileSmellName = "file_god-component_";
		int fileSmellIndex = 1;
		for (Map.Entry<Long, List<FileGod>> fileGodComponent : fileGodComponents.entrySet()) {
			long projectId = fileGodComponent.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			List<FileGod> files = fileGodComponent.getValue();
			for (FileGod file : files) {
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.GOD_COMPONENT);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Contain contain = new Contain(smell, file.getFile());
				smellContains.add(contain);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建God Component Smell节点关系完成");
	}

	public void createUnutilizedAbstractionSmell(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.UNTILIZED_ABSTRACTION);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Unutilized Abstraction Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.UNTILIZED_ABSTRACTION);
		smellRepository.deleteSmellMetric(SmellType.UNTILIZED_ABSTRACTION);
		smellRepository.deleteSmells(SmellType.UNTILIZED_ABSTRACTION);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Map<Long, List<UnutilizedAbstraction<ProjectFile>>> fileUnutilizeds = unutilizedAbstractionDetector.fileUnutilizeds();
		String fileSmellName = "file_unutilized-abstraction_";
		int fileSmellIndex = 1;
		for (Map.Entry<Long, List<UnutilizedAbstraction<ProjectFile>>> fileUnutilized : fileUnutilizeds.entrySet()) {
			long projectId = fileUnutilized.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			List<UnutilizedAbstraction<ProjectFile>> files = fileUnutilized.getValue();
			for (UnutilizedAbstraction<ProjectFile> file : files) {
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(1);
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.UNTILIZED_ABSTRACTION);
				smell.setLevel(SmellLevel.FILE);
				smells.add(smell);
				Contain contain = new Contain(smell, file.getComponent());
				smellContains.add(contain);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Unutilized Abstraction Smell节点关系完成");
	}
}
