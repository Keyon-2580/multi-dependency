package cn.edu.fudan.se.multidependency.service.spring;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;

public interface NodeService {
	Package queryPackage(long id);

	ProjectFile queryFile(long id);

	Namespace queryNamespace(long id);

	Type queryType(long id);

	Function queryFunction(long id);
	
	Project queryProject(Long id);

}
