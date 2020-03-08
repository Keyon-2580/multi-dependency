package cn.edu.fudan.se.multidependency.service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import lombok.Getter;
import lombok.Setter;

public final class RepositoryService implements InserterForNeo4j {

	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryService.class);
	
	protected BatchInserterService batchInserterService = BatchInserterService.getInstance();
	
	private static RepositoryService repository = new RepositoryService();

	@Setter
	protected Long currentEntityId = new Long(0L);

	@Setter
	private String databasePath;

	@Setter
	private boolean delete;
	
	@Override
	public Long generateEntityId() {
		return currentEntityId++;
	}

	private RepositoryService() {}
	
	public static RepositoryService getInstance() {
		return repository;
	}
	
	@Getter
	private Nodes nodes = new Nodes();

	@Getter
	private Relations relations = new Relations();
	
	@Override
	public void insertToNeo4jDataBase() throws Exception {
		LOGGER.info("start to store datas to database");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		LOGGER.info("开始时间：" + sdf.format(new Timestamp(System.currentTimeMillis())));
		batchInserterService.init(databasePath, delete);
		batchInserterService.insertNodes(nodes);
		batchInserterService.insertRelations(relations);
		closeBatchInserter();
		LOGGER.info("结束时间：" + sdf.format(new Timestamp(System.currentTimeMillis())));
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

	@Override
	public boolean existNode(Node node) {
		return this.nodes.existNode(node);
	}

	@Override
	public boolean existRelation(Relation relation) {
		return this.relations.existRelation(relation);
	}
}
