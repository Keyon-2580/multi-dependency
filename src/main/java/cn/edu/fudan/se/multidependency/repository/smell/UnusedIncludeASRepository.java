package cn.edu.fudan.se.multidependency.repository.smell;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface UnusedIncludeASRepository extends Neo4jRepository<ProjectFile, Long> {

	@Query("MATCH (file1:ProjectFile)-[:" + RelationType.str_INCLUDE + "]->(file2:ProjectFile),(file1)-[r:DEPENDS_ON]->(file2) where r.times - r.`dependsOnTypes.INCLUDE` <= 0 return collect(distinct file1);")
	public Set<ProjectFile> findFileWithUnusedInclude();

	@Query("MATCH (file1:ProjectFile)-[:" + RelationType.str_INCLUDE + "]->(file2:ProjectFile),(file1)-[r:DEPENDS_ON]->(file2) where id(file1) = $fileId and r.times - r.`dependsOnTypes.INCLUDE` <= 0 return collect(distinct file2);")
	public Set<ProjectFile> findUnusedIncludeByFileId(@Param("fileId") Long fileId);
}
