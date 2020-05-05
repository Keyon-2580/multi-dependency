package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.Contain;

public interface ContainRelationService {
	
	Iterable<Package> findProjectContainPackages(Project project);
	
	Iterable<Function> findProjectContainFunctions(Project project);
	
	Iterable<ProjectFile> findPackageContainFiles(Package pck);
	
	Iterable<Type> findFileContainTypes(ProjectFile codeFile);
	
	Iterable<Variable> findTypeContainVariables(Type type);
	
	Iterable<Variable> findFunctionContainVariables(Function function);
	
	Iterable<Function> findFileContainFunctions(ProjectFile codeFile);
	
	Iterable<Function> findTypeContainFunctions(Type type);
	
	Package findTypeBelongToPackage(Type type);
	
	Package findFileBelongToPackage(ProjectFile file);
	
	Type findFunctionBelongToType(Function function);
	
	ProjectFile findFunctionBelongToFile(Function function);
	
	ProjectFile findTypeBelongToFile(Type type);
	
	ProjectFile findVariableBelongToFile(Variable variable);
	
	Project findFunctionBelongToProject(Function function);
	
	Library findAPIBelongToLibrary(LibraryAPI api);

	Iterable<Project> findMicroServiceContainProjects(MicroService ms);
	
	Iterable<Function> findMicroServiceContainFunctions(MicroService ms);
	
	MicroService findProjectBelongToMicroService(Project project);
	
	List<RestfulAPI> findMicroServiceContainRestfulAPI(MicroService microService);
	
	List<Span> findTraceContainSpans(Trace trace);
	
	List<Contain> findAllFeatureContainFeatures();
	
}
