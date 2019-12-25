package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.StaticCodeNodes;
import depends.entity.repo.EntityRepo;

public interface InsertCodeToNeo4j {
	
	public void insertCodeToNeo4jDataBase() throws Exception ;
	
	public StaticCodeNodes getStaticCodeNodes();
	
	public void setEntityRepo(EntityRepo entityRepo);
	
	public void setDatabasePath(String databasePath);
	
	public void setDelete(boolean delete);
	
	public void setLanguage(Language language);
}
