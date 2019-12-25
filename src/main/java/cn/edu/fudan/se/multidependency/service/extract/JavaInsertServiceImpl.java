package cn.edu.fudan.se.multidependency.service.extract;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.CodeFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Package;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;
import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsType;
import cn.edu.fudan.se.multidependency.model.relation.code.PackageContainsFile;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeContainsFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeContainsVariable;

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
				insertNode(pck, entity.getId());
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
							insertNode(type, typeEntity.getId());
							batchInserterService.insertNode(type);
							FileContainsType fileContainType = new FileContainsType(file, type);
							batchInserterService.insertRelation(fileContainType);
							typeEntity.getChildren().forEach(typeEntityChild -> {
								if(typeEntityChild instanceof FunctionEntity) {
									Function function = new Function();
									function.setFunctionName(typeEntityChild.getRawName().getName());
									function.setEntityId(typeEntityChild.getId());
									insertNode(function, typeEntityChild.getId());
									batchInserterService.insertNode(function);
									TypeContainsFunction containFunction = new TypeContainsFunction(type, function);
									batchInserterService.insertRelation(containFunction);
								} else if(typeEntityChild instanceof VarEntity) {
									Variable variable = new Variable();
									variable.setEntityId(typeEntityChild.getId());
									variable.setVariableName(typeEntityChild.getQualifiedName());
									insertNode(variable, typeEntityChild.getId());
									batchInserterService.insertNode(variable);
									TypeContainsVariable containVariable = new TypeContainsVariable(type, variable);
									batchInserterService.insertRelation(containVariable);
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
