package cn.edu.fudan.se.multidependency.repository.node;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.spring.metric.FileMetrics;

@Repository
public interface ProjectFileRepository extends Neo4jRepository<ProjectFile, Long> {
	
	@Query("match (f:ProjectFile) where f.path={filePath} return f")
	public ProjectFile findFileByPath(@Param("filePath") String filePath);
	
	@Query("MATCH (file:ProjectFile)\r\n" + 
			"WITH size((file)-[:" + RelationType.str_DEPEND_ON + "]->()) as fanOut, \r\n" + 
			"     size((file)<-[:" + RelationType.str_DEPEND_ON + "]-()) as fanIn,\r\n" + 
			"     size((file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-()) as changeTimes,\r\n" + 
			"     size((file)-[:" + RelationType.str_CONTAIN + "*1..3]->(:Function)) as nom,\r\n" + 
			"     file.endLine as loc,\r\n" + 
			"     file\r\n" + 
			"RETURN  file,fanIn,fanOut,changeTimes,nom,loc order by(file.path) desc;")
	public List<FileMetrics> calculateFileMetrics();
	
}
