package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackagePairDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackagePair;
import org.neo4j.kernel.api.exceptions.index.IndexProxyAlreadyClosedKernelException;
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
	
	@Override
	public Collection<SimilarComponents<ProjectFile>> fileSimilars() {
		String key = "fileSimilars";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<SimilarComponents<ProjectFile>> result = new ArrayList<>();
		Collection<Clone> clones = basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE);
		Collection<FileCloneWithCoChange> clonesWithCoChange = null;
		try {
			clonesWithCoChange = cloneAnalyseService.addCoChangeToFileClones(clones);
		} catch (Exception e) {
			clonesWithCoChange = new ArrayList<>();
		}
		Map<Long, FileMetrics> fileMetrics = metricCalculatorService.calculateFileMetrics();
		for(FileCloneWithCoChange clone : clonesWithCoChange) {
			ProjectFile file1 = clone.getFile1();
			ProjectFile file2 = clone.getFile2();
			if(clone.getCochangeTimes() < minCoChange) {
				continue;
			}
			if(!moduleService.isInDifferentModule(file1, file2)) {
				continue;
			}
			SimilarComponents<ProjectFile> temp = new SimilarComponents<ProjectFile>(file1, file2, clone.getFileClone().getValue(), fileMetrics.get(file1.getId()).getEvolutionMetric().getCommits(), fileMetrics.get(file2.getId()).getEvolutionMetric().getCommits(), clone.getCochangeTimes());
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
			result.add(temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<SimilarComponents<Package>> packageSimilars() {
		List<HotspotPackagePair> hotspotPackagePairs = hotspotPackagePairDetector.detectHotspotPackagePairWithFileClone();
		return getPackageSimilars(hotspotPackagePairs);
	}

	public Collection<SimilarComponents<Package>> getPackageSimilars(List<HotspotPackagePair> hotspotPackagePairs) {
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
	
	private int minCoChange = 10;
	
}
