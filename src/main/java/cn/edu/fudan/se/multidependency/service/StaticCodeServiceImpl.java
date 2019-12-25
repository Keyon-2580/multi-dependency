package cn.edu.fudan.se.multidependency.service;

import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.code.CodeFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeExtendsType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsType;
import cn.edu.fudan.se.multidependency.neo4j.repository.FileContainTypeRepository;
import cn.edu.fudan.se.multidependency.neo4j.repository.FileRepository;
import cn.edu.fudan.se.multidependency.neo4j.repository.FunctionCallFunctionRepository;
import cn.edu.fudan.se.multidependency.neo4j.repository.FunctionRepository;
import cn.edu.fudan.se.multidependency.neo4j.repository.PackageContainFileRepository;
import cn.edu.fudan.se.multidependency.neo4j.repository.PackageRepository;
import cn.edu.fudan.se.multidependency.neo4j.repository.ProjectRepository;
import cn.edu.fudan.se.multidependency.neo4j.repository.TypeContainsFunctionRepository;
import cn.edu.fudan.se.multidependency.neo4j.repository.TypeExtendsTypeRepository;
import cn.edu.fudan.se.multidependency.neo4j.repository.TypeImplementsTypeRepository;
import cn.edu.fudan.se.multidependency.neo4j.repository.TypeRepository;

@Service
public class StaticCodeServiceImpl implements StaticCodeService {
	
	@Autowired
    PackageRepository packageRepository;

    @Autowired
    PackageContainFileRepository packageContainFileRepository;
    
    @Autowired
    TypeRepository typeRepository;
    
    @Autowired
    FunctionRepository functionRepository;
    
    @Autowired
    TypeContainsFunctionRepository typeContainsFunctionRepository;
    
    @Autowired
    TypeExtendsTypeRepository typeExtendsTypeRepository;

    @Autowired
    TypeImplementsTypeRepository typeImplementsTypeRepository;
    
    @Autowired
    FunctionCallFunctionRepository functionCallFunctionRepository;
    
    @Autowired
    FileContainTypeRepository fileContainTypeRepository;
    
    @Autowired
    FileRepository fileRepository;
    
    @Autowired
    ProjectRepository projectRepository;
    
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
	public List<Type> findTypesInFile(CodeFile codeFile) {
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
}
