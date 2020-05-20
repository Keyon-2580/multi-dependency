package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
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
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;

@Service
public class ContainRelationServiceImpl implements ContainRelationService {
    
    @Autowired
    ContainRepository containRepository;
    
    private Map<Node, Project> nodeBelongToProjectCache = new HashMap<>();
    private Map<Node, Package> nodeBelongToPackageCache = new HashMap<>();
    private Map<Node, ProjectFile> nodeBelongToProjectFileCache = new HashMap<>();
    private Map<Node, Namespace> nodeBelongToNamespaceCache = new HashMap<>();
    private Map<Node, Type> nodeBelongToTypeCache = new HashMap<>();
    private Map<Node, Function> nodeBelongToFunctionCache = new HashMap<>();
    private Map<Node, Library> nodeBelongToLibrary = new HashMap<>();

	Map<Project, Collection<Package>> projectContainPakcagesCache = new HashMap<>();
	@Override
	public Collection<Package> findProjectContainPackages(Project project) {
		Collection<Package> result = projectContainPakcagesCache.getOrDefault(project, containRepository.findProjectContainPackages(project.getId()));
		projectContainPakcagesCache.put(project, result);
		result.forEach(pck -> {
			nodeBelongToProjectCache.put(pck, project);
		});
		return result;
	}

	Map<Package, Collection<ProjectFile>> packageContainFilesCache = new HashMap<>();
	@Override
	public Collection<ProjectFile> findPackageContainFiles(Package pck) {
		Collection<ProjectFile> result = packageContainFilesCache.getOrDefault(pck, containRepository.findPackageContainFiles(pck.getId()));
		packageContainFilesCache.put(pck, result);
		result.forEach(file -> {
			nodeBelongToPackageCache.put(file, pck);
		});
		return result;
	}
	
	Map<ProjectFile, Map<NodeLabelType, Collection<? extends Node>>> fileDirectlyContainNodesCache = new HashMap<>();
	private Collection<? extends Node> findFileDirectlyContainNodes(ProjectFile file, NodeLabelType nodeLabelType) {
		Map<NodeLabelType, Collection<? extends Node>> temp = fileDirectlyContainNodesCache.getOrDefault(file, new HashMap<>());
		Collection<? extends Node> result = temp.get(nodeLabelType);
		if(result != null) {
			return result;
		}
		switch(nodeLabelType) {
		case Namespace:
			result = containRepository.findFileDirectlyContainNamespaces(file.getId());
			break;
		case Type:
			result = containRepository.findFileDirectlyContainTypes(file.getId());
			break;
		case Function:
			result = containRepository.findFileDirectlyContainFunctions(file.getId());
			break;
		case Variable:
			result = containRepository.findFileDirectlyContainVariables(file.getId());
			break;
		default:
			return new ArrayList<>();
		}
		temp.put(nodeLabelType, result);
		fileDirectlyContainNodesCache.put(file, temp);
		result.forEach(node -> {
			nodeBelongToProjectFileCache.put(node, file);
		});
		return result;
	}

	@Override
	public Collection<Type> findFileDirectlyContainTypes(ProjectFile file) {
		return (Collection<Type>) findFileDirectlyContainNodes(file, NodeLabelType.Type);
	}

	@Override
	public Collection<Function> findFileDirectlyContainFunctions(ProjectFile file) {
		return (Collection<Function>) findFileDirectlyContainNodes(file, NodeLabelType.Function);
	}

	@Override
	public Collection<Namespace> findFileContainNamespaces(ProjectFile file) {
		return (Collection<Namespace>) findFileDirectlyContainNodes(file, NodeLabelType.Namespace);
	}

	@Override
	public Collection<Variable> findFileDirectlyContainVariables(ProjectFile file) {
		return (Collection<Variable>) findFileDirectlyContainNodes(file, NodeLabelType.Variable);
	}
	
	Map<Type, Map<NodeLabelType, Collection<? extends Node>>> typeDirectlyContainNodesCache = new HashMap<>();
	private Collection<? extends Node> findTypeDirectlyContainNodes(Type type, NodeLabelType nodeLabelType) {
		Map<NodeLabelType, Collection<? extends Node>> temp = typeDirectlyContainNodesCache.getOrDefault(type, new HashMap<>());
		Collection<? extends Node> result = temp.get(nodeLabelType);
		if(result != null) {
			return result;
		}
		switch(nodeLabelType) {
		case Function:
			result = containRepository.findTypeDirectlyContainFunctions(type.getId());
			break;
		case Variable:
			result = containRepository.findTypeDirectlyContainFields(type.getId());
			break;
		default:
			return new ArrayList<>();
		}
		temp.put(nodeLabelType, result);
		typeDirectlyContainNodesCache.put(type, temp);
		result.forEach(node -> {
			nodeBelongToTypeCache.put(node, type);
		});
		return result;
	}
	
	@Override
	public Collection<Function> findTypeDirectlyContainFunctions(Type type) {
		return (Collection<Function>) findTypeDirectlyContainNodes(type, NodeLabelType.Function);
	}

	@Override
	public Collection<Variable> findTypeDirectlyContainFields(Type type) {
		return (Collection<Variable>) findTypeDirectlyContainNodes(type, NodeLabelType.Variable);
	}

	Map<Function, Collection<Variable>> functionContainVariablesCache = new HashMap<>();
	@Override
	public Collection<Variable> findFunctionDirectlyContainVariables(Function function) {
		Collection<Variable> result = functionContainVariablesCache.getOrDefault(function, containRepository.findFunctionDirectlyContainVariables(function.getId()));
		functionContainVariablesCache.put(function, result);
		result.forEach(v -> {
			nodeBelongToFunctionCache.put(v, function);
		});
		return result;
	}

	Map<Library, Collection<LibraryAPI>> libContainApisCache = new HashMap<>();
	@Override
	public Collection<LibraryAPI> findLibraryContainAPIs(Library lib) {
		Collection<LibraryAPI> result = libContainApisCache.getOrDefault(lib, containRepository.findLibraryContainLibraryAPIs(lib.getId()));
		libContainApisCache.put(lib, result);
		result.forEach(api -> {
			nodeBelongToLibrary.put(api, lib);
		});
		return result;
	}

	@Override
	public Project findPackageBelongToProject(Package pck) {
		Project result = nodeBelongToProjectCache.getOrDefault(pck, containRepository.findPackageBelongToProject(pck.getId()));
		nodeBelongToProjectCache.put(pck, result);
		return result;
	}

	@Override
	public Project findFileBelongToProject(ProjectFile file) {
		return findPackageBelongToProject(findFileBelongToPackage(file));
	}

	@Override
	public Collection<Type> findNamespaceDirectlyContainTypes(Namespace namespace) {
		return (Collection<Type>) findNamespaceDirectlyContainNodes(namespace, NodeLabelType.Variable);
	}

	@Override
	public Collection<Function> findNamespaceDirectlyContainFunctions(Namespace namespace) {
		return (Collection<Function>) findNamespaceDirectlyContainNodes(namespace, NodeLabelType.Variable);
	}

	@Override
	public Collection<Variable> findNamespaceDirectlyContainVariables(Namespace namespace) {
		return (Collection<Variable>) findNamespaceDirectlyContainNodes(namespace, NodeLabelType.Variable);
	}
	
	Map<Namespace, Map<NodeLabelType, Collection<? extends Node>>> namespaceDirectlyContainNodesCache = new HashMap<>();
	private Collection<? extends Node> findNamespaceDirectlyContainNodes(Namespace namespace, NodeLabelType nodeLabelType) {
		Map<NodeLabelType, Collection<? extends Node>> temp = namespaceDirectlyContainNodesCache.getOrDefault(namespace, new HashMap<>());
		Collection<? extends Node> result = temp.get(nodeLabelType);
		if(result != null) {
			return result;
		}
		switch(nodeLabelType) {
		case Type:
			result = containRepository.findNamespaceDirectlyContainTypes(namespace.getId());
			break;
		case Function:
			result = containRepository.findNamespaceDirectlyContainFunctions(namespace.getId());
			break;
		case Variable:
			result = containRepository.findNamespaceDirectlyContainVariables(namespace.getId());
			break;
		default:
			return new ArrayList<>();
		}
		temp.put(nodeLabelType, result);
		namespaceDirectlyContainNodesCache.put(namespace, temp);
		result.forEach(node -> {
			nodeBelongToNamespaceCache.put(node, namespace);
		});
		return result;
	}
	
	@Override
	public Package findTypeBelongToPackage(Type type) {
		Package result = nodeBelongToPackageCache.getOrDefault(type, findFileBelongToPackage(findTypeBelongToFile(type)));
		nodeBelongToPackageCache.put(type, result);
		return result;
	}

	@Override
	public Project findFunctionBelongToProject(Function function) {
		Project project = nodeBelongToProjectCache.getOrDefault(function, findFileBelongToProject(findFunctionBelongToFile(function)));
		nodeBelongToProjectCache.put(function, project);
		return project;
	}
	
	private Map<Project, List<Function>> projectContainFunctionsCache = new HashMap<>();
	@Override
	public Collection<Function> findProjectContainAllFunctions(Project project) {
		List<Function> functions = projectContainFunctionsCache.getOrDefault(project, containRepository.findProjectContainFunctions(project.getId()));
		projectContainFunctionsCache.put(project, functions);
		functions.forEach(f -> {
			nodeBelongToProjectCache.put(f, project);
		});
		return functions;
	}
	
	@Override
	public ProjectFile findFunctionBelongToFile(Function function) {
		ProjectFile file = nodeBelongToProjectFileCache.getOrDefault(function, containRepository.findFunctionBelongToFile(function.getId()));
		nodeBelongToProjectFileCache.put(function, file);
		return file;
	}
	@Override
	public ProjectFile findTypeBelongToFile(Type type) {
		ProjectFile file = nodeBelongToProjectFileCache.getOrDefault(type, containRepository.findFunctionBelongToFile(type.getId()));
		nodeBelongToProjectFileCache.put(type, file);
		return file;
	}
	@Override
	public ProjectFile findVariableBelongToFile(Variable variable) {
		ProjectFile file = nodeBelongToProjectFileCache.getOrDefault(variable, containRepository.findFunctionBelongToFile(variable.getId()));
		nodeBelongToProjectFileCache.put(variable, file);
		return file;
	}

	@Override
	public Package findFileBelongToPackage(ProjectFile file) {
		Package pck = nodeBelongToPackageCache.getOrDefault(file, containRepository.findFileBelongToPackage(file.getId()));
		nodeBelongToPackageCache.put(file, pck);
		return pck;
	}

	@Override
	public Type findFunctionBelongToType(Function function) {
		Type type = nodeBelongToTypeCache.getOrDefault(function, containRepository.findFunctionBelongToType(function.getId()));
		nodeBelongToTypeCache.put(function, type);
		return type;
	}

	@Override
	public Library findAPIBelongToLibrary(LibraryAPI api) {
		return containRepository.findLibraryAPIBelongToLibrary(api.getId());
	}

	@Override
	public Collection<Project> findMicroServiceContainProjects(MicroService ms) {
		return containRepository.findMicroServiceContainProjects(ms.getId());
	}
	
	@Override
	public Iterable<Function> findMicroServiceContainFunctions(MicroService ms) {
		Iterable<Project> projects = findMicroServiceContainProjects(ms);
		List<Function> result = new ArrayList<>();
		for(Project project : projects) {
			for(Function function : findProjectContainAllFunctions(project)) {
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
