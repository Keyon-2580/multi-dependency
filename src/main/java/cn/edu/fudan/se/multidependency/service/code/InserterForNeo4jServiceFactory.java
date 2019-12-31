package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelations;
import cn.edu.fudan.se.multidependency.utils.YamlUtils;
import depends.entity.repo.EntityRepo;

public class InserterForNeo4jServiceFactory {
	
	private static InserterForNeo4jServiceFactory instance = new InserterForNeo4jServiceFactory();
	
	private InserterForNeo4jServiceFactory() {}
	
	public static InserterForNeo4jServiceFactory getInstance() {
		return instance;
	}
	
	public ExtractorForNodesAndRelations createCodeInserterService(String projectPath, EntityRepo entityRepo, Language language) throws Exception {
		switch(language) {
		case java:
			return new JavaInsertServiceImpl(projectPath, entityRepo, language);
		case cpp:
			return new CppInsertServiceImpl(projectPath, entityRepo, language);
		}
		throw new Exception("程序语言不为java或c/c++，提取失败");
	}
	
	public ExtractorForNodesAndRelations createCodeInserterService(YamlUtils.YamlObject yaml, EntityRepo entityRepo) throws Exception {
		return createCodeInserterService(yaml.getCodeProjectPath(), entityRepo, Language.valueOf(yaml.getCodeLanguage()));
	}
}
