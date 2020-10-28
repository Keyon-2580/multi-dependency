package cn.edu.fudan.se.multidependency.service.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.service.insert.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;
import cn.edu.fudan.se.multidependency.repository.node.clone.CloneGroupRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.AggregationCloneRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackageDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackage;
import org.apache.catalina.Host;
import org.neo4j.cypher.internal.v3_4.expressions.Add;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.config.PropertyConfig;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.clone.CloneGroupRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.ModuleCloneRepository;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneValueService;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;

@Component
public class BeanCreator {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanCreator.class);
	@Autowired
	private CloneValueService cloneValueService;

	@Autowired
	private BasicCloneQueryService basicCloneQueryService;

	@Autowired
	private HotspotPackageDetector hotspotPackageDetector;

	@Bean
	public int setCommitSize(CommitUpdateFileRepository commitUpdateFileRepository) {
		LOGGER.info("设置commit update文件数...");
		return commitUpdateFileRepository.setCommitFilesSize();
	}
    
	@Bean("createCoChanges")
	public List<CoChange> createCoChanges(PropertyConfig propertyConfig, CoChangeRepository cochangeRepository) {
		List<CoChange> coChanges = new ArrayList<>();
		if(propertyConfig.isCalculateCoChange()) {
			coChanges = cochangeRepository.findCoChangesLimit();
			if ( coChanges != null && coChanges.size() > 0){
				LOGGER.info("已存在cochange关系" );
				return coChanges;
			} else {
				LOGGER.info("创建cochange关系...");
				cochangeRepository.deleteAll();
				coChanges.addAll( cochangeRepository.createCoChanges(Constant.COUNT_OF_MIN_COCHANGE));
				LOGGER.info("创建module cochange关系");
				coChanges.addAll(cochangeRepository.createCoChangesForModule(Constant.COUNT_OF_MIN_COCHANGE));
			}
		}
		return coChanges;
	}

	@Bean("createDependsOn")
	public List<DependsOn> createDependsOn(PropertyConfig propertyConfig, DependsOnRepository dependsOnRepository, ProjectFileRepository fileRepository) {
		List<DependsOn> dependsOns = new ArrayList<>();
		if(propertyConfig.isCalculateDependsOn()) {
			dependsOns = dependsOnRepository.findFileDepends();
			if(dependsOns != null && dependsOns.size() > 0){
				LOGGER.info("已存在Depends On关系" );
				return dependsOns;
			} else {
				LOGGER.info("创建Depends On关系...");
				dependsOnRepository.deleteAll();

				dependsOnRepository.createDependsOnWithExtendsInFiles();
				dependsOnRepository.createDependsOnWithImplementsInFiles();
				dependsOnRepository.createDependsOnWithDependencyInFiles();
				dependsOnRepository.createDependsOnWithAssociationInFiles();
				dependsOnRepository.createDependsOnWithAnnotationInFiles();
				dependsOnRepository.createDependsOnWithTimesInFiles();
				dependsOnRepository.deleteNullAggregationDependsOnInFiles();

				dependsOnRepository.createDependsOnInPackages();

				fileRepository.pageRank(20, 0.85);
			}

		}
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
	
	@Bean
	public boolean setPackageLoSc(PackageRepository packageRepository) {
		LOGGER.info("计算Package总代码行...");
		packageRepository.setEmptyPackageLocAndLines();
		packageRepository.setPackageLoc();
		packageRepository.setPackageLines();
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
					Collection<CloneValueForDoubleNodes<Package>> result = cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE));
					for(CloneValueForDoubleNodes<Package> moduleClone : result) {
						moduleCloneRepository.createModuleClone(moduleClone.getNode1().getId(), moduleClone.getNode2().getId(), moduleClone.getChildren().size(), moduleClone.getAllNodesInNode1().size(), moduleClone.getAllNodesInNode2().size(), moduleClone.getNodesInNode1().size(), moduleClone.getNodesInNode2().size());
					}
				}
				LOGGER.info("设置Module Clone基础信息(co-change)...");
				moduleCloneRepository.setModuleCloneCochangeTimes(3);
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
					Collection<HotspotPackage> hotspotPackages = hotspotPackageDetector.detectHotspotPackages();
					AddChildrenPackages(-1, -1, hotspotPackages, aggregationCloneRepository);
				}
				aggregationClone = aggregationCloneRepository.getAllAggregationClone();
			}
			return aggregationClone;
		}
		return new ArrayList<>();
	}

	public void AddChildrenPackages(long parent1Id, long parent2Id, Collection<HotspotPackage> hotspotPackages, AggregationCloneRepository aggregationCloneRepository) {
		for(HotspotPackage hotspotPackage : hotspotPackages) {
			Collection<HotspotPackage> childrenHotspotPackages = hotspotPackage.getChildrenHotspotPackages();
			AddChildrenPackages(hotspotPackage.getPackage1().getId(), hotspotPackage.getPackage2().getId(), childrenHotspotPackages, aggregationCloneRepository);
			aggregationCloneRepository.createAggregationClone(hotspotPackage.getPackage1().getId(), hotspotPackage.getPackage2().getId(), parent1Id, parent2Id, hotspotPackage.getRelationPackages().getChildren().size(), hotspotPackage.getAllNodes1(), hotspotPackage.getAllNodes2(), hotspotPackage.getRelationNodes1(), hotspotPackage.getRelationNodes2());
		}
	}
}
