package cn.edu.fudan.se.multidependency.service.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.config.PropertyConfig;
import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.lib.FunctionCallLibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.structure.Access;
import cn.edu.fudan.se.multidependency.model.relation.structure.Annotation;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;
import cn.edu.fudan.se.multidependency.model.relation.structure.Cast;
import cn.edu.fudan.se.multidependency.model.relation.structure.Extends;
import cn.edu.fudan.se.multidependency.model.relation.structure.Implements;
import cn.edu.fudan.se.multidependency.model.relation.structure.Import;
import cn.edu.fudan.se.multidependency.model.relation.structure.Include;
import cn.edu.fudan.se.multidependency.model.relation.structure.Parameter;
import cn.edu.fudan.se.multidependency.model.relation.structure.Return;
import cn.edu.fudan.se.multidependency.model.relation.structure.Throw;
import cn.edu.fudan.se.multidependency.model.relation.structure.VariableType;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.FunctionRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.NamespaceRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.TypeRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.VariableRepository;
import cn.edu.fudan.se.multidependency.repository.node.lib.LibraryRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.AccessRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.AnnotationRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.CallRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.CastRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ExtendsRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ImplementsRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ImportRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.IncludeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ParameterRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ReturnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ThrowRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.VariableTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.lib.FunctionCallLibraryAPIRepository;
import cn.edu.fudan.se.multidependency.service.query.metric.Fan_IO;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.utils.query.PageUtil;

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
    ExtendsRepository typeInheritsTypeRepository;
    
    @Autowired
    ImplementsRepository implementsRepository;

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
    
    @Autowired
    DependsOnRepository dependsOnRepository;

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
//		return typeInheritsTypeRepository.findExtendsTypesByTypeId(type.getId());
		/// FIXME
		return new ArrayList<>();
	}

	@Override
	public Collection<Type> findInheritsType(Type type) {
//		return typeInheritsTypeRepository.findInheritsFromTypeByTypeId(type.getId());
		/// FIXME
		return new ArrayList<>();
	}
	
	@Override
	public Collection<Type> findInheritsFromType(Type type) {
//		return typeInheritsTypeRepository.findInheritsTypesByTypeId(type.getId());
		/// FIXME
		return new ArrayList<>();
	}

	@Override
	public List<StructureRelation> findProjectContainStructureRelations(Project project) {
		List<StructureRelation> result = new ArrayList<>();
		result.addAll(findProjectContainFunctionCallFunctionRelations(project));
		result.addAll(findProjectContainInheritsRelations(project));
		result.addAll(findProjectContainTypeCallFunctions(project));
		return result;
	}

	private Map<Project, List<Import>> projectContainImportRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Import> findProjectContainImportRelations(Project project) {
		if(projectContainImportRelationsCache.get(project) == null) {
			projectContainImportRelationsCache.put(project, importRepository.findProjectContainImportRelations(project.getId()));
		}
		return projectContainImportRelationsCache.get(project);
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

	private Map<Project, List<Call>> projectContainFunctionCallFunctionRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Call> findProjectContainFunctionCallFunctionRelations(Project project) {
		if(projectContainFunctionCallFunctionRelationsCache.get(project) == null) {
			projectContainFunctionCallFunctionRelationsCache.put(project, callRepository.findProjectContainFunctionCallFunctionRelations(project.getId()));
		}
		return projectContainFunctionCallFunctionRelationsCache.get(project);
	}
	
	private Map<Project, List<Call>> projectContainTypeCallFunctionRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Call> findProjectContainTypeCallFunctions(Project project) {
		if(projectContainTypeCallFunctionRelationsCache.get(project) == null) {
			projectContainTypeCallFunctionRelationsCache.put(project, callRepository.findProjectContainTypeCallFunctionRelations(project.getId()));
		}
		return projectContainTypeCallFunctionRelationsCache.get(project);
	}

	private Map<Project, List<Extends>> projectContainInheritsRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Extends> findProjectContainInheritsRelations(Project project) {
//		if(projectContainInheritsRelationsCache.get(project) == null) {
//			projectContainInheritsRelationsCache.put(project, typeInheritsTypeRepository.findProjectContainTypeInheritsTypeRelations(project.getId()));
//		}
//		return projectContainInheritsRelationsCache.get(project);
		/// FIXME
		return new ArrayList<>();
	}

	private Map<Project, List<Cast>> projectContainFunctionCastTypeRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Cast> findProjectContainFunctionCastTypeRelations(Project project) {
		if(projectContainFunctionCastTypeRelationsCache.get(project) == null) {
			projectContainFunctionCastTypeRelationsCache.put(project, functionCastTypeRepository.findProjectContainFunctionCastTypeRelations(project.getId()));
		}
		return projectContainFunctionCastTypeRelationsCache.get(project);
	}
	
	private Map<Project, List<Parameter>> projectContainParameterRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Parameter> findProjectContainParameterRelations(Project project) {
		if(projectContainParameterRelationsCache.get(project) == null) {
			projectContainParameterRelationsCache.put(project, parameterRepository.findProjectContainParameterRelations(project.getId()));
		}
		return projectContainParameterRelationsCache.get(project);
	}

	@Override
	public List<Parameter> findProjectContainFunctionParameterTypeRelations(Project project) {
		return parameterRepository.findProjectContainFunctionParameterTypeRelations(project.getId());
	}

	@Override
	public List<Parameter> findProjectContainVariableTypeParameterTypeRelations(Project project) {
		return parameterRepository.findProjectContainVariableTypeParameterTypeRelations(project.getId());
	}

	private Map<Project, List<Return>> projectContainFunctionReturnTypeRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Return> findProjectContainFunctionReturnTypeRelations(Project project) {
		if(projectContainFunctionReturnTypeRelationsCache.get(project) == null) {
			projectContainFunctionReturnTypeRelationsCache.put(project, functionReturnTypeRepository.findProjectContainFunctionReturnTypeRelations(project.getId()));
		}
		return projectContainFunctionReturnTypeRelationsCache.get(project);
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
		Iterable<Call> allCalls = findProjectContainFunctionCallFunctionRelations(project);
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
			Function caller = (Function) access.getStartNode();
			List<Access> group = result.getOrDefault(caller, new ArrayList<>());
			group.add(access);
			result.put(caller, group);
		}
		return result;
	}

	private Map<Type, Map<Type, Boolean>> subTypeCache = new HashMap<>();
	@Override
	public boolean isSubType(Type subType, Type superType) {
		/*Map<Type, Boolean> superTypeMap = subTypeCache.getOrDefault(subType, new HashMap<>());
		Boolean is = superTypeMap.get(superType);
		if(is == null) {
			Type queryType = typeInheritsTypeRepository.findIsTypeInheritsType(subType.getId(), superType.getId());
			is = queryType != null;
			superTypeMap.put(superType, is);
		}
		subTypeCache.put(subType, superTypeMap);*/
		/// FIXME
		return false;
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

	@Override
	public List<Implements> findProjectContainImplementsRelations(Project project) {
		/// FIXME
		return new ArrayList<>();
	}

	@Override
	public boolean isInDifferentModule(ProjectFile file1, ProjectFile file2) {
		Package pck1 = containRelationService.findFileBelongToPackage(file1);
		Package pck2 = containRelationService.findFileBelongToPackage(file2);
		return !pck1.equals(pck2);
	}

	@Override
	public boolean isDependsOn(ProjectFile file1, ProjectFile file2) {
		return !dependsOnRepository.findDependsOnInFiles(file1.getId(), file2.getId()).isEmpty();
	}
	
	@Bean("createDependsOn")
	public List<DependsOn> createDependsOn(PropertyConfig propertyConfig, DependsOnRepository dependsOnRepository, ProjectFileRepository fileRepository) {
		if(propertyConfig.isCalculateDependsOn()) {
			System.out.println("创建Depends On关系");
			dependsOnRepository.deleteAll();
			dependsOnRepository.createDependsOnWithExtends();
			dependsOnRepository.createDependsOnWithImplements();
			dependsOnRepository.createDependsOnWithCall();
			dependsOnRepository.createDependsOnWithCreate();
			dependsOnRepository.createDependsOnWithCast();
			dependsOnRepository.createDependsOnWithThrow();
			dependsOnRepository.createDependsOnWithParameter();
			dependsOnRepository.createDependsOnWithVariableType();
			dependsOnRepository.createDependsOnWithAccess();
			dependsOnRepository.createDependsOnWithImpllink();
			dependsOnRepository.createDependsOnWithAnnotation();
			dependsOnRepository.createDependsOnWithTimes();
			dependsOnRepository.deleteNullTimesDependsOn();
			dependsOnRepository.createDependsOnInPackage();
			dependsOnRepository.addTimesOnDependsOnInPackage();
			dependsOnRepository.deleteNullTimesDependsOnInPackage();
			fileRepository.pageRank(20, 0.85);
		}
		return new ArrayList<>();
	}

}
