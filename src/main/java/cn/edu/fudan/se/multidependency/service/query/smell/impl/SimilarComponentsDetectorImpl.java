package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackagePairDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackagePair;
import cn.edu.fudan.se.multidependency.service.query.smell.data.Cycle;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.ModuleService;
import cn.edu.fudan.se.multidependency.service.query.smell.SimilarComponentsDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.SimilarComponents;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.clone.data.FileCloneWithCoChange;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculatorService;

@Service
public class SimilarComponentsDetectorImpl implements SimilarComponentsDetector {
	
	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@Autowired
	private CloneAnalyseService cloneAnalyseService;
	
	@Autowired
	private ModuleService moduleService;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private DependsOnRepository dependsOnRepository;
	
	@Autowired
	private MetricCalculatorService metricCalculatorService;

	@Autowired
	private HotspotPackagePairDetector hotspotPackagePairDetector;

	@Autowired
	private CommitUpdateFileRepository commitUpdateFileRepository;

	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private ContainRelationService containRelationService;

	@Autowired
	private CloneRepository cloneRepository;

	@Autowired
	private CoChangeRepository coChangeRepository;

	private static final int minCoChange = 10;

	@Override
	public Map<Long, List<SimilarComponents<ProjectFile>>> getFileSimilarComponents() {
		String key = "fileSimilarComponents";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<SimilarComponents<ProjectFile>>> result = new HashMap<>();
		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE, SmellType.SIMILAR_COMPONENTS));
		smells.sort((smell1, smell2) -> {
			List<String> namePart1 = Arrays.asList(smell1.getName().split("_"));
			List<String> namePart2 = Arrays.asList(smell2.getName().split("_"));
			int partition1 = Integer.parseInt(namePart1.get(namePart1.size() - 1));
			int partition2 = Integer.parseInt(namePart2.get(namePart2.size() - 1));
			return Integer.compare(partition1, partition2);
		});
		List<SimilarComponents<ProjectFile>> similarComponentsList = new ArrayList<>();
		for (Smell smell : smells) {
			List<Node> files = new ArrayList<>(smellRepository.findSmellContains(smell.getId()));
			ProjectFile file1 = (ProjectFile) files.get(0);
			ProjectFile file2 = (ProjectFile) files.get(1);
			List<Clone> clones = new ArrayList<>(cloneRepository.judgeCloneByFileId(file1.getId(), file2.getId()));
			if (clones.size() == 0) {
				clones = new ArrayList<>(cloneRepository.judgeCloneByFileId(file2.getId(), file1.getId()));
			}
			Clone clone = clones.get(0);
			int node1ChangeTimes;
			int node2ChangeTimes;
			CoChange coChange = coChangeRepository.findCoChangesBetweenTwoFiles(file1.getId(), file2.getId());
			if (coChange != null) {
				node1ChangeTimes = coChange.getNode1ChangeTimes();
				node2ChangeTimes = coChange.getNode2ChangeTimes();
			}
			else {
				coChange = coChangeRepository.findCoChangesBetweenTwoFiles(file2.getId(), file1.getId());
				node2ChangeTimes = coChange.getNode1ChangeTimes();
				node1ChangeTimes = coChange.getNode2ChangeTimes();
			}
			SimilarComponents<ProjectFile> temp = new SimilarComponents<>(file1, file2, clone.getValue(), node1ChangeTimes, node2ChangeTimes, coChange.getTimes());
			temp.setModule1(moduleService.findFileBelongToModule(file1));
			temp.setModule2(moduleService.findFileBelongToModule(file2));
			temp.setCloneType(clone.getCloneType());
			Collection<DependsOn> file1DependsOns = dependsOnRepository.findFileDependsOn(file1.getId());
			Collection<DependsOn> file2DependsOns = dependsOnRepository.findFileDependsOn(file2.getId());
			for(DependsOn file1DependsOn : file1DependsOns) {
				temp.addNode1DependsOn(file1DependsOn.getEndNode());
			}
			for(DependsOn file2DependsOn : file2DependsOns) {
				temp.addNode2DependsOn(file2DependsOn.getEndNode());
			}
			temp.setSameDependsOnRatio();
			similarComponentsList.add(temp);
		}
		for(SimilarComponents<ProjectFile> similarComponents : similarComponentsList) {
			Project project1 = containRelationService.findFileBelongToProject(similarComponents.getNode1());
			Project project2 = containRelationService.findFileBelongToProject(similarComponents.getNode1());
			if (project1 != null && project2 != null) {
				if(project1.getId().equals(project2.getId())) {
					List<SimilarComponents<ProjectFile>> temp = result.getOrDefault(project1.getId(), new ArrayList<>());
					temp.add(similarComponents);
					result.put(project1.getId(), temp);
				}
				//涉及到多个项目的Similar Components，使用-1表示其项目ID，该项目不存在
				else {
					List<SimilarComponents<ProjectFile>> temp = result.getOrDefault(-1L, new ArrayList<>());
					temp.add(similarComponents);
					result.put(-1L, temp);
				}
			}
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<SimilarComponents<Package>>> getPackageSimilarComponents() {
		String key = "fileSimilarComponents";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<SimilarComponents<Package>>> result = new HashMap<>();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Long, List<SimilarComponents<ProjectFile>>> detectFileSimilarComponents() {
		Map<Long, List<SimilarComponents<ProjectFile>>> result = new HashMap<>();
		Collection<Clone> clones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		Collection<FileCloneWithCoChange> clonesWithCoChange;
		try {
			clonesWithCoChange = cloneAnalyseService.addCoChangeToFileClones(clones);
		} catch (Exception e) {
			clonesWithCoChange = new ArrayList<>();
		}
		Map<Long, FileMetrics> fileMetrics = metricCalculatorService.calculateFileMetrics();
		List<SimilarComponents<ProjectFile>> similarComponentsList = new ArrayList<>();
		for(FileCloneWithCoChange clone : clonesWithCoChange) {
			ProjectFile file1 = clone.getFile1();
			ProjectFile file2 = clone.getFile2();
			if(clone.getCochangeTimes() < minCoChange) {
				continue;
			}
			if(!moduleService.isInDifferentModule(file1, file2)) {
				continue;
			}
			SimilarComponents<ProjectFile> temp = new SimilarComponents<>(file1, file2, clone.getFileClone().getValue(), fileMetrics.get(file1.getId()).getEvolutionMetric().getCommits(), fileMetrics.get(file2.getId()).getEvolutionMetric().getCommits(), clone.getCochangeTimes());
			temp.setModule1(moduleService.findFileBelongToModule(file1));
			temp.setModule2(moduleService.findFileBelongToModule(file2));
			temp.setCloneType(clone.getFileClone().getCloneType());
			Collection<DependsOn> file1DependsOns = dependsOnRepository.findFileDependsOn(file1.getId());
			Collection<DependsOn> file2DependsOns = dependsOnRepository.findFileDependsOn(file2.getId());
			for(DependsOn file1DependsOn : file1DependsOns) {
				temp.addNode1DependsOn(file1DependsOn.getEndNode());
			}
			for(DependsOn file2DependsOn : file2DependsOns) {
				temp.addNode2DependsOn(file2DependsOn.getEndNode());
			}
			temp.setSameDependsOnRatio();
			similarComponentsList.add(temp);
		}
		for(SimilarComponents<ProjectFile> similarComponents : similarComponentsList) {
			Project project1 = containRelationService.findFileBelongToProject(similarComponents.getNode1());
			Project project2 = containRelationService.findFileBelongToProject(similarComponents.getNode1());
			if (project1 != null && project2 != null) {
				if(project1.getId().equals(project2.getId())) {
					List<SimilarComponents<ProjectFile>> temp = result.getOrDefault(project1.getId(), new ArrayList<>());
					temp.add(similarComponents);
					result.put(project1.getId(), temp);
				}
				//涉及到多个项目的Similar Components，使用-1表示其项目ID，该项目不存在
				else {
					List<SimilarComponents<ProjectFile>> temp = result.getOrDefault(-1L, new ArrayList<>());
					temp.add(similarComponents);
					result.put(-1L, temp);
				}
			}
		}
		return result;
	}

	@Override
	public Map<Long, List<SimilarComponents<Package>>> detectPackageSimilarComponents() {
		Map<Long, List<SimilarComponents<Package>>> result = new HashMap<>();
		Map<Long, List<HotspotPackagePair>> projectHotspotPackagePairList = new HashMap<>();
		List<HotspotPackagePair> hotspotPackagePairs = hotspotPackagePairDetector.detectHotspotPackagePairWithFileClone();
		for (HotspotPackagePair hotspotPackagePair : hotspotPackagePairs) {
			Project project1 = containRelationService.findPackageBelongToProject(hotspotPackagePair.getPackage1());
			Project project2 = containRelationService.findPackageBelongToProject(hotspotPackagePair.getPackage1());
			if (project1 != null && project2 != null) {
				if (project1.getId().equals(project2.getId())) {
					List<HotspotPackagePair> temp = projectHotspotPackagePairList.getOrDefault(project1.getId(), new ArrayList<>());
					temp.add(hotspotPackagePair);
					projectHotspotPackagePairList.put(project1.getId(), temp);
				}
				//涉及到多个项目的Similar Components，使用-1表示其项目ID，该项目不存在
				else {
					List<HotspotPackagePair> temp = projectHotspotPackagePairList.getOrDefault(-1L, new ArrayList<>());
					temp.add(hotspotPackagePair);
					projectHotspotPackagePairList.put(-1L, temp);
				}
			}
		}
		for (Map.Entry<Long, List<HotspotPackagePair>> entry : projectHotspotPackagePairList.entrySet()) {
			result.put(entry.getKey(), getPackageSimilars(entry.getValue()));
		}
		return result;
	}

	public List<SimilarComponents<Package>> getPackageSimilars(List<HotspotPackagePair> hotspotPackagePairs) {
		List<SimilarComponents<Package>> result = new ArrayList<>();
		for (HotspotPackagePair hotspotPackagePair : hotspotPackagePairs) {
			Package pck1 = hotspotPackagePair.getPackage1();
			Package pck2 = hotspotPackagePair.getPackage2();
			Set<Commit> pck1CommitSet = new HashSet<>(commitUpdateFileRepository.findCommitInPackageByPackageId(pck1.getId()));
			Set<Commit> pck2CommitSet = new HashSet<>(commitUpdateFileRepository.findCommitInPackageByPackageId(pck2.getId()));
			Set<Commit> pckCommitSet = new HashSet<>(pck1CommitSet);
			pckCommitSet.retainAll(pck2CommitSet);
			SimilarComponents<Package> similarComponents = new SimilarComponents<>(pck1, pck2, hotspotPackagePair.getPackagePairRelationData().getValue(), pck1CommitSet.size(), pck2CommitSet.size(), pckCommitSet.size());
			result.add(similarComponents);
			result.addAll(getPackageSimilars(hotspotPackagePair.getChildrenHotspotPackagePairs()));
		}
		return result;
	}
}
