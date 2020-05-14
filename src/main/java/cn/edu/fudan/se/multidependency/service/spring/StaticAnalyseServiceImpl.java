package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.lib.FunctionCallLibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportVariable;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileIncludeFile;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCastType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionParameterType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionReturnType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionThrowType;
import cn.edu.fudan.se.multidependency.model.relation.structure.NodeAnnotationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.TypeCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.TypeInheritsType;
import cn.edu.fudan.se.multidependency.model.relation.structure.VariableIsType;
import cn.edu.fudan.se.multidependency.model.relation.structure.VariableTypeParameterType;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.FunctionRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.NamespaceRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.TypeRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.VariableRepository;
import cn.edu.fudan.se.multidependency.repository.node.lib.LibraryRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.FunctionCloneFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileImportFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileImportTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileImportVariableRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileIncludeFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionCastTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionParameterTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionReturnTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionThrowTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.NodeAnnotationTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.TypeCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.TypeInheritsTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.VariableIsTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.VariableTypeParameterTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.lib.FunctionCallLibraryAPIRepository;

/**
 * 
 * @author fan
 *
 */
@Service
public class StaticAnalyseServiceImpl implements StaticAnalyseService {
	
	
	@Autowired
	ProjectFileRepository fileRepository;
	
	@Autowired
	FileIncludeFileRepository fileIncludeFileRepository;
	
	@Autowired
	FileImportTypeRepository fileImportTypeRepository;
	@Autowired
	FileImportFunctionRepository fileImportFunctionRepository;
	@Autowired
	FileImportVariableRepository fileImportVariableRepository;
	
	@Autowired
	FunctionCallFunctionRepository functionCallFunctionRepository;
	
	@Autowired
	TypeCallFunctionRepository typeCallFunctionRepository;
	
	@Autowired
	FunctionRepository functionRepository;
	
	@Autowired
	FunctionDynamicCallFunctionRepository functionDynamicCallFunctionRepository;
	
	@Autowired
	FunctionReturnTypeRepository functionReturnTypeRepository;
	
	@Autowired
	FunctionParameterTypeRepository functionParameterTypeRepository;
	
	@Autowired
	NamespaceRepository namespaceRepository;
	
	@Autowired
    PackageRepository packageRepository;

    @Autowired
    ProjectRepository projectRepository;
    
    @Autowired
    TypeRepository typeRepository;
    
    @Autowired
    TypeInheritsTypeRepository typeInheritsTypeRepository;

    @Autowired
    VariableIsTypeRepository variableIsTypeRepository;
    
    @Autowired
    VariableRepository variableRepository;
    
    @Autowired
    FunctionCastTypeRepository functionCastTypeRepository;
    
    @Autowired
    FunctionThrowTypeRepository functionThrowTypeRepository;
    
    @Autowired
    NodeAnnotationTypeRepository nodeAnnotationTypeRepository;
    
    @Autowired
    VariableTypeParameterTypeRepository variableTypeParameterTypeRepository;
    
    @Autowired
    FunctionCallLibraryAPIRepository functionCallLibraryAPIRepository;
    
    @Autowired
    FunctionCloneFunctionRepository functionCloneFunctionRepository;
    
    @Autowired
    LibraryRepository libraryRepository;
    
    @Autowired
    ContainRelationService containRelationService;

	@Override
	public CallLibrary<Project> findProjectCallLibraries(Project project) {
		CallLibrary<Project> result = new CallLibrary<Project>();
		result.setCaller(project);
		Map<Function, List<FunctionCallLibraryAPI>> functionCallLibAPIs = findAllFunctionCallLibraryAPIs();
		Iterable<Function> functions = containRelationService.findProjectContainAllFunctions(project);
		for(Function function : functions) {
			List<FunctionCallLibraryAPI> calls = functionCallLibAPIs.getOrDefault(function, new ArrayList<>());
			for(FunctionCallLibraryAPI call : calls) {
				LibraryAPI api = call.getApi();
				Library lib = containRelationService.findAPIBelongToLibrary(api);
				result.addLibraryAPI(api, lib, call.getTimes());
			}
		}
		return result;
	}
    
    private Map<Long, Type> typesCache = new HashMap<>();
	@Override
	public Map<Long, Type> findTypes() {
		if(typesCache == null || typesCache.isEmpty()) {
			typesCache = new HashMap<>();
			Iterable<Type> allTypes = typeRepository.findAll();
			for(Type type : allTypes) {
				typesCache.put(type.getId(), type);
			}
		}
		return typesCache;
	}
	
	@Override
	public List<Type> findExtendsType(Type type) {
		return typeInheritsTypeRepository.findExtendsTypesByTypeId(type.getId());
	}

	@Override
	public List<Function> allFunctions() {
		List<Function> functions = new ArrayList<>();
		functionRepository.findAll().forEach(function -> {
			functions.add(function);
		});
		return functions;
	}

	private Map<Long, ProjectFile> allFilesCache = new HashMap<>();
	@Override
	public Map<Long, ProjectFile> allFiles() {
		if(allFilesCache == null || allFilesCache.isEmpty()) {
			allFilesCache = new HashMap<>();
			Iterable<ProjectFile> files = fileRepository.findAll();
			for(ProjectFile file : files) {
				allFilesCache.put(file.getId(), file);
			}
		}
		return allFilesCache;
	}

	private Map<Long, Project> projectsCache = new HashMap<>();
	@Override
	public Map<Long, Project> allProjects() {
		if(projectsCache.size() == 0) {
			for(Project project : projectRepository.findAll()) {
				projectsCache.put(project.getId(), project);
			}
		}
		return projectsCache;
	}

	@Override
	public Project findProject(Long id) {
		return allProjects().get(id);
	}

	@Override
	public List<FileImportType> findProjectContainFileImportTypeRelations(Project project) {
		return fileImportTypeRepository.findProjectContainFileImportTypeRelations(project.getId());
	}

	@Override
	public List<FileImportFunction> findProjectContainFileImportFunctionRelations(Project project) {
		return fileImportFunctionRepository.findProjectContainFileImportFunctionRelations(project.getId());
	}

	@Override
	public List<FileImportVariable> findProjectContainFileImportVariableRelations(Project project) {
		return fileImportVariableRepository.findProjectContainFileImportVariableRelations(project.getId());
	}

	@Override
	public List<FunctionCallFunction> findFunctionCallFunctionRelations(Project project) {
		return functionCallFunctionRepository.findProjectContainFunctionCallFunctionRelations(project.getId());
	}

	@Override
	public List<TypeInheritsType> findProjectContainInheritsRelations(Project project) {
		return typeInheritsTypeRepository.findProjectContainTypeInheritsTypeRelations(project.getId());
	}

	@Override
	public List<TypeCallFunction> findProjectContainTypeCallFunctions(Project project) {
		return typeCallFunctionRepository.findProjectContainTypeCallFunctionRelations(project.getId());
	}

	@Override
	public List<FunctionCastType> findProjectContainFunctionCastTypeRelations(Project project) {
		return functionCastTypeRepository.findProjectContainFunctionCastTypeRelations(project.getId());
	}

	@Override
	public List<FunctionParameterType> findProjectContainFunctionParameterTypeRelations(Project project) {
		return functionParameterTypeRepository.findProjectContainFunctionParameterTypeRelations(project.getId());
	}

	@Override
	public List<FunctionReturnType> findProjectContainFunctionReturnTypeRelations(Project project) {
		return functionReturnTypeRepository.findProjectContainFunctionReturnTypeRelations(project.getId());
	}

	@Override
	public List<FunctionThrowType> findProjectContainFunctionThrowTypeRelations(Project project) {
		return functionThrowTypeRepository.findProjectContainFunctionThrowTypeRelations(project.getId());
	}

	@Override
	public List<NodeAnnotationType> findProjectContainNodeAnnotationTypeRelations(Project project) {
		return nodeAnnotationTypeRepository.findProjectContainNodeAnnotationTypeRelations(project.getId());
	}

	@Override
	public List<VariableIsType> findProjectContainVariableIsTypeRelations(Project project) {
		return variableIsTypeRepository.findProjectContainVariableIsTypeRelations(project.getId());
	}

	@Override
	public List<VariableTypeParameterType> findProjectContainVariableTypeParameterTypeRelations(Project project) {
		return variableTypeParameterTypeRepository.findProjectContainVariableTypeParameterTypeRelations(project.getId());
	}

	@Override
	public List<FileIncludeFile> findProjectContainFileIncludeFileRelations(Project project) {
		return fileIncludeFileRepository.findProjectContainFileIncludeFileRelations(project.getId());
	}

	@Override
	public Iterable<TypeInheritsType> findAllInheritsRelations() {
		return typeInheritsTypeRepository.findAll();
	}

	@Override
	public Iterable<FileIncludeFile> findAllFileIncludeFileRelations() {
		return fileIncludeFileRepository.findAll();
	}

	@Override
	public Iterable<FileImportType> findAllFileImportTypeRelations() {
		return fileImportTypeRepository.findAll();
	}

	@Override
	public Iterable<FileImportFunction> findAllFileImportFunctionRelations() {
		return fileImportFunctionRepository.findAll();
	}

	@Override
	public Iterable<FileImportVariable> findAllFileImportVariableRelations() {
		return fileImportVariableRepository.findAll();
	}

	private Iterable<FunctionCallFunction> functionCallFunctionCache = null;
	@Override
	public Iterable<FunctionCallFunction> findAllFunctionCallFunctionRelations() {
		if(functionCallFunctionCache == null) {
			functionCallFunctionCache = functionCallFunctionRepository.findAll();
		}
		return functionCallFunctionCache;
	}

	@Override
	public Iterable<TypeCallFunction> findAllTypeCallFunctions() {
		return typeCallFunctionRepository.findAll();
	}

	@Override
	public Iterable<FunctionCastType> findAllFunctionCastTypeRelations() {
		return functionCastTypeRepository.findAll();
	}

	@Override
	public Iterable<FunctionParameterType> findAllFunctionParameterTypeRelations() {
		return functionParameterTypeRepository.findAll();
	}

	@Override
	public Iterable<FunctionReturnType> findAllFunctionReturnTypeRelations() {
		return functionReturnTypeRepository.findAll();
	}

	@Override
	public Iterable<FunctionThrowType> findAllFunctionThrowTypeRelations() {
		return functionThrowTypeRepository.findAll();
	}

	@Override
	public Iterable<NodeAnnotationType> findAllNodeAnnotationTypeRelations() {
		return nodeAnnotationTypeRepository.findAll();
	}

	@Override
	public Iterable<VariableIsType> findAllVariableIsTypeRelations() {
		return variableIsTypeRepository.findAll();
	}

	@Override
	public Iterable<VariableTypeParameterType> findAllVariableTypeParameterTypeRelations() {
		return variableTypeParameterTypeRepository.findAll();
	}

	@Override
	public Map<Function, List<FunctionCallFunction>> findAllFunctionCallRelationsGroupByCaller() {
		Iterable<FunctionCallFunction> allCalls = findAllFunctionCallFunctionRelations();
		Map<Function, List<FunctionCallFunction>> result = new HashMap<>();
		for(FunctionCallFunction call : allCalls) {
			Function caller = call.getFunction();
			List<FunctionCallFunction> group = result.getOrDefault(caller, new ArrayList<>());
			group.add(call);
			result.put(caller, group);
		}
		return result;
	}

	private Map<Type, Map<Type, Boolean>> subTypeCache = new HashMap<>();
	@Override
	public boolean isSubType(Type subType, Type superType) {
		Map<Type, Boolean> superTypeMap = subTypeCache.getOrDefault(subType, new HashMap<>());
		Boolean is = superTypeMap.get(superType);
		if(is == null) {
			Type queryType = typeInheritsTypeRepository.findIsTypeInheritsType(subType.getId(), superType.getId());
			is = queryType != null;
			superTypeMap.put(superType, is);
		}
		subTypeCache.put(subType, superTypeMap);
		return is;
	}


	Map<Function, List<FunctionCallLibraryAPI>> allFunctionCallLibraryAPIsCache = new ConcurrentHashMap<>();
	@Override
	public Map<Function, List<FunctionCallLibraryAPI>> findAllFunctionCallLibraryAPIs() {
		if(allFunctionCallLibraryAPIsCache.isEmpty()) {
			Iterable<FunctionCallLibraryAPI> calls = functionCallLibraryAPIRepository.findAll();
			for(FunctionCallLibraryAPI call : calls) {
				Function function = call.getFunction();
				List<FunctionCallLibraryAPI> temp = allFunctionCallLibraryAPIsCache.getOrDefault(function, new ArrayList<>());
				temp.add(call);
				allFunctionCallLibraryAPIsCache.put(function, temp);
			}
		}
		return allFunctionCallLibraryAPIsCache;
	}
	
	private Clone<Project> hasClone(Map<Project, Map<Project, Clone<Project>>> projectToProjectClones, Project project1, Project project2) {
		Map<Project, Clone<Project>> project1ToClones = projectToProjectClones.getOrDefault(project1, new HashMap<>());
		Clone<Project> clone = project1ToClones.get(project2);
		if(clone != null) {
			return clone;
		}
		Map<Project, Clone<Project>> project2ToClones = projectToProjectClones.getOrDefault(project2, new HashMap<>());
		clone = project2ToClones.get(project1);
		return clone;
	}

	@Override
	public Iterable<Clone<Project>> findProjectClone(Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode) {
		List<Clone<Project>> result = new ArrayList<>();
		Map<Project, Map<Project, Clone<Project>>> projectToProjectClones = new HashMap<>();
		for(FunctionCloneFunction functionCloneFunction : functionClones) {
			Function function1 = functionCloneFunction.getFunction1();
			Function function2 = functionCloneFunction.getFunction2();
			if(function1.equals(function2)) {
				continue;
			}
			Project project1 = containRelationService.findFunctionBelongToProject(function1);
			Project project2 = containRelationService.findFunctionBelongToProject(function2);
			if(removeSameNode && project1.equals(project2)) {
				continue;
			}
			Clone<Project> clone = hasClone(projectToProjectClones, project1, project2);
			if(clone == null) {
				clone = new Clone<Project>();
				clone.setNode1(project1);
				clone.setNode2(project2);
				result.add(clone);
			}
			// 函数间的克隆作为Children
			clone.addChild(functionCloneFunction);
			
			Map<Project, Clone<Project>> project1ToClones = projectToProjectClones.getOrDefault(project1, new HashMap<>());
			project1ToClones.put(project2, clone);
			projectToProjectClones.put(project1, project1ToClones);
		}
		return result;
	}

	
	private Iterable<FunctionCloneFunction> allClonesCache = null;
	@Override
	public Iterable<FunctionCloneFunction> findAllFunctionCloneFunctions() {
		if(allClonesCache == null) {
			allClonesCache = functionCloneFunctionRepository.findAll();
		}
		return allClonesCache;
	}

	@Override
	public Iterable<FunctionCloneFunction> findProjectContainFunctionCloneFunctions(Project project) {
//		List<FunctionCloneFunction> result = functionCloneFunctionRepository.findProjectContainFunctionCloneFunctionRelations(project.getId());
		Iterable<FunctionCloneFunction> allClones = findAllFunctionCloneFunctions();
		List<FunctionCloneFunction> result = new ArrayList<>();
		for(FunctionCloneFunction clone : allClones) {
			if(containRelationService.findFunctionBelongToProject(clone.getFunction1()).equals(project)
					&& containRelationService.findFunctionBelongToProject(clone.getFunction2()).equals(project)) {
				result.add(clone);
			}
		}
		
		return result;
	}
	
	private Iterable<Library> cacheForAllLibraries = null;
	@Override
	public Iterable<Library> findAllLibraries() {
		if(cacheForAllLibraries == null) {
			cacheForAllLibraries = libraryRepository.findAll();
		}
		return cacheForAllLibraries;
	}
	
}
