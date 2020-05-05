package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
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
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;

@Service
public class ContainRelationServiceImpl implements ContainRelationService {
    
    @Autowired
    ContainRepository containRepository;

	Map<Project, Iterable<Package>> projectContainPakcagesCache = new HashMap<>();
	@Override
	public Iterable<Package> findProjectContainPackages(Project project) {
		Iterable<Package> result = projectContainPakcagesCache.getOrDefault(project, containRepository.findProjectContainPackages(project.getId()));
		projectContainPakcagesCache.put(project, result);
		return result;
	}

	Map<Package, Iterable<ProjectFile>> packageContainFilesCache = new HashMap<>();
	@Override
	public Iterable<ProjectFile> findPackageContainFiles(Package pck) {
		Iterable<ProjectFile> result = packageContainFilesCache.getOrDefault(pck, containRepository.findPackageContainFiles(pck.getId()));
		packageContainFilesCache.put(pck, result);
		return result;
	}

	Map<ProjectFile, Iterable<Type>> fileContainTypesCache = new HashMap<>();
	@Override
	public Iterable<Type> findFileContainTypes(ProjectFile codeFile) {
		Iterable<Type> result = fileContainTypesCache.getOrDefault(codeFile, containRepository.findFileContainTypes(codeFile.getId()));
		fileContainTypesCache.put(codeFile, result);
		return result;
	}

	Map<ProjectFile, Iterable<Function>> fileContainFunctionsCache = new HashMap<>();
	@Override
	public Iterable<Function> findFileContainFunctions(ProjectFile codeFile) {
		Iterable<Function> result = fileContainFunctionsCache.getOrDefault(codeFile, containRepository.findFileContainFunctions(codeFile.getId()));
		fileContainFunctionsCache.put(codeFile, result);
		return result;
	}

	Map<Type, Iterable<Function>> typeContainFunctionsCache = new HashMap<>();
	@Override
	public Iterable<Function> findTypeContainFunctions(Type type) {
		Iterable<Function> result = typeContainFunctionsCache.getOrDefault(type, containRepository.findTypeContainFunctions(type.getId()));
		typeContainFunctionsCache.put(type, result);
		return result;
	}

	Map<Type, Iterable<Variable>> typeContainVariablesCache = new HashMap<>();
	@Override
	public Iterable<Variable> findTypeContainVariables(Type type) {
		Iterable<Variable> result = typeContainVariablesCache.getOrDefault(type, containRepository.findTypeContainVariables(type.getId()));
		typeContainVariablesCache.put(type, result);
		return result;
	}

	Map<Function, Iterable<Variable>> functionContainVariablesCache = new HashMap<>();
	@Override
	public Iterable<Variable> findFunctionContainVariables(Function function) {
		Iterable<Variable> result = functionContainVariablesCache.getOrDefault(function, containRepository.findFunctionContainVariables(function.getId()));
		functionContainVariablesCache.put(function, result);
		return result;
	}
	


	@Override
	public Package findTypeBelongToPackage(Type type) {
		return null;
	}

	@Override
	public Project findFunctionBelongToProject(Function function) {
		for(Project project : projectContainFunctionsCache.keySet()) {
			List<Function> functions = projectContainFunctionsCache.get(project);
			if(functions.contains(function)) {
				return project;
			}
		}
		Project project = containRepository.findFunctionBelongToProjectByFunctionId(function.getId());
		assert(project != null);
		findProjectContainFunctions(project);
		return project;
	}
	
	private Map<Project, List<Function>> projectContainFunctionsCache = new HashMap<>();
	@Override
	public Iterable<Function> findProjectContainFunctions(Project project) {
		List<Function> functions = projectContainFunctionsCache.get(project);
		if(functions == null) {
			functions = containRepository.findProjectContainFunctionsByProjectId(project.getId());
			projectContainFunctionsCache.put(project, functions);
		}
		return functions;
	}
	
	private Map<Node, ProjectFile> nodeBelongToFileCache = new HashMap<>();
	@Override
	public ProjectFile findFunctionBelongToFile(Function function) {
		ProjectFile file = nodeBelongToFileCache.get(function);
		if(file == null) {
			file = containRepository.findFunctionBelongToFileByFunctionId(function.getId());
			nodeBelongToFileCache.put(function, file);
		}
		return file;
	}
	@Override
	public ProjectFile findTypeBelongToFile(Type type) {
		ProjectFile file = nodeBelongToFileCache.get(type);
		if(file == null) {
			file = containRepository.findTypeBelongToFileByTypeId(type.getId());
			nodeBelongToFileCache.put(type, file);
		}
		return file;
	}
	@Override
	public ProjectFile findVariableBelongToFile(Variable variable) {
		ProjectFile file = nodeBelongToFileCache.get(variable);
		if(file == null) {
			file = containRepository.findVariableBelongToFileByVariableId(variable.getId());
			nodeBelongToFileCache.put(variable, file);
		}
		return file;
	}

	@Override
	public Package findFileBelongToPackage(ProjectFile file) {
		return containRepository.findFileBelongToPackageByFileId(file.getId());
	}

	private Map<Function, Type> functionBelongToTypeCache = new HashMap<>();
	@Override
	public Type findFunctionBelongToType(Function function) {
		Type type = functionBelongToTypeCache.get(function);
		if(type == null) {
			type = containRepository.findFunctionBelongToTypeByFunctionId(function.getId());
			functionBelongToTypeCache.put(function, type);
		}
		return type;
	}

	@Override
	public Library findAPIBelongToLibrary(LibraryAPI api) {
		return containRepository.findLibraryAPIBelongToLibrary(api.getId());
	}

	@Override
	public Iterable<Project> findMicroServiceContainProjects(MicroService ms) {
		return containRepository.findMicroServiceContainProjects(ms.getId());
	}
	
	@Override
	public Iterable<Function> findMicroServiceContainFunctions(MicroService ms) {
		Iterable<Project> projects = findMicroServiceContainProjects(ms);
		List<Function> result = new ArrayList<>();
		for(Project project : projects) {
			for(Function function : findProjectContainFunctions(project)) {
				result.add(function);
			}
		}
		return result;
	}
	
	@Override
	public List<RestfulAPI> findMicroServiceContainRestfulAPI(MicroService microService) {
		return containRepository.findMicroServiceContainRestfulAPI(microService.getId());
	}
	
	@Override
	public MicroService findProjectBelongToMicroService(Project project) {
		return containRepository.findProjectBelongToMicroService(project.getId());
	}

	@Override
	public List<Span> findTraceContainSpans(Trace trace) {
		List<Span> spans = containRepository.findTraceContainSpansByTraceId(trace.getTraceId());
		spans.sort(new Comparator<Span>() {
			@Override
			public int compare(Span o1, Span o2) {
				return o1.getOrder().compareTo(o2.getOrder());
			}
		});
		return spans;
	}

	@Override
	public List<Contain> findAllFeatureContainFeatures() {
		List<Contain> featureContainFeatures = containRepository.findAllFeatureContainFeatures();
		return featureContainFeatures;
	}
}
