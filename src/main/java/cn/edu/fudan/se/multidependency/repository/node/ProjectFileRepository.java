package cn.edu.fudan.se.multidependency.repository.node;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.IssueType;
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
	
	@Query("match (f:ProjectFile) where f.path=$filePath return f")
	public ProjectFile findFileByPath(@Param("filePath") String filePath);

	@Query("MATCH (file:ProjectFile)\r\n" +
			"WITH file, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "]->(:Type)) as noc, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "*2..3]->(:Function)) as nom \r\n" +
			"SET file += {noc: noc, nom: nom};")
	public void setFileMetrics();
	
	/**
	 * 所有文件的指标
	 * @return
	 */
	@Query("MATCH (file:ProjectFile)\r\n" +
			"WITH file, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "]->(:Type)) as noc, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "*2..3]->(:Function)) as nom, " +
			"     file.loc as loc, " +
			"     size((file)-[:"+ RelationType.str_DEPENDS_ON + "]->()) as fanOut, " +
			"     size((file)<-[:"+ RelationType.str_DEPENDS_ON + "]-()) as fanIn \r\n" +
			"RETURN  file,noc,nom,loc,fanOut,fanIn order by(file.path) desc;")
	public List<FileMetrics.StructureMetric> calculateFileStructureMetrics();
	
	@Query("MATCH (file:ProjectFile) \r\n" +
            "where id(file)= $fileId \r\n" +
			"WITH file, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "]->(:Type)) as noc, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "*2..3]->(:Function)) as nom, " +
			"     file.loc as loc, " +
			"     size((file)-[:"+ RelationType.str_DEPENDS_ON + "]->()) as fanOut, " +
			"     size((file)<-[:"+ RelationType.str_DEPENDS_ON + "]-()) as fanIn \r\n" +
			"RETURN  file,noc,nom,loc,fanOut,fanIn;")
	public FileMetrics.StructureMetric calculateFileStructureMetrics(@Param("fileId") long fileId);

	/**
	 * 所有文件的指标
	 * @return
	 */
	@Query("MATCH (file:ProjectFile) <-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit)<-[:" +
			RelationType.str_DEVELOPER_SUBMIT_COMMIT + "]- (d:Developer) \r\n" +
			"with file, count(distinct c) as commits, count(distinct d) as developers," +
			"     size((file)-[:" + RelationType.str_CO_CHANGE +"]-(:ProjectFile)) as coChangeFiles \r\n" +
			"RETURN  file,developers,commits,coChangeFiles order by(file.path) desc;")
	public List<FileMetrics.EvolutionMetric> calculateFileEvolutionMetrics();

	@Query("MATCH (file:ProjectFile) <-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit)<-[:" +
			RelationType.str_DEVELOPER_SUBMIT_COMMIT + "]- (d:Developer) \r\n" +
			"where id(file)= $fileId \r\n" +
			"with file, count(c) as commits,count(distinct d) as developers," +
			"     size((file)-[:" + RelationType.str_CO_CHANGE +"]-(:ProjectFile)) as coChangeFiles \r\n" +
			"RETURN  file,commits,developers,coChangeFiles;")
	public FileMetrics.EvolutionMetric calculateFileEvolutionMetrics(@Param("fileId") long fileId);

	@Query("MATCH (file:ProjectFile) <-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(:Commit)-[:" +
			RelationType.str_COMMIT_ADDRESS_ISSUE + "]-> (issue:Issue) \r\n" +
			"with file, collect(distinct issue) as issueList \r\n" +
			"with file, size(issueList) as issues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.BUG + "\' then 1 else 0 end)) as bugIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.NEW_FEATURE + "\' then 1 else 0 end)) as newFeatureIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.IMPROVEMENT + "\' then 1 else 0 end)) as improvementIssues \r\n" +
			"RETURN  file,issues,bugIssues,newFeatureIssues,improvementIssues order by(file.path) desc;")
	public List<FileMetrics.DebtMetric> calculateFileDebtMetrics();

	@Query("MATCH (file:ProjectFile) <-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(:Commit)-[:" +
			RelationType.str_COMMIT_ADDRESS_ISSUE + "]-> (issue:Issue) \r\n" +
			"where id(file)= $fileId \r\n" +
			"with file, collect(distinct issue) as issueList \r\n" +
			"with file, size(issueList) as issues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.BUG + "\' then 1 else 0 end)) as bugIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.NEW_FEATURE + "\' then 1 else 0 end)) as newFeatureIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.IMPROVEMENT + "\' then 1 else 0 end)) as improvementIssues \r\n" +
			"RETURN  file,issues,bugIssues,newFeatureIssues,improvementIssues;")
	public FileMetrics.DebtMetric calculateFileDebtMetrics(@Param("fileId") long fileId);
	
//	@Query("match (file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit) where id(file) = $fileId with c where size((c)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(:ProjectFile)) > 1 return c")
//	public List<Commit> cochangeCommitsWithFile(@Param("fileId") long fileId);
	
	/**
	 * 有commit更新的文件，并且该commit提交文件的个数大于1
	 * @return
	 */
	@Query("MATCH (file:ProjectFile)\r\n" + 
			"with file\r\n" +
			"match (file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit) \r\n" +
			"with file, c \r\n" +
			"where size((c)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(:ProjectFile)) > 1 " +
			"with file, count(c) as cochangeCommitTimes\r\n" +
			"WITH size((file)-[:" + RelationType.str_DEPENDS_ON + "]->()) as fanOut, \r\n" + 
			"     size((file)<-[:" + RelationType.str_DEPENDS_ON + "]-()) as fanIn,\r\n" + 
			"     size((file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-()) as commits,\r\n" +
			"     size((file)-[:" + RelationType.str_CONTAIN + "*1..3]->(:Function)) as nom,\r\n" + 
			"     size((file)-[:" + RelationType.str_CO_CHANGE + "]-(:ProjectFile)) as coChangeFile,\r\n" +
			"     file.endLine as loc,\r\n" + 
			"     file.score as score,\r\n" + 
			"     cochangeCommitTimes,\r\n" + 
			"     file\r\n" + 
			"RETURN  file,fanIn,fanOut,commits,cochangeCommitTimes,nom,loc,score,coChangeFiles order by(file.path) desc;")
	public List<FileMetrics> calculateFileMetricsWithCoChangeCommitTimes();
	
	@Query("CALL gds.pageRank.stream({" +
			"nodeProjection:\'ProjectFile\', " +
			"relationshipProjection: \'" + RelationType.str_DEPENDS_ON + "\', " +
			"maxIterations: $iterations, " +
			"dampingFactor: $dampingFactor}) " +
			"YIELD nodeId, score " +
			"with gds.util.asNode(nodeId) AS file, score " +
			"set file.score=score " +
			"RETURN file " +
			"ORDER BY score DESC")
	public List<ProjectFile> pageRank(@Param("iterations") int iterations, @Param("dampingFactor") double dampingFactor);
	
	@Query("match (f1:ProjectFile)-[:DEPENDS_ON]->(f2:ProjectFile) where id(f2)=$fileId return f1")
	public List<ProjectFile> calculateFanIn(@Param("fileId") long fileId);
	
	@Query("match (f1:ProjectFile)-[:DEPENDS_ON]->(f2:ProjectFile) where id(f1)=$fileId return f2")
	public List<ProjectFile> calculateFanOut(@Param("fileId") long fileId);
	
	@Query("match (f1:ProjectFile)-[:IMPORT]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImportBtwFile(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:INCLUDE]->(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getIncludeBtwFile(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[:EXTENDS]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getExtendBtwType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[:IMPLEMENTS]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImplementBtwType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[:IMPLEMENTS]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImplementBtwFunc(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:CREATE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCreateBtwType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:CREATE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCreateFromFuncToType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:IMPLLINK]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImpllinkBtwFunc(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:CALL]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCallBtwFunc(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:CALL]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCallFromTypeToFunc(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:ACCESS]->(:Variable)<-[:CONTAIN*1..4]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getAccessFromFuncToVar(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[:CONTAIN]->(:Variable)-[r:VARIABLE_TYPE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getMemberVarTypeFromVarToType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[:CONTAIN]->(:Variable)-[r:VARIABLE_TYPE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getLocalVarTypeFromVarToType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:RETURN]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getReturnFromFuncToType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:THROW]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getThrowFromFuncToType(@Param("fileId") long fileId);

	//泛型
	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Variable)-[r:PARAMETER]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getParaFromVarToType(@Param("fileId") long fileId);

	//函数中包含强转
	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:CAST]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCastFromFuncToType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:DYNAMIC_CALL]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getDynamicCallBtwFunc(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[r:CO_CHANGE]->(f2:ProjectFile) where id(f1)=$fileId return f2 as projectFile, r.times as count")
	List<DependencyPair> getCoChangeFiles(@Param("fileId") long fileId);

}
