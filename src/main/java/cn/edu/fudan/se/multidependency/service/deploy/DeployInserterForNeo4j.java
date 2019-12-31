package cn.edu.fudan.se.multidependency.service.deploy;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.service.BatchInserterService;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;

public class DeployInserterForNeo4j implements InserterForNeo4j{
	
	private Integer currentEntityId = 0;

	@Override
	public Integer generateId() {
		return currentEntityId++;
	}

	@Override
	public void addNodesAndRelations() throws Exception {
		
	}

	protected BatchInserterService batchInserterService;
	
	@Override
	public void insertToNeo4jDataBase() throws Exception {
		System.out.println("start to store datas to database");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("开始时间：" + sdf.format(currentTime));
//		batchInserterService.init(databasePath, false);
//		batchInserterService.insertNodes(dynamicNodes);
//		batchInserterService.insertRelations(relations);
//		closeBatchInserter();
		currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("结束时间：" + sdf.format(currentTime));
	}

	@Override
	public Nodes getNodes() {
		return null;
	}

	@Override
	public Relations getRelations() {
		return null;
	}

	@Override
	public void setDatabasePath(String databasePath) {
		
	}

	@Override
	public void setDelete(boolean delete) {
		
	}

	@Override
	public void setLanguage(Language language) {
		
	}

}
