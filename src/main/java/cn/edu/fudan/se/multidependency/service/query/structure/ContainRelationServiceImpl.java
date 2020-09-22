package cn.edu.fudan.se.multidependency.service.query.structure;

import java.util.*;

import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;
import cn.edu.fudan.se.multidependency.model.relation.structure.Import;
import cn.edu.fudan.se.multidependency.model.relation.structure.Include;
import cn.edu.fudan.se.multidependency.repository.node.code.TypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.CallRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ImportRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.IncludeRepository;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
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
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.GitRepoRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;

@Service
public class ContainRelationServiceImpl implements ContainRelationService {
    
    @Autowired
    ContainRepository containRepository;
    
    @Autowired
    CacheService cache;
    
    @Autowired
    GitRepoRepository gitRepoRepository;
    
    @Autowired
    NodeService nodeService;
    
    @Autowired
    PackageRepository packageRepository;

    @Autowired
	CallRepository callRepository;

    @Autowired
	ImportRepository importRepository;

	@Autowired
	IncludeRepository includeRepository;

	@Autowired
	TypeRepository typeRepository;
    
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
		ProjectFile result = belongToNode == null ? containRepository.findSnippetDirectlyBelongToFile(snippet.getId()) : (ProjectFile) belongToNode;
		if(result == null) {
			Type type = findSnippetDirectlyBelongToType(snippet);
			if(type != null) {
				result = findTypeBelongToFile(type);
			} else {
				Function function = findSnippetDirectlyBelongToFunction(snippet);
				result = findFunctionBelongToFile(function);
			}
		}
		cache.cacheNodeBelongToNode(snippet, result);
		return result;
	}

	@Override
	public Type findSnippetDirectlyBelongToType(Snippet snippet) {
		Node belongToNode = cache.findNodeBelongToNode(snippet, NodeLabelType.Type);
		Type result = belongToNode == null ? containRepository.findSnippetDirectlyBelongToType(snippet.getId()) : (Type) belongToNode;
		cache.cacheNodeBelongToNode(snippet, result);
		return result;
	}

	@Override
	public Function findSnippetDirectlyBelongToFunction(Snippet snippet) {
		Node belongToNode = cache.findNodeBelongToNode(snippet, NodeLabelType.Function);
		Function result = belongToNode == null ? containRepository.findSnippetDirectlyBelongToFunction(snippet.getId()) : (Function) belongToNode;
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
	public Iterable<ProjectFile> findMicroServiceContainFiles(MicroService ms) {
		Iterable<Project> projects = findMicroServiceContainProjects(ms);
		List<ProjectFile> result = new ArrayList<>();
		for(Project project : projects) {
			for(ProjectFile file : findProjectContainAllFiles(project)) {
				result.add(file);
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
		cache.cacheNodeBelongToNode(file, result);
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

	@Override
	public Package findPackageInPackage(Package pck) {
		return nodeService.queryPackage(pck.lastPackageDirectoryPath());
	}

	@Override
	public Collection<Package> findPackageContainSubPackages(Package pck) {
		return packageRepository.findPackageContainSubPackages(pck.getDirectoryPath());
	}

	@Override
	public Collection<Call> findFunctionContainCalls(Function function) {
		return callRepository.findFunctionContainCalls(function.getId());
	}

	@Override
	public Collection<Type> findFileDirectlyImportTypes(ProjectFile file) {
		return importRepository.findFileImportTypes(file.getId());
	}

	@Override
	public Collection<Function> findFileDirectlyImportFunctions(ProjectFile file) {
		return importRepository.findFileImportFunctions(file.getId());
	}

	@Override
	public Collection<Variable> findFileDirectlyImportVariables(ProjectFile file) {
		return importRepository.findFileImportVariables(file.getId());
	}

	@Override
	public Collection<Import> findFileDirectlyImports(ProjectFile file) {
		return importRepository.findFileImports(file.getId());
	}

	@Override
	public Collection<Include> findFileDirectlyIncludes(ProjectFile file) {
		return includeRepository.findFileIncludes(file.getId());
	}


	public Map<RelationType, Collection<Relation>> findTypeStructureDependencyRelations(Type type) {
		Map<RelationType, Collection<Relation>> structureDependencyTypes = new ConcurrentHashMap<>();
		Collection<Relation> dependencyRelations = typeRepository.findTypeStructureDependencyRelations(type.getId());
		dependencyRelations.forEach( dependencyRelation -> {
			Collection<Relation> tmpDependencyRelations = structureDependencyTypes.getOrDefault(dependencyRelation.getRelationType(), new ArrayList<>());
			tmpDependencyRelations.add(dependencyRelation);
			structureDependencyTypes.put(dependencyRelation.getRelationType(),tmpDependencyRelations);
		});
		return structureDependencyTypes;
	}

	@Override
	public JSONObject doubleFileStructure(List<ProjectFile> fileList) {
		JSONObject result = new JSONObject();
		for(ProjectFile file: fileList){
			JSONArray result1 = new JSONArray();
			JSONObject temp1 = new JSONObject();
			temp1.put("name","#F: "+file.getPath());
			temp1.put("open",true);
			JSONArray fileChildren = new JSONArray();

			List<Import> fileImports = (List<Import>)findFileDirectlyImports(file);
			List<Include> fileIncludes = (List<Include>)findFileDirectlyIncludes(file);
			List<Type> containTypes = (List<Type>)findFileDirectlyContainTypes(file);

			if(fileImports != null && fileImports.size() > 0){
				fileImports.sort( (o1,o2) -> {
					int typeBool = o1.getEndCodeNode().getNodeType().compareTo(o2.getEndCodeNode().getNodeType());
					int nameBool = o1.getEndCodeNode().getName().compareTo(o2.getEndCodeNode().getName());
					return  typeBool != 0 ? typeBool : nameBool;
				});

				JSONObject temp2 = new JSONObject();
				temp2.put("name", "#R: " + "IMPORT(" + fileImports.size() + ")");
				temp2.put("open",true);
				JSONArray arraytemp2 = new JSONArray();
				for(Import fileImport: fileImports){
					JSONObject temp3 = new JSONObject();
					temp3.put("name", "#" + fileImport.getEndCodeNode().getNodeType().toString() + ": " + fileImport.getEndCodeNode().getName());
					arraytemp2.add(temp3);
				}
				temp2.put("children", arraytemp2);
				fileChildren.add(temp2);
			}

			if(fileIncludes != null && fileIncludes.size() > 0){
				fileIncludes.sort( (o1,o2) -> {
					int typeBool = o1.getEndCodeNode().getNodeType().compareTo(o2.getEndCodeNode().getNodeType());
					int nameBool = o1.getEndCodeNode().getName().compareTo(o2.getEndCodeNode().getName());
					return  typeBool != 0 ? typeBool : nameBool;
				});

				JSONObject temp2 = new JSONObject();
				temp2.put("name", "#R: " + "INCLUDE(" + fileIncludes.size() + ")");
				temp2.put("open",true);
				JSONArray arraytemp2 = new JSONArray();
				for(Include fileInclude: fileIncludes){
					JSONObject temp3 = new JSONObject();
					temp3.put("name", "#" + fileInclude.getEndCodeNode().getNodeType().toString() + ": " + fileInclude.getEndCodeNode().getName());
					arraytemp2.add(temp3);
				}
				temp2.put("children", arraytemp2);
				fileChildren.add(temp2);
			}

			if(containTypes != null && containTypes.size() > 0){
				containTypes.sort((o1,o2) -> {
					return  o1.getName().compareTo(o2.getName());
				});

				JSONObject temp2 = new JSONObject();
				temp2.put("name", "#R: CONTAIN(" + containTypes.size() + ")");
				temp2.put("open",true);
				JSONArray arraytemp2 = new JSONArray();
				for(Type type: containTypes){
					JSONObject temp3 = new JSONObject();
					temp3.put("name", "#T: " + type.getName());
					temp3.put("open",true);
					JSONArray arraytemp3 = new JSONArray();

					List<Variable> containVariables = (List<Variable>)findTypeDirectlyContainFields(type);
					containVariables.sort((o1,o2) -> {
						return  o1.getName().compareTo(o2.getName());
					});

					if(containVariables != null && containVariables.size() > 0){
						JSONObject temp4 = new JSONObject();
						temp4.put("name", "#R: CONTAIN(" + containVariables.size() + ")");
						temp4.put("open",true);
						JSONArray arraytemp4 = new JSONArray();
						for(Variable variable: containVariables){
							JSONObject temp5 = new JSONObject();
							temp5.put("name","#V: " + variable.getName());
							arraytemp4.add(temp5);
						}
						temp4.put("children",arraytemp4);
						arraytemp3.add(temp4);
					}
					List<Function> containFunctions = (List<Function>)findTypeDirectlyContainFunctions(type);
					containFunctions.sort((o1,o2) -> {
						return  o1.getName().compareTo(o2.getName());
					});
					if(containFunctions != null && containFunctions.size() > 0){
						JSONObject temp4 = new JSONObject();
						temp4.put("name", "#R: CONTAIN(" + containFunctions.size() + ")");
						temp4.put("open",true);
						JSONArray arraytemp4 = new JSONArray();
						for(Function variable: containFunctions){
							JSONObject temp5 = new JSONObject();
							temp5.put("name","#M: " + variable.getName());
							arraytemp4.add(temp5);
						}
						temp4.put("children",arraytemp4);
						arraytemp3.add(temp4);
					}
					Map<RelationType, Collection<Relation>> dependencyRelations = findTypeStructureDependencyRelations(type);
					Set<RelationType> relationTypeList = dependencyRelations.keySet();
					if(relationTypeList != null && !relationTypeList.isEmpty()){
						for(RelationType relationType : relationTypeList){
							JSONObject temp4 = new JSONObject();
							temp4.put("name", "#R: " + relationType + "(" + dependencyRelations.get(relationType).size() + ")");
							temp4.put("open",true);
							JSONArray arraytemp4 = new JSONArray();

							List<Relation> relationList = (List<Relation>)dependencyRelations.get(relationType);
							relationList.sort( (o1,o2) -> {
								int typeBool = o1.getEndNode().getNodeType().compareTo(o2.getEndNode().getNodeType());
								int nameBool = o1.getEndNode().getName().compareTo(o2.getEndNode().getName());
								return  typeBool != 0 ? typeBool : nameBool;
							});

							for(Relation relation : relationList){
								JSONObject temp5 = new JSONObject();
								temp5.put("name","#" + relation.getEndNode().getNodeType().toString().substring(0,1) + ": "+ relation.getEndNode().getName());
								arraytemp4.add(temp5);
							}
							temp4.put("children",arraytemp4);
							arraytemp3.add(temp4);
						}
						temp3.put("children",arraytemp3);
						arraytemp2.add(temp3);
					}
				}
				temp2.put("children", arraytemp2);
				fileChildren.add(temp2);
			}

			temp1.put("children",fileChildren);
			result1.add(temp1);
			result.put(file.getId().toString(),result1);
		}
		return result;
	}
}
