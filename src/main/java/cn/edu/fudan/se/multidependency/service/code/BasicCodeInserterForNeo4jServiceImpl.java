package cn.edu.fudan.se.multidependency.service.code;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.StaticCodeNodes;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.service.BatchInserterService;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;

/**
 * 静态分析，将节点和关系插入neo4j
 * @author fan
 *
 */
public abstract class BasicCodeInserterForNeo4jServiceImpl implements InserterForNeo4j {
	
	private Integer currentEntityId = 0;

	@Override
	public Integer generateId() {
		return currentEntityId++;
	}
	
	public BasicCodeInserterForNeo4jServiceImpl(String projectPath, String databasePath, boolean delete, Language language) {
		super();
		this.databasePath = databasePath;
		this.delete = delete;
		this.language = language;
		this.batchInserterService = BatchInserterService.getInstance();
		this.nodes = new StaticCodeNodes();
		project = new Project(projectPath, projectPath, language);
		this.nodes.setProject(project);
		this.relations = new Relations();
	}

	protected Project project;
	
	protected String databasePath;
	protected boolean delete;
	protected Language language;
	
	protected StaticCodeNodes nodes;
	protected Relations relations;
	
	protected BatchInserterService batchInserterService;
	
	protected void addNodeToNodes(Node node, Integer entityId) {
		if(!node.getEntityId().equals(entityId)) {
			try {
				throw new Exception("节点id没有对应");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.nodes.addNode(node);
	}
	
	protected void insertRelationToRelations(Relation relation) {
		this.relations.addRelation(relation);
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
	
	public abstract void addNodesAndRelations() throws Exception;
	
	@Override
	public void setDatabasePath(String databasePath) {
		this.databasePath = databasePath;
	}

	@Override
	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	@Override
	public void setLanguage(Language language) {
		this.language = language;
	}

	private void closeBatchInserter() {
		if(this.batchInserterService != null) {
			this.batchInserterService.close();
		}
	}

	@Override
	public Nodes getNodes() {
		return nodes;
	}

	@Override
	public Relations getRelations() {
		return relations;
	}
	

}
