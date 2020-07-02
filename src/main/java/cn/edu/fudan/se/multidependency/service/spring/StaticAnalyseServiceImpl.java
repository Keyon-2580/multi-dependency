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
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.lib.FunctionCallLibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.structure.Access;
import cn.edu.fudan.se.multidependency.model.relation.structure.Annotation;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;
import cn.edu.fudan.se.multidependency.model.relation.structure.Cast;
import cn.edu.fudan.se.multidependency.model.relation.structure.Import;
import cn.edu.fudan.se.multidependency.model.relation.structure.Include;
import cn.edu.fudan.se.multidependency.model.relation.structure.Inherits;
import cn.edu.fudan.se.multidependency.model.relation.structure.Parameter;
import cn.edu.fudan.se.multidependency.model.relation.structure.Return;
import cn.edu.fudan.se.multidependency.model.relation.structure.Throw;
import cn.edu.fudan.se.multidependency.model.relation.structure.VariableType;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.FunctionRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.NamespaceRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.TypeRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.VariableRepository;
import cn.edu.fudan.se.multidependency.repository.node.lib.LibraryRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.AccessRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.AnnotationRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.CallRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.CastRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ImportRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.IncludeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.InheritsRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ParameterRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ReturnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ThrowRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.VariableTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.lib.FunctionCallLibraryAPIRepository;
import cn.edu.fudan.se.multidependency.service.spring.metric.Fan_IO;
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
	IncludeRepository fileIncludeFileRepository;
	
	@Autowired
	ImportRepository importRepository;
	
	@Autowired
	CallRepository callRepository;
	
	@Autowired
	FunctionRepository functionRepository;
	
	@Autowired
	AccessRepository functionAccessFieldRepository;
	
	@Autowired
	FunctionDynamicCallFunctionRepository functionDynamicCallFunctionRepository;
	
	@Autowired
	ReturnRepository functionReturnTypeRepository;
	
	@Autowired
	ParameterRepository parameterRepository;
	
	@Autowired
	NamespaceRepository namespaceRepository;
	
	@Autowired
    PackageRepository packageRepository;

    @Autowired
    ProjectRepository projectRepository;
    
    @Autowired
    TypeRepository typeRepository;
    
    @Autowired
    InheritsRepository typeInheritsTypeRepository;

    @Autowired
    VariableTypeRepository variableIsTypeRepository;
    
    @Autowired
    VariableRepository variableRepository;
    
    @Autowired
    CastRepository functionCastTypeRepository;
    
    @Autowired
    ThrowRepository functionThrowTypeRepository;
    
    @Autowired
    AnnotationRepository nodeAnnotationTypeRepository;
    
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
	public List<Import> findProjectContainImportRelations(Project project) {
		return importRepository.findProjectContainImportRelations(project.getId());
	}
	
	@Override
	public List<Import> findProjectContainFileImportTypeRelations(Project project) {
		return importRepository.findProjectContainFileImportTypeRelations(project.getId());
	}

	@Override
	public List<Import> findProjectContainFileImportFunctionRelations(Project project) {
		return importRepository.findProjectContainFileImportFunctionRelations(project.getId());
	}

	@Override
	public List<Import> findProjectContainFileImportVariableRelations(Project project) {
		return importRepository.findProjectContainFileImportVariableRelations(project.getId());
	}

	@Override
	public List<Call> findFunctionCallFunctionRelations(Project project) {
		return callRepository.findProjectContainFunctionCallFunctionRelations(project.getId());
	}

	@Override
	public List<Inherits> findProjectContainInheritsRelations(Project project) {
		return typeInheritsTypeRepository.findProjectContainTypeInheritsTypeRelations(project.getId());
	}

	@Override
	public List<Call> findProjectContainTypeCallFunctions(Project project) {
		return callRepository.findProjectContainTypeCallFunctionRelations(project.getId());
	}

	@Override
	public List<Cast> findProjectContainFunctionCastTypeRelations(Project project) {
		return functionCastTypeRepository.findProjectContainFunctionCastTypeRelations(project.getId());
	}
	
	@Override
	public List<Parameter> findProjectContainParameterRelations(Project project) {
		return parameterRepository.findProjectContainParameterRelations(project.getId());
	}

	@Override
	public List<Parameter> findProjectContainFunctionParameterTypeRelations(Project project) {
		return parameterRepository.findProjectContainFunctionParameterTypeRelations(project.getId());
	}

	@Override
	public List<Parameter> findProjectContainVariableTypeParameterTypeRelations(Project project) {
		return parameterRepository.findProjectContainVariableTypeParameterTypeRelations(project.getId());
	}

	@Override
	public List<Return> findProjectContainFunctionReturnTypeRelations(Project project) {
		return functionReturnTypeRepository.findProjectContainFunctionReturnTypeRelations(project.getId());
	}

	@Override
	public List<Throw> findProjectContainFunctionThrowTypeRelations(Project project) {
		return functionThrowTypeRepository.findProjectContainFunctionThrowTypeRelations(project.getId());
	}

	@Override
	public List<Annotation> findProjectContainNodeAnnotationTypeRelations(Project project) {
		return nodeAnnotationTypeRepository.findProjectContainNodeAnnotationTypeRelations(project.getId());
	}

	@Override
	public List<VariableType> findProjectContainVariableIsTypeRelations(Project project) {
		return variableIsTypeRepository.findProjectContainVariableIsTypeRelations(project.getId());
	}

	@Override
	public List<Include> findProjectContainFileIncludeFileRelations(Project project) {
		return fileIncludeFileRepository.findProjectContainFileIncludeFileRelations(project.getId());
	}
	
	@Override
	public List<Access> findProjectContainFunctionAccessVariableRelations(Project project) {
		return functionAccessFieldRepository.findProjectContainFunctionAccessFieldRelations(project.getId());
	}

	@Override
	public Map<Function, List<Call>> findAllFunctionCallRelationsGroupByCaller() {
		List<Call> allCalls = findAllFunctionCallFunctionRelations();
		Map<Function, List<Call>> result = new HashMap<>();
		for(Call call : allCalls) {
			CodeNode callerNode = call.getCallerNode();
			if(!(callerNode instanceof Function)) {
				continue;
			}
			Function caller = (Function) callerNode;
			List<Call> group = result.getOrDefault(caller, new ArrayList<>());
			group.add(call);
			result.put(caller, group);
		}
		return result;
	}

	@Override
	public Map<Function, List<Call>> findAllFunctionCallRelationsGroupByCaller(Project project) {
		Iterable<Call> allCalls = findFunctionCallFunctionRelations(project);
		Map<Function, List<Call>> result = new HashMap<>();
		for(Call call : allCalls) {
			CodeNode callerNode = call.getCallerNode();
			if(!(callerNode instanceof Function)) {
				continue;
			}
			Function caller = (Function) callerNode;
			List<Call> group = result.getOrDefault(caller, new ArrayList<>());
			group.add(call);
			result.put(caller, group);
		}
		return result;
	}

	@Override
	public Map<Function, List<Access>> findAllFunctionAccessRelationsGroupByCaller(Project project) {
		Iterable<Access> allAccesses = findProjectContainFunctionAccessVariableRelations(project);
		Map<Function, List<Access>> result = new HashMap<>();
		for(Access access : allAccesses) {
			Function caller = access.getFunction();
			List<Access> group = result.getOrDefault(caller, new ArrayList<>());
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
			Collection<Call> calls = queryFunctionCallFunctions(function);
			for(Call call : calls) {
				ProjectFile belongToFile = containRelationService.findFunctionBelongToFile(call.getCallFunction());
				if(!file.equals(belongToFile)) {
					result.addFanOut(belongToFile);
					result.addFanOutRelations(call);
				}
			}
			
			calls = queryFunctionCallByFunctions(function);
			for(Call call : calls) {
				ProjectFile belongToFile = containRelationService.findCodeNodeBelongToFile(call.getCallerNode());
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
	public Collection<Call> queryFunctionCallFunctions(Function function) {
		return callRepository.queryFunctionCallFunctions(function.getId());
	}

	@Override
	public Collection<Call> queryFunctionCallByFunctions(Function function) {
		return callRepository.queryFunctionCallByFunctions(function.getId());
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


	@Override
	public List<Call> findAllFunctionCallFunctionRelations() {
		return callRepository.findAllFunctionCallFunctionRelations();
	}


}
