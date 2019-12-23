package fan.md.service;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import depends.deptypes.DependencyType;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.repo.EntityRepo;
import depends.entity.repo.InMemoryEntityRepo;
import depends.extractor.FileParser;
import depends.extractor.cpp.CppBuiltInType;
import depends.extractor.cpp.CppImportLookupStrategy;
import depends.extractor.cpp.CppProcessor;
import depends.extractor.cpp.MacroFileRepo;
import depends.extractor.cpp.MacroRepo;
import depends.extractor.cpp.cdt.CdtCppFileParser;
import depends.extractor.cpp.cdt.PreprocessorHandler;
import depends.extractor.java.JavaBuiltInType;
import depends.extractor.java.JavaFileParser;
import depends.extractor.java.JavaImportLookupStrategy;
import depends.relations.Inferer;
import depends.util.FileTraversal;
import depends.util.FileUtil;
import depends.util.TemporaryFile;
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
import fan.md.neo4j.service.BatchInserterService;

public class DependsCodeInsertService implements InsertDependsCodeToNeo4j {
	
	private DependsCodeInsertService() {
		
	}
	private static DependsCodeInsertService instance = new DependsCodeInsertService();
	
	public static DependsCodeInsertService getInstance() {
		return instance;
	}
	
	private EntityRepo entityRepo ;
	private Inferer inferer;
    private PreprocessorHandler preprocessorHandler;
    private FileTraversal fileTransversal;
	private MacroRepo macroRepo;

	private void initJavaExtractor() {
		entityRepo = new InMemoryEntityRepo();
		inferer = new Inferer(entityRepo,new JavaImportLookupStrategy(),new JavaBuiltInType(),false);
    	TemporaryFile.reset();
	}
	
	private void initCppExtractor(String src) {
		entityRepo = new InMemoryEntityRepo();
    	inferer = new Inferer(entityRepo,new CppImportLookupStrategy(),new CppBuiltInType(),false);
    	preprocessorHandler = new PreprocessorHandler(src, new ArrayList<>());
    	TemporaryFile.reset();
    	macroRepo = new MacroFileRepo(entityRepo);
	}
	
	private EntityRepo extractJava(String directory) throws Exception {
	    fileTransversal = new FileTraversal(new FileTraversal.IFileVisitor(){
			@Override
			public void visit(File file) {
				String fileFullPath = file.getAbsolutePath();
				fileFullPath = FileUtil.uniqFilePath(fileFullPath);
				if (!fileFullPath.startsWith(directory)) {
					return;
				}
	            FileParser fileParser = new JavaFileParser(fileFullPath, entityRepo, inferer);
	            try {
	                System.out.println("parsing " + fileFullPath 
	                		+ "...");		
	                fileParser.parse();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }	
			}
    		
    	});
    	fileTransversal.extensionFilter(new String[] {".java"});
		fileTransversal.travers(directory);
	    inferer.resolveAllBindings();
		return entityRepo;
	}

	private EntityRepo extractCpp(String directory) throws Exception {
		FileTraversal fileTransversal = new FileTraversal(new FileTraversal.IFileVisitor(){
			@Override
			public void visit(File file) {
				String fileFullPath = file.getAbsolutePath();
				fileFullPath = FileUtil.uniqFilePath(fileFullPath);
				if (!fileFullPath.startsWith(directory)) {
					return;
				}
	            FileParser fileParser = new CdtCppFileParser(fileFullPath,entityRepo,preprocessorHandler,inferer, macroRepo);
	            try {
	                System.out.println("parsing " + fileFullPath 
	                		+ "...");
	                fileParser.parse();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }	
			}
    		
    	});
    	fileTransversal.extensionFilter(new CppProcessor().fileSuffixes());
		fileTransversal.travers(directory);
		inferer.resolveAllBindings();
		return entityRepo;
	}
	
	@Override
	public EntityRepo extractEntityRepo(String src, String language) throws Exception {
		if("java".equals(language)) {
			initJavaExtractor();
			return extractJava(src);
		} else {
			initCppExtractor(src);
			return extractCpp(src);
		}
	}
	
	Map<Integer, Package> pcks = new HashMap<>();
	Map<Integer, Long> pcksId = new HashMap<>();
	
	Map<Integer, CodeFile> files = new HashMap<>();
	Map<Integer, Long> filesId = new HashMap<>();
	
	Map<Integer, Type> types = new HashMap<>();
	Map<Integer, Long> typesId = new HashMap<>();
	
	Map<Integer, Function> functions = new HashMap<>();
	Map<Integer, Long> functionsId = new HashMap<>();
	
	@Override
	public void insertCodeToNeo4jDataBase(String databasePath, boolean delete) throws Exception {
		System.out.println("start to store datas to database");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("开始时间：" + sdf.format(currentTime));
		try(BatchInserterService myBatchInserter = BatchInserterService.getInstance()) {
			myBatchInserter.init(databasePath, delete);
			entityRepo.getEntities().forEach(entity -> {
				if(entity instanceof PackageEntity) {
					Package pck = new Package();
					pck.setPackageName(entity.getQualifiedName());
					pck.setEntityId(entity.getId());
					pcks.put(entity.getId(), pck);
					myBatchInserter.insertPackageForJava(pck);
					entity.getChildren().forEach(fileEntity -> {
						if(fileEntity instanceof FileEntity) {
							CodeFile file = new CodeFile();
							file.setFileName(fileEntity.getRawName().getName());
							file.setPath(fileEntity.getQualifiedName());
							file.setEntityId(fileEntity.getId());
							myBatchInserter.insertCodeFile(file);
							PackageContainFile packageContainFile = new PackageContainFile();
							packageContainFile.setPck(pck);
							packageContainFile.setFile(file);
							myBatchInserter.insertRelation(packageContainFile);
							List<TypeEntity> typeEntities = ((FileEntity) fileEntity).getDeclaredTypes();
							typeEntities.forEach(typeEntity -> {
								Type type = new Type();
								type.setTypeName(typeEntity.getQualifiedName());
								type.setPackageName(pck.getPackageName());
								type.setEntityId(typeEntity.getId());
								types.put(typeEntity.getId(), type);
								myBatchInserter.insertType(type);
								FileContainType fileContainType = new FileContainType();
								fileContainType.setFile(file);
								fileContainType.setType(type);
								myBatchInserter.insertRelation(fileContainType);
								typeEntity.getChildren().forEach(typeEntityChild -> {
									if(typeEntityChild.getClass() == FunctionEntity.class) {
										Function function = new Function();
										function.setFunctionName(typeEntityChild.getRawName().getName());
										function.setEntityId(typeEntityChild.getId());
										functions.put(typeEntityChild.getId(), function);
										myBatchInserter.insertFunction(function);
										TypeContainsFunction containFunction = new TypeContainsFunction();
										containFunction.setType(type);
										containFunction.setFunction(function);
										myBatchInserter.insertRelation(containFunction);
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
						myBatchInserter.insertRelation(typeExtends);
					}
				});
				Collection<TypeEntity> imps = typeEntity.getImplementedTypes();
				imps.forEach(imp -> {
					Type other = types.get(imp.getId());
					if(other != null) {
						TypeImplementsType typeImplements = new TypeImplementsType();
						typeImplements.setStart(type);
						typeImplements.setEnd(other);
						myBatchInserter.insertRelation(typeImplements);
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
								myBatchInserter.insertRelation(call);
							}
//					} else {
//						System.out.println(relation.getEntity().getClass() + " " + relation.getEntity());
						}
					}
					if(DependencyType.RETURN.equals(relation.getType())) {
						
					}
				});
			});
		}
		currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("结束时间：" + sdf.format(currentTime));
	}
	

}
