package cn.edu.fudan.se.multidependency.repository.node;

import java.util.List;

import cn.edu.fudan.se.multidependency.service.query.ar.DependencyPair;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetrics;

@Repository
public interface ProjectFileRepository extends Neo4jRepository<ProjectFile, Long> {
	
	@Query("match (f:ProjectFile) where f.path={filePath} return f")
	public ProjectFile findFileByPath(@Param("filePath") String filePath);
	
	/**
	 * 所有文件的指标
	 * @return
	 */
	@Query("MATCH (file:ProjectFile)\r\n" + 
			"WITH size((file)-[:DEPENDS_ON]->()) as fanOut, \r\n" + 
			"     size((file)<-[:DEPENDS_ON]-()) as fanIn,\r\n" + 
			"     size((file)<-[:COMMIT_UPDATE_FILE]-()) as changeTimes,\r\n" + 
			"     size((file)-[:CONTAIN*1..3]->(:Function)) as nom,\r\n" + 
			"     size((file)-[:CO_CHANGE]-(:ProjectFile)) as cochangeFileCount,\r\n" + 
			"     file.endLine as loc,\r\n" + 
			"     file\r\n" + 
			"RETURN  file,fanIn,fanOut,changeTimes,nom,loc,cochangeFileCount order by(file.path) desc;")
	public List<FileMetrics> calculateFileMetrics();
	
	@Query("MATCH (file:ProjectFile) where id(file)={fileId} \r\n" + 
			"WITH size((file)-[:DEPENDS_ON]->()) as fanOut, \r\n" + 
			"     size((file)<-[:DEPENDS_ON]-()) as fanIn,\r\n" + 
			"     size((file)<-[:COMMIT_UPDATE_FILE]-()) as changeTimes,\r\n" + 
			"     size((file)-[:CONTAIN*1..3]->(:Function)) as nom,\r\n" + 
			"     size((file)-[:CO_CHANGE]-(:ProjectFile)) as cochangeFileCount,\r\n" + 
			"     file.endLine as loc,\r\n" + 
			"     file.instability as instability,\r\n" + 
			"     file\r\n" + 
			"RETURN  file,fanIn,fanOut,changeTimes,nom,loc,instability,cochangeFileCount order by(file.path) desc;")
	public FileMetrics calculateFileMetrics(@Param("fileId") long fileId);
	
//	@Query("match (file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit) where id(file) = {fileId} with c where size((c)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(:ProjectFile)) > 1 return c")
//	public List<Commit> cochangeCommitsWithFile(@Param("fileId") long fileId);
	
	/**
	 * 有commit更新的文件，并且该commit提交文件的个数大于1
	 * @return
	 */
	@Query("MATCH (file:ProjectFile)\r\n" + 
			"with file\r\n" +
			"match (file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit) with file, c where size((c)-[:" 
				+ RelationType.str_COMMIT_UPDATE_FILE + "]->(:ProjectFile)) > 1 with file, count(c) as cochangeCommitTimes\r\n" + 
			"WITH size((file)-[:" + RelationType.str_DEPENDS_ON + "]->()) as fanOut, \r\n" + 
			"     size((file)<-[:" + RelationType.str_DEPENDS_ON + "]-()) as fanIn,\r\n" + 
			"     size((file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-()) as changeTimes,\r\n" + 
			"     size((file)-[:" + RelationType.str_CONTAIN + "*1..3]->(:Function)) as nom,\r\n" + 
			"     size((file)-[:" + RelationType.str_CO_CHANGE + "]-(:ProjectFile)) as cochangeFileCount,\r\n" + 
			"     file.endLine as loc,\r\n" + 
			"     file.score as score,\r\n" + 
			"     cochangeCommitTimes,\r\n" + 
			"     file\r\n" + 
			"RETURN  file,fanIn,fanOut,changeTimes,cochangeCommitTimes,nom,loc,score,cochangeFileCount order by(file.path);")
	public List<FileMetrics> calculateFileMetricsWithCoChangeCommitTimes();
	
	@Query("CALL gds.pageRank.stream({" +
			"nodeProjection:\'ProjectFile\', " +
			"relationshipProjection: \'" + RelationType.str_DEPENDS_ON + "\', " +
			"maxIterations: {iterations}, " +
			"dampingFactor: {dampingFactor}}) \r\n" +
			"YIELD nodeId, score\r\n" + 
			"with gds.util.asNode(nodeId) AS file, score \r\n" +
			"set file.score=score\r\n" + 
			"RETURN file\r\n" + 
			"ORDER BY score DESC")
	public List<ProjectFile> pageRank(@Param("iterations") int iterations, @Param("dampingFactor") double dampingFactor);
	
	@Query("match (f1:ProjectFile)-[r:DEPENDS_ON]->(f2:ProjectFile) where id(f2)={fileId} return f1")
	public List<ProjectFile> calculateFanIn(@Param("fileId") long fileId);
	
	@Query("match (f1:ProjectFile)-[r:DEPENDS_ON]->(f2:ProjectFile) where id(f1)={fileId} return f2")
	public List<ProjectFile> calculateFanOut(@Param("fileId") long fileId);
	
	@Query("match (f1:ProjectFile)-[r:IMPORT]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImportBtwFile(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[r:INCLUDE]->(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getIncludeBtwFile(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:EXTENDS]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getExtendBtwType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:IMPLEMENTS]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImplementBtwType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:IMPLEMENTS]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImplementBtwFunc(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:CREATE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCreateBtwType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:CREATE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCreateFromFuncToType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:IMPLLINK]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImpllinkBtwFunc(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:CALL]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCallBtwFunc(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:CALL]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCallFromTypeToFunc(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:ACCESS]->(:Variable)<-[:CONTAIN*1..4]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getAccessFromFuncToVar(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[:CONTAIN]->(:Variable)-[r:VARIABLE_TYPE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getMemberVarTypeFromVarToType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[:CONTAIN]->(:Variable)-[r:VARIABLE_TYPE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getLocalVarTypeFromVarToType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:RETURN]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getReturnFromFuncToType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:THROW]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getThrowFromFuncToType(@Param("fileId") long fileId);

	//泛型
	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Variable)-[r:PARAMETER]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getParaFromVarToType(@Param("fileId") long fileId);

	//函数中包含强转
	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:CAST]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCastFromFuncToType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:DYNAMIC_CALL]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)={fileId} and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getDynamicCallBtwFunc(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[r:CO_CHANGE]->(f2:ProjectFile) where id(f1)={fileId} return f2 as projectFile, r.times as count")
	List<DependencyPair> getCoChangeFiles(@Param("fileId") long fileId);

}
