package cn.edu.fudan.se.multidependency.service.extract;

import cn.edu.fudan.se.multidependency.model.Language;
import depends.entity.repo.EntityRepo;

public interface InsertDependsCodeToNeo4j {
	
	public void insertCodeToNeo4jDataBase() throws Exception ;
	
	public EntityRepo getEntityRepo();
	
	public void setEntityRepo(EntityRepo entityRepo);
	
	public void setDatabasePath(String databasePath);
	
	public void setDelete(boolean delete);
	
	public void setLanguage(Language language);
	
}
