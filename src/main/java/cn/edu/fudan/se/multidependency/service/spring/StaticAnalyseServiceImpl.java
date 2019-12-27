package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsType;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeExtendsType;
import cn.edu.fudan.se.multidependency.repository.node.FileRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.FunctionRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.NamespaceRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.TypeRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.VariableRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileContainFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileContainTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileContainsVariableRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileImportTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FileIncludeFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionContainsTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionContainsVariableRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionParameterTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.FunctionReturnTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.PackageContainFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.TypeContainsFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.TypeContainsTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.TypeContainsVariableRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.TypeExtendsTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.TypeImplementsTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.VariableIsTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;

@Service
public class StaticAnalyseServiceImpl implements StaticAnalyseService {
	
	@Autowired
	FileContainTypeRepository fileContainTypeRepository;
	
	@Autowired
	FileRepository fileRepository;
	
	@Autowired
	FileContainFunctionRepository fileContainFunctionRepository;
	
	@Autowired
	FileContainsVariableRepository fileContainsVariableRepository;
	
	@Autowired
	FileIncludeFileRepository fileIncludeFileRepository;
	
	@Autowired
	FileImportTypeRepository fileImportTypeRepository;
	
	@Autowired
	FunctionCallFunctionRepository functionCallFunctionRepository;
	
	@Autowired
	FunctionRepository functionRepository;
	
	@Autowired
	FunctionContainsVariableRepository functionContainsVariableRepository;
	
	@Autowired
	FunctionDynamicCallFunctionRepository functionDynamicCallFunctionRepository;
	
	@Autowired
	FunctionReturnTypeRepository functionReturnTypeRepository;
	
	@Autowired
	FunctionParameterTypeRepository functionParameterTypeRepository;
	
	@Autowired
	FunctionContainsTypeRepository functionContainsTypeRepository;
	
	@Autowired
	NamespaceRepository namespaceRepository;
	
	@Autowired
    PackageRepository packageRepository;

    @Autowired
    PackageContainFileRepository packageContainFileRepository;
    
    @Autowired
    ProjectRepository projectRepository;
    
    @Autowired
    TypeRepository typeRepository;
    
    @Autowired
    TypeContainsFunctionRepository typeContainsFunctionRepository;
    
    @Autowired
    TypeContainsTypeRepository typeContainsTypeRepository;
    
    @Autowired
    TypeContainsVariableRepository typeContainsVariableRepository;
    
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
		System.out.println(findAllTypes().size());
		Iterable<FileContainsType> temp = fileContainTypeRepository.findAll();
		temp.forEach(t -> {
			System.out.println(t.getFile().getFileName());
			System.out.println(t.getType().getTypeName());
		});
		return null;
	}

	@Override
	public List<TypeExtendsType> findAllExtends() {
		List<TypeExtendsType> allExtends = new ArrayList<>();
		typeExtendsTypeRepository.findAll().forEach(e -> {
			allExtends.add(e);
			System.out.println(e.getStart().getTypeName() + " " + e.getEnd().getTypeName());
			
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
}