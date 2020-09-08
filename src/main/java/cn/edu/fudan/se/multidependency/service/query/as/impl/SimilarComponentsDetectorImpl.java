package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.as.ModuleService;
import cn.edu.fudan.se.multidependency.service.query.as.SimilarComponentsDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.SimilarComponents;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.clone.data.FileCloneWithCoChange;

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
	
	@Override
	public Collection<SimilarComponents<ProjectFile>> similarFiles() {
		String key = "similarFiles";
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
		for(FileCloneWithCoChange clone : clonesWithCoChange) {
			ProjectFile file1 = clone.getFile1();
			ProjectFile file2 = clone.getFile2();
			if(clone.getCochangeTimes() < minCoChange) {
				continue;
			}
			if(!moduleService.isInDifferentModule(file1, file2)) {
				continue;
			}
			SimilarComponents<ProjectFile> temp = new SimilarComponents<ProjectFile>(file1, file2, clone.getFileClone().getValue(), clone.getCochangeTimes());
			temp.setModule1(moduleService.findFileBelongToModule(file1));
			temp.setModule2(moduleService.findFileBelongToModule(file2));
			result.add(temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<SimilarComponents<Package>> similarPackages() {
		List<SimilarComponents<Package>> result = new ArrayList<>();
		return result;
	}
	
	private int minCoChange = 10;
	
}
