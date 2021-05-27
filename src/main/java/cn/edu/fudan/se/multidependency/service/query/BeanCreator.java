package cn.edu.fudan.se.multidependency.service.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.*;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.*;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculatorService;
import cn.edu.fudan.se.multidependency.service.query.metric.ModularityCalculator;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellDetectorService;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellMetricCalculatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import cn.edu.fudan.se.multidependency.model.relation.git.AggregationCoChange;
import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.config.PropertyConfig;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.smell.ASRepository;
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

	@Autowired
	private MetricCalculatorService metricCalculatorService;

	@Autowired
	private SmellMetricCalculatorService smellMetricCalculatorService;

	@Autowired
	private SmellDetectorService smellDetectorService;

	@Resource(name="modularityCalculatorImplForFieldMethodLevel")
	private ModularityCalculator modularityCalculator;

	@Bean
	public boolean setCommitSize(CommitUpdateFileRepository commitUpdateFileRepository, CommitRepository commitRepository,ProjectRepository projectRepository) {
		LOGGER.info("设置Commit Update文件数...");
		commitUpdateFileRepository.setCommitFilesSize();
		LOGGER.info("设置Project Commits数...");
		Project project = projectRepository.queryProjectWithLimitOne();
		if(project != null && project.getCommits() > 0){
			LOGGER.info("已存在Project Commits数");
			return true;
		}
		commitRepository.setCommitsForAllProject();
		return true;
	}

	@Bean("setCoChange")
	public List<CoChange> setCoChange(PropertyConfig propertyConfig, CoChangeRepository coChangeRepository) {
		if (!propertyConfig.isSetCoChange()) {
			return new ArrayList<>();
		}
		List<CoChange> coChanges = coChangeRepository.findCoChangesLimit();
		if(coChanges != null && !coChanges.isEmpty()){
			LOGGER.info("已存在CoChange关系" );
		}
		else {
			LOGGER.info("创建CoChange关系...");
			coChangeRepository.deleteAll();
			coChangeRepository.createCoChanges(Constant.COUNT_OF_MIN_COCHANGE);
			coChangeRepository.updateCoChangesForFile();
		}
		return coChanges;
	}

//	@Bean("setModuleCoChange")
//	public List<CoChange> setModuleCoChange(PropertyConfig propertyConfig, CoChangeRepository coChangeRepository) {
//		if (!propertyConfig.isSetModuleCoChange()) {
//			return new ArrayList<>();
//		}
//		List<CoChange> moduleCoChanges = coChangeRepository.findModuleCoChangeLimit();
//		if(moduleCoChanges != null && !moduleCoChanges.isEmpty()){
//			LOGGER.info("已存在Module CoChange关系" );
//		}
//		else {
//			LOGGER.info("创建Module CoChange关系...");
//			Map<String, List<CoChange>> coChangeMap = hotspotPackagePairDetector.detectHotspotPackagePairWithCoChange();
//			List<CoChange> moduleCoChangeList = new ArrayList<>(coChangeMap.get(RelationType.str_CO_CHANGE));
//			List<CoChange> moduleCoChangeListTmp = new ArrayList<>();
//			int size = 0;
//			for(CoChange coChange : moduleCoChangeList) {
//				moduleCoChangeListTmp.add(coChange);
//				if(++size > 500){
//					coChangeRepository.saveAll(moduleCoChangeListTmp);
//					moduleCoChangeListTmp.clear();
//					size = 0;
//				}
//			}
//			coChangeRepository.saveAll(moduleCoChangeListTmp);
//		}
//		return moduleCoChanges;
//	}

//	@Bean("setAggregationCoChange")
//	public List<CoChange> setAggregationCoChange(PropertyConfig propertyConfig, CoChangeRepository coChangeRepository, AggregationCoChangeRepository aggregationCoChangeRepository) {
//		if (!propertyConfig.isSetAggregationCoChange()) {
//			return new ArrayList<>();
//		}
//		List<CoChange> aggregationCoChanges = coChangeRepository.findAggregationCoChangeLimit();
//		if(aggregationCoChanges != null && !aggregationCoChanges.isEmpty()){
//			LOGGER.info("已存在Aggregation CoChange关系" );
//		}
//		else {
//			LOGGER.info("创建Aggregation CoChange关系...");
//			Map<String, List<CoChange>> coChangeMap = hotspotPackagePairDetector.detectHotspotPackagePairWithCoChange();
//			List<CoChange> aggregationCoChangeList = new ArrayList<>(coChangeMap.get(RelationType.str_AGGREGATION_CO_CHANGE));
//			List<AggregationCoChange> aggregationCoChangeListTmp = new ArrayList<>();
//			int size = 0;
//			for(CoChange coChange : aggregationCoChangeList) {
//				aggregationCoChangeListTmp.add(new AggregationCoChange(coChange));
//				if(++size > 500){
//					aggregationCoChangeRepository.saveAll(aggregationCoChangeListTmp);
//					aggregationCoChangeListTmp.clear();
//					size = 0;
//				}
//			}
//			aggregationCoChangeRepository.saveAll(aggregationCoChangeListTmp);
//		}
//		return aggregationCoChanges;
//	}

	@Bean("setDependsOn")
	public List<DependsOn> setDependsOn(PropertyConfig propertyConfig, DependsOnRepository dependsOnRepository, ProjectFileRepository fileRepository) {
		if (!propertyConfig.isSetDependsOn()) {
			return new ArrayList<>();
		}
		List<DependsOn> dependsOns = dependsOnRepository.findFileDependsWithLimit();
		if(dependsOns != null && !dependsOns.isEmpty()){
			LOGGER.info("已存在Depends On关系" );
		}
		else {
			LOGGER.info("创建Depends On关系...");
			dependsOnRepository.deleteAll();

			dependsOnRepository.createDependsOnWithExtendsInTypes();
			dependsOnRepository.createDependsOnWithImplementsInTypes();
			dependsOnRepository.createDependsOnWithMemberVariableInTypes();
			dependsOnRepository.createDependsOnWithLocalVariableInTypes();
			dependsOnRepository.createDependsOnWithAnnotationInTypes();
			dependsOnRepository.createDependsOnWithCallInTypes();
			dependsOnRepository.createDependsOnWithImplinkInTypes();
			dependsOnRepository.createDependsOnWithCreateInTypes();
			dependsOnRepository.createDependsOnWithCastInTypes();
			dependsOnRepository.createDependsOnWithThrowInTypes();
			dependsOnRepository.createDependsOnWithParameterInTypes();
			dependsOnRepository.createDependsOnWithReturnInTypes();
			dependsOnRepository.createDependsOnWithUseTypeInTypes();
			createDependsOnWithTimesInNode(dependsOnRepository,NodeLabelType.Type);
			dependsOnRepository.deleteNullAggregationDependsOnInTypes();

//			dependsOnRepository.createDependsOnWithImportInFiles();
			dependsOnRepository.createDependsOnWithIncludeInFiles();
			dependsOnRepository.createDependsOnWithExtendsInFiles();
			dependsOnRepository.createDependsOnWithImplementsInFiles();
			dependsOnRepository.createDependsOnWithImplementsCInFiles();
			dependsOnRepository.createDependsOnWithMemberVariableInFiles();
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
			dependsOnRepository.createDependsOnWithUseTypeInFiles();
			createDependsOnWithTimesInNode(dependsOnRepository,NodeLabelType.ProjectFile);
			dependsOnRepository.deleteNullAggregationDependsOnInFiles();

			//计算文件的依赖PageRank值
			fileRepository.pageRank(20, 0.85);
		}
		return dependsOns;
	}

//	@Bean("setModuleDependsOn")
//	public List<DependsOn> setModuleDependsOn(PropertyConfig propertyConfig, DependsOnRepository dependsOnRepository) {
//		if (!propertyConfig.isSetModuleDependsOn()) {
//			return new ArrayList<>();
//		}
//		List<DependsOn> moduleDependsOns = dependsOnRepository.findModuleDependsOnWithLimit();
//		if(moduleDependsOns != null && !moduleDependsOns.isEmpty()){
//			LOGGER.info("已存在Module Depends On关系" );
//		}
//		else {
//			LOGGER.info("创建Module Depends On关系...");
//			Map<String, List<DependsOn>> dependsOnMap = hotspotPackagePairDetector.detectHotspotPackagePairWithDependsOn();
//			List<DependsOn> moduleDependsOnList = new ArrayList<>(dependsOnMap.get(RelationType.str_DEPENDS_ON));
//			List<DependsOn> moduleDependsOnListTmp = new ArrayList<>();
//			int size = 0;
//			for(DependsOn dependsOn : moduleDependsOnList) {
//				moduleDependsOnListTmp.add(dependsOn);
//				if(++size > 500){
//					dependsOnRepository.saveAll(moduleDependsOnListTmp);
//					moduleDependsOnListTmp.clear();
//					size = 0;
//				}
//			}
//			dependsOnRepository.saveAll(moduleDependsOnListTmp);
//		}
//		return moduleDependsOns;
//	}

//	@Bean("setAggregationDependsOn")
//	public List<DependsOn> setAggregationDependsOn(PropertyConfig propertyConfig, DependsOnRepository dependsOnRepository, AggregationDependsOnRepository aggregationDependsOnRepository) {
//		if (!propertyConfig.isSetAggregationDependsOn()) {
//			return new ArrayList<>();
//		}
//		List<DependsOn> aggregationDependsOns = dependsOnRepository.findAggregationDependsOnWithLimit();
//		if(aggregationDependsOns != null && !aggregationDependsOns.isEmpty()){
//			LOGGER.info("已存在Aggregation Depends On关系" );
//		}
//		else {
//			LOGGER.info("创建Aggregation Depends On关系...");
//			Map<String, List<DependsOn>> dependsOnMap = hotspotPackagePairDetector.detectHotspotPackagePairWithDependsOn();
//			List<DependsOn> aggregationDependsOnList = new ArrayList<>(dependsOnMap.get(RelationType.str_AGGREGATION_DEPENDS_ON));
//			List<AggregationDependsOn> aggregationDependsOnListTmp = new ArrayList<>();
//			int size = 0;
//			for(DependsOn dependsOn : aggregationDependsOnList) {
//				aggregationDependsOnListTmp.add(new AggregationDependsOn(dependsOn));
//				if(++size > 500){
//					aggregationDependsOnRepository.saveAll(aggregationDependsOnListTmp);
//					aggregationDependsOnListTmp.clear();
//					size = 0;
//				}
//			}
//			aggregationDependsOnRepository.saveAll(aggregationDependsOnListTmp);
//		}
//		return aggregationDependsOns;
//	}

	private void createDependsOnWithTimesInNode(DependsOnRepository dependsOnRepository,NodeLabelType nodeLabelType){
		Map<Node, Map<Node, DependsOn>> nodeDependsOnNode = new HashMap<>();
		List<DependsOn> dependsOnList;
		switch (nodeLabelType){
			case ProjectFile:
				dependsOnList = dependsOnRepository.findFileDepends();
				break;
			case Type:
				dependsOnList = dependsOnRepository.findTypeDepends();
				break;
			default:
				dependsOnList = new ArrayList<>();
				break;
		}

		for (DependsOn dependsOn : dependsOnList){
			if(dependsOn.getDependsOnType() != null){
				Node node1 = dependsOn.getStartNode();
				Node node2 = dependsOn.getEndNode();
				Map<Node, DependsOn> dependsOnMap = nodeDependsOnNode.getOrDefault(node1, new HashMap<>());
				DependsOn nodeDependsOn = dependsOnMap.get(node2);
				if (nodeDependsOn != null ){
					if ( nodeDependsOn.getDependsOnTypes().containsKey(dependsOn.getDependsOnType()) ) {
						Long times = nodeDependsOn.getDependsOnTypes().get(dependsOn.getDependsOnType());
						times += (long) dependsOn.getTimes();
						nodeDependsOn.getDependsOnTypes().put(dependsOn.getDependsOnType(), times);
					}else {
						nodeDependsOn.getDependsOnTypes().put(dependsOn.getDependsOnType(), (long) dependsOn.getTimes());
						String dTypes = nodeDependsOn.getDependsOnType();
						nodeDependsOn.setDependsOnType(dTypes + "__" + dependsOn.getDependsOnType());
					}
					int timesTmp = nodeDependsOn.getTimes() + dependsOn.getTimes();
					nodeDependsOn.setTimes(timesTmp);
					dependsOnMap.put(node2, nodeDependsOn);
				} else {
					DependsOn newDepends = new DependsOn(node1, node2);
					newDepends.getDependsOnTypes().put(dependsOn.getDependsOnType(), (long) dependsOn.getTimes());
					newDepends.setDependsOnType(dependsOn.getDependsOnType());
					newDepends.setTimes(dependsOn.getTimes());
					dependsOnMap.put(node2, newDepends);
				}
				nodeDependsOnNode.put(node1, dependsOnMap);
			}
		}

		for (Map.Entry<Node, Map<Node, DependsOn>> entry : nodeDependsOnNode.entrySet()){
			Node node1 = entry.getKey();
			List<DependsOn> dependsOnListTmp = new ArrayList<>();
			int size = 0;
			for (DependsOn nDependsOn : nodeDependsOnNode.get(node1).values()){
				nDependsOn.getDependsOnTypes().forEach( (key, value) -> {
					Double weight = RelationType.relationWeights.get(RelationType.valueOf(key));
					if(weight != null){
						double weight_value = value * weight > 1 ? 1.0 : value * weight;
						BigDecimal weightedTimes  =  new BigDecimal(weight_value );
						nDependsOn.addWeightedTimes(weightedTimes.setScale(2, RoundingMode.HALF_UP).doubleValue());
					} else {
						System.out.println("关系权重未定义：" + key);
					}
				});
				dependsOnListTmp.add(nDependsOn);
				if(size++ > 500){
					dependsOnRepository.saveAll(dependsOnListTmp);
					dependsOnListTmp.clear();
					size = 0;
				}
			}
			dependsOnRepository.saveAll(dependsOnListTmp);
		}
	}


	@Bean("setCloneGroup")
	public List<CloneGroup> setCloneGroup(PropertyConfig propertyConfig, CloneGroupRepository cloneGroupRepository, CloneRepository cloneRepository) {
		if (!propertyConfig.isSetCloneGroup()) {
			return new ArrayList<>();
		}
		List<CloneGroup> cloneGroups = cloneGroupRepository.findCloneGroupWithLimit();
		if ( cloneGroups != null && !cloneGroups.isEmpty()){
			LOGGER.info("已存在Clone Group关系");
		}
		else {
			LOGGER.info("创建Clone Group关系...");
			List<Clone> clones = cloneRepository.findClonesLimit();
			if(clones == null || clones.isEmpty()){
				LOGGER.info("不存在Clone数据，跳过分析！！！" );
				return new ArrayList<>();
			}
			cloneGroupRepository.setJavaLanguageBySuffix();
			cloneGroupRepository.setCppLanguageBySuffix();
			cloneGroupRepository.deleteCloneGroupContainRelations();
			cloneGroupRepository.deleteCloneGroupRelations();
			cloneGroupRepository.setNodeGroup();
			cloneGroupRepository.createFileCloneGroupRelations();
			cloneGroupRepository.createTypeCloneGroupRelations();
			cloneGroupRepository.createFunctionCloneGroupRelations();
			cloneGroupRepository.createCloneGroupContainRelations();
			cloneGroupRepository.setCloneGroupContainSize();
			cloneGroupRepository.setCloneGroupLanguage();
			LOGGER.info("创建Clone Group关系完成！！！");
		}
		return cloneGroups;
	}


	@Bean("setModuleClone")
	public List<ModuleClone> setModuleClone(PropertyConfig propertyConfig, ModuleCloneRepository moduleCloneRepository,CloneRepository cloneRepository) {
		if (!propertyConfig.isSetModuleClone()) {
			return new ArrayList<>();
		}
		List<ModuleClone> moduleClones = moduleCloneRepository.getAllModuleCloneWithLimit();
		if(moduleClones != null &&moduleClones.size() > 0) {
			LOGGER.info("已存在Module Clone关系...");
		}
		else {
			LOGGER.info("创建Module Clone关系...");
			List<Clone> clones = cloneRepository.findClonesLimit();
			if(clones == null || clones.isEmpty()){
				LOGGER.info("不存在Clone数据，跳过分析！！！" );
				return new ArrayList<>();
			}
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
//			LOGGER.info("设置Module Clone基础信息(co-change)...");
			moduleCloneRepository.setCloneNodesCoChangeTimes(3);
			moduleClones = moduleCloneRepository.getAllModuleClone();
		}
		return moduleClones;
	}

	@Bean("setAggregationClone")
	public List<AggregationClone> setAggregationClone(PropertyConfig propertyConfig, AggregationCloneRepository aggregationCloneRepository,CloneRepository cloneRepository) {
		if (!propertyConfig.isSetAggregationClone()) {
			return new ArrayList<>();
		}
		List<AggregationClone> aggregationClone = aggregationCloneRepository.getAllAggregationClone();
		if(aggregationClone != null && aggregationClone.size() > 0) {
			LOGGER.info("已存在Aggregation Clone关系");
		}
		else {
			LOGGER.info("创建Aggregation Clone关系...");
			List<Clone> clones = cloneRepository.findClonesLimit();
			if(clones == null || clones.isEmpty()){
				LOGGER.info("不存在Clone数据，跳过分析！！！" );
				return new ArrayList<>();
			}
			if(aggregationCloneRepository.getNumberOfAggregationClone() == 0) {
				Collection<HotspotPackagePair> hotspotPackagePairs = hotspotPackagePairDetector.detectHotspotPackagePairs();
				AddChildrenPackages(-1, -1, hotspotPackagePairs, aggregationCloneRepository);
			}
			aggregationClone = aggregationCloneRepository.getAllAggregationClone();
		}
		return aggregationClone;
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
	public boolean setProjectMetrics(PropertyConfig propertyConfig, ProjectRepository projectRepository) {
		LOGGER.info("创建File Metric度量值节点和关系...");
		metricCalculatorService.createFileMetric(false);

		LOGGER.info("创建Package Metric度量值节点和关系...");
		metricCalculatorService.createPackageMetric(false);

		LOGGER.info("创建Project Metric度量值节点和关系...");
		metricCalculatorService.createProjectMetric(false);

		LOGGER.info("创建GitRepo Metric度量值节点和关系...");
		metricCalculatorService.createGitRepoMetric(false);

		if(propertyConfig.isCalculateModularity()){
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
	public boolean setPackageDepth(ProjectRepository projectRepository, ContainRepository containRepository) {
		LOGGER.info("设置Package深度值...");
		List<Project> projectList = projectRepository.queryAllProjects();
		for(Project project : projectList) {
			List<Package> rootPackageList = containRepository.findProjectRootPackages(project.getId());
			for(Package rootPackage : rootPackageList) {
				Queue<Package> packageQueue = new LinkedList<>();
				packageQueue.offer(rootPackage);
				while(!packageQueue.isEmpty()) {
					Package pck = packageQueue.poll();
					containRepository.setChildPackageDepth(pck.getId());
					packageQueue.addAll(containRepository.findPackagesWithChildPackagesForParentPackage(pck.getId()));
				}
			}
		}
		return true;
	}

	@Bean("smell")
	public boolean configSmellDetect(PropertyConfig propertyConfig, ASRepository asRepository) {
		if(propertyConfig.isDetectAS()) {
			LOGGER.info("异味检测配置");
			if(!asRepository.existModule()){
				asRepository.createModule();
				asRepository.createModuleDependsOn();
				asRepository.createModuleContain();
				asRepository.createProjectContainsModule();
				asRepository.setModuleInstability();
			}
			asRepository.setFileInstability();
			asRepository.setPackageInstability();

			if(propertyConfig.isSetDependsOn()) {
				LOGGER.info("创建Cyclic Dependency Smell节点关系...");
				smellDetectorService.createCycleDependencySmells(false);

				LOGGER.info("创建Hub-Like Dependency Smell节点关系...");
				smellDetectorService.createHubLikeDependencySmells(false);

				LOGGER.info("创建Unstable Dependency Smell节点关系...");
				smellDetectorService.createUnstableDependencySmells(false);

				if(propertyConfig.isSetCoChange()){
					LOGGER.info("创建Unstable Interface Smell节点关系...");
					smellDetectorService.createUnstableInterfaceSmells(false);
				}

				LOGGER.info("创建Unutilized Abstraction Smell节点关系...");
				smellDetectorService.createUnutilizedAbstractionSmells(false);

				LOGGER.info("创建Unused Include Smell节点关系...");
				smellDetectorService.createUnusedIncludeSmells(false);
			}

			if(propertyConfig.isSetCloneGroup()){
				LOGGER.info("创建Clone Smell节点关系...");
				smellDetectorService.createCloneSmells(false);
			}

			if(propertyConfig.isSetCloneGroup() && propertyConfig.isSetCloneGroup()){
				LOGGER.info("创建Similar Components Smell节点关系...");
//				smellDetectorService.createSimilarComponentsSmells(false);
			}

			if(propertyConfig.isSetCoChange()){
				LOGGER.info("创建Implicit Cross Module Dependency Smell节点关系...");
				smellDetectorService.createImplicitCrossModuleDependencySmells(false);
			}

			LOGGER.info("创建God Component Smell节点关系...");
			smellDetectorService.createGodComponentSmells(false);

		}
		return true;
	}

	@Bean
	public boolean setSmellMetrics() {
		LOGGER.info("创建Smell Metric度量值节点和关系...");
		smellMetricCalculatorService.createSmellMetric(false);
		return true;
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
