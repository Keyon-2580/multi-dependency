package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.StaticCodeNodes;

public class KiekerDynamicInserterForNeo4jService extends DynamicInserterForNeo4jService {

	public KiekerDynamicInserterForNeo4jService(StaticCodeNodes staticCodeNodes, String projectPath,
			String databasePath, Language language) {
		super(staticCodeNodes, projectPath, databasePath, language);
	}
	
	protected void insertToNeo4jDataBase(String scenarioName, List<String> featureName, String testcaseName,
			File executeFile) throws Exception {
		extractFunctionNodes(executeFile);
	}
	
	protected void extractFunctionNodes(File executeFile2) {
		
	}

}
