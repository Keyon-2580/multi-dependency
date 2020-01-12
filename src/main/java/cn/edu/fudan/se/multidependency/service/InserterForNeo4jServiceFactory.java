package cn.edu.fudan.se.multidependency.service;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.build.BuildInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.service.code.CppInsertServiceImpl;
import cn.edu.fudan.se.multidependency.service.code.JavaInsertServiceImpl;
import cn.edu.fudan.se.multidependency.service.dynamic.CppDynamicInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.service.dynamic.DynamicInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.service.dynamic.KiekerDynamicExecutionInserterForNeo4jService;
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
	
	public DynamicInserterForNeo4jService createDynamicInserterService(Language language) throws Exception {
		switch(language) {
		case java:
			return new KiekerDynamicExecutionInserterForNeo4jService();
		case cpp:
			return new CppDynamicInserterForNeo4jService();
		}
		throw new Exception("程序语言不为java或c/c++，提取失败");
	}
	
	public BuildInserterForNeo4jService createBuildInserterService(Language language) {
		return new BuildInserterForNeo4jService();
	}
}
