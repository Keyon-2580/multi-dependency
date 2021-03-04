package cn.edu.fudan.se.multidependency.service.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.*;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.relation.*;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.HasRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
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

	@Autowired
	private MetricCalculatorService metricCalculatorService;

	@Autowired
	private SmellMetricCalculatorService smellMetricCalculatorService;

	@Autowired
	private SmellDetectorService smellDetectorService;

	@Bean("createCoChanges")
	public List<CoChange> createCoChanges(PropertyConfig propertyConfig, CoChangeRepository cochangeRepository, AggregationCoChangeRepository aggregationCoChangeRepository) {
		List<CoChange> coChanges = new ArrayList<>();
		if(propertyConfig.isCalculateCoChange()) {
			coChanges = cochangeRepository.findCoChangesLimit();
			if(coChanges != null && !coChanges.isEmpty()){
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


	@Bean("createDependsOn")
	public List<DependsOn> createDependsOn(PropertyConfig propertyConfig, DependsOnRepository dependsOnRepository, AggregationDependsOnRepository aggregationDependsOnRepository, ProjectFileRepository fileRepository, ASRepository asRepository) {
		if(!propertyConfig.isCalculateDependsOn()) {
			return new ArrayList<>();
		}
		List<DependsOn> dependsOns = dependsOnRepository.findFileDependsWithLimit(10);
		if(dependsOns != null && !dependsOns.isEmpty()){
			LOGGER.info("已存在Depends On关系" );
		}
		else {
			LOGGER.info("创建Depends On关系...");
			dependsOnRepository.deleteAll();

			dependsOnRepository.createDependsOnWithExtendsInTypes();
			dependsOnRepository.createDependsOnWithImplementsInTypes();
			dependsOnRepository.createDependsOnWithGlobalVariableInTypes();
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
			dependsOnRepository.createDependsOnWithUseTypeInFiles();
			createDependsOnWithTimesInNode(dependsOnRepository,NodeLabelType.ProjectFile);
			dependsOnRepository.deleteNullAggregationDependsOnInFiles();

			//计算文件的依赖PageRank值
			fileRepository.pageRank(20, 0.85);
		}

		LOGGER.info("创建Module Depends On关系...");
		List<DependsOn> moduleDependsOns = dependsOnRepository.findPackageDependsOnWithLimit(10);
		if(moduleDependsOns != null && !moduleDependsOns.isEmpty()){
			LOGGER.info("已存在Module Depends On关系" );
		}else {
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

		}

//		configSmellDetect(propertyConfig, asRepository);
		return dependsOns;
	}

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
						times += Long.valueOf(dependsOn.getTimes());
						nodeDependsOn.getDependsOnTypes().put(dependsOn.getDependsOnType(), times);
					}else {
						nodeDependsOn.getDependsOnTypes().put(dependsOn.getDependsOnType(), Long.valueOf(dependsOn.getTimes()));
						String dTypes = nodeDependsOn.getDependsOnType();
						nodeDependsOn.setDependsOnType(dTypes + "__" + dependsOn.getDependsOnType());
					}
					int timesTmp = nodeDependsOn.getTimes() + dependsOn.getTimes();
					nodeDependsOn.setTimes(timesTmp);
					dependsOnMap.put(node2, nodeDependsOn);
				} else {
					DependsOn newDepends = new DependsOn(node1, node2);
					newDepends.getDependsOnTypes().put(dependsOn.getDependsOnType(), Long.valueOf(dependsOn.getTimes()));
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
						BigDecimal weightedTimes  =  new BigDecimal( value * weight);
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


	@Bean("createCloneGroup")
	public List<CloneGroup> createCloneGroup(PropertyConfig propertyConfig,
											 CloneGroupRepository cloneGroupRepository,
											 SmellRepository smellRepository) {
		if(propertyConfig.isCalculateCloneGroup()) {
			List<CloneGroup> cloneGroups = cloneGroupRepository.findCloneGroupWithLimit();
			if ( cloneGroups != null && !cloneGroups.isEmpty()){
				LOGGER.info("已存在Clone Group关系");
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
				LOGGER.info("创建Clone Group关系完成！！！");
//				smellDetectorService.createCloneSmells();

			}
			List<Smell> smells = smellRepository.findSmellWithLimit();

//			smellRepository.deleteSmellContainRelations();
//			smellRepository.deleteSmellHasMetricRelation();
//			smellRepository.deleteSmells();
//			smellRepository.createCloneSmells();
//			smellRepository.createCloneSmellContains();
//			smellRepository.setSmellProject();
//			LOGGER.info("创建Smell节点关系完成！！！");

			return cloneGroups;
		}
		return new ArrayList<>();
	}


	@Bean("setModuleClone")
	public List<ModuleClone> setModuleClone(PropertyConfig propertyConfig, ModuleCloneRepository moduleCloneRepository) {
		if(propertyConfig.isSetModuleClone()) {
			List<ModuleClone> moduleClones = moduleCloneRepository.getAllModuleCloneWithLimit();
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

	@Bean("smell")
	public boolean configSmellDetect(PropertyConfig propertyConfig, ASRepository asRepository) {
		if(propertyConfig.isDetectAS() && !asRepository.existModule()) {
			LOGGER.info("异味检测配置");
			asRepository.createModule();
			asRepository.createModuleDependsOn();
			asRepository.createModuleContain();
			asRepository.createProjectContainsModule();
			asRepository.setModuleInstability();
			asRepository.setFileInstability();
			asRepository.setPackageInstability();
		}
		LOGGER.info("创建Clone Smell节点关系！！！");
		smellDetectorService.createCloneSmells();
		LOGGER.info("创建Cycle Smell节点关系！！！");
		smellDetectorService.createCycleDependencySmells();
		return true;
	}

	@Bean
	public boolean setCommitSize(CommitUpdateFileRepository commitUpdateFileRepository, CommitRepository commitRepository) {
		LOGGER.info("设置Commit Update文件数...");
		commitUpdateFileRepository.setCommitFilesSize();
		LOGGER.info("设置Project Commits数...");
		commitRepository.setCommitsForAllProject();
		return true;
	}

	@Bean
	public boolean setProjectMetrics(PropertyConfig propertyConfig, ProjectRepository projectRepository,
									 PackageRepository packageRepository, ProjectFileRepository projectFileRepository,
									 MetricRepository metricRepository, HasRepository hasRepository) {
		LOGGER.info("计算Project/Package/ProjectFile基本度量值...");
		projectFileRepository.setFileMetrics();
		packageRepository.setEmptyPackageMetrics();
		packageRepository.setPackageMetrics();
		projectRepository.setProjectMetrics();

		if(propertyConfig.isCalModularity()){
			LOGGER.info("计算Project模块性度量值...");
			projectRepository.queryAllProjects().forEach( (project) ->{
				if(project.getModularity() <= 0.0){
					double value = modularityCalculator.calculate(project).getValue();
					projectRepository.setModularityMetricsForProject(project.getId(), value);
				}
			});
		}

		LOGGER.info("创建File Metric度量值节点和关系...");
		hasRepository.clearHasMetricRelation();
		metricRepository.deleteAll();

		Map<ProjectFile, Metric> fileMetricNodesMap = metricCalculatorService.generateFileMetricNodes();
		if(fileMetricNodesMap != null && !fileMetricNodesMap.isEmpty()){
			Collection<Metric> fileMetricNodes = fileMetricNodesMap.values();
			metricRepository.saveAll(fileMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			int size = 0;
			for(Map.Entry<ProjectFile, Metric> entry : fileMetricNodesMap.entrySet()){
				Has has = new Has(entry.getKey(), entry.getValue());
				hasMetrics.add(has);
				if(++size > 500){
					hasRepository.saveAll(hasMetrics);
					hasMetrics.clear();
					size = 0;
				}
			}
			hasRepository.saveAll(hasMetrics);
		}
		LOGGER.info("创建Package Metric度量值节点和关系...");
		Map<Package, Metric> packageMetricNodesMap = metricCalculatorService.generatePackageMetricNodes();
		if(packageMetricNodesMap != null && !packageMetricNodesMap.isEmpty()){
			Collection<Metric> pckMetricNodes = packageMetricNodesMap.values();
			metricRepository.saveAll(pckMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			int size = 0;
			for(Map.Entry<Package, Metric> entry : packageMetricNodesMap.entrySet()){
				Has has = new Has(entry.getKey(), entry.getValue());
				hasMetrics.add(has);
				if(++size > 500){
					hasRepository.saveAll(hasMetrics);
					hasMetrics.clear();
					size = 0;
				}
			}
			hasRepository.saveAll(hasMetrics);
		}
		LOGGER.info("创建Project Metric度量值节点和关系...");
		Map<Project, Metric> projectMetricNodesMap = metricCalculatorService.generateProjectMetricNodes();
		if(projectMetricNodesMap != null && !projectMetricNodesMap.isEmpty()){
			Collection<Metric> projectMetricNodes = projectMetricNodesMap.values();
			metricRepository.saveAll(projectMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			for(Map.Entry<Project, Metric> entry : projectMetricNodesMap.entrySet()){
				Has has = new Has(entry.getKey(), entry.getValue());
				hasMetrics.add(has);
			}
			hasRepository.saveAll(hasMetrics);
		}

		LOGGER.info("创建Smell Metric度量值节点和关系...");
//		smellMetricCalculatorService.createSmellMetricNodesInFileLevel();
		Map<Smell, Metric> smellMetricMap = smellMetricCalculatorService.generateSmellMetricNodesInFileLevel();
		if(smellMetricMap != null && !smellMetricMap.isEmpty()){
			Collection<Metric> fileMetricNodes = smellMetricMap.values();
			metricRepository.saveAll(fileMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			int size = 0;
			for(Map.Entry<Smell, Metric> entry : smellMetricMap.entrySet()){
				Has has = new Has(entry.getKey(), entry.getValue());
				hasMetrics.add(has);
				if(++size > 500){
					hasRepository.saveAll(hasMetrics);
					hasMetrics.clear();
					size = 0;
				}
			}
			hasRepository.saveAll(hasMetrics);
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
