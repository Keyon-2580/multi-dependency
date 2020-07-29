package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.SimilarComponents;

public interface SimilarComponentsDetector {

	Collection<SimilarComponents<ProjectFile>> similarFiles();	
	
	Collection<SimilarComponents<Package>> similarPackages();	
}
