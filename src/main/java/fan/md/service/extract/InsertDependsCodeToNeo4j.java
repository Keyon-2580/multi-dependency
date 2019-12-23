package fan.md.service.extract;

import depends.entity.repo.EntityRepo;
import fan.md.model.Language;

public interface InsertDependsCodeToNeo4j {
	
	public void insertCodeToNeo4jDataBase() throws Exception ;
	
	public EntityRepo getEntityRepo();
	
	public void setEntityRepo(EntityRepo entityRepo);
	
	public void setDatabasePath(String databasePath);
	
	public void setDelete(boolean delete);
	
	public void setLanguage(Language language);
	
}
