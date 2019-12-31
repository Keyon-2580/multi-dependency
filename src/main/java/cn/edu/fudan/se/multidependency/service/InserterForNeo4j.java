package cn.edu.fudan.se.multidependency.service;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.relation.Relations;

public interface InserterForNeo4j {
	
	public Integer generateId();
	
	public void insertToNeo4jDataBase() throws Exception ;
	
	public void addNodesAndRelations() throws Exception ;
	
	public Nodes getNodes();
	
	public Relations getRelations();
	
	public void setDatabasePath(String databasePath);
	
	public void setDelete(boolean delete);
	
	public void setLanguage(Language language);
	
}
