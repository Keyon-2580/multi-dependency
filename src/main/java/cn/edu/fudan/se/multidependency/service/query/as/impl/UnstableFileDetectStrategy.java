package cn.edu.fudan.se.multidependency.service.query.as.impl;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;

public interface UnstableFileDetectStrategy {
	
	UnstableFile isUnstableFile(Project project, FileMetrics metrics);
	
}
