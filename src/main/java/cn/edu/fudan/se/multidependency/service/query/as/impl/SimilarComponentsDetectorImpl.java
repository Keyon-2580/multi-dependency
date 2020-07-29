package cn.edu.fudan.se.multidependency.service.query.as.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.as.SimilarComponentsDetector;
import cn.edu.fudan.se.multidependency.service.query.as.data.SimilarComponents;

@Service
public class SimilarComponentsDetectorImpl implements SimilarComponentsDetector {

	@Override
	public Collection<SimilarComponents<ProjectFile>> similarFiles() {
		return null;
	}

	@Override
	public Collection<SimilarComponents<Package>> similarPackages() {
		return null;
	}

}
