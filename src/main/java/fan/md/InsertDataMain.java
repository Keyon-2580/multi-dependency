package fan.md;

import depends.entity.repo.EntityRepo;
import fan.md.model.Language;
import fan.md.service.extract.DependsEntityRepoExtractor;
import fan.md.service.extract.DependsEntityRepoExtractorImpl;
import fan.md.service.extract.InsertServiceFactory;
import fan.md.utils.YamlUtils;

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
