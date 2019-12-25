package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.model.Language;
import depends.entity.repo.EntityRepo;
import cn.edu.fudan.se.multidependency.utils.YamlUtils;

public class InsertServiceFactory {
	
	private static InsertServiceFactory instance = new InsertServiceFactory();
	
	private InsertServiceFactory() {}
	
	public static InsertServiceFactory getInstance() {
		return instance;
	}
	
	public InsertDependsCodeToNeo4j createInsertService(String projectPath, EntityRepo entityRepo, String databasePath, boolean delete, Language language) throws Exception {
		switch(language) {
		case java:
//			return new JavaInsertServiceImpl(projectPath, entityRepo, databasePath, delete, language);
			return new NewJavaInsertServiceImpl(projectPath, entityRepo, databasePath, delete, language);
		case cpp:
//			return new CppInsertServiceImpl(projectPath, entityRepo, databasePath, delete, language);
			return new NewCppInsertServiceImpl(projectPath, entityRepo, databasePath, delete, language);
		}
		throw new Exception("程序语言不为java或c/c++，提取失败");
	}
	
	public InsertDependsCodeToNeo4j createInsertService(YamlUtils.YamlObject yaml, EntityRepo entityRepo, boolean delete) throws Exception {
		return createInsertService(yaml.getCodeProjectPath(), entityRepo, yaml.getNeo4jDatabasePath(), delete, Language.valueOf(yaml.getCodeLanguage()));
	}
}
