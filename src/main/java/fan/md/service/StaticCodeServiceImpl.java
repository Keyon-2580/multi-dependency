package fan.md.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fan.md.model.entity.code.CodeFile;
import fan.md.model.entity.code.Function;
import fan.md.model.entity.code.Type;
import fan.md.model.relation.code.FileContainType;
import fan.md.model.relation.code.TypeExtendsType;
import fan.md.neo4j.repository.FileContainTypeRepository;
import fan.md.neo4j.repository.FileRepository;
import fan.md.neo4j.repository.FunctionCallFunctionRepository;
import fan.md.neo4j.repository.FunctionRepository;
import fan.md.neo4j.repository.PackageContainFileRepository;
import fan.md.neo4j.repository.PackageRepository;
import fan.md.neo4j.repository.ProjectRepository;
import fan.md.neo4j.repository.TypeContainsFunctionRepository;
import fan.md.neo4j.repository.TypeExtendsTypeRepository;
import fan.md.neo4j.repository.TypeImplementsTypeRepository;
import fan.md.neo4j.repository.TypeRepository;

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
		List<Type> fathers = new ArrayList<>();
		typeExtendsTypeRepository.findExtendsTypesByTypeId(type.getId()).forEach(father -> {
			fathers.add(father);
		});
		return fathers;
	}

	@Override
	public List<Type> findTypesInFile(CodeFile codeFile) {
		System.out.println(findAllTypes().size());
		Iterable<FileContainType> temp = fileContainTypeRepository.findAll();
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
