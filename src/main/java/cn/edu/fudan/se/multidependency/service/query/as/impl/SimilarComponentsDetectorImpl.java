package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.as.SimilarComponentsDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.SimilarComponents;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.clone.PredicateForCloneGroup;
import cn.edu.fudan.se.multidependency.service.query.clone.PredicateForDataFile;

@Service
public class SimilarComponentsDetectorImpl implements SimilarComponentsDetector {
	
//	@Autowired
//	private BasicCloneQueryService basicCloneQueryService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private CloneAnalyseService cloneAnalyseService;
	
	@Override
	public Collection<SimilarComponents<ProjectFile>> similarFiles() {
		List<SimilarComponents<ProjectFile>> result = new ArrayList<>();

		CloneLevel cloneLevel = CloneLevel.file;
		PredicateForCloneGroup predicate = new PredicateForCloneGroup();
		predicate.addFilter(new PredicateForDataFile(staticAnalyseService));
		Collection<CloneGroup> groups = cloneAnalyseService.group(cloneLevel, predicate);
		for(CloneGroup group : groups) {
			SimilarComponents<ProjectFile> files = new SimilarComponents<>();
			for(CodeNode node : group.getNodes()) {
				files.add((ProjectFile) node);
			}
			result.add(files);
		}
		return result;
	}

	@Override
	public Collection<SimilarComponents<Package>> similarPackages() {
		List<SimilarComponents<Package>> result = new ArrayList<>();
		return result;
	}

}
