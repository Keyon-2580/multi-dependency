package fan.md;

import depends.entity.repo.EntityRepo;
import fan.md.model.Language;
import fan.md.service.extract.DependsEntityRepoExtractor;
import fan.md.service.extract.DependsEntityRepoExtractorImpl;
import fan.md.service.extract.InsertDependsCodeToNeo4j;
import fan.md.service.extract.InsertServiceImple;
import fan.md.utils.YamlUtils;

public class InsertDataMain {

    public static void main(String[] args) throws Exception {
    	insert();
	}
    
    public static void insert() {
		try {
			YamlUtils.YamlObject yaml = YamlUtils.getDataBasePath("src/main/resources/application.yml");
			String databasePath = yaml.getNeo4jDatabasePath();
			String projectPath = yaml.getCodeProjectPath();
			String language = yaml.getCodeLanguage();
			
			DependsEntityRepoExtractor extractor = DependsEntityRepoExtractorImpl.getInstance();
			extractor.setLanguage(Language.valueOf(language));
			extractor.setProjectPath(projectPath);
			EntityRepo entityRepo = extractor.extractEntityRepo();
			
			InsertDependsCodeToNeo4j insertCode = new InsertServiceImple(entityRepo, databasePath, true, Language.valueOf(language));
			insertCode.insertCodeToNeo4jDataBase();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
