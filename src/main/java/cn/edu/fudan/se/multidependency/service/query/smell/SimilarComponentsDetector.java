package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.smell.data.SimilarComponents;

public interface SimilarComponentsDetector {

	Collection<SimilarComponents<ProjectFile>> fileSimilars();
	
	Collection<SimilarComponents<Package>> packageSimilars();
}
