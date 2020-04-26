package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
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
import cn.edu.fudan.se.multidependency.model.relation.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
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
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
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
 * 结构、三方、克隆，应只从数据库查找，内部不应与其它Service有关系
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
    ContainRepository containRepository;
    
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
    
    public void clearCache() {
    	this.functionBelongToTypeCache.clear();
    	this.typesCache.clear();
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
	public List<Function> findProjectContainFunctions(Project project) {
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

	@Override
	public Package findFileBelongToPackage(ProjectFile file) {
		return containRepository.findFileBelongToPackageByFileId(file.getId());
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

	Map<Project, Iterable<Package>> projectContainPakcagesCache = new HashMap<>();
	@Override
	public Iterable<Package> allPackagesInProject(Project project) {
		Iterable<Package> result = projectContainPakcagesCache.getOrDefault(project, containRepository.findProjectContainPackages(project.getId()));
		projectContainPakcagesCache.put(project, result);
		return result;
	}

	Map<Package, Iterable<ProjectFile>> packageContainFilesCache = new HashMap<>();
	@Override
	public Iterable<ProjectFile> allFilesInPackage(Package pck) {
		Iterable<ProjectFile> result = packageContainFilesCache.getOrDefault(pck, containRepository.findPackageContainFiles(pck.getId()));
		packageContainFilesCache.put(pck, result);
		return result;
	}

	Map<ProjectFile, Iterable<Type>> fileContainTypesCache = new HashMap<>();
	@Override
	public Iterable<Type> allTypesInFile(ProjectFile codeFile) {
		Iterable<Type> result = fileContainTypesCache.getOrDefault(codeFile, containRepository.findFileContainTypes(codeFile.getId()));
		fileContainTypesCache.put(codeFile, result);
		return result;
	}

	Map<ProjectFile, Iterable<Function>> fileContainFunctionsCache = new HashMap<>();
	@Override
	public Iterable<Function> allFunctionsInFile(ProjectFile codeFile) {
		Iterable<Function> result = fileContainFunctionsCache.getOrDefault(codeFile, containRepository.findFileContainFunctions(codeFile.getId()));
		fileContainFunctionsCache.put(codeFile, result);
		return result;
	}

	Map<Type, Iterable<Function>> typeContainFunctionsCache = new HashMap<>();
	@Override
	public Iterable<Function> allFunctionsInType(Type type) {
		Iterable<Function> result = typeContainFunctionsCache.getOrDefault(type, containRepository.findTypeContainFunctions(type.getId()));
		typeContainFunctionsCache.put(type, result);
		return result;
	}

	Map<Type, Iterable<Variable>> typeContainVariablesCache = new HashMap<>();
	@Override
	public Iterable<Variable> allVariablesInType(Type type) {
		Iterable<Variable> result = typeContainVariablesCache.getOrDefault(type, containRepository.findTypeContainVariables(type.getId()));
		typeContainVariablesCache.put(type, result);
		return result;
	}

	Map<Function, Iterable<Variable>> functionContainVariablesCache = new HashMap<>();
	@Override
	public Iterable<Variable> allVariablesInFunction(Function function) {
		Iterable<Variable> result = functionContainVariablesCache.getOrDefault(function, containRepository.findFunctionContainVariables(function.getId()));
		functionContainVariablesCache.put(function, result);
		return result;
	}

	Iterable<FunctionCallLibraryAPI> allFunctionCallLibraryAPIsCache = null;
	@Override
	public Iterable<FunctionCallLibraryAPI> findAllFunctionCallLibraryAPIs() {
		if(allFunctionCallLibraryAPIsCache == null) {
			allFunctionCallLibraryAPIsCache = functionCallLibraryAPIRepository.findAll();
		}
		return allFunctionCallLibraryAPIsCache;
	}

	@Override
	public Iterable<Clone> findProjectClone(Iterable<FunctionCloneFunction> functionClones) {
		List<Clone> result = new ArrayList<>();
		return result;
	}
	
}
