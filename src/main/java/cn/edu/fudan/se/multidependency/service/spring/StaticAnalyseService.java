package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeExtendsType;

public interface StaticAnalyseService {
	
	public Map<Long, Project> findAllProjects();
	
	public List<Type> findAllTypes();
	
	public List<Type> findTypesInFile(ProjectFile codeFile);
	
	public List<Type> findExtendsType(Type type);
	
	public List<TypeExtendsType> findAllExtends();
	
	public List<Function> findAllFunctions();
	
	public Package findTypeInPackage(Type type);
	
	public Package findFileInPackage(ProjectFile file);
	
	public ProjectFile findFunctionBelongToCodeFile(Function function);
	
	public List<ProjectFile> findAllProjectFile();
	
}
