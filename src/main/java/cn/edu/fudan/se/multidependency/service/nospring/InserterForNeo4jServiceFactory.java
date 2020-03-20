package cn.edu.fudan.se.multidependency.service.nospring;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.nospring.build.BuildInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.service.nospring.code.CppInsertServiceImpl;
import cn.edu.fudan.se.multidependency.service.nospring.code.JavaInsertServiceImpl;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.CppDynamicInserter;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.DynamicInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.service.nospring.dynamic.JavassistDynamicInserter;
import depends.entity.repo.EntityRepo;

public class InserterForNeo4jServiceFactory {
	
	private static InserterForNeo4jServiceFactory instance = new InserterForNeo4jServiceFactory();
	
	private InserterForNeo4jServiceFactory() {}
	
	public static InserterForNeo4jServiceFactory getInstance() {
		return instance;
	}
	
	public ExtractorForNodesAndRelations createCodeInserterService(String projectPath, String projectName, 
			EntityRepo entityRepo, Language language, boolean isMicroservice, String serviceGroupName) throws Exception {
		switch(language) {
		case java:
			return new JavaInsertServiceImpl(projectPath, projectName, entityRepo, language, isMicroservice, serviceGroupName);
		case cpp:
			return new CppInsertServiceImpl(projectPath, projectName, entityRepo, language, isMicroservice, serviceGroupName);
		}
		throw new Exception("程序语言不为java或c/c++，提取失败");
	}
	
	public BuildInserterForNeo4jService createBuildInserterService(Language language) {
		return new BuildInserterForNeo4jService();
	}
}
