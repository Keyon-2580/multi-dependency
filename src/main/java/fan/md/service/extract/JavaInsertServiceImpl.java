package fan.md.service.extract;

import java.util.List;

import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.repo.EntityRepo;
import fan.md.exception.LanguageErrorException;
import fan.md.model.Language;
import fan.md.model.node.code.CodeFile;
import fan.md.model.node.code.Function;
import fan.md.model.node.code.Package;
import fan.md.model.node.code.Type;
import fan.md.model.relation.code.FileContainsType;
import fan.md.model.relation.code.PackageContainsFile;
import fan.md.model.relation.code.TypeContainsFunction;

public class JavaInsertServiceImpl extends InsertServiceImpl {

	public JavaInsertServiceImpl(String projectPath, EntityRepo entityRepo, String databasePath, boolean delete, Language language) {
		super(projectPath, entityRepo, databasePath, delete, language);
	}

	@Override
	protected void insertNodesWithContainRelations() throws LanguageErrorException {
		if(language != Language.java) {
			throw new LanguageErrorException();
		}
		entityRepo.getEntities().forEach(entity -> {
			if(entity instanceof PackageEntity) {
				// Java 从包开始
				Package pck = new Package();
				pck.setPackageName(entity.getQualifiedName());
				pck.setEntityId(entity.getId());
				pcks.put(entity.getId(), pck);
				batchInserterService.insertNode(pck);
				entity.getChildren().forEach(fileEntity -> {
					if(fileEntity instanceof FileEntity) {
						CodeFile file = new CodeFile();
						file.setFileName(fileEntity.getRawName().getName());
						file.setPath(fileEntity.getQualifiedName());
						file.setEntityId(fileEntity.getId());
						batchInserterService.insertNode(file);
						PackageContainsFile packageContainFile = new PackageContainsFile(pck, file);
						batchInserterService.insertRelation(packageContainFile);
						List<TypeEntity> typeEntities = ((FileEntity) fileEntity).getDeclaredTypes();
						typeEntities.forEach(typeEntity -> {
							Type type = new Type();
							type.setTypeName(typeEntity.getQualifiedName());
							type.setPackageName(pck.getPackageName());
							type.setEntityId(typeEntity.getId());
							types.put(typeEntity.getId(), type);
							batchInserterService.insertNode(type);
							FileContainsType fileContainType = new FileContainsType(file, type);
							batchInserterService.insertRelation(fileContainType);
							typeEntity.getChildren().forEach(typeEntityChild -> {
								if(typeEntityChild instanceof FunctionEntity) {
									Function function = new Function();
									function.setFunctionName(typeEntityChild.getRawName().getName());
									function.setEntityId(typeEntityChild.getId());
									functions.put(typeEntityChild.getId(), function);
									batchInserterService.insertNode(function);
									TypeContainsFunction containFunction = new TypeContainsFunction(type, function);
									batchInserterService.insertRelation(containFunction);
								}
							});
						});
					}
				});
			}
		});
	}

	@Override
	protected void insertRelations() throws LanguageErrorException {
		extractRelationsFromTypes();
		extractRelationsFromFunctions();
	}
	
}
