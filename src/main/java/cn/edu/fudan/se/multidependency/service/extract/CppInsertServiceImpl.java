package cn.edu.fudan.se.multidependency.service.extract;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.CodeFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Package;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsFunction;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;
import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsType;
import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsVariable;
import cn.edu.fudan.se.multidependency.model.relation.code.PackageContainsFile;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeContainsFunction;

@Deprecated
public class CppInsertServiceImpl extends InsertServiceImpl {

	public CppInsertServiceImpl(String projectPath, EntityRepo entityRepo, String databasePath, boolean delete, Language language) {
		super(projectPath, entityRepo, databasePath, delete, language);
	}

	@Override
	protected void insertNodesWithContainRelations() throws LanguageErrorException {
		if(language != Language.cpp) {
			throw new LanguageErrorException();
		}
		entityRepo.getEntities().forEach(entity -> {
			if(entity instanceof FileEntity) {
				String fileName = entity.getQualifiedName();
				String packageName = null;
				if(fileName.contains("\\")) {
					packageName = fileName.substring(0, fileName.lastIndexOf("\\"));
				} else if(fileName.contains("/")) {
					packageName = fileName.substring(0, fileName.lastIndexOf("/"));
				} else {
					packageName = "default";
				}
				// cpp项目的Package是文件所在的路径
				Package pck = new Package();
				pck.setPackageName(packageName);
				batchInserterService.insertNode(pck);
				CodeFile file = new CodeFile();
				file.setFileName(entity.getRawName().getName());
				file.setPath(entity.getQualifiedName());
				file.setEntityId(entity.getId());
				batchInserterService.insertNode(file);
				PackageContainsFile packageContainFile = new PackageContainsFile(pck, file);
				batchInserterService.insertRelation(packageContainFile);
				entity.getChildren().forEach(fileEntityChild -> {
					if(fileEntityChild instanceof PackageEntity) {
						// 命名空间
					} else if(fileEntityChild instanceof FunctionEntity) {
						// 文件内的函数
						Function function = new Function();
						function.setFunctionName(fileEntityChild.getRawName().getName());
						function.setEntityId(fileEntityChild.getId());
						insertNode(function, fileEntityChild.getId());
						batchInserterService.insertNode(function);
						FileContainsFunction containFunction = new FileContainsFunction(file, function);
						batchInserterService.insertRelation(containFunction);
					} else if(fileEntityChild instanceof VarEntity) {
						Variable variable = new Variable();
						variable.setEntityId(fileEntityChild.getId());
						variable.setVariableName(fileEntityChild.getQualifiedName());
						insertNode(variable, fileEntityChild.getId());
						batchInserterService.insertNode(variable);
						FileContainsVariable containVariable = new FileContainsVariable(file, variable);
						batchInserterService.insertRelation(containVariable);
					}
				});
				List<TypeEntity> typeEntities = ((FileEntity) entity).getDeclaredTypes();
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
						}
					});
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
