package cn.edu.fudan.se.multidependency.service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;

public final class RepositoryService implements InserterForNeo4j {
	
	protected BatchInserterService batchInserterService = BatchInserterService.getInstance();
	
	private static RepositoryService repository = new RepositoryService();

	protected Long currentEntityId = new Long(0L);
	
	@Override
	public Long generateEntityId() {
		return currentEntityId++;
	}

	@Override
	public void setCurrentEntityId(Long entityId) {
		this.currentEntityId = entityId;
	}

	private RepositoryService() {}
	
	public static RepositoryService getInstance() {
		return repository;
	}
	
	private Nodes nodes = new Nodes();
	private Relations relations = new Relations();
	
	public Nodes getNodes() {
		return nodes;
	}
	
	public void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}
	
	public Relations getRelations() {
		return relations;
	}
	
	public void setRelations(Relations relations) {
		this.relations = relations;
	}

	@Override
	public void insertToNeo4jDataBase() throws Exception {
		System.out.println("start to store datas to database");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("开始时间：" + sdf.format(currentTime));
		batchInserterService.init(databasePath, delete);
		batchInserterService.insertNodes(nodes);
		batchInserterService.insertRelations(relations);
		closeBatchInserter();
		currentTime = new Timestamp(System.currentTimeMillis());
		System.out.println("结束时间：" + sdf.format(currentTime));
	}
	
	private String databasePath;
	private boolean delete;
	
	@Override
	public void setDatabasePath(String databasePath) {
		this.databasePath = databasePath;
	}

	@Override
	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	@Override
	public boolean addRelation(Relation relation) {
		this.relations.addRelation(relation);
		return true;
	}

	private void closeBatchInserter() {
		if(this.batchInserterService != null) {
			this.batchInserterService.close();
		}
	}

	/**
	 * 表示该节点属于哪个Project，Project可以为null
	 * @param node
	 * @param inProject 
	 * @return
	 */
	@Override
	public boolean addNode(Node node, Project inProject) {
		this.nodes.addNode(node, inProject);
		return true;
	}
}
