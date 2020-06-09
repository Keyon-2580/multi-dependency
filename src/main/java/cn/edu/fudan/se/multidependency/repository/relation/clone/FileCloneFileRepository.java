package cn.edu.fudan.se.multidependency.repository.relation.clone;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;

@Repository
public interface FileCloneFileRepository extends Neo4jRepository<FileCloneFile, Long> {
	
	@Query("match p= (g:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->(file1:ProjectFile)-[:" + RelationType.str_FILE_CLONE_FILE + "]->(file2:ProjectFile)<-[:" + RelationType.str_CONTAIN + "]-(g) where id(g)={groupId} return p")
	public List<FileCloneFile> findCloneGroupContainFileClones(@Param("groupId") long groupId);

}
