package cn.edu.fudan.se.multidependency;

import java.io.File;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelations;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.service.RepositoryService;
import cn.edu.fudan.se.multidependency.service.code.DependsEntityRepoExtractor;
import cn.edu.fudan.se.multidependency.service.code.DependsEntityRepoExtractorImpl;
import cn.edu.fudan.se.multidependency.service.code.InserterForNeo4jServiceFactory;
import cn.edu.fudan.se.multidependency.service.dynamic.DynamicInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.service.dynamic.KiekerDynamicInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.utils.YamlUtils;
import depends.entity.repo.EntityRepo;

public class InsertDataMain {

    public static void main(String[] args) throws Exception {
    	insert();
	}
    
    public static void insert() {
		try {
			YamlUtils.YamlObject yaml = YamlUtils.getDataBasePath("src/main/resources/application.yml");
			String projectPath = yaml.getCodeProjectPath();
			Language language = Language.valueOf(yaml.getCodeLanguage());
	    	DependsEntityRepoExtractor extractor = DependsEntityRepoExtractorImpl.getInstance();
			extractor.setLanguage(language);
			extractor.setProjectPath(projectPath);
			EntityRepo entityRepo = extractor.extractEntityRepo();
			
			InserterForNeo4j repository = RepositoryService.getInstance();
			repository.setDatabasePath(yaml.getNeo4jDatabasePath());
			repository.setDelete(true);
			/**
			 * 静态分析
			 */
			insertStaticCode(yaml, entityRepo);
			/**
			 * 动态分析
			 */
			File directory = new File("src/main/resources/dynamic/kieker");
			insertDynamicCall(directory.listFiles());
			///FIXME
			//其它
			
			
			//最后统一插入数据库
			repository.insertToNeo4jDataBase();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static Nodes insertStaticCode(YamlUtils.YamlObject yaml, EntityRepo entityRepo) throws Exception {
		ExtractorForNodesAndRelations dependsInserter = InserterForNeo4jServiceFactory.getInstance().createCodeInserterService(yaml, entityRepo);
		dependsInserter.addNodesAndRelations();
		return dependsInserter.getNodes();
    }
    
    public static void insertDynamicCall(File... files) throws Exception {
    	DynamicInserterForNeo4jService kiekerInserter = new KiekerDynamicInserterForNeo4jService();
    	for(File file : files) {
    		kiekerInserter.setExecuteFile(file);
    		kiekerInserter.addNodesAndRelations();
    	}
    }
}
