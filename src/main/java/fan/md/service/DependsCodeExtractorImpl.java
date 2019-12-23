package fan.md.service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import depends.deptypes.DependencyType;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.repo.EntityRepo;
import fan.md.model.entity.code.CodeFile;
import fan.md.model.entity.code.Function;
import fan.md.model.entity.code.Package;
import fan.md.model.entity.code.Type;
import fan.md.model.relation.code.FileContainType;
import fan.md.model.relation.code.FunctionCallFunction;
import fan.md.model.relation.code.PackageContainFile;
import fan.md.model.relation.code.TypeContainsFunction;
import fan.md.model.relation.code.TypeExtendsType;
import fan.md.model.relation.code.TypeImplementsType;
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
@Deprecated
public class DependsCodeExtractorImpl implements DependsCodeExtractor {
	
    private DependsCodeExtractor dependsCodeInsertService = DependsCodeInsertService.getInstance();

	@Override
	public EntityRepo extractEntityRepo(String src, String language) throws Exception {
		entityRepo = dependsCodeInsertService.extractEntityRepo(src, language);
		return entityRepo;
	}
	
	private EntityRepo entityRepo;
	
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

	@Deprecated
	public void insertDependsCodeBySpring(String src, String language) throws Exception {
		extractEntityRepo(src, language);
		System.out.println("start to store datas to database");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("开始时间：" + sdf.format(currentTime));
		projectRepository.clearRelation();
		projectRepository.clearNode();
		entityRepo.getEntities().forEach(entity -> {
			if(entity instanceof PackageEntity) {
				Package pck = new Package();
				pck.setPackageName(entity.getQualifiedName());
				pcks.put(entity.getId(), pck);
				packageRepository.save(pck);
				
				entity.getChildren().forEach(fileEntity -> {
					if(fileEntity instanceof FileEntity) {
						CodeFile file = new CodeFile();
						file.setFileName(fileEntity.getQualifiedName());
						fileRepository.save(file);
						
						PackageContainFile containFile = new PackageContainFile();
						containFile.setPck(pck);
						containFile.setFile(file);
						packageContainFileRepository.save(containFile);
						
						List<TypeEntity> typeEntities = ((FileEntity) fileEntity).getDeclaredTypes();
						typeEntities.forEach(typeEntity -> {
							Type type = new Type();
							type.setTypeName(typeEntity.getQualifiedName());
							type.setPackageName(pck.getPackageName());
							types.put(typeEntity.getId(), type);
							typeRepository.save(type);
							
							FileContainType fileContainType = new FileContainType();
							fileContainType.setFile(file);
							fileContainType.setType(type);
							fileContainTypeRepository.save(fileContainType);
							
							typeEntity.getChildren().forEach(typeEntityChild -> {
								if(typeEntityChild.getClass() == FunctionEntity.class) {
									Function function = new Function();
									function.setFunctionName(typeEntityChild.getRawName().getName());
									functionRepository.save(function);
									functions.put(typeEntityChild.getId(), function);
									
									TypeContainsFunction containFunction = new TypeContainsFunction();
									containFunction.setType(type);
									containFunction.setFunction(function);
									typeContainsFunctionRepository.save(containFunction);
									
								}
							});
						});
					}
				});
			}
		});
		types.forEach((id, type) -> {
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(id);
			Collection<TypeEntity> inherits = typeEntity.getInheritedTypes();
			inherits.forEach(inherit -> {
				Type other = types.get(inherit.getId());
				if(other != null) {
					TypeExtendsType typeExtends = new TypeExtendsType();
					typeExtends.setStart(type);
					typeExtends.setEnd(other);
					typeExtendsTypeRepository.save(typeExtends);
				}
			});
			Collection<TypeEntity> imps = typeEntity.getImplementedTypes();
			imps.forEach(imp -> {
				Type other = types.get(imp.getId());
				if(other != null) {
					TypeImplementsType typeImplements = new TypeImplementsType();
					typeImplements.setStart(type);
					typeImplements.setEnd(other);
					typeImplementsTypeRepository.save(typeImplements);
				}
			});
		});
		functions.forEach((id, function) -> {
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(id);
			functionEntity.getRelations().forEach(relation -> {
				if(DependencyType.CALL.equals(relation.getType())) {
					if(relation.getEntity().getClass() == FunctionEntity.class) {
						Function other = functions.get(relation.getEntity().getId());
						if(other != null) {
							FunctionCallFunction call = new FunctionCallFunction();
							call.setFunction(function);
							call.setCallFunction(other);
							functionCallFunctionRepository.save(call);
						}
//					} else {
//						System.out.println(relation.getEntity().getClass() + " " + relation.getEntity());
					}
				}
			});
		});
		currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("结束时间：" + sdf.format(currentTime));
	}

	Map<Integer, Package> pcks = new HashMap<>();

	Map<Integer, Long> pcksId = new HashMap<>();

	Map<Integer, CodeFile> files = new HashMap<>();

	Map<Integer, Long> filesId = new HashMap<>();

	Map<Integer, Type> types = new HashMap<>();

	Map<Integer, Long> typesId = new HashMap<>();

	Map<Integer, Function> functions = new HashMap<>();

	Map<Integer, Long> functionsId = new HashMap<>();
}
