package cn.edu.fudan.se.multidependency.service.deploy;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.service.InserterForNeo4j;

public class DeployInserterForNeo4j implements InserterForNeo4j{

	@Override
	public void insertToNeo4jDataBase() throws Exception {
		
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
