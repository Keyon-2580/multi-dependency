package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.code.StaticCodeNodes;
import cn.edu.fudan.se.multidependency.model.node.testcase.DynamicNodes;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.service.BatchInserterService;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;

public abstract class DynamicInserterForNeo4jService implements InserterForNeo4j {
	
	private Integer currentEntityId = 0;

	@Override
	public Integer generateId() {
		return currentEntityId++;
	}
	
	public DynamicInserterForNeo4jService(StaticCodeNodes staticCodeNodes, String databasePath) {
		super();
		this.staticCodeNodes = staticCodeNodes;
		this.databasePath = databasePath;
		this.dynamicNodes = new DynamicNodes();
		this.relations = new Relations();
		this.batchInserterService = BatchInserterService.getInstance();
	}

	protected String databasePath;
	
	protected StaticCodeNodes staticCodeNodes;
	protected DynamicNodes dynamicNodes;
	protected Relations relations;
	
	protected String scenarioName;
	protected List<String> featureName = new ArrayList<>();
	protected String testcaseName;
	protected File executeFile;

	protected BatchInserterService batchInserterService;
	
	@Override
	public void insertToNeo4jDataBase() throws Exception {
		System.out.println("start to store datas to database");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("开始时间：" + sdf.format(currentTime));
		batchInserterService.init(databasePath, false);
		batchInserterService.insertNodes(dynamicNodes);
		batchInserterService.insertRelations(relations);
		closeBatchInserter();
		currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("结束时间：" + sdf.format(currentTime));
	}
	
	@Override
	public void addNodesAndRelations() throws Exception {
		extractScenarioAndTestCaseAndFeatures();
		addNodesAndRelations(scenarioName, featureName, testcaseName, executeFile);
	}
	
	protected abstract void extractScenarioAndTestCaseAndFeatures();

	protected abstract void addNodesAndRelations(String scenarioName, List<String> featureName, String testcaseName,
			File executeFile) throws Exception;
	
	public Nodes getNodes() {
		return dynamicNodes;
	}

	public Relations getRelations() {
		return relations;
	}

	@Override
	public void setDatabasePath(String databasePath) {
		this.databasePath = databasePath;
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

	@Override
	public void setDelete(boolean delete) {}

	@Override
	public void setLanguage(Language language) {}
	
	private void closeBatchInserter() {
		if(this.batchInserterService != null) {
			this.batchInserterService.close();
		}
	}

}
