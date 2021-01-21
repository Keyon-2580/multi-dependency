package cn.edu.fudan.se.multidependency.service.query;

import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.relation.HasRepository;
import cn.edu.fudan.se.multidependency.service.query.as.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.metric.ModularityCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.relation.AggregationDependsOn;
import cn.edu.fudan.se.multidependency.model.relation.git.AggregationCoChange;
import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.config.PropertyConfig;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.as.ASRepository;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.clone.CloneGroupRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.AggregationCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.ModuleCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.AggregationDependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.AggregationCoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackagePairDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.SummaryAggregationDataService;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.BasicDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.CloneRelationDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackagePair;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Component
public class BeanCreator {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanCreator.class);

	@Autowired
	private BasicCloneQueryService basicCloneQueryService;

	@Autowired
	private HotspotPackagePairDetector hotspotPackagePairDetector;

	@Autowired
	private SummaryAggregationDataService summaryAggregationDataService;

	@Autowired
	private CyclicDependencyDetector cyclicDependencyDetector;

	@Resource(name="modularityCalculatorImplForFieldMethodLevel")
	private ModularityCalculator modularityCalculator;

	@Bean
	public boolean setCommitSize(CommitUpdateFileRepository commitUpdateFileRepository, CommitRepository commitRepository) {
		LOGGER.info("设置Commit Update文件数...");
		commitUpdateFileRepository.setCommitFilesSize();
		LOGGER.info("设置Project Commits数...");
		commitRepository.setCommitsForAllProject();
		return true;
	}

	@Bean
	public boolean setProjectMetrics(PropertyConfig propertyConfig, ProjectRepository projectRepository, PackageRepository packageRepository, ProjectFileRepository projectFileRepository) {
		LOGGER.info("计算Project/Package/ProjectFile基本度量值...");
		projectRepository.setProjectMetrics();
		packageRepository.setPackageMetrics();
		packageRepository.setEmptyPackageMetrics();
		projectFileRepository.setFileMetrics();
		if(propertyConfig.isCalModularity()){
			LOGGER.info("计算Project模块性度量值...");
			projectRepository.queryAllProjects().forEach( (project) ->{
				if(project.getModularity() <= 0.0){
					double value = modularityCalculator.calculate(project).getValue();
					projectRepository.setModularityMetricsForProject(project.getId(), value);
				}
			});
		}
		return true;
	}

	@Bean
	public boolean setPackageDepth(ProjectRepository projectRepository, HasRepository hasRepository) {
		LOGGER.info("设置Package深度值...");
		List<Project> projectList = projectRepository.queryAllProjects();
		for(Project project : projectList) {
			List<Package> rootPackageList = hasRepository.findProjectHasPackages(project.getId());
			for(Package rootPackage : rootPackageList) {
				Queue<Package> packageQueue = new LinkedList<>();
				packageQueue.offer(rootPackage);
				while(!packageQueue.isEmpty()) {
					Package pck = packageQueue.poll();
					hasRepository.setChildPackageDepth(pck.getId());
					packageQueue.addAll(hasRepository.findPackagesWithChildPackagesForParentPackage(pck.getId()));
				}
			}
		}
		return true;
	}

	@Bean("createCoChanges")
	public List<CoChange> createCoChanges(PropertyConfig propertyConfig, CoChangeRepository cochangeRepository, AggregationCoChangeRepository aggregationCoChangeRepository) {
		List<CoChange> coChanges = new ArrayList<>();
		if(propertyConfig.isCalculateCoChange()) {
			coChanges = cochangeRepository.findCoChangesLimit();
			if(coChanges != null && coChanges.size() > 0){
				LOGGER.info("已存在CoChange关系" );
				return coChanges;
			}
			else {
				LOGGER.info("创建CoChange关系...");
				cochangeRepository.deleteAll();
				cochangeRepository.createCoChanges(Constant.COUNT_OF_MIN_COCHANGE);
				cochangeRepository.updateCoChangesForFile();

				LOGGER.info("创建Module CoChange关系...");
				Map<String, List<CoChange>> coChangeMap = hotspotPackagePairDetector.detectHotspotPackagePairWithCoChange();
				List<CoChange> moduleCoChangeList = new ArrayList<>(coChangeMap.get(RelationType.str_CO_CHANGE));
				List<CoChange> aggregationCoChangeList = new ArrayList<>(coChangeMap.get(RelationType.str_AGGREGATION_CO_CHANGE));
				List<CoChange> moduleCoChangeListTmp = new ArrayList<>();
				List<AggregationCoChange> aggregationCoChangeListTmp = new ArrayList<>();
				int size = 0;
				for(CoChange coChange : moduleCoChangeList) {
					moduleCoChangeListTmp.add(coChange);
					if(++size > 500){
						cochangeRepository.saveAll(moduleCoChangeListTmp);
						moduleCoChangeListTmp.clear();
						size = 0;
					}
				}
				cochangeRepository.saveAll(moduleCoChangeListTmp);

				LOGGER.info("创建Aggregation CoChange关系...");
				size = 0;
				for(CoChange coChange : aggregationCoChangeList) {
					aggregationCoChangeListTmp.add(new AggregationCoChange(coChange));
					if(++size > 500){
						aggregationCoChangeRepository.saveAll(aggregationCoChangeListTmp);
						aggregationCoChangeListTmp.clear();
						size = 0;
					}
				}
				aggregationCoChangeRepository.saveAll(aggregationCoChangeListTmp);
			}
		}
		return coChanges;
	}

	private void configSmellDetect(PropertyConfig propertyConfig, ASRepository asRepository) {
		if(propertyConfig.isDetectAS() && !asRepository.existModule()) {
			LOGGER.info("异味检测配置");
			asRepository.createModule();
			asRepository.createModuleDependsOn();
			asRepository.createModuleHas();
			asRepository.createProjectContainsModule();
			asRepository.setModuleInstability();
			asRepository.setFileInstability();
			asRepository.setPackageInstability();
		}
	}

	@Bean("createDependsOn")
	public List<DependsOn> createDependsOn(PropertyConfig propertyConfig, DependsOnRepository dependsOnRepository, AggregationDependsOnRepository aggregationDependsOnRepository, ProjectFileRepository fileRepository, ASRepository asRepository) {
		if(!propertyConfig.isCalculateDependsOn()) {
			return new ArrayList<>();
		}
		List<DependsOn> dependsOns = dependsOnRepository.findFileDepends();
		if(dependsOns != null && dependsOns.size() > 0){
			LOGGER.info("已存在Depends On关系" );
		}
		else {
			LOGGER.info("创建Depends On关系...");
			dependsOnRepository.deleteAll();
			dependsOnRepository.createDependsOnWithExtendsInFiles();
			dependsOnRepository.createDependsOnWithImplementsInFiles();
			dependsOnRepository.createDependsOnWithGlobalVariableInFiles();
			dependsOnRepository.createDependsOnWithLocalVariableInFiles();
			dependsOnRepository.createDependsOnWithAnnotationInFiles();
			dependsOnRepository.createDependsOnWithCallInFiles();
			dependsOnRepository.createDependsOnWithImpllinkInFiles();
			dependsOnRepository.createDependsOnWithCreateInFiles();
			dependsOnRepository.createDependsOnWithCastInFiles();
			dependsOnRepository.createDependsOnWithThrowInFiles();
			dependsOnRepository.createDependsOnWithParameterInFiles();
			dependsOnRepository.createDependsOnWithReturnInFiles();
			dependsOnRepository.createDependsOnWithReferenceTypeInFiles();
			dependsOnRepository.createDependsOnWithTimesInNode(NodeLabelType.ProjectFile);
			dependsOnRepository.deleteNullAggregationDependsOnInFiles();

			LOGGER.info("创建Module Depends On关系...");
			Map<String, List<DependsOn>> dependsOnMap = hotspotPackagePairDetector.detectHotspotPackagePairWithDependsOn();
			List<DependsOn> moduleDependsOnList = new ArrayList<>(dependsOnMap.get(RelationType.str_DEPENDS_ON));
			List<DependsOn> aggregationDependsOnList = new ArrayList<>(dependsOnMap.get(RelationType.str_AGGREGATION_DEPENDS_ON));
			List<DependsOn> moduleDependsOnListTmp = new ArrayList<>();
			List<AggregationDependsOn> aggregationDependsOnListTmp = new ArrayList<>();
			int size = 0;
			for(DependsOn dependsOn : moduleDependsOnList) {
				moduleDependsOnListTmp.add(dependsOn);
				if(++size > 500){
					dependsOnRepository.saveAll(moduleDependsOnListTmp);
					moduleDependsOnListTmp.clear();
					size = 0;
				}
			}
			dependsOnRepository.saveAll(moduleDependsOnListTmp);

			LOGGER.info("创建Aggregation Depends On关系...");
			size = 0;
			for(DependsOn dependsOn : aggregationDependsOnList) {
				aggregationDependsOnListTmp.add(new AggregationDependsOn(dependsOn));
				if(++size > 500){
					aggregationDependsOnRepository.saveAll(aggregationDependsOnListTmp);
					aggregationDependsOnListTmp.clear();
					size = 0;
				}
			}
			aggregationDependsOnRepository.saveAll(aggregationDependsOnListTmp);
			fileRepository.pageRank(20, 0.85);
		}
		configSmellDetect(propertyConfig, asRepository);
		return dependsOns;
	}

	@Bean("createCloneGroup")
	public List<CloneGroup> createCloneGroup(PropertyConfig propertyConfig, CloneGroupRepository cloneGroupRepository) {
		if(propertyConfig.isCalculateCloneGroup()) {
			List<CloneGroup> cloneGroups = cloneGroupRepository.findCoChangesLimit();
			if ( cloneGroups != null && cloneGroups.size() > 0){
				LOGGER.info("已存在Clone Group关系");
				return cloneGroups;
			} else {
				LOGGER.info("创建Clone Group关系...");
				cloneGroupRepository.deleteAll();
				cloneGroupRepository.setJavaLanguageBySuffix();
				cloneGroupRepository.setCppLanguageBySuffix();
				cloneGroupRepository.deleteCloneGroupContainRelations();
				cloneGroupRepository.deleteCloneGroupRelations();
				cloneGroupRepository.setFileGroup();
				cloneGroupRepository.createCloneGroupRelations();
				cloneGroupRepository.createCloneGroupContainRelations();
				cloneGroupRepository.setCloneGroupContainSize();
				cloneGroupRepository.setCloneGroupLanguage();
			}

		}
		return new ArrayList<>();
	}


	@Bean("setModuleClone")
	public List<ModuleClone> setModuleClone(PropertyConfig propertyConfig, ModuleCloneRepository moduleCloneRepository) {
		if(propertyConfig.isSetModuleClone()) {
			List<ModuleClone> moduleClones = moduleCloneRepository.getAllModuleClone();
			if(moduleClones != null &&moduleClones.size() > 0) {
				LOGGER.info("已存在Module Clone基础信息...");
			}
			else {
				LOGGER.info("设置Module Clone基础信息...");
				if(moduleCloneRepository.getNumberOfModuleClone() == 0) {
					List<BasicDataForDoubleNodes<Node, Relation>> clonePackagePairs = summaryAggregationDataService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE));
					for(BasicDataForDoubleNodes<Node, Relation> clonePackagePair : clonePackagePairs) {
						CloneRelationDataForDoubleNodes<Node, Relation> packagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) clonePackagePair;
						moduleCloneRepository.createModuleClone(
								clonePackagePair.getNode1().getId(),
								clonePackagePair.getNode2().getId(),
								packagePairCloneRelationData.getClonePairs(),
								packagePairCloneRelationData.getCloneNodesCount1(),
								packagePairCloneRelationData.getCloneNodesCount2(),
								packagePairCloneRelationData.getAllNodesCount1(),
								packagePairCloneRelationData.getAllNodesCount2(),
								packagePairCloneRelationData.getCloneNodesLoc1(),
								packagePairCloneRelationData.getCloneNodesLoc2(),
								packagePairCloneRelationData.getAllNodesLoc1(),
								packagePairCloneRelationData.getAllNodesLoc2(),
								packagePairCloneRelationData.getCloneType1Count(),
								packagePairCloneRelationData.getCloneType2Count(),
								packagePairCloneRelationData.getCloneType3Count(),
								packagePairCloneRelationData.getCloneSimilarityValue()
						);
					}
				}
				LOGGER.info("设置Module Clone基础信息(co-change)...");
				moduleCloneRepository.setCloneNodesCoChangeTimes(3);
				moduleClones = moduleCloneRepository.getAllModuleClone();
			}
			return moduleClones;
		}
		return new ArrayList<>();
	}

	@Bean("setAggregationClone")
	public List<AggregationClone> setAggregationClone(PropertyConfig propertyConfig, AggregationCloneRepository aggregationCloneRepository) {
		if(propertyConfig.isSetAggregationClone()) {
			List<AggregationClone> aggregationClone = aggregationCloneRepository.getAllAggregationClone();
			if(aggregationClone != null && aggregationClone.size() > 0) {
				LOGGER.info("已存在Aggregation Clone聚合结果...");
			}
			else {
				LOGGER.info("设置Aggregation Clone聚合结果...");
				if(aggregationCloneRepository.getNumberOfAggregationClone() == 0) {
					Collection<HotspotPackagePair> hotspotPackagePairs = hotspotPackagePairDetector.detectHotspotPackagePairs();
					AddChildrenPackages(-1, -1, hotspotPackagePairs, aggregationCloneRepository);
				}
				aggregationClone = aggregationCloneRepository.getAllAggregationClone();
			}
			return aggregationClone;
		}
		return new ArrayList<>();
	}

	public void AddChildrenPackages(long parent1Id, long parent2Id, Collection<HotspotPackagePair> hotspotPackagePairs, AggregationCloneRepository aggregationCloneRepository) {
		for(HotspotPackagePair hotspotPackagePair : hotspotPackagePairs) {
			Collection<HotspotPackagePair> childrenHotspotPackagePairs = hotspotPackagePair.getChildrenHotspotPackagePairs();
			AddChildrenPackages(hotspotPackagePair.getPackage1().getId(), hotspotPackagePair.getPackage2().getId(), childrenHotspotPackagePairs, aggregationCloneRepository);
			CloneRelationDataForDoubleNodes<Node, Relation> packagePairCloneRelationData = (CloneRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair.getPackagePairRelationData();
			aggregationCloneRepository.createAggregationClone(
					hotspotPackagePair.getPackage1().getId(),
					hotspotPackagePair.getPackage2().getId(),
					parent1Id,
					parent2Id,
					packagePairCloneRelationData.getClonePairs(),
					packagePairCloneRelationData.getCloneNodesCount1(),
					packagePairCloneRelationData.getCloneNodesCount2(),
					packagePairCloneRelationData.getAllNodesCount1(),
					packagePairCloneRelationData.getAllNodesCount2(),
					packagePairCloneRelationData.getCloneNodesLoc1(),
					packagePairCloneRelationData.getCloneNodesLoc2(),
					packagePairCloneRelationData.getAllNodesLoc1(),
					packagePairCloneRelationData.getAllNodesLoc2(),
					packagePairCloneRelationData.getCloneType1Count(),
					packagePairCloneRelationData.getCloneType2Count(),
					packagePairCloneRelationData.getCloneType3Count(),
					packagePairCloneRelationData.getCloneSimilarityValue()
			);
		}
	}

	@Bean
	public boolean exportCyclicDependency(PropertyConfig propertyConfig) {
		if (propertyConfig.isExportCyclicDependency()) {
			LOGGER.info("export cyclic dependency...");
			cyclicDependencyDetector.exportCycleDependency();
			System.exit(0);
			return true;
		}
		return false;
	}
}
