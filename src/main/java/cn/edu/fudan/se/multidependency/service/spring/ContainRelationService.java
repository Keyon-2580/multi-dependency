package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
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
	
	Collection<Package> findProjectContainPackages(Project project);
	Collection<Function> findProjectContainAllFunctions(Project project);
	
	Collection<ProjectFile> findPackageContainFiles(Package pck);
	
	Collection<Namespace> findFileContainNamespaces(ProjectFile file);
	Collection<Type> findFileDirectlyContainTypes(ProjectFile file);
	Collection<Function> findFileDirectlyContainFunctions(ProjectFile file);
	Collection<Variable> findFileDirectlyContainVariables(ProjectFile file);
	
	Collection<Type> findNamespaceDirectlyContainTypes(Namespace namespace);
	Collection<Function> findNamespaceDirectlyContainFunctions(Namespace namespace);
	Collection<Variable> findNamespaceDirectlyContainVariables(Namespace namespace);
	
	Collection<Function> findTypeDirectlyContainFunctions(Type type);
	Collection<Variable> findTypeDirectlyContainFields(Type type);
	
	Collection<Variable> findFunctionDirectlyContainVariables(Function function);
	
	Package findTypeBelongToPackage(Type type);
	
	Package findFileBelongToPackage(ProjectFile file);
	
	Project findFileBelongToProject(ProjectFile file);
	
	Type findFunctionBelongToType(Function function);
	
	ProjectFile findFunctionBelongToFile(Function function);
	
	ProjectFile findTypeBelongToFile(Type type);
	
	ProjectFile findVariableBelongToFile(Variable variable);
	
	Project findFunctionBelongToProject(Function function);
	
	Project findPackageBelongToProject(Package pck);
	
	Library findAPIBelongToLibrary(LibraryAPI api);
	
	Iterable<LibraryAPI> findLibraryContainAPIs(Library lib);

	Collection<Project> findMicroServiceContainProjects(MicroService ms);
	
	Iterable<Function> findMicroServiceContainFunctions(MicroService ms);
	
	MicroService findProjectBelongToMicroService(Project project);
	
	List<RestfulAPI> findMicroServiceContainRestfulAPI(MicroService microService);
	
	List<Span> findTraceContainSpans(Trace trace);
	
	List<Contain> findAllFeatureContainFeatures();
	
}
