package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelationsImpl;

public abstract class DynamicInserterForNeo4jService extends ExtractorForNodesAndRelationsImpl {
	
	protected String scenarioName;
	protected List<String> featureName = new ArrayList<>();
	protected String testcaseName;
	
	protected File executeFile;
	protected File markFile;
	
	protected abstract void extractScenarioAndTestCaseAndFeatures();

	protected abstract void addNodesAndRelations(String scenarioName, List<String> featureName, String testcaseName,
			File executeFile) throws Exception;
	
	@Override
	public void addNodesAndRelations() {
		try {
			extractScenarioAndTestCaseAndFeatures();
			addNodesAndRelations(scenarioName, featureName, testcaseName, executeFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public List<String> getFeatureName() {
		return featureName;
	}

	public void setFeatureName(List<String> featureName) {
		this.featureName = featureName;
	}

	public String getTestcaseName() {
		return testcaseName;
	}

	public void setTestcaseName(String testcaseName) {
		this.testcaseName = testcaseName;
	}

	public File getExecuteFile() {
		return executeFile;
	}

	public void setExecuteFile(File executeFile) {
		this.executeFile = executeFile;
	}

	public File getMarkFile() {
		return markFile;
	}

	public void setMarkFile(File markFile) {
		this.markFile = markFile;
	}

}
