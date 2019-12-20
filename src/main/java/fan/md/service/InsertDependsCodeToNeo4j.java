package fan.md.service;

public interface InsertDependsCodeToNeo4j extends DependsCodeExtractor {
	
	public void insertCodeToNeo4jDataBase(String databasePath, boolean delete) throws Exception ;
	
}
