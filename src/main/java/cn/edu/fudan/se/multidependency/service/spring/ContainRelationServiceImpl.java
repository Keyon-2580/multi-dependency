package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.repository.node.git.GitRepoRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;

@Service
public class ContainRelationServiceImpl implements ContainRelationService {
    
    @Autowired
    ContainRepository containRepository;
    
    @Autowired
    CacheService cache;
    
    @Autowired
    GitRepoRepository gitRepoRepository;
    
	Map<Project, Collection<ProjectFile>> projectContainFilesCache = new ConcurrentHashMap<>();
	@Override
	public Collection<ProjectFile> findProjectContainAllFiles(Project project) {
		Collection<ProjectFile> result = projectContainFilesCache.getOrDefault(project, containRepository.findProjectContainFiles(project.getId()));
		projectContainFilesCache.put(project, result);
		result.forEach(file -> {
			cache.cacheNodeBelongToNode(file, project);
		});
		return result;
	}
	
	Map<Project, Collection<Package>> projectContainPakcagesCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Package> findProjectContainPackages(Project project) {
		Collection<Package> result = projectContainPakcagesCache.getOrDefault(project, containRepository.findProjectContainPackages(project.getId()));
		projectContainPakcagesCache.put(project, result);
		result.forEach(pck -> {
			cache.cacheNodeBelongToNode(pck, project);
		});
		return result;
	}

	Map<Package, Collection<ProjectFile>> packageContainFilesCache = new ConcurrentHashMap<>();
	@Override
	public Collection<ProjectFile> findPackageContainFiles(Package pck) {
		Collection<ProjectFile> result = packageContainFilesCache.getOrDefault(pck, containRepository.findPackageContainFiles(pck.getId()));
		packageContainFilesCache.put(pck, result);
		result.forEach(file -> {
			cache.cacheNodeBelongToNode(file, pck);
		});
		return result;
	}
	
	Map<ProjectFile, Map<NodeLabelType, Collection<? extends Node>>> fileDirectlyContainNodesCache = new ConcurrentHashMap<>();
	private Collection<? extends Node> findFileDirectlyContainNodes(ProjectFile file, NodeLabelType nodeLabelType) {
		Map<NodeLabelType, Collection<? extends Node>> temp = fileDirectlyContainNodesCache.getOrDefault(file, new ConcurrentHashMap<>());
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
//			nodeBelongToProjectFileCache.put(node, file);
			cache.cacheNodeBelongToNode(node, file);
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
	
	Map<Type, Map<NodeLabelType, Collection<? extends Node>>> typeDirectlyContainNodesCache = new ConcurrentHashMap<>();
	private Collection<? extends Node> findTypeDirectlyContainNodes(Type type, NodeLabelType nodeLabelType) {
		Map<NodeLabelType, Collection<? extends Node>> temp = typeDirectlyContainNodesCache.getOrDefault(type, new ConcurrentHashMap<>());
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
//			nodeBelongToTypeCache.put(node, type);
			cache.cacheNodeBelongToNode(node, type);
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

	Map<Function, Collection<Variable>> functionContainVariablesCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Variable> findFunctionDirectlyContainVariables(Function function) {
		Collection<Variable> result = functionContainVariablesCache.getOrDefault(function, containRepository.findFunctionDirectlyContainVariables(function.getId()));
		functionContainVariablesCache.put(function, result);
		result.forEach(v -> {
//			nodeBelongToFunctionCache.put(v, function);
			cache.cacheNodeBelongToNode(v, function);
		});
		return result;
	}

	Map<Library, Collection<LibraryAPI>> libContainApisCache = new ConcurrentHashMap<>();
	@Override
	public Collection<LibraryAPI> findLibraryContainAPIs(Library lib) {
		Collection<LibraryAPI> result = libContainApisCache.getOrDefault(lib, containRepository.findLibraryContainLibraryAPIs(lib.getId()));
		libContainApisCache.put(lib, result);
		result.forEach(api -> {
//			nodeBelongToLibraryCache.put(api, lib);
			cache.cacheNodeBelongToNode(api, lib);
		});
		return result;
	}

	@Override
	public Project findPackageBelongToProject(Package pck) {
		Node belongToNode = cache.findNodeBelongToNode(pck, NodeLabelType.Project);
		Project result = belongToNode == null ? containRepository.findPackageBelongToProject(pck.getId()) : (Project) belongToNode;
		cache.cacheNodeBelongToNode(pck, result);
		return result;
	}

	@Override
	public Project findFileBelongToProject(ProjectFile file) {
		Package pck = findFileBelongToPackage(file);
		return pck == null ? null : findPackageBelongToProject(pck);
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
	
	Map<Namespace, Map<NodeLabelType, Collection<? extends Node>>> namespaceDirectlyContainNodesCache = new ConcurrentHashMap<>();
	private Collection<? extends Node> findNamespaceDirectlyContainNodes(Namespace namespace, NodeLabelType nodeLabelType) {
		Map<NodeLabelType, Collection<? extends Node>> temp = namespaceDirectlyContainNodesCache.getOrDefault(namespace, new ConcurrentHashMap<>());
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
			cache.cacheNodeBelongToNode(node, namespace);
		});
		return result;
	}
	
	@Override
	public Project findFunctionBelongToProject(Function function) {
		Node belongToNode = cache.findNodeBelongToNode(function, NodeLabelType.Project);
		if(belongToNode != null) {
			return (Project) belongToNode;
		}
		ProjectFile belongToFile = findFunctionBelongToFile(function);
		if(belongToFile == null) {
			return null;
		}
		Project project = findFileBelongToProject(belongToFile);
		cache.cacheNodeBelongToNode(function, project);
		return project;
	}
	
	private Map<Project, List<Function>> projectContainFunctionsCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Function> findProjectContainAllFunctions(Project project) {
		List<Function> functions = projectContainFunctionsCache.getOrDefault(project, containRepository.findProjectContainFunctions(project.getId()));
		projectContainFunctionsCache.put(project, functions);
		functions.forEach(f -> {
			cache.cacheNodeBelongToNode(f, project);
		});
		return functions;
	}
	
	@Override
	public ProjectFile findFunctionBelongToFile(Function function) {
		Node belongToNode = cache.findNodeBelongToNode(function, NodeLabelType.ProjectFile);
		ProjectFile result = belongToNode == null ? containRepository.findFunctionBelongToFile(function.getId()) : (ProjectFile) belongToNode;
		cache.cacheNodeBelongToNode(function, result);
		return result;
	}
	@Override
	public ProjectFile findTypeBelongToFile(Type type) {
		Node belongToNode = cache.findNodeBelongToNode(type, NodeLabelType.ProjectFile);
		ProjectFile result = belongToNode == null ? containRepository.findTypeBelongToFile(type.getId()) : (ProjectFile) belongToNode;
		cache.cacheNodeBelongToNode(type, result);
		return result;
	}
	@Override
	public ProjectFile findVariableBelongToFile(Variable variable) {
		Node belongToNode = cache.findNodeBelongToNode(variable, NodeLabelType.ProjectFile);
		ProjectFile result = belongToNode == null ? containRepository.findVariableBelongToFile(variable.getId()) : (ProjectFile) belongToNode;
		cache.cacheNodeBelongToNode(variable, result);
		return result;
	}

	@Override
	public Package findFileBelongToPackage(ProjectFile file) {
		Node belongToNode = cache.findNodeBelongToNode(file, NodeLabelType.Package);
		Package result = belongToNode == null ? containRepository.findFileBelongToPackage(file.getId()) : (Package) belongToNode;
		cache.cacheNodeBelongToNode(file, result);
		return result;
	}

	@Override
	public ProjectFile findSnippetBelongToFile(Snippet snippet) {
		Node belongToNode = cache.findNodeBelongToNode(snippet, NodeLabelType.ProjectFile);
		ProjectFile result = belongToNode == null ? containRepository.findSnippetBelongToFile(snippet.getId()) : (ProjectFile) belongToNode;
		cache.cacheNodeBelongToNode(snippet, result);
		return result;
	}

	@Override
	public Type findFunctionBelongToType(Function function) {
		Node belongToNode = cache.findNodeBelongToNode(function, NodeLabelType.Type);
		Type result = belongToNode == null ? containRepository.findFunctionBelongToType(function.getId()) : (Type) belongToNode;
		cache.cacheNodeBelongToNode(function, result);
		return result;
	}

	@Override
	public Type findVariableBelongToType(Variable variable) {
		Node belongToNode = cache.findNodeBelongToNode(variable, NodeLabelType.Type);
		Type result = belongToNode == null ? containRepository.findVariableBelongToType(variable.getId()) : (Type) belongToNode;
		cache.cacheNodeBelongToNode(variable, result);
		return result;
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

	Map<CloneGroup, Collection<ProjectFile>> groupContainFilesCache = new ConcurrentHashMap<>();
	@Override
	public Collection<ProjectFile> findCloneGroupContainFiles(CloneGroup group) {
		Collection<ProjectFile> result = groupContainFilesCache.get(group);
		if(result == null) {
			result = containRepository.findCloneGroupContainFiles(group.getId());
			groupContainFilesCache.put(group, result);
		}
		return result;
	}

	Map<CloneGroup, Collection<Function>> groupContainFunctionsCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Function> findCloneGroupContainFunctions(CloneGroup group) {
		Collection<Function> result = groupContainFunctionsCache.get(group);
		if(result == null) {
			result = containRepository.findCloneGroupContainFunctions(group.getId());
			groupContainFunctionsCache.put(group, result);
		}
		return result;
	}

	@Override
	public CloneGroup findFileBelongToCloneGroup(ProjectFile file) {
		Node node = cache.findNodeBelongToNode(file, NodeLabelType.CloneGroup);
		CloneGroup result = null;
		if(node == null) {
			result = containRepository.findFileBelongToCloneGroup(file.getId());
		} else {
			result = (CloneGroup) node;
		}
		if(result != null) {
			cache.cacheNodeBelongToNode(file, result);
		}
		return result;
	}

	@Override
	public CloneGroup findFunctionBelongToCloneGroup(Function function) {
		Node node = cache.findNodeBelongToNode(function, NodeLabelType.CloneGroup);
		CloneGroup result = null;
		if(node == null) {
			result = containRepository.findFunctionBelongToCloneGroup(function.getId());
		} else {
			result = (CloneGroup) node;
		}
		if(result != null) {
			cache.cacheNodeBelongToNode(function, result);
		}
		return result;
	}

	@Override
	public Project findCodeNodeBelongToProject(CodeNode node) {
		if(node instanceof ProjectFile) {
			return findFileBelongToProject((ProjectFile) node);
		} else if(node instanceof Type) {
			return findFileBelongToProject(findTypeBelongToFile((Type) node));
		} else if(node instanceof Function) {
			return findFunctionBelongToProject((Function) node);
		} else if(node instanceof Variable) {
			return findFileBelongToProject(findVariableBelongToFile((Variable) node));
		} else if(node instanceof Snippet) {
			return findFileBelongToProject(findSnippetBelongToFile((Snippet) node));
		}
		return null;
	}

	@Override
	public ProjectFile findCodeNodeBelongToFile(CodeNode node) {
		if(node instanceof ProjectFile) {
			return (ProjectFile) node;
		} else if(node instanceof Type) {
			return findTypeBelongToFile((Type) node);
		} else if(node instanceof Function) {
			return findFunctionBelongToFile((Function) node);
		} else if(node instanceof Variable) {
			return findVariableBelongToFile((Variable) node);
		} else if(node instanceof Snippet) {
			return findSnippetBelongToFile((Snippet) node);
		}
		return null;
	}

	@Override
	public boolean isDifferentPackage(ProjectFile file1, ProjectFile file2) {
		return findFileBelongToPackage(file1).equals(findFileBelongToPackage(file2));
	}

	@Override
	public GitRepository findCommitBelongToGitRepository(Commit commit) {
		Node belongToNode = cache.findNodeBelongToNode(commit, NodeLabelType.GitRepository);
		GitRepository result = belongToNode == null ? gitRepoRepository.findCommitBelongToGitRepository(commit.getId()) : (GitRepository) belongToNode;
		cache.cacheNodeBelongToNode(commit, result);
		cache.cacheNodeById(commit);
		cache.cacheNodeById(result);
		return result;
	}

}
