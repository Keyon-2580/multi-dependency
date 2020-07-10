package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;

public interface NodeService {
	Node queryNodeById(long id);
	
	Map<Long, Package> queryAllPackages();
	
	Package queryPackage(long id);

	ProjectFile queryFile(long id);
	
	ProjectFile queryFile(String path);
	
	List<ProjectFile> queryAllFiles();

	Namespace queryNamespace(long id);

	Type queryType(long id);

	Function queryFunction(long id);
	
	Project queryProject(long id);
	
	Project queryProject(String name, Language language);
	
	Collection<Project> queryProjects(Language language);
	
	CloneGroup queryCloneGroup(long id);

}
