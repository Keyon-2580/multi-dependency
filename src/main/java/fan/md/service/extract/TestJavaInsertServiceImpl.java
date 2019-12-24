package fan.md.service.extract;

import depends.entity.Entity;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;
import fan.md.exception.LanguageErrorException;
import fan.md.model.Language;
import fan.md.model.node.code.CodeFile;
import fan.md.model.node.code.Function;
import fan.md.model.node.code.Package;
import fan.md.model.node.code.Type;
import fan.md.model.node.code.Variable;
import fan.md.model.relation.code.FileContainsType;
import fan.md.model.relation.code.FunctionContainsVariable;
import fan.md.model.relation.code.PackageContainsFile;
import fan.md.model.relation.code.TypeContainsFunction;
import fan.md.model.relation.code.TypeContainsVariable;
import fan.md.utils.FileUtils;

public class TestJavaInsertServiceImpl extends InsertServiceImpl {

	public TestJavaInsertServiceImpl(String projectPath, EntityRepo entityRepo, String databasePath, boolean delete,
			Language language) {
		super(projectPath, entityRepo, databasePath, delete, language);
	}

	@Override
	protected void insertNodesWithContainRelations() throws LanguageErrorException {
		entityRepo.getEntities().forEach(entity -> {
			// 每个entity对应相应的node
			if(entity instanceof PackageEntity) {
				Package pck = new Package();
				pck.setPackageName(entity.getQualifiedName());
				pck.setEntityId(entity.getId());
				pck.setDirectory(false);
				insertNode(pck, entity.getId());
				batchInserterService.insertNode(pck);
			} else if(entity instanceof FileEntity) {
				CodeFile file = new CodeFile();
				file.setEntityId(entity.getId());
				file.setFileName(entity.getQualifiedName());
				file.setPath(entity.getQualifiedName());
				insertNode(file, entity.getId());
				batchInserterService.insertNode(file);
			} else if(entity instanceof FunctionEntity) {
				Function function = new Function();
				function.setFunctionName(entity.getQualifiedName());
				function.setEntityId(entity.getId());
				insertNode(function, entity.getId());
				batchInserterService.insertNode(function);
			} else if(entity instanceof VarEntity) {
				Variable variable = new Variable();
				variable.setEntityId(entity.getId());
				variable.setVariableName(entity.getQualifiedName());
				insertNode(variable, entity.getId());
				batchInserterService.insertNode(variable);
			} else if(entity.getClass() == TypeEntity.class) {
				Type type = new Type();
				type.setEntityId(entity.getId());
				type.setTypeName(entity.getQualifiedName());
				insertNode(type, entity.getId());
				batchInserterService.insertNode(type);
			}
		});
		this.nodes.findFiles().forEach((entityId, codeFile) -> {
			PackageContainsFile containFile = new PackageContainsFile();
			containFile.setFile(codeFile);
			// 在java中，文件上面是包，若包不存在，则将该文件的包设为当前目录
			Entity fileEntity = entityRepo.getEntity(entityId);
			Entity parentEntity = fileEntity.getParent();
			Package pck = null;
			if(parentEntity == null) {
				String fileName = codeFile.getFileName();
				String packageName = FileUtils.findDirectoryFromFile(fileName);
				pck = this.nodes.findPackageByPackageName(packageName);
				if(pck == null) {
					pck = new Package();
					pck.setEntityId(entityRepo.generateId());
					pck.setPackageName(packageName);
					pck.setDirectory(true);
					insertNode(pck, pck.getEntityId());
					batchInserterService.insertNode(pck);
				}
			} else {
				pck = this.nodes.findPackage(parentEntity.getId());
			}
			containFile.setPck(pck);
			batchInserterService.insertRelation(containFile);
		});
		this.nodes.findTypes().forEach((entityId, type) -> {
			TypeEntity typeEntity = (TypeEntity) entityRepo.getEntity(entityId);
			Entity parentEntity = typeEntity.getParent();
			/*Entity currentEntity = typeEntity;
				Node currentNode = type;*/
			while(!(parentEntity instanceof FileEntity)) {
				///FIXME 内部类的情况
				/*if(parentEntity instanceof FunctionEntity) {
						Function function = this.nodes.findFunction(parentEntity.getId());
						
					} else if(parentEntity.getClass() == TypeEntity.class) {
						
					}*/
				parentEntity = parentEntity.getParent();
			}
			CodeFile file = this.nodes.findCodeFile(parentEntity.getId());
			FileContainsType fileContainsType = new FileContainsType(file, type);
			batchInserterService.insertRelation(fileContainsType);
		});
		this.nodes.findFunctions().forEach((entityId, function) -> {
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(entityId);
			Entity parentEntity = functionEntity.getParent();
			
			while(parentEntity.getClass() != TypeEntity.class) {
				// 方法内的匿名类的方法的parentEntity是该方法
				parentEntity = parentEntity.getParent();
			}
			Type type = this.nodes.findType(parentEntity.getId());
			TypeContainsFunction typeContainsFunction = new TypeContainsFunction(type, function);
			batchInserterService.insertRelation(typeContainsFunction);
		});
		this.nodes.findVariables().forEach((entityId, variable) -> {
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId);
			Entity parentEntity = varEntity.getParent();
			if(parentEntity instanceof FunctionEntity) {
				Function function = this.nodes.findFunction(parentEntity.getId());
				FunctionContainsVariable functionContainsVariable = new FunctionContainsVariable(function, variable);
				batchInserterService.insertRelation(functionContainsVariable);
			} else if(parentEntity.getClass() == TypeEntity.class) {
				Type type = this.nodes.findType(parentEntity.getId());
				TypeContainsVariable typeContainsVariable = new TypeContainsVariable(type, variable);
				batchInserterService.insertRelation(typeContainsVariable);
			} else {
//				System.out.println(varEntity);
//				System.out.println(parentEntity.getClass() + " " + parentEntity);
			}
		});
	}
	
	@Override
	protected void insertRelations() throws LanguageErrorException {
		extractRelationsFromTypes();
		extractRelationsFromFunctions();
		extractRelationsFromVariables();
	}

}
