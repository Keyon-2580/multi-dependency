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

	@Autowired
	private UnusedIncludeDetector unusedIncludeDetector;

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
			LOGGER.info("已存在Cyclic Dependency Smell");
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

		Map<Long, Map<Integer, Cycle<Type>>> typeCyclicDependencies = new HashMap<>(cyclicDependencyDetector.detectTypeCyclicDependency());
		String typeSmellName = SmellLevel.TYPE + "_" + SmellType.CYCLIC_DEPENDENCY + "_";
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
		Map<Long, Map<Integer, Cycle<ProjectFile>>> fileCyclicDependencies = new HashMap<>(cyclicDependencyDetector.detectFileCyclicDependency());
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.CYCLIC_DEPENDENCY + "_";
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
		Map<Long, Map<Integer, Cycle<Package>>> packageCyclicDependencies = new HashMap<>(cyclicDependencyDetector.detectPackageCyclicDependency());
		String packageSmellName = SmellLevel.PACKAGE + "_" + SmellType.CYCLIC_DEPENDENCY + "_";
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
		LOGGER.info("创建Cyclic Dependency Smell节点关系完成");
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
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.HUBLIKE_DEPENDENCY + "_";
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
		String packageSmellName = SmellLevel.PACKAGE + "_" + SmellType.HUBLIKE_DEPENDENCY + "_";
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
		String fileSmellname = SmellLevel.FILE + "_" + SmellType.UNSTABLE_DEPENDENCY + "_";
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
		String packageSmellname = SmellLevel.PACKAGE + "_" + SmellType.UNSTABLE_DEPENDENCY + "_";
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

	public void createSimilarComponentsSmells(boolean isRecreate) {
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

		Map<Long, List<SimilarComponents<ProjectFile>>> fileSimilars = similarComponentsDetector.detectFileSimilarComponents();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.SIMILAR_COMPONENTS + "_";
		int fileSmellIndex = 1;
		for (Map.Entry<Long, List<SimilarComponents<ProjectFile>>> entry : fileSimilars.entrySet()) {
			for (SimilarComponents<ProjectFile> fileSimilarComponents : entry.getValue()) {
				Smell smell = new Smell();
				Package pck1 = containRepository.findFileBelongToPackage(fileSimilarComponents.getNode1().getId());
				Package pck2 = containRepository.findFileBelongToPackage(fileSimilarComponents.getNode2().getId());
				Project project1 = containRepository.findPackageBelongToProject(pck1.getId());
				Project project2 = containRepository.findPackageBelongToProject(pck2.getId());
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
				Contain contain1 = new Contain(smell, fileSimilarComponents.getNode1());
				Contain contain2 = new Contain(smell, fileSimilarComponents.getNode2());
				smellContains.add(contain1);
				smellContains.add(contain2);
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);

		smells.clear();
		smellContains.clear();
		Map<Long, List<SimilarComponents<Package>>> packageSimilars = similarComponentsDetector.detectPackageSimilarComponents();
		String packageSmellName = SmellLevel.PACKAGE + "_" + SmellType.SIMILAR_COMPONENTS + "_";
		int packageSmellIndex = 1;
		for (Map.Entry<Long, List<SimilarComponents<Package>>> entry : packageSimilars.entrySet()) {
			for (SimilarComponents<Package> packageSimilar : entry.getValue()) {
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
				smell.setLevel(SmellLevel.PACKAGE);
				smells.add(smell);
				Contain contain1 = new Contain(smell, packageSimilar.getNode1());
				Contain contain2 = new Contain(smell, packageSimilar.getNode2());
				smellContains.add(contain1);
				smellContains.add(contain2);
				packageSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Similar Components Smell节点关系完成");
	}

	public void createImplicitCrossModuleDependencySmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Implicit CrossModule Dependency Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		smellRepository.deleteSmellMetric(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		smellRepository.deleteSmells(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Collection<LogicCouplingComponents<ProjectFile>> fileLogicals = implicitCrossModuleDependencyDetector.cochangesInDifferentFile();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY + "_";
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
			smell.setType(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
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
		String packageSmellName = SmellLevel.PACKAGE + "_" + SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY + "_";
		int packageSmellIndex = 1;
		for (LogicCouplingComponents<Package> packageLogical : packageLogicals) {
			Project project = containRepository.findPackageBelongToProject(packageLogical.getNode1().getId());
			Smell smell = new Smell();
			smell.setName(packageSmellName + packageSmellIndex);
			smell.setSize(2);
			smell.setLanguage(project.getLanguage());
			smell.setProjectId(project.getId());
			smell.setProjectName(project.getName());
			smell.setType(SmellType.IMPLICIT_CROSS_MODULE_DEPENDENCY);
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
		LOGGER.info("创建Implicit CrossModule Dependency Smell节点关系完成");
	}

	public void createGodComponentSmells(boolean isRecreate) {
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
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.GOD_COMPONENT + "_";
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

	public void createUnutilizedAbstractionSmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.UNUTILIZED_ABSTRACTION);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Unutilized Abstraction Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.UNUTILIZED_ABSTRACTION);
		smellRepository.deleteSmellMetric(SmellType.UNUTILIZED_ABSTRACTION);
		smellRepository.deleteSmells(SmellType.UNUTILIZED_ABSTRACTION);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Map<Long, List<UnutilizedAbstraction<ProjectFile>>> fileUnutilizeds = unutilizedAbstractionDetector.detectFileUnutilizedAbstraction();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.UNUTILIZED_ABSTRACTION + "_";
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
				smell.setType(SmellType.UNUTILIZED_ABSTRACTION);
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

	public void createUnusedIncludeSmells(boolean isRecreate) {
		List<Smell> smellsTmp = smellRepository.findSmellsByTypeWithLimit(SmellType.UNUSED_INCLUDE);
		if(smellsTmp != null && !smellsTmp.isEmpty()){
			LOGGER.info("已存在Unused Include Smell");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}
		smellRepository.deleteSmellContainRelations(SmellType.UNUSED_INCLUDE);
		smellRepository.deleteSmellMetric(SmellType.UNUSED_INCLUDE);
		smellRepository.deleteSmells(SmellType.UNUSED_INCLUDE);
		List<Smell> smells = new ArrayList<>();
		List<Contain> smellContains = new ArrayList<>();

		Map<Long, List<UnusedInclude>> unusedIncludeMap = unusedIncludeDetector.detectFileUnusedInclude();
		String fileSmellName = SmellLevel.FILE + "_" + SmellType.UNUSED_INCLUDE + "_";
		int fileSmellIndex = 1;
		for (Map.Entry<Long, List<UnusedInclude>> entry : unusedIncludeMap.entrySet()) {
			long projectId = entry.getKey();
			Project project = (Project) projectRepository.queryNodeById(projectId);
			List<UnusedInclude> unusedIncludeList = entry.getValue();
			for (UnusedInclude unusedInclude : unusedIncludeList) {
				ProjectFile coreFile = unusedInclude.getCoreFile();
				Set<ProjectFile> unusedIncludeFiles = unusedInclude.getUnusedIncludeFiles();
				Smell smell = new Smell();
				smell.setName(fileSmellName + fileSmellIndex);
				smell.setSize(unusedIncludeFiles.size());
				smell.setLanguage(project.getLanguage());
				smell.setProjectId(projectId);
				smell.setProjectName(project.getName());
				smell.setType(SmellType.UNUSED_INCLUDE);
				smell.setLevel(SmellLevel.FILE);
				smell.setCoreNodePath(coreFile.getPath());
				smell.setCoreNodeId(coreFile.getId());
				smells.add(smell);
				for (ProjectFile unusedIncludeFile : unusedIncludeFiles) {
					Contain contain = new Contain(smell, unusedIncludeFile);
					smellContains.add(contain);
				}
				fileSmellIndex ++;
			}
		}
		smellRepository.saveAll(smells);
		containRepository.saveAll(smellContains);
		LOGGER.info("创建Unused Include Smell节点关系完成");
	}
}
