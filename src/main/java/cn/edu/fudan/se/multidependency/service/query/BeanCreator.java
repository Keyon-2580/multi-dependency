package cn.edu.fudan.se.multidependency.service.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.config.PropertyConfig;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
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
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackagePairDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.SummaryAggregationDataService;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.BasicDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.CloneRelationDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackagePair;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;

@Component
public class BeanCreator {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanCreator.class);

	@Autowired
	private BasicCloneQueryService basicCloneQueryService;

	@Autowired
	private HotspotPackagePairDetector hotspotPackagePairDetector;

	@Autowired
	private SummaryAggregationDataService summaryAggregationDataService;

	@Bean
	public int setCommitSize(CommitUpdateFileRepository commitUpdateFileRepository) {
		LOGGER.info("设置Commit Update文件数...");
		return commitUpdateFileRepository.setCommitFilesSize();
	}
    
	@Bean("createCoChanges")
	public List<CoChange> createCoChanges(PropertyConfig propertyConfig, CoChangeRepository cochangeRepository) {
		List<CoChange> coChanges = new ArrayList<>();
		if(propertyConfig.isCalculateCoChange()) {
			coChanges = cochangeRepository.findCoChangesLimit();
			if ( coChanges != null && coChanges.size() > 0){
				LOGGER.info("已存在CoChange关系" );
				return coChanges;
			}
			else {
				LOGGER.info("创建CoChange关系...");
				cochangeRepository.deleteAll();
				cochangeRepository.createCoChanges(Constant.COUNT_OF_MIN_COCHANGE);
				cochangeRepository.updateCoChangesForFile();
				LOGGER.info("创建Module CoChange关系");
//				cochangeRepository.createCoChangesForModule(Constant.COUNT_OF_MIN_COCHANGE);
//				cochangeRepository.updateCoChangesForModule();

				Map<Package, Map<Package, List<CoChange>>> packageCoChangePackage = new HashMap<>(hotspotPackagePairDetector.detectHotspotPackagePairWithCoChange());
				for(Map.Entry<Package, Map<Package, List<CoChange>>> entry : packageCoChangePackage.entrySet()){
					Package pck1 = entry.getKey();
					List<CoChange> coChangeListTmp = new ArrayList<>();
					int size = 0;
					for(List<CoChange> packageCoChangeList : packageCoChangePackage.get(pck1).values()){
						for(CoChange packageCoChange : packageCoChangeList) {
							coChangeListTmp.add(packageCoChange);
							if(++size > 500){
								cochangeRepository.saveAll(coChangeListTmp);
								coChangeListTmp.clear();
								size = 0;
							}
						}
					}
					cochangeRepository.saveAll(coChangeListTmp);
				}
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
	public List<DependsOn> createDependsOn(PropertyConfig propertyConfig, DependsOnRepository dependsOnRepository, ProjectFileRepository fileRepository, ASRepository asRepository) {
		if(!propertyConfig.isCalculateDependsOn()) {
			return new ArrayList<>();
		}
		List<DependsOn> dependsOns = dependsOnRepository.findFileDepends();
		if(dependsOns != null && dependsOns.size() > 0){
			LOGGER.info("已存在Depends On关系" );
		} else {
			LOGGER.info("创建Depends On关系...");
			dependsOnRepository.deleteAll();
			
			//dependsOnRepository.createDependsOnWithImportInFiles();
			//dependsOnRepository.createDependsOnWithIncludeInFiles();
			dependsOnRepository.createDependsOnWithExtendsInFiles();
			dependsOnRepository.createDependsOnWithImplementsInFiles();
			dependsOnRepository.createDependsOnWithAssociationInFiles();
			dependsOnRepository.createDependsOnWithAnnotationInFiles();
			dependsOnRepository.createDependsOnWithCallInFiles();
			dependsOnRepository.createDependsOnWithImpllinkInFiles();
			dependsOnRepository.createDependsOnWithCreateInFiles();
			dependsOnRepository.createDependsOnWithCastInFiles();
			dependsOnRepository.createDependsOnWithThrowInFiles();
			dependsOnRepository.createDependsOnWithParameterInFiles();
			dependsOnRepository.createDependsOnWithReturnInFiles();
			dependsOnRepository.createDependsOnWithUseTypeInFiles();
			dependsOnRepository.createDependsOnWithTimesInNode(NodeLabelType.ProjectFile);
			dependsOnRepository.deleteNullAggregationDependsOnInFiles();

			//创建包间dependsOn
//			Map<Package, Map<Package, DependsOn>> packageDependsOnPackage = createPackageDependsOn(dependsOnRepository);
			Map<Package, Map<Package, List<DependsOn>>> packageDependsOnPackage = new HashMap<>(hotspotPackagePairDetector.detectHotspotPackagePairWithDependsOn());
			for(Map.Entry<Package, Map<Package, List<DependsOn>>> entry : packageDependsOnPackage.entrySet()){
				Package pck1 = entry.getKey();
				List<DependsOn> dependsOnListTmp = new ArrayList<>();
				int size = 0;
				for(List<DependsOn> pckDependsOnList : packageDependsOnPackage.get(pck1).values()){
					for(DependsOn pckDependsOn : pckDependsOnList) {
						pckDependsOn.getDependsOnTypes().forEach((key, value) -> {
							Double weight = RelationType.relationWeights.get(RelationType.valueOf(key));
							if(weight != null){
								BigDecimal weightedTimes  =  new BigDecimal(value * weight);
								pckDependsOn.addWeightedTimes(weightedTimes.setScale(2, RoundingMode.HALF_UP).doubleValue());
							}
							else {
								LOGGER.info("关系权重未定义：" + key);
							}
						});
						dependsOnListTmp.add(pckDependsOn);
						if(++size > 500){
							dependsOnRepository.saveAll(dependsOnListTmp);
							dependsOnListTmp.clear();
							size = 0;
						}
					}
				}
				dependsOnRepository.saveAll(dependsOnListTmp);
			}
			fileRepository.pageRank(20, 0.85);
		}
		configSmellDetect(propertyConfig, asRepository);
		return dependsOns;
	}

	private Map<Package, Map<Package, DependsOn>> createPackageDependsOn(DependsOnRepository dependsOnRepository){
		Map<Package, Map<Package, DependsOn>> packageDependsOnPackage = new HashMap<>();
		for (DependsOn dependsOn : dependsOnRepository.findFileDepends()){
			Package pck1 = dependsOnRepository.findFileBelongPackageByFileId(dependsOn.getStartNode().getId());
			Package pck2 = dependsOnRepository.findFileBelongPackageByFileId(dependsOn.getEndNode().getId());

			if(pck1 != null && pck2 != null && !pck1.getId().equals(pck2.getId()) && dependsOn.getDependsOnTypes() != null && !dependsOn.getDependsOnTypes().isEmpty()){
				Map<Package, DependsOn> dependsOnMap = packageDependsOnPackage.getOrDefault(pck1, new HashMap<>());
				DependsOn pckDependsOn = dependsOnMap.get(pck2);
				if (pckDependsOn != null ){
					for(Map.Entry<String, Long> entry : dependsOn.getDependsOnTypes().entrySet()){
						String dependsOnKey = entry.getKey();
						Long typeTimes = entry.getValue();
						if ( pckDependsOn.getDependsOnTypes().containsKey(dependsOnKey) ) {
							Long times = pckDependsOn.getDependsOnTypes().get(dependsOnKey);
							times += typeTimes;
							pckDependsOn.getDependsOnTypes().put(dependsOnKey, times);
						}else {
							pckDependsOn.getDependsOnTypes().put(dependsOnKey, typeTimes);
							String dTypes = pckDependsOn.getDependsOnType();
							pckDependsOn.setDependsOnType(dTypes + "__" + dependsOnKey);
						}
						int timesTmp = pckDependsOn.getTimes() + typeTimes.intValue();
						pckDependsOn.setTimes(timesTmp);
						dependsOnMap.put(pck2, pckDependsOn);
					}
				} else {
					DependsOn newDependsOn = new DependsOn(pck1, pck2);
					newDependsOn.getDependsOnTypes().putAll(dependsOn.getDependsOnTypes());
					newDependsOn.setDependsOnType(dependsOn.getDependsOnType());
					newDependsOn.setTimes(dependsOn.getTimes());
					dependsOnMap.put(pck2, newDependsOn);
				}

				packageDependsOnPackage.put(pck1, dependsOnMap);
			}
		}
		return packageDependsOnPackage;
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
	
	@Bean
	public boolean setPackageLoSc(PackageRepository packageRepository) {
		LOGGER.info("计算Package内文件数、总行数与总代码行...");
		packageRepository.setEmptyPackageLocAndLinesAndSize();
		packageRepository.setPackageLocAndLinesAndSize();
		return true;
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
}
