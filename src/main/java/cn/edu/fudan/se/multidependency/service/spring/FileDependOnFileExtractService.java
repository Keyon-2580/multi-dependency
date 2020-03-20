package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FileDependOnFile;

public interface FileDependOnFileExtractService {

	void setProject(Long projectGraphId);
	
	void setProject(Project project);
	
	Map<ProjectFile, Map<ProjectFile, FileDependOnFile>> extractFileDependOnFiles() throws Exception;
	
	void save();
	
}
