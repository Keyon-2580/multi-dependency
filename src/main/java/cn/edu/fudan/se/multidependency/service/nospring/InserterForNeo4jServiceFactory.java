package cn.edu.fudan.se.multidependency.service.nospring;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.nospring.build.BuildInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.service.nospring.code.BasicCodeInserterForNeo4jServiceImpl;
import cn.edu.fudan.se.multidependency.service.nospring.code.CppInsertServiceImpl;
import cn.edu.fudan.se.multidependency.service.nospring.code.JavaInsertServiceImpl;
import cn.edu.fudan.se.multidependency.utils.ProjectConfigUtil;
import depends.entity.repo.EntityRepo;

public class InserterForNeo4jServiceFactory {
	
	private static InserterForNeo4jServiceFactory instance = new InserterForNeo4jServiceFactory();
	
	private InserterForNeo4jServiceFactory() {}
	
	public static InserterForNeo4jServiceFactory getInstance() {
		return instance;
	}
	
	public BasicCodeInserterForNeo4jServiceImpl createCodeInserterService(EntityRepo entityRepo, ProjectConfigUtil.ProjectConfig config) throws Exception {
		switch(config.getLanguage()) {
		case java:
			return new JavaInsertServiceImpl(entityRepo, config);
		case cpp:
			return new CppInsertServiceImpl(entityRepo, config);
		}
		throw new Exception("程序语言不为java或c/c++，提取失败");
	}
	
	public BuildInserterForNeo4jService createBuildInserterService(Language language) {
		return new BuildInserterForNeo4jService();
	}
}
