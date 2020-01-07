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
	
	protected File[] dynamicFunctionCallFiles;
	protected File markFile;
	
	protected abstract void extractScenarioAndTestCaseAndFeatures();

	protected abstract void extractNodesAndRelations() throws Exception;
	
	@Override
	public void addNodesAndRelations() {
		try {
			extractScenarioAndTestCaseAndFeatures(); //分析.mark文件
			extractNodesAndRelations(); //分析.dat文件或
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File[] getDynamicFunctionCallFiles() {
		return dynamicFunctionCallFiles;
	}

	public void setDynamicFunctionCallFiles(File... dynamicFunctionCallFiles) {
		this.dynamicFunctionCallFiles = dynamicFunctionCallFiles;
	}

	public File getMarkFile() {
		return markFile;
	}

	public void setMarkFile(File markFile) {
		this.markFile = markFile;
	}

}
