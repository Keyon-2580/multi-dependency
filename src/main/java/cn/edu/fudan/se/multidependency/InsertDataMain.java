package cn.edu.fudan.se.multidependency;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.StaticCodeNodes;
import cn.edu.fudan.se.multidependency.service.extract.DependsEntityRepoExtractor;
import cn.edu.fudan.se.multidependency.service.extract.DependsEntityRepoExtractorImpl;
import cn.edu.fudan.se.multidependency.service.extract.InsertDependsCodeToNeo4j;
import cn.edu.fudan.se.multidependency.service.extract.InsertServiceFactory;
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
			StaticCodeNodes staticCodeNodes = insertStaticCode(yaml);
			System.out.println("节点数：" + staticCodeNodes.size());
			
			///FIXME
			//其它
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static StaticCodeNodes insertStaticCode(YamlUtils.YamlObject yaml) throws Exception {
		String projectPath = yaml.getCodeProjectPath();
		Language language = Language.valueOf(yaml.getCodeLanguage());
    	DependsEntityRepoExtractor extractor = DependsEntityRepoExtractorImpl.getInstance();
		extractor.setLanguage(language);
		extractor.setProjectPath(projectPath);
		EntityRepo entityRepo = extractor.extractEntityRepo();
		
		InsertDependsCodeToNeo4j dependsInserter = InsertServiceFactory.getInstance().createInsertService(yaml, entityRepo, true);
		dependsInserter.insertCodeToNeo4jDataBase();
		return dependsInserter.getStaticCodeNodes();
    }
}
