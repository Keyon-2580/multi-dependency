package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Package;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.code.FileContainsType;
import cn.edu.fudan.se.multidependency.model.relation.code.FileImportFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.FileImportType;
import cn.edu.fudan.se.multidependency.model.relation.code.FileImportVariable;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionContainsVariable;
import cn.edu.fudan.se.multidependency.model.relation.code.PackageContainsFile;
import cn.edu.fudan.se.multidependency.model.relation.code.ProjectContainsPackage;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeContainsFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeContainsVariable;
import cn.edu.fudan.se.multidependency.utils.FileUtils;
import depends.entity.Entity;
import depends.entity.FileEntity;
import depends.entity.FunctionEntity;
import depends.entity.PackageEntity;
import depends.entity.TypeEntity;
import depends.entity.VarEntity;
import depends.entity.repo.EntityRepo;

public class JavaInsertServiceImpl extends DependsCodeInserterForNeo4jServiceImpl {

	public JavaInsertServiceImpl(String projectPath, EntityRepo entityRepo, String databasePath, boolean delete,
			Language language) {
		super(projectPath, entityRepo, databasePath, delete, language);
	}
	
	@Override
	protected void addNodesWithContainRelations() throws LanguageErrorException {
		entityRepo.getEntities().forEach(entity -> {
			// 每个entity对应相应的node
			if(entity instanceof PackageEntity) {
				Package pck = new Package();
				pck.setPackageName(entity.getQualifiedName());
				pck.setEntityId(entity.getId());
				pck.setDirectory(false);
				addNodeToNodes(pck, entity.getId());
				ProjectContainsPackage projectContainsPackage = new ProjectContainsPackage(project, pck);
				insertRelationToRelations(projectContainsPackage);
			} else if(entity instanceof FileEntity) {
				ProjectFile file = new ProjectFile();
				file.setEntityId(entity.getId());
				file.setFileName(entity.getQualifiedName());
				file.setPath(entity.getQualifiedName());
				file.setSuffix(FileUtils.extractSuffix(entity.getQualifiedName()));
				addNodeToNodes(file, entity.getId());
			} else if(entity instanceof FunctionEntity) {
				Function function = new Function();
				function.setFunctionName(entity.getQualifiedName());
				function.setEntityId(entity.getId());
				addNodeToNodes(function, entity.getId());
			} else if(entity instanceof VarEntity) {
				Variable variable = new Variable();
				variable.setEntityId(entity.getId());
				variable.setTypeIdentify(((VarEntity) entity).getRawType().getName());
				variable.setVariableName(entity.getQualifiedName());
				addNodeToNodes(variable, entity.getId());
			} else if(entity.getClass() == TypeEntity.class) {
				Type type = new Type();
				type.setEntityId(entity.getId());
				type.setTypeName(entity.getQualifiedName());
				addNodeToNodes(type, entity.getId());
			}
		});
		this.nodes.findFiles().forEach((entityId, codeFile) -> {
			PackageContainsFile containFile = new PackageContainsFile();
//			containFile.setEnd(codeFile);
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
					addNodeToNodes(pck, pck.getEntityId());
					ProjectContainsPackage projectContainsPackage = new ProjectContainsPackage(project, pck);
					insertRelationToRelations(projectContainsPackage);
				}
			} else {
				pck = this.nodes.findPackage(parentEntity.getId());
			}
			containFile.setPck(pck);
//			containFile.setStart(pck);
			insertRelationToRelations(containFile);
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
			ProjectFile file = this.nodes.findCodeFile(parentEntity.getId());
			FileContainsType fileContainsType = new FileContainsType(file, type);
			insertRelationToRelations(fileContainsType);
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
			insertRelationToRelations(typeContainsFunction);
			for(VarEntity varEntity : functionEntity.getParameters()) {
				String parameterName = varEntity.getRawType().getName();
				TypeEntity typeEntity = varEntity.getType();
				if(typeEntity != null && nodes.findType(typeEntity.getId()) != null) {
					function.addParameterIdentifies(typeEntity.getQualifiedName());
				} else {
					function.addParameterIdentifies(parameterName);
				}
			}

		});
		this.nodes.findVariables().forEach((entityId, variable) -> {
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId);
			Entity parentEntity = varEntity.getParent();
			if(parentEntity instanceof FunctionEntity) {
				Function function = this.nodes.findFunction(parentEntity.getId());
				FunctionContainsVariable functionContainsVariable = new FunctionContainsVariable(function, variable);
				insertRelationToRelations(functionContainsVariable);
			} else if(parentEntity.getClass() == TypeEntity.class) {
				Type type = this.nodes.findType(parentEntity.getId());
				TypeContainsVariable typeContainsVariable = new TypeContainsVariable(type, variable);
				insertRelationToRelations(typeContainsVariable);
			} else {
//				System.out.println(varEntity);
//				System.out.println(parentEntity.getClass() + " " + parentEntity);
			}
		});
	}
	
	@Override
	protected void addRelations() throws LanguageErrorException {
		extractRelationsFromTypes();
		extractRelationsFromFunctions();
		extractRelationsFromVariables();		
		extractRelationsFromFiles();
//		extractRelationsFromDependsType();
	}
	
	protected void extractRelationsFromFiles() {
		nodes.findFiles().forEach((entityId, file) -> {
			FileEntity fileEntity = (FileEntity) entityRepo.getEntity(entityId);
			fileEntity.getImportedTypes().forEach(entity -> {
				if(entity instanceof FunctionEntity) {
					Function function = nodes.findFunction(entity.getId());
					if(function != null) {
						FileImportFunction fileImportFunction = new FileImportFunction(file, function);
						insertRelationToRelations(fileImportFunction);
					}
				} else if(entity instanceof VarEntity) {
					Variable variable = nodes.findVariable(entity.getId());
					if(variable != null) {
						FileImportVariable fileImportVariable = new FileImportVariable(file, variable);
						insertRelationToRelations(fileImportVariable);
					}
				} else if(entity.getClass() == TypeEntity.class) {
					Type type = nodes.findType(entity.getId());
					if(type != null) {
						FileImportType fileImportType = new FileImportType(file, type);
						insertRelationToRelations(fileImportType);
					}
				} else {
					// MultiDeclareEntities
					System.out.println(entity.getClass());
				}
			});
		});
	}
}
