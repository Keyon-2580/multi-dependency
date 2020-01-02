package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeExtendsType;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.FunctionRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.NamespaceRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.TypeRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.VariableRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileImportTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileIncludeFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionParameterTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionReturnTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.TypeExtendsTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.TypeImplementsTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.VariableIsTypeRepository;
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
	FunctionCallFunctionRepository functionCallFunctionRepository;
	
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
    TypeExtendsTypeRepository typeExtendsTypeRepository;

    @Autowired
    TypeImplementsTypeRepository typeImplementsTypeRepository;
    
    @Autowired
    VariableIsTypeRepository variableIsTypeRepository;
    
    @Autowired
    VariableRepository variableRepository;
    
	@Override
	public List<Type> findAllTypes() {
		List<Type> types = new ArrayList<>();
		typeRepository.findAll().forEach(type -> {
			types.add(type);
		});
		return types;
	}
	
	@Override
	public List<Type> findExtendsType(Type type) {
		return typeExtendsTypeRepository.findExtendsTypesByTypeId(type.getId());
	}

	@Override
	public List<Type> findTypesInFile(ProjectFile codeFile) {
		return new ArrayList<>();
	}

	@Override
	public List<TypeExtendsType> findAllExtends() {
		List<TypeExtendsType> allExtends = new ArrayList<>();
		typeExtendsTypeRepository.findAll().forEach(e -> {
			allExtends.add(e);
			
		});
		return allExtends;
	}

	@Override
	public List<Function> findAllFunctions() {
		List<Function> functions = new ArrayList<>();
		functionRepository.findAll().forEach(function -> {
			functions.add(function);
		});
		return functions;
	}

	@Override
	public Package findTypeInPackage(Type type) {
		return null;
	}
	

	private Map<Long, ProjectFile> cacheFunction = new HashMap<>();
	
	/**
	 * 找出某函数所在的文件
	 */
	@Override
	public ProjectFile findFunctionBelongToCodeFile(Function function) {
		ProjectFile result = cacheFunction.get(function.getId());
		if(result != null) {
			return result;
		}
		result = functionRepository.findFunctionBelongToFileByFunctionId(function.getId());
		cacheFunction.put(function.getId(), result);
		return result;
	}

	@Override
	public List<ProjectFile> findAllProjectFile() {
		return fileRepository.findAllProjectFiles();
	}
}
