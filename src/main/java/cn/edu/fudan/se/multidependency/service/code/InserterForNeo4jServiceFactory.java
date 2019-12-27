package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.utils.YamlUtils;
import depends.entity.repo.EntityRepo;

public class InserterForNeo4jServiceFactory {
	
	private static InserterForNeo4jServiceFactory instance = new InserterForNeo4jServiceFactory();
	
	private InserterForNeo4jServiceFactory() {}
	
	public static InserterForNeo4jServiceFactory getInstance() {
		return instance;
	}
	
	public InserterForNeo4j createCodeInserterService(String projectPath, EntityRepo entityRepo, String databasePath, boolean delete, Language language) throws Exception {
		switch(language) {
		case java:
			return new JavaInsertServiceImpl(projectPath, entityRepo, databasePath, delete, language);
		case cpp:
			return new CppInsertServiceImpl(projectPath, entityRepo, databasePath, delete, language);
		}
		throw new Exception("程序语言不为java或c/c++，提取失败");
	}
	
	public InserterForNeo4j createCodeInserterService(YamlUtils.YamlObject yaml, EntityRepo entityRepo, boolean delete) throws Exception {
		return createCodeInserterService(yaml.getCodeProjectPath(), entityRepo, yaml.getNeo4jDatabasePath(), delete, Language.valueOf(yaml.getCodeLanguage()));
	}
}
