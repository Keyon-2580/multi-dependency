package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelationsImpl;

public abstract class DynamicInserterForNeo4jService extends ExtractorForNodesAndRelationsImpl {
	
	protected Map<Long, List<Scenario>> nodeEntityIdToScenarios = new HashMap<>();
	protected Map<Long, List<Feature>> nodeEntityIdToFeatures = new HashMap<>();
	protected Map<Long, TestCase> nodeEntityIdToTestCase = new HashMap<>();
	
	protected File executeFile;
	protected File markFile;
	
	protected abstract void extractScenarioAndTestCaseAndFeatures();

	protected abstract void extractNodesAndRelations() throws Exception;
	
	@Override
	public void addNodesAndRelations() {
		try {
			extractScenarioAndTestCaseAndFeatures();
			extractNodesAndRelations();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
