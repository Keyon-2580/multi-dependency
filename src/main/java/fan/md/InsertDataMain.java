package fan.md;

import java.io.File;
import java.util.Map;

import org.ho.yaml.Yaml;

import fan.md.service.DependsCodeInsertService;
import fan.md.service.InsertDependsCodeToNeo4j;

public class InsertDataMain {

    public static void main1(String[] args) throws Exception {
    	insert();
	}
    
    public static void insert() {
		try {
			File file = new File("src/main/resources/application.yml");
			Map<?, ?> yaml = (Map<?, ?>) Yaml.load(file);
			String databasePath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("neo4j")).get("path");
			String projectPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("code")).get("path");
			String language = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("code")).get("language");
			InsertDependsCodeToNeo4j codeExtractor = DependsCodeInsertService.getInstance();
			codeExtractor.extractEntityRepo(projectPath, language);
			codeExtractor.insertCodeToNeo4jDataBase(databasePath, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
