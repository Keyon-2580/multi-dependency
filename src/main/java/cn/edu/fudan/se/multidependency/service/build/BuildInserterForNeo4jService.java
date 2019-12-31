package cn.edu.fudan.se.multidependency.service.build;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.code.StaticCodeNodes;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.service.BatchInserterService;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;

public class BuildInserterForNeo4jService implements InserterForNeo4j {
	
	private Integer currentEntityId = 0;

	@Override
	public Integer generateId() {
		return currentEntityId++;
	}
	
	public BuildInserterForNeo4jService(StaticCodeNodes staticCodeNodes, String databasePath) {
		super();
//		this.staticCodeNodes = staticCodeNodes;
		this.databasePath = databasePath;
		this.nodes = new Nodes();
		this.relations = new Relations();
		this.batchInserterService = BatchInserterService.getInstance();
	}
	
	protected String databasePath;
	private Nodes nodes;
	private Relations relations;
	
//	private StaticCodeNodes staticCodeNodes;
	
	protected BatchInserterService batchInserterService;

	@Override
	public void addNodesAndRelations() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void insertToNeo4jDataBase() throws Exception {
		System.out.println("start to store datas to database");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("开始时间：" + sdf.format(currentTime));
		batchInserterService.init(databasePath, false);
		
		//操作
		//inserter
		batchInserterService.insertNodes(nodes);
		batchInserterService.insertRelations(relations);
		
		closeBatchInserter();
		currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("结束时间：" + sdf.format(currentTime));
	}
	
	@Override
	public Nodes getNodes() {
		return nodes;
	}

	@Override
	public Relations getRelations() {
		return relations;
	}

	@Override
	public void setDatabasePath(String databasePath) {
		this.databasePath = databasePath;
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
