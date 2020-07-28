package cn.edu.fudan.se.multidependency.repository.node;

import java.util.Collection;
import java.util.List;

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
	
	@Query("MATCH (file:ProjectFile)\r\n" + 
			"with file\r\n" +
			"match (file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit) with file, c where size((c)-[:" 
				+ RelationType.str_COMMIT_UPDATE_FILE + "]->(:ProjectFile)) > 1 with file, count(c) as cochangeTimes\r\n" + 
			"WITH size((file)-[:" + RelationType.str_DEPENDS_ON + "]->()) as fanOut, \r\n" + 
			"     size((file)<-[:" + RelationType.str_DEPENDS_ON + "]-()) as fanIn,\r\n" + 
			"     size((file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-()) as changeTimes,\r\n" + 
			"     size((file)-[:" + RelationType.str_CONTAIN + "*1..3]->(:Function)) as nom,\r\n" + 
			"     file.endLine as loc,\r\n" + 
			"     file.score as score,\r\n" + 
			"     cochangeTimes,\r\n" + 
			"     file\r\n" + 
			"RETURN  file,fanIn,fanOut,changeTimes,cochangeTimes,nom,loc,score order by(file.path);")
	public List<FileMetrics> calculateFileMetrics();
	
	@Query("CALL algo.pageRank.stream('ProjectFile', '" + RelationType.str_DEPENDS_ON + "', {iterations:{iterations}, dampingFactor:{dampingFactor}})\r\n" + 
			"YIELD nodeId, score\r\n" + 
			"with algo.getNodeById(nodeId) AS file, score \r\n" + 
			"set file.score=score\r\n" + 
			"RETURN file, score\r\n" + 
			"ORDER BY score DESC")
	public Collection<ProjectFile> pageRank(@Param("iterations") int iterations, @Param("dampingFactor") double dampingFactor);
}
