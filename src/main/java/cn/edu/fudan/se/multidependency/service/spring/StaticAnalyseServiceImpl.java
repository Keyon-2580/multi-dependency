package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.lib.FunctionCallLibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportVariable;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileIncludeFile;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionAccessField;
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
import cn.edu.fudan.se.multidependency.repository.relation.code.FileImportFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileImportTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileImportVariableRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileIncludeFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionAccessFieldRepository;
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
import cn.edu.fudan.se.multidependency.service.spring.data.Fan_IO;
import cn.edu.fudan.se.multidependency.utils.PageUtil;

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
	FunctionAccessFieldRepository functionAccessFieldRepository;
	
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
    LibraryRepository libraryRepository;
    
    @Autowired
    ContainRelationService containRelationService;
    
    @Autowired
    CacheService cache;

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
    
	
	@Override
	public Collection<Type> findExtendsType(Type type) {
		return typeInheritsTypeRepository.findExtendsTypesByTypeId(type.getId());
	}

	@Override
	public Collection<Type> findInheritsType(Type type) {
		return typeInheritsTypeRepository.findInheritsFromTypeByTypeId(type.getId());
	}
	
	@Override
	public Collection<Type> findInheritsFromType(Type type) {
		return typeInheritsTypeRepository.findInheritsTypesByTypeId(type.getId());
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
	public List<FunctionAccessField> findProjectContainFunctionAccessVariableRelations(Project project) {
		return functionAccessFieldRepository.findProjectContainFunctionAccessFieldRelations(project.getId());
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

	@Override
	public Map<Function, List<FunctionCallFunction>> findAllFunctionCallRelationsGroupByCaller(Project project) {
		Iterable<FunctionCallFunction> allCalls = findFunctionCallFunctionRelations(project);
		Map<Function, List<FunctionCallFunction>> result = new HashMap<>();
		for(FunctionCallFunction call : allCalls) {
			Function caller = call.getFunction();
			List<FunctionCallFunction> group = result.getOrDefault(caller, new ArrayList<>());
			group.add(call);
			result.put(caller, group);
		}
		return result;
	}

	@Override
	public Map<Function, List<FunctionAccessField>> findAllFunctionAccessRelationsGroupByCaller(Project project) {
		Iterable<FunctionAccessField> allAccesses = findProjectContainFunctionAccessVariableRelations(project);
		Map<Function, List<FunctionAccessField>> result = new HashMap<>();
		for(FunctionAccessField access : allAccesses) {
			Function caller = access.getFunction();
			List<FunctionAccessField> group = result.getOrDefault(caller, new ArrayList<>());
			group.add(access);
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
	
	private Iterable<Library> cacheForAllLibraries = null;
	@Override
	public Iterable<Library> findAllLibraries() {
		if(cacheForAllLibraries == null) {
			cacheForAllLibraries = libraryRepository.findAll();
		}
		return cacheForAllLibraries;
	}

	private Map<Integer, List<Project>> pageProjectsCache = new ConcurrentHashMap<>();
	@Override
	public List<Project> queryAllProjectsByPage(int page, int size, String... sortByProperties) {
		List<Project> result = pageProjectsCache.get(page);
		if(result == null || result.size() == 0) {
			result = new ArrayList<>();
			Pageable pageable = PageUtil.generatePageable(page, size, sortByProperties);
			Page<Project> pageProjects = projectRepository.findAll(pageable);
			for(Project project : pageProjects) {
				result.add(project);
			}
			pageProjectsCache.put(page, result);
		}
		return result;
	}

	@Override
	public long countOfAllProjects() {
		return projectRepository.count();
	}

	Iterable<Project> allProjectsCache = null;
	@Override
	public Iterable<Project> allProjects() {
		allProjectsCache = allProjectsCache == null ? projectRepository.findAll() : allProjectsCache;
		for(Project project : allProjectsCache) {
			cache.cacheNodeById(project);
		}
		return allProjectsCache;
	}

	@Override
	public Fan_IO<ProjectFile> queryJavaFileFanIO(ProjectFile file) {
		Fan_IO<ProjectFile> result = new Fan_IO<ProjectFile>(file);
		Collection<Type> typesInFile = containRelationService.findFileDirectlyContainTypes(file);
		Collection<Function> functions = new ArrayList<>();
		Collection<Variable> variables = new ArrayList<>();
		for(Type type : typesInFile) {
			Collection<Function> functionsInType = containRelationService.findTypeDirectlyContainFunctions(type);
			functions.addAll(functionsInType);
			for(Function function : functions) {
				Collection<Variable> variablesInFunction = containRelationService.findFunctionDirectlyContainVariables(function);
				variables.addAll(variablesInFunction);
			}
			Collection<Variable> variablesInType = containRelationService.findTypeDirectlyContainFields(type);
			variables.addAll(variablesInType);
		}
		
		for(Type type : typesInFile) {
			Collection<Type> inherits = findInheritsType(type);
			for(Type inheritsType : inherits) {
				ProjectFile belongToFile = containRelationService.findTypeBelongToFile(inheritsType);
				if(!file.equals(belongToFile)) {
					result.addFanOut(belongToFile);
				}
			}
			inherits = findInheritsFromType(type);
			for(Type inheritsType : inherits) {
				ProjectFile belongToFile = containRelationService.findTypeBelongToFile(inheritsType);
				if(!file.equals(belongToFile)) {
					result.addFanIn(belongToFile);
				}
			}
		}
		
		for(Function function : functions) {
			Collection<FunctionCallFunction> calls = queryFunctionCallFunctions(function);
			for(FunctionCallFunction call : calls) {
				ProjectFile belongToFile = containRelationService.findFunctionBelongToFile(call.getCallFunction());
				if(!file.equals(belongToFile)) {
					result.addFanOut(belongToFile);
					result.addFanOutRelations(call);
				}
			}
			
			calls = queryFunctionCallByFunctions(function);
			for(FunctionCallFunction call : calls) {
				ProjectFile belongToFile = containRelationService.findFunctionBelongToFile(call.getFunction());
				if(!file.equals(belongToFile)) {
					result.addFanIn(belongToFile);
					result.addFanInRelations(call);
				}
			}
		}
		
		for(Variable variable : variables) {
			
		}
		
		
		return result;
	}

	@Override
	public List<Fan_IO<ProjectFile>> queryAllFileFanIOs(Project project) {
		List<Fan_IO<ProjectFile>> result = new ArrayList<>();
		Collection<ProjectFile> files = containRelationService.findProjectContainAllFiles(project);
		for(ProjectFile file : files) {
			Fan_IO<ProjectFile> fanIO = queryJavaFileFanIO(file);
			result.add(fanIO);
		}
		result.sort(new Comparator<Fan_IO<ProjectFile>>() {
			@Override
			public int compare(Fan_IO<ProjectFile> o1, Fan_IO<ProjectFile> o2) {
				if(o1.size() == o2.size()) {
					return o1.getNode().getName().compareTo(o2.getNode().getName());
				}
				return o2.size() - o1.size();
			}
		});
		return result;
	}

	@Override
	public Collection<FunctionCallFunction> queryFunctionCallFunctions(Function function) {
		return functionCallFunctionRepository.queryFunctionCallFunctions(function.getId());
	}

	@Override
	public Collection<FunctionCallFunction> queryFunctionCallByFunctions(Function function) {
		return functionCallFunctionRepository.queryFunctionCallByFunctions(function.getId());
	}
	
	@Override
	public boolean isDataClass(Type type) {
		Collection<Variable> fields = containRelationService.findTypeDirectlyContainFields(type);
		Collection<Function> functions = containRelationService.findTypeDirectlyContainFunctions(type);
		if(fields.isEmpty() && functions.isEmpty()) {
			return true;
		} else if(fields.isEmpty()) {
			return false;
		} else if(functions.isEmpty()) {
			return true;
		}
		for(Function f : functions) {
			if(f.isConstructor()) {
				continue;
			}
			if(!f.getSimpleName().startsWith("get") 
					&& !f.getSimpleName().startsWith("set") 
					&& !f.getSimpleName().startsWith("is")
					&& !"equals".equals(f.getSimpleName())
					&& !"toString".equals(f.getSimpleName())
					&& !"hashCode".equals(f.getSimpleName())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isDataFile(ProjectFile file) {
		if(!ProjectFile.SUFFIX_JAVA.equals(file.getSuffix())) {
			// 如果不是java文件，直接返回false
			return false;
		}
		Collection<Type> types = containRelationService.findFileDirectlyContainTypes(file);
		if(types.isEmpty()) {
			return false;
		}
		for(Type type : types) {
			if(!isDataClass(type)) {
				return false;
			}
		}
		return true;
	}

}
