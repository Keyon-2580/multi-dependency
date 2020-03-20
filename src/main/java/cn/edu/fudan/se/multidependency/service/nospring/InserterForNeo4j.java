package cn.edu.fudan.se.multidependency.service.nospring;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;

public interface InserterForNeo4j {

	public Long generateEntityId();
	
	public void setCurrentEntityId(Long entityId);
	
	public void insertToNeo4jDataBase() throws Exception ;

	public void setDatabasePath(String databasePath);
	
	public void setDelete(boolean delete);
	
	public boolean addNode(Node node, Project inProject);
	
	public boolean addRelation(Relation relation);

	public Nodes getNodes();
	
	public Relations getRelations();
	
	public boolean existNode(Node node);
	
	public boolean existRelation(Relation relation);
	
}
