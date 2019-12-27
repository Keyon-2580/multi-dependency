package cn.edu.fudan.se.multidependency;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.service.code.DependsEntityRepoExtractor;
import cn.edu.fudan.se.multidependency.service.code.DependsEntityRepoExtractorImpl;
import cn.edu.fudan.se.multidependency.service.code.InserterForNeo4jServiceFactory;
import cn.edu.fudan.se.multidependency.utils.YamlUtils;
import depends.entity.repo.EntityRepo;

public class InsertDataMain {

    public static void main(String[] args) throws Exception {
    	insert();
	}
    
    public static void insert() {
		try {
			YamlUtils.YamlObject yaml = YamlUtils.getDataBasePath("src/main/resources/application.yml");

			/**
			 * 静态分析
			 */
			Nodes staticCodeNodes = insertStaticCode(yaml);
			System.out.println("节点数：" + staticCodeNodes.size());
			
			///FIXME
			//其它
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static Nodes insertStaticCode(YamlUtils.YamlObject yaml) throws Exception {
		String projectPath = yaml.getCodeProjectPath();
		Language language = Language.valueOf(yaml.getCodeLanguage());
    	DependsEntityRepoExtractor extractor = DependsEntityRepoExtractorImpl.getInstance();
		extractor.setLanguage(language);
		extractor.setProjectPath(projectPath);
		EntityRepo entityRepo = extractor.extractEntityRepo();
		
		InserterForNeo4j dependsInserter = InserterForNeo4jServiceFactory.getInstance().createCodeInserterService(yaml, entityRepo, true);
		dependsInserter.insertToNeo4jDataBase();
		return dependsInserter.getNodes();
    }
}
