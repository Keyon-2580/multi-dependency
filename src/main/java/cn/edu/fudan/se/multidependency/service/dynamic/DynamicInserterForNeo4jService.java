package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelationsImpl;

public abstract class DynamicInserterForNeo4jService extends ExtractorForNodesAndRelationsImpl {
	
	protected Map<String, List<Function>> nameToFunctions = new HashMap<>();
	protected Map<String, List<Type>> nameToTypes = new HashMap<>();
	
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
