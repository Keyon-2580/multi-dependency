package cn.edu.fudan.se.multidependency;

import java.io.File;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.code.StaticCodeNodes;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;
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
			/**
			 * 静态分析
			 */
			Nodes staticCodeNodes = insertStaticCode(yaml, entityRepo);
			System.out.println("节点数：" + staticCodeNodes.size());
			insertDynamicCall(yaml, (StaticCodeNodes) staticCodeNodes);
			///FIXME
			//其它
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static Nodes insertStaticCode(YamlUtils.YamlObject yaml, EntityRepo entityRepo) throws Exception {
		InserterForNeo4j dependsInserter = InserterForNeo4jServiceFactory.getInstance().createCodeInserterService(yaml, entityRepo, true);
		dependsInserter.insertToNeo4jDataBase();
		return dependsInserter.getNodes();
    }
    
    public static void insertDynamicCall(YamlUtils.YamlObject yaml, StaticCodeNodes staticCodeNodes, File... files) throws Exception {
    	DynamicInserterForNeo4jService kiekerInserter = new KiekerDynamicInserterForNeo4jService(staticCodeNodes, yaml.getCodeProjectPath(), yaml.getNeo4jDatabasePath(), Language.valueOf(yaml.getCodeLanguage()));
    	kiekerInserter.setDelete(false);
    	String testFile = new String("src/test/resources/kieker/kieker-test.dat");
    	testFile = "D:\\fan\\analysis\\depends-0.9.5c\\kieker-JavaFileImportTest\\kieker-20191230-080951606-UTC-001.dat";
    	kiekerInserter.setExecuteFile(new File(testFile));
    	kiekerInserter.insertToNeo4jDataBase();
    }
}
