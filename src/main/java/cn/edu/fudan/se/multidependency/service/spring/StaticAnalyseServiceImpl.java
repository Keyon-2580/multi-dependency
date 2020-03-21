package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
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
    
	@Override
	public List<Type> findTypes() {
		List<Type> types = new ArrayList<>();
		typeRepository.findAll().forEach(type -> {
			types.add(type);
		});
		return types;
	}
	
	@Override
	public List<Type> findExtendsType(Type type) {
		return typeInheritsTypeRepository.findExtendsTypesByTypeId(type.getId());
	}

	@Override
	public List<Type> findTypes(ProjectFile codeFile) {
		return new ArrayList<>();
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
	public ProjectFile findFunctionBelongToFile(Function function) {
		return containRepository.findFunctionBelongToFileByFunctionId(function.getId());
	}

	@Override
	public ProjectFile findTypeBelongToFile(Type type) {
		return containRepository.findTypeBelongToFileByTypeId(type.getId());
	}

	@Override
	public ProjectFile findVariableBelongToFile(Variable variable) {
		return containRepository.findVariableBelongToFileByVariableId(variable.getId());
	}

	@Override
	public List<ProjectFile> allFiles() {
		return fileRepository.findAllProjectFiles();
	}

	@Override
	public Package findFileBelongToPackage(ProjectFile file) {
		return containRepository.findFileBelongToPackageByFileId(file.getId());
	}

	@Override
	public Map<Long, Project> allProjects() {
		Iterable<Project> projects = projectRepository.findAll();
		Map<Long, Project> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), project);
		}
		return result;
	}

	@Override
	public Project findProject(Long id) {
		return projectRepository.findById(id).get();
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

	@Override
	public Iterable<FunctionCallFunction> findAllFunctionCallFunctionRelations() {
		return functionCallFunctionRepository.findAll();
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
	public Type findFunctionBelongToType(Function function) {
		return containRepository.findFunctionBelongToTypeByFunctionId(function.getId());
	}

	@Override
	public boolean isSubType(Type superType, Type subType) {
		
		return false;
	}
	
}
