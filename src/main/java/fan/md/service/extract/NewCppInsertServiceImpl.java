package fan.md.service.extract;

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
import fan.md.model.node.code.Namespace;
import fan.md.model.node.code.Package;
import fan.md.model.node.code.Type;
import fan.md.model.node.code.Variable;
import fan.md.model.relation.code.PackageContainsFile;
import fan.md.utils.FileUtils;

public class NewCppInsertServiceImpl extends InsertServiceImpl {

	public NewCppInsertServiceImpl(String projectPath, EntityRepo entityRepo, String databasePath, boolean delete,
			Language language) {
		super(projectPath, entityRepo, databasePath, delete, language);
	}

	@Override
	protected void insertNodesWithContainRelations() throws LanguageErrorException {
		entityRepo.getEntities().forEach(entity -> {
			// 每个entity对应相应的node
			if(entity instanceof PackageEntity) {
				Namespace namespace = new Namespace();
				namespace.setNamespaceName(entity.getQualifiedName());
				namespace.setEntityId(entity.getId());
				insertNode(namespace, entity.getId());
				batchInserterService.insertNode(namespace);
			} else if(entity instanceof FileEntity) {
				CodeFile file = new CodeFile();
				String fileName = entity.getQualifiedName();
				file.setEntityId(entity.getId());
				file.setFileName(fileName);
				file.setPath(entity.getQualifiedName());
				insertNode(file, entity.getId());
				batchInserterService.insertNode(file);
				// 文件所在目录
				String packageName = FileUtils.findDirectoryFromFile(fileName);
				Package pck = this.nodes.findPackageByPackageName(packageName);
				if(pck == null) {
					pck = new Package();
					pck.setEntityId(entityRepo.generateId());
					pck.setPackageName(packageName);
					pck.setDirectory(true);
					insertNode(pck, pck.getEntityId());
					batchInserterService.insertNode(pck);
				}
				PackageContainsFile containFile = new PackageContainsFile(pck, file);
				batchInserterService.insertRelation(containFile);
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
	}

	@Override
	protected void insertRelations() throws LanguageErrorException {
		extractRelationsFromTypes();
		extractRelationsFromFunctions();
		extractRelationsFromVariables();		
	}

}
