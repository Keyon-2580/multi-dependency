package fan.md.service.extract;

public interface InsertDependsCodeToNeo4j extends DependsEntityRepoExtractor {
	
	public void insertCodeToNeo4jDataBase(String databasePath, boolean delete) throws Exception ;
	
}
