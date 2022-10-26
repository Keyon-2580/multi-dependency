package cn.edu.fudan.se.multidependency.service.query;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import cn.edu.fudan.se.multidependency.model.node.*;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.hierarchical_clustering.HierarchicalCluster;
import cn.edu.fudan.se.multidependency.model.relation.*;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.hierarchical_clustering.ClusterContain;
import cn.edu.fudan.se.multidependency.repository.node.NodeRepository;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.node.hierarchical_clustering.HierarchicalClusterRepository;
import cn.edu.fudan.se.multidependency.repository.relation.*;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.coupling.CouplingRepository;
import cn.edu.fudan.se.multidependency.repository.relation.hierarchical_clustering.ClusterContainRepository;
import cn.edu.fudan.se.multidependency.service.insert.RepositoryService;
import cn.edu.fudan.se.multidependency.service.query.coupling.CouplingService;
import cn.edu.fudan.se.multidependency.service.query.coupling.HierarchicalClusteringService;
import cn.edu.fudan.se.multidependency.service.query.smell.CyclicDependencyDetector;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculatorService;
import cn.edu.fudan.se.multidependency.service.query.metric.ModularityCalculator;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellDetectorService;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellMetricCalculatorService;
import cn.edu.fudan.se.multidependency.utils.DataUtil;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.graphdb.Label;
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
import cn.edu.fudan.se.multidependency.repository.relation.clone.AggregationCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.ModuleCloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
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

	private Map<NodeLabelType, List<Label>> mapLabels = new HashMap<>();

	private Nodes nodesAll;

	private Relations relationsAll;

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

	@Autowired
	private CouplingService couplingService;

	@Autowired
	private HierarchicalClusteringService hierarchicalClusteringService;

	@Resource(name="modularityCalculatorImplForFieldMethodLevel")
	private ModularityCalculator modularityCalculator;

	@Bean("insertNodes")
	public boolean insertNodes(PropertyConfig propertyConfig, NodeRepository nodeRepository) {
		if (nodeRepository.alreadyInserted()) {
			LOGGER.info("Nodes已存在");
			return false;
		}
		if (!propertyConfig.isInsertNodes()) {
			return false;
		}
		LOGGER.info("插入Nodes");
		try {
			RepositoryService serializedService =
					(RepositoryService) FileUtil.readObject(propertyConfig.getSerializePath());
			this.nodesAll = serializedService.getNodes();
			this.relationsAll = serializedService.getRelations();
			LOGGER.info("总计节点数：" + this.nodesAll.size());
			for(NodeLabelType nodeType : NodeLabelType.values()) {
				List<Label> labels = new ArrayList<>();
				for(String labelStr : nodeType.labels()) {
					Label label = Label.label(labelStr);
					labels.add(label);
				}
				mapLabels.put(nodeType, labels);
			}
			this.nodesAll.getAllNodes().forEach((nodeType, nodes) -> {
				nodeRepository.saveAll(nodes);
//				nodes.forEach(node -> {
//					if(node.getId() == null || !nodeRepository.existsById(node.getId())) {
//						List<Label> labels = mapLabels.get(node.getNodeType());
//						List<String> labelStrings = labels.stream()
//								.map(Label::name)
//								.collect(Collectors.toList());
//						node.setId(nodeRepository.save(node).getId());
//					}
//				});
			});
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		LOGGER.info("Nodes入库完成！");
		LOGGER.info("创建Node properties索引");
		nodeRepository.createFeatureIndex();
		nodeRepository.createCommitIndex();
		nodeRepository.createPackageIndex();
		nodeRepository.createProjectIndex();
		nodeRepository.createCloneGroupIndex();
		nodeRepository.createTraceIndex();
		nodeRepository.createMicroServiceIndex();
		nodeRepository.createProjectFileIndex();
		LOGGER.info("Node properties索引已创建");
		return true;
	}

	@Bean("insertRelations")
	public boolean insertOther(PropertyConfig propertyConfig, RelationRepository relationRepository) {
		if (relationRepository.alreadyInserted()) {
			LOGGER.info("已存在底层依赖关系");
			return false;
		}
		LOGGER.info("插入底层关系");
		if (!propertyConfig.isInsertRelations()
				|| propertyConfig.getSerializePath() == null
				|| propertyConfig.getSerializePath().equals("")) {
			return false;
		}
		try {
			LOGGER.info("总计关系数：" + relationsAll.size());
			LOGGER.info("开始插入底层关系");
			relationsAll.getAllRelations().forEach((relationType, relations) -> {
				relationRepository.saveAll(relations);
//				if (relationType == RelationType.ACCESS) {
//					for (Relation relation : relations) {
//						relationRepository.insertAccess(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.ANNOTATION) {
//					for (Relation relation : relations) {
//						relationRepository.insertAnnotation(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.CALL) {
//					for (Relation relation : relations) {
//						relationRepository.insertCall(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.CAST) {
//					for (Relation relation : relations) {
//						relationRepository.insertCast(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.CREATE) {
//					for (Relation relation : relations) {
//						relationRepository.insertCreate(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.EXTENDS) {
//					for (Relation relation : relations) {
//						relationRepository.insertExtends(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.GENERIC_PARAMETER) {
//					for (Relation relation : relations) {
//						relationRepository.insertGenericParam(relation.getStartNodeGraphId(),
//								relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.GLOBAL_VARIABLE) {
//					for (Relation relation : relations) {
//						relationRepository.insertGlobalVariable(relation.getStartNodeGraphId(),
//								relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.IMPLEMENTS) {
//					for (Relation relation : relations) {
//						relationRepository.insertImplements(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.IMPLEMENTS_C) {
//					for (Relation relation : relations) {
//						relationRepository.insertImplementsC(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.IMPLLINK) {
//					for (Relation relation : relations) {
//						relationRepository.insertImplink(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.IMPORT) {
//					for (Relation relation : relations) {
//						relationRepository.insertImport(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.INCLUDE) {
//					for (Relation relation : relations) {
//						relationRepository.insertInclude(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.LOCAL_VARIABLE) {
//					for (Relation relation : relations) {
//						relationRepository.insertLocalVariable(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.MEMBER_VARIABLE) {
//					for (Relation relation : relations) {
//						relationRepository.insertMemberVariable(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.PARAMETER) {
//					for (Relation relation : relations) {
//						relationRepository.insertParameter(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.RETURN) {
//					for (Relation relation : relations) {
//						relationRepository.insertReturn(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.THROW) {
//					for (Relation relation : relations) {
//						relationRepository.insertThrow(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.USE) {
//					for (Relation relation : relations) {
//						relationRepository.insertUse(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.VARIABLE_TYPE) {
//					for (Relation relation : relations) {
//						relationRepository.insertVariableType(relation.getStartNodeGraphId(),
//								relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.HAS) {
//					for (Relation relation : relations) {
//						relationRepository.insertHas(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.RELATE_TO) {
//					for (Relation relation : relations) {
//						relationRepository.insertRelateTo(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
//				if (relationType == RelationType.CONTAIN) {
//					for (Relation relation : relations) {
//						relationRepository.insertContain(relation.getStartNodeGraphId(), relation.getEndNodeGraphId()
//								, relation.getProperties());
//					}
//				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		LOGGER.info("入库完成！");
		return true;
	}

	@Bean
	public boolean sumAllCcnToPackage(PackageRepository packageRepository,
								   ContainRepository containRepository,
								   ProjectRepository projectRepository,
								   PropertyConfig propertyConfig){
		List<Project> projectList = projectRepository.queryAllProjects();
		if (!propertyConfig.isCalculateCcn()) {
			return false;
		}
		LOGGER.info("整理包的Ccn值");

		for(Project project : projectList) {
			List<Package> rootPackageList = containRepository.findProjectRootPackages(project.getId());
			if (packageRepository.hasCalculateCcn()) {
				continue;
			}
			for(Package pack : rootPackageList){
				setPackageWmc(pack, containRepository, packageRepository);
			}

		}
		return true;
	}

	private List<Double> setPackageWmc(Package pack, ContainRepository containRepository, PackageRepository packageRepository){
		long packageId = pack.getId();
		double wmc = 0;
		double amc = 0;
		List<Package> packageList= containRepository.findPackageContainPackages(packageId);
		List<ProjectFile> fileList= containRepository.findPackageContainFiles(packageId);
		if (fileList.size() > 0) {
			for(ProjectFile projectFile : fileList){
				wmc += projectFile.getWmc();
				amc += projectFile.getAmc();
			}
		}

		if(packageList.size() > 0){
			for(Package childrenPack : packageList){
				List<Double> packCcn = setPackageWmc(childrenPack, containRepository, packageRepository);
				wmc += packCcn.get(0);
				amc += packCcn.get(1);
			}

		}
		packageRepository.setPackageWmc(packageId, wmc);
		packageRepository.setPackageAmc(packageId, amc);
		List<Double> packTotalCcn = new ArrayList<>();
		packTotalCcn.add(wmc);
		packTotalCcn.add(amc);
		return packTotalCcn;
	}

	@Bean
	public boolean mergeSeparateFileIntoPackage(PackageRepository packageRepository,
												NodeRepository nodeRepository,
												ProjectFileRepository projectFileRepository,
												ContainRepository containRepository,
												RelationRepository relationRepository) {
		if (!packageRepository.findIfSeparateFileExists()) {
			LOGGER.info("已合并分散文件");
			return false;
		}
		LOGGER.info("开始合并分散文件");
		// merge separate files into packages
		List<Package> packageList = packageRepository.queryAllPackage();
		List<Relation> containRelations = new ArrayList<>();
		for (Package pkg : packageList) {
			Long pkgId = pkg.getId();
			if (packageRepository.findIfPackageContainFiles(pkgId) && packageRepository.findIfPackageContainPackages(pkgId)) {
				List<ProjectFile> projectFiles = projectFileRepository.findPackageContainedFiles(pkgId);
				// remove contain file relations
				Package virtualPackage = new Package();
				virtualPackage.setName(pkg.getName()+"-default");
				virtualPackage.setDirectoryPath(pkg.getDirectoryPath()+"default");
				virtualPackage.setLanguage(pkg.getLanguage());
				virtualPackage.setNof(projectFiles.size());
				int locSum = projectFiles.stream().mapToInt(ProjectFile::getLoc).sum();
				virtualPackage.setLoc(locSum);
				for (ProjectFile projectFile : projectFiles) {

					Relation contain =
							containRepository.findContainByPkgAndFile(pkg.getId(), projectFile.getId());
					relationRepository.delete(contain);
					Contain containFile = new Contain(virtualPackage, projectFile);
					containRelations.add(containFile);
				}

				nodeRepository.save(virtualPackage);
				Relation containRelation = new Contain(pkg, virtualPackage);
//				relationRepository.save(containRelation);
				containRelations.add(containRelation);
			}
		}
		relationRepository.saveAll(containRelations);
		LOGGER.info("合并成功");
		return true;
	}

	//	@Bean
	public boolean setCommitSize(CommitUpdateFileRepository commitUpdateFileRepository,
								 CommitRepository commitRepository,
								 ProjectRepository projectRepository) {
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

//			coChangeRepository.createCoChanges(Constant.COUNT_OF_MIN_COCHANGE);
			coChangeRepository.createCoChanges(Constant.COUNT_OF_MIN_COCHANGE, Constant.MIN_CHANGE_CONFIDENCE);
			coChangeRepository.updateCoChangesForFile();
		}
		return coChanges;
	}

	@Bean("setModuleCoChange")
	public List<CoChange> setModuleCoChange(PropertyConfig propertyConfig, CoChangeRepository coChangeRepository) {
//		if (!propertyConfig.isSetModuleCoChange()) {
		if (!propertyConfig.isSetCoChange()) {
			return new ArrayList<>();
		}
		List<CoChange> moduleCoChanges = coChangeRepository.findModuleCoChangeLimit();
		if(moduleCoChanges != null && !moduleCoChanges.isEmpty()){
			LOGGER.info("已存在Module CoChange关系" );
		}
		else {
			LOGGER.info("创建Module CoChange关系...");
			Map<String, List<CoChange>> coChangeMap = hotspotPackagePairDetector.detectHotspotPackagePairWithCoChange();
			List<CoChange> moduleCoChangeList = new ArrayList<>(coChangeMap.get(RelationType.str_CO_CHANGE));
			List<CoChange> moduleCoChangeListTmp = new ArrayList<>();
			int size = 0;
			for(CoChange coChange : moduleCoChangeList) {
				moduleCoChangeListTmp.add(coChange);
				if(++size > 500){
					coChangeRepository.saveAll(moduleCoChangeListTmp);
					moduleCoChangeListTmp.clear();
					size = 0;
				}
			}
			coChangeRepository.saveAll(moduleCoChangeListTmp);
		}
		return moduleCoChanges;
	}

	@Bean("setAggregationCoChange")
	public List<AggregationCoChange> setAggregationCoChange(PropertyConfig propertyConfig,
															CoChangeRepository coChangeRepository,
															AggregationCoChangeRepository aggregationCoChangeRepository) {
//		if (!propertyConfig.isSetAggregationCoChange()) {
		if (!propertyConfig.isSetCoChange()) {
			return new ArrayList<>();
		}
		List<AggregationCoChange> aggregationCoChanges = aggregationCoChangeRepository.findAggregationCoChangeLimit();
		if(aggregationCoChanges != null && !aggregationCoChanges.isEmpty()){
			LOGGER.info("已存在Aggregation CoChange关系" );
		}
		else {
			LOGGER.info("创建Aggregation CoChange关系...");
			aggregationCoChangeRepository.deleteAll();

			Map<String, List<CoChange>> coChangeMap = hotspotPackagePairDetector.detectHotspotPackagePairWithCoChange();
			List<CoChange> aggregationCoChangeList = new ArrayList<>(coChangeMap.get(RelationType.str_AGGREGATION_CO_CHANGE));
			List<AggregationCoChange> aggregationCoChangeListTmp = new ArrayList<>();
			int size = 0;
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
		return aggregationCoChanges;
	}

	@Bean("setDependsOn")
	public List<DependsOn> setDependsOn(PropertyConfig propertyConfig,
										DependsOnRepository dependsOnRepository,
										ProjectFileRepository fileRepository) {
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
//			fileRepository.pageRank(20, 0.85);
		}
		return dependsOns;
	}

	//	@Bean("setModuleDependsOn")
	public List<DependsOn> setModuleDependsOn(PropertyConfig propertyConfig, DependsOnRepository dependsOnRepository) {
//		if (!propertyConfig.isSetModuleDependsOn()) {
		if (!propertyConfig.isSetDependsOn()) {
			return new ArrayList<>();
		}
		List<DependsOn> moduleDependsOns = dependsOnRepository.findModuleDependsOnWithLimit();
		if(moduleDependsOns != null && !moduleDependsOns.isEmpty()){
			LOGGER.info("已存在Module Depends On关系" );
		}
		else {
			LOGGER.info("创建Module Depends On关系...");
			Map<String, List<DependsOn>> dependsOnMap = hotspotPackagePairDetector.detectHotspotPackagePairWithDependsOn();
			List<DependsOn> moduleDependsOnList = new ArrayList<>(dependsOnMap.get(RelationType.str_DEPENDS_ON));
			List<DependsOn> moduleDependsOnListTmp = new ArrayList<>();
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
		}
		return moduleDependsOns;
	}

	//	@Bean("setAggregationDependsOn")
	public List<AggregationDependsOn> setAggregationDependsOn(PropertyConfig propertyConfig,
															  DependsOnRepository dependsOnRepository,
															  AggregationDependsOnRepository aggregationDependsOnRepository) {
//		if (!propertyConfig.isSetAggregationDependsOn()) {
		if (!propertyConfig.isSetDependsOn()) {
			return new ArrayList<>();
		}
		List<AggregationDependsOn> aggregationDependsOns = aggregationDependsOnRepository.findAggregationDependsOnWithLimit();
		if(aggregationDependsOns != null && !aggregationDependsOns.isEmpty()){
			LOGGER.info("已存在Aggregation Depends On关系" );
		}
		else {
			LOGGER.info("创建Aggregation Depends On关系...");
			aggregationDependsOnRepository.deleteAll();

			Map<String, List<DependsOn>> dependsOnMap = hotspotPackagePairDetector.detectHotspotPackagePairWithDependsOn();
			List<DependsOn> aggregationDependsOnList = new ArrayList<>(dependsOnMap.get(RelationType.str_AGGREGATION_DEPENDS_ON));
			List<AggregationDependsOn> aggregationDependsOnListTmp = new ArrayList<>();
			int size = 0;
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
		return aggregationDependsOns;
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
	public List<CloneGroup> setCloneGroup(PropertyConfig propertyConfig,
										  CloneGroupRepository cloneGroupRepository,
										  CloneRepository cloneRepository) {
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
	public List<ModuleClone> setModuleClone(PropertyConfig propertyConfig,
											ModuleCloneRepository moduleCloneRepository,
											CloneRepository cloneRepository) {
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
				List<BasicDataForDoubleNodes<Node, Relation>> clonePackagePairs =
						summaryAggregationDataService.queryPackageCloneFromFileCloneSort(
								basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE
								));
				for(BasicDataForDoubleNodes<Node, Relation> clonePackagePair : clonePackagePairs) {
					CloneRelationDataForDoubleNodes<Node, Relation> packagePairCloneRelationData =
							(CloneRelationDataForDoubleNodes<Node, Relation>) clonePackagePair;
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
	public List<AggregationClone> setAggregationClone(PropertyConfig propertyConfig,
													  AggregationCloneRepository aggregationCloneRepository,
													  CloneRepository cloneRepository) {
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

	public void AddChildrenPackages(long parent1Id, long parent2Id,
									Collection<HotspotPackagePair> hotspotPackagePairs,
									AggregationCloneRepository aggregationCloneRepository) {
		for(HotspotPackagePair hotspotPackagePair : hotspotPackagePairs) {
			Collection<HotspotPackagePair> childrenHotspotPackagePairs = hotspotPackagePair.getChildrenHotspotPackagePairs();
			AddChildrenPackages(hotspotPackagePair.getPackage1().getId(),
					hotspotPackagePair.getPackage2().getId(), childrenHotspotPackagePairs, aggregationCloneRepository);
			CloneRelationDataForDoubleNodes<Node, Relation> packagePairCloneRelationData =
					(CloneRelationDataForDoubleNodes<Node, Relation>) hotspotPackagePair.getPackagePairRelationData();
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

	@Bean("setPackageDepth")
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
			//smellDetectorService.createGodComponentSmells(false);

		}
		return true;
	}

	//	@Bean
	public boolean setSmellMetrics() {
		LOGGER.info("创建Smell Metric度量值节点和关系...");
		smellMetricCalculatorService.createSmellMetric(false);
		return true;
	}

	//	@Bean
	public boolean exportCyclicDependency(PropertyConfig propertyConfig) {
		if (propertyConfig.isExportCyclicDependency()) {
			LOGGER.info("export cyclic dependency...");
			cyclicDependencyDetector.exportCycleDependency();
			System.exit(0);
			return true;
		}
		return false;
	}

	@Bean("setCoupling")
	public List<Coupling> setCouplingValue(PropertyConfig propertyConfig, CouplingRepository couplingRepository,
										   DependsOnRepository dependsOnRepository) {
		if (!propertyConfig.isSetCoupling()) {
			return new ArrayList<>();
		}
		List<Coupling> couplings = couplingRepository.findFileDependsWithLimit();
		if(couplings != null && !couplings.isEmpty()){
			LOGGER.info("已存在文件级Coupling关系" );
		}
		else{
			LOGGER.info("开始入库文件级Coupling数据...");
			List<Coupling> couplingsTmp = new ArrayList<>();
			List<DependsOn> allDependsOn = dependsOnRepository.findFileDepends();
			HashSet<String> allFilesPair = new HashSet<>();

			for(DependsOn dependsOn: allDependsOn){
				long file1Id = dependsOn.getStartNode().getId();
				long file2Id = dependsOn.getEndNode().getId();
				String filesPair = file1Id + "_" + file2Id;
				String filesPairReverse = file2Id + "_" + file1Id;

				if(!allFilesPair.contains(filesPair) && !allFilesPair.contains(filesPairReverse)){
					allFilesPair.add(filesPair);
//					System.out.println(file1Id + "  " + file2Id);

					int funcAndVarNumAAtoB = couplingRepository.queryTwoFilesDependsOnFunctionsNum(file1Id, file2Id) +
							couplingRepository.queryTwoFilesDependsOnVariablesNum(file1Id, file2Id);
					int funcAndVarNumBAtoB = couplingRepository.queryTwoFilesDependsByFunctionsNum(file1Id, file2Id) +
							couplingRepository.queryTwoFilesDependsByVariablesNum(file1Id, file2Id);
					int funcAndVarNumABtoA = couplingRepository.queryTwoFilesDependsByFunctionsNum(file2Id, file1Id) +
							couplingRepository.queryTwoFilesDependsByVariablesNum(file2Id, file1Id);
					int funcAndVarNumBBtoA = couplingRepository.queryTwoFilesDependsOnFunctionsNum(file2Id, file1Id) +
							couplingRepository.queryTwoFilesDependsOnVariablesNum(file2Id, file1Id);

					DependsOn dependsOnAtoB = dependsOnRepository.findDependsOnBetweenFiles(file1Id, file2Id);
					DependsOn dependsOnBtoA = dependsOnRepository.findDependsOnBetweenFiles(file2Id, file1Id);

					int dependsOntimesAtoB = 0;
					int dependsOntimesBtoA = 0;

					if(dependsOnAtoB != null) {
						Map<String, Long> dependsOnTypesAtoB = dependsOnAtoB.getDependsOnTypes();

						for (String type : dependsOnTypesAtoB.keySet()) {
							if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN")
									|| type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("IMPLEMENTS")
									|| type.equals("MEMBER_VARIABLE") || type.equals("CREATE")) {
								dependsOntimesAtoB += dependsOnTypesAtoB.get(type);
							}
						}
					}else{
						dependsOnAtoB = new DependsOn();
					}

					if(dependsOnBtoA != null) {
						Map<String, Long> dependsOnTypesBtoA = dependsOnBtoA.getDependsOnTypes();

						for (String type : dependsOnTypesBtoA.keySet()) {
							if (type.equals("USE") || type.equals("CALL") || type.equals("EXTENDS") || type.equals("RETURN")
									|| type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("IMPLEMENTS")
									|| type.equals("MEMBER_VARIABLE") || type.equals("CREATE")) {
								dependsOntimesBtoA += dependsOnTypesBtoA.get(type);
							}
						}
					}else{
						dependsOnBtoA = new DependsOn();
					}

					if(dependsOntimesAtoB != 0 || dependsOntimesBtoA != 0) {
						double C_AtoB = couplingService.calC1to2(funcAndVarNumAAtoB,funcAndVarNumBAtoB);
						double C_BtoA = couplingService.calC1to2(funcAndVarNumABtoA,funcAndVarNumBBtoA);
						double C_AandB = couplingService.calC(C_AtoB, C_BtoA);
						double U_AtoB = couplingService.calU1to2(dependsOntimesAtoB, dependsOntimesBtoA);
						double U_BtoA = couplingService.calU1to2(dependsOntimesBtoA, dependsOntimesAtoB);
						double I_AandB = couplingService.calI(dependsOntimesAtoB, dependsOntimesBtoA);
						double disp_AandB = couplingService.calDISP(C_AandB, dependsOntimesAtoB, dependsOntimesBtoA);
//						double dist = 1.0 / Math.log(I_AandB + 1);
						double dist = 1.0 / (C_AandB + I_AandB);

						couplingsTmp.add(new Coupling(dependsOn.getStartNode(), dependsOn.getEndNode(), dependsOnAtoB,dependsOnBtoA,
								funcAndVarNumAAtoB, funcAndVarNumBAtoB, funcAndVarNumABtoA, funcAndVarNumBBtoA, dependsOntimesAtoB, dependsOntimesBtoA,
								C_AtoB, C_BtoA, C_AandB, U_AtoB, U_BtoA, I_AandB, disp_AandB, dist));
					}
				}
			}
			couplingRepository.saveAll(couplingsTmp);
		}
		LOGGER.info("文件级coupling数据入库完成!");
		return couplings;
	}

	@Bean
	public boolean setPackageCouplingValue(CouplingRepository couplingRepository,
										   DependsOnRepository dependsOnRepository,
										   PackageRepository packageRepository,
										   CouplingService couplingService) {
		List<Coupling> pkgCoupling = couplingRepository.findPkgDependsWithLimit();
		if (pkgCoupling != null && !pkgCoupling.isEmpty()) {
			LOGGER.info("已存在包级别Coupling");
			return false;
		}
		LOGGER.info("开始入库package coupling数据");
		Map<Map<Package, Package>, Set<DependsOn>> pkgDependOns = new HashMap<>();
		Set<Coupling> allCoupling = new HashSet<>();
		Map<String, Integer> dMap = new HashMap<>();
		List<Package> allPackages = packageRepository.queryAllPackage();
		for (int i = 0; i < allPackages.size(); i++) {
			for (int j = i+1; j < allPackages.size(); j++) {
				Package pkg1 = allPackages.get(i);
				Package pkg2 = allPackages.get(j);
				List<DependsOn> outDepends = dependsOnRepository.findAllDependsOnBetweenPackages(pkg1.getId(), pkg2.getId());
				List<DependsOn> inDepends = dependsOnRepository.findAllDependsOnBetweenPackages(pkg2.getId(), pkg1.getId());
				if (!outDepends.isEmpty()) {
					Map<Package, Package> key = new HashMap<>();
					key.put(pkg1, pkg2);
					if (pkgDependOns.containsKey(key)) {
						pkgDependOns.get(key).addAll(outDepends);
					} else {
						pkgDependOns.put(key, new HashSet<>(outDepends));
					}
				}
				if (!inDepends.isEmpty()) {
					Map<Package, Package> key = new HashMap<>();
					key.put(pkg2, pkg1);
					if (pkgDependOns.containsKey(key)) {
						pkgDependOns.get(key).addAll(inDepends);
					} else {
						pkgDependOns.put(key, new HashSet<>(inDepends));
					}
				}
			}
		}
		JSONArray edges = new JSONArray();
		for(Map<Package, Package> map: pkgDependOns.keySet()){
			JSONObject tmpEdge = new JSONObject();
			int DAtoB = 0;
			int DBtoA = 0;
			double dist = 0.0;
			double distSum = 0;

			for(Package pck: map.keySet()){
				tmpEdge.put("id", pck.getId().toString() + "_" + map.get(pck).getId().toString());
				tmpEdge.put("source", pck.getId().toString());
				tmpEdge.put("target", map.get(pck).getId().toString());
			}
			Set<Long> fileIdSet1 = new HashSet<>();
			Set<Long> fileIdSet2 = new HashSet<>();
			Set<String> aggregatedTypes = new HashSet<>();
			for(DependsOn dependsOn: pkgDependOns.get(map)){
				boolean flag = false;
				Map<String, Long> dependsOnTypes = dependsOn.getDependsOnTypes();
				for (String type : dependsOnTypes.keySet()) {
					if (type.equals("EXTENDS") || type.equals("IMPLEMENTS") ||
							type.equals("USE") || type.equals("CALL") || type.equals("RETURN")
							|| type.equals("PARAMETER") || type.equals("LOCAL_VARIABLE") || type.equals("CREATE")
							|| type.equals("MEMBER_VARIABLE")) {
						flag = true;
						aggregatedTypes.add(type);
						break;
					}
				}
				if(flag){
					Coupling coupling = couplingRepository.queryCouplingBetweenTwoFiles(dependsOn.getStartNode().getId()
							, dependsOn.getEndNode().getId());
					DAtoB += coupling.getDAtoB();
					DBtoA += coupling.getDBtoA();
					dist += coupling.getDist();
					distSum  += 1;
					fileIdSet1.add(dependsOn.getStartNode().getId());
					fileIdSet2.add(dependsOn.getEndNode().getId());
				}
			}
			double fileHMean = couplingService.calcH(fileIdSet1.size(), fileIdSet2.size());
			tmpEdge.put("dist", dist / distSum);
			tmpEdge.put("dependsOnNum", pkgDependOns.get(map).size());
			tmpEdge.put("C", DataUtil.toFixed(fileHMean));
			double logD = Math.max(0, Math.log10(DAtoB));
			tmpEdge.put("D", DAtoB);
			tmpEdge.put("type", String.join(",", aggregatedTypes));
			tmpEdge.put("logD", DataUtil.toFixed(logD));
			dMap.put((String)tmpEdge.get("id"), DAtoB);
			edges.add(tmpEdge);
		}
		Set<String> tmp = new HashSet<>();
		for (int i = 0; i < edges.size(); i++) {
			JSONObject edge = edges.getJSONObject(i);
			String reverseId = edge.get("target") + "_" + edge.get("source");
			int D = (int) edge.get("D");
			if (tmp.contains((String) edge.get("id"))) {
				edge.put("I", -1);
				continue;
			}
			if (dMap.containsKey(reverseId)) {
				edge.put("I", couplingService.calPkgI(D, dMap.get(reverseId)));
				tmp.add(reverseId);
			} else {
				edge.put("I", D);
			}
		}
		for (int i = 0; i < edges.size(); i++) {
			JSONObject edge = edges.getJSONObject(i);
			Coupling coupling = new Coupling();
			coupling.setC(edge.getDouble("C"));
			coupling.setDAtoB(edge.getInteger("D"));
			coupling.setDist(edge.getDouble("dist"));
			coupling.setI(edge.getDouble("I"));
			coupling.setDependsOnTypeStartToEnd(edge.getString("type"));
			coupling.setStartNode(packageRepository.findPackageById(edge.getLong("source")));
			coupling.setEndNode(packageRepository.findPackageById(edge.getLong("target")));
			allCoupling.add(coupling);
		}
		couplingRepository.saveAll(allCoupling);
		LOGGER.info("包级coupling数据入库完成!");
		return true;
	}
	/**
	 * 使用层次聚类算法，将涉及到Coupling关系的文件进行聚类
	 */
	@Bean("setHierarchicalCluster")
	public List<HierarchicalCluster> setHierarchicalCluster(PropertyConfig propertyConfig,
												 HierarchicalClusterRepository hierarchicalClusterRepository,
												 ClusterContainRepository clusterContainRepository,
												 CouplingRepository couplingRepository,
												 ProjectFileRepository projectFileRepository){
		if (!propertyConfig.isSetHierarchicalCluster()) {
			return new ArrayList<>();
		}
		List<ClusterContain> clusterContains = clusterContainRepository.findClusterContainWithLimit();
		List<HierarchicalCluster> hierarchicalClusters = hierarchicalClusterRepository.findHierarchicalClusterWithLimit();
		if((clusterContains != null && !clusterContains.isEmpty())
				|| (hierarchicalClusters != null && !hierarchicalClusters.isEmpty())){
			LOGGER.info("已存在Cluster Contain关系" );
		}else{
			LOGGER.info("创建Hierarchical Cluster以及Cluster Contain...");

			List<HierarchicalCluster> hierarchicalClustersTmp = new ArrayList<>();
			List<ClusterContain> clusterContainsTmp = new ArrayList<>();
			List<Coupling> allCouplings = couplingRepository.queryAllCouplingsOrderByDist();
			List<ProjectFile> allFilesRelated = projectFileRepository.queryAllFilesRelatedByCouplings();
			Map<ProjectFile, HierarchicalCluster> topClusterMap = new HashMap<>();
			Map<ProjectFile, Integer> topClusterLengthMap = new HashMap<>();

			for(ProjectFile projectFile: allFilesRelated){
				HierarchicalCluster cluster = new HierarchicalCluster(true);
				ClusterContain contain = new ClusterContain(cluster, projectFile);
				hierarchicalClustersTmp.add(cluster);
				clusterContainsTmp.add(contain);
				topClusterMap.put(projectFile, cluster);
				topClusterLengthMap.put(projectFile, 0);
			}

			hierarchicalClusterRepository.saveAll(hierarchicalClustersTmp);
			clusterContainRepository.saveAll(clusterContainsTmp);
			hierarchicalClustersTmp.clear();
			clusterContainsTmp.clear();

			for(Coupling coupling: allCouplings){
				ProjectFile start = (ProjectFile) coupling.getStartNode();
				ProjectFile end = (ProjectFile) coupling.getEndNode();

				HierarchicalCluster startParentCluster = topClusterMap.get(start);
				HierarchicalCluster endParentCluster = topClusterMap.get(end);

				if(!startParentCluster.equals(endParentCluster)){
					HierarchicalCluster newParentCluster = new HierarchicalCluster();

					ClusterContain tmpContain1 = new ClusterContain(newParentCluster, startParentCluster);
					ClusterContain tmpContain2 = new ClusterContain(newParentCluster, endParentCluster);
					hierarchicalClustersTmp.add(newParentCluster);
					clusterContainsTmp.add(tmpContain1);
					clusterContainsTmp.add(tmpContain2);

					topClusterMap.put(start, newParentCluster);
					topClusterMap.put(end, newParentCluster);
					topClusterLengthMap.merge(start, 1, Integer::sum);
					topClusterLengthMap.merge(end, 1, Integer::sum);
				}
				couplingRepository.setCouplingClusterDistance(coupling.getId(), topClusterLengthMap.get(start)
						+ topClusterLengthMap.get(end));
			}

			hierarchicalClusterRepository.saveAll(hierarchicalClustersTmp);
			clusterContainRepository.saveAll(clusterContainsTmp);

			HierarchicalCluster finalTopCluster = new HierarchicalCluster();
			hierarchicalClusterRepository.save(finalTopCluster);
			List<HierarchicalCluster> allTopParentClusters = hierarchicalClusterRepository.findAllTopParentCluster();

			for(HierarchicalCluster cluster: allTopParentClusters){
				clusterContainRepository.save(new ClusterContain(finalTopCluster, cluster));
			}
		}
		return hierarchicalClusters;
	}

	@Bean("setLooseDegree")
	public List<Package> setLooseDegree(PropertyConfig propertyConfig, PackageRepository packageRepository) {
		List<Package> pcks = packageRepository.queryAllPackageWithLimit();
		if(pcks.get(0).getLooseDegree() != -2){
			LOGGER.info("已存在Loose Degree属性" );
			return new ArrayList<>();
		}
		if (propertyConfig.isSetLooseDegree()) {
			LOGGER.info("创建Loose Degree属性" );
			List<Package> allPcks = packageRepository.queryAllPackage();

			for(Package pck: allPcks){
				if(pck.getDepth() <= 1){
					packageRepository.setPackageLooseDegree(pck.getId(), -1);
				}else{
					hierarchicalClusteringService.calPackageComplexityByCluster(pck.getId());
				}
			}
		}
		return pcks;
	}
}
