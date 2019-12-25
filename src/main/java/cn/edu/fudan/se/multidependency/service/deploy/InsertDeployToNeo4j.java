package cn.edu.fudan.se.multidependency.service.deploy;

import cn.edu.fudan.se.multidependency.model.node.Nodes;

public interface InsertDeployToNeo4j {

	public void insertDeployToNeo4jDataBase() throws Exception ;
	
	public Nodes getNodes();
	
	public void setDatabasePath(String databasePath);
	
	public void setDelete(boolean delete);
}
