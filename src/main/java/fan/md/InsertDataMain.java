package fan.md;

import fan.md.service.DependsCodeInsertService;
import fan.md.service.InsertDependsCodeToNeo4j;
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
			
			InsertDependsCodeToNeo4j codeExtractor = DependsCodeInsertService.getInstance();
			codeExtractor.extractEntityRepo(projectPath, language);
			codeExtractor.insertCodeToNeo4jDataBase(databasePath, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
