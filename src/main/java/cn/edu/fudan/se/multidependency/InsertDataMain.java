package cn.edu.fudan.se.multidependency;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.extract.DependsEntityRepoExtractor;
import cn.edu.fudan.se.multidependency.service.extract.DependsEntityRepoExtractorImpl;
import cn.edu.fudan.se.multidependency.service.extract.InsertServiceFactory;
import depends.entity.repo.EntityRepo;
import cn.edu.fudan.se.multidependency.utils.YamlUtils;

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
			
			InsertServiceFactory.getInstance().createInsertService(yaml, entityRepo, true).insertCodeToNeo4jDataBase();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
