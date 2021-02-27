package cn.edu.fudan.se.multidependency.repository.node.clone;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;

@Repository
public interface CloneGroupRepository extends Neo4jRepository<CloneGroup, Long> {

	@Query("match p= (g:CloneGroup) where g.cloneLevel = $cloneLevel return g")
	public List<CloneGroup> findGroups(@Param("cloneLevel") String cloneLevel);
	
	@Query("match (group:CloneGroup) where group.name=$name return group")
	CloneGroup queryCloneGroup(@Param("name") String groupName);

	@Query("match (n:ProjectFile) where n.suffix=\".java\" set n.language = \"java\";")
	void setJavaLanguageBySuffix();

	@Query("match (n:ProjectFile) where n.suffix<>\".java\" set n.language = \"cpp\";")
	void setCppLanguageBySuffix();

	@Query("match p = (n:CloneGroup)-[r:CONTAIN]-() delete r;")
	void deleteCloneGroupContainRelations();

	@Query("match (n:CloneGroup) delete n;")
	void deleteCloneGroupRelations();

	@Query("CALL gds.wcc.stream({" +
			"nodeProjection: \'ProjectFile\', " +
			"relationshipProjection: \'" + RelationType.str_CLONE + "\'}) " +
			"YIELD nodeId, componentId " +
			"with componentId as setId, collect(gds.util.asNode(nodeId)) AS files\n" +
			"where size(files) > 1\n" +
			"match (file:ProjectFile) where file in files set file.cloneGroupId = \"file_group_\" + setId;")
	void setFileGroup();

	@Query("match (file:ProjectFile) " +
			"where file.cloneGroupId is not null " +
			"with file.cloneGroupId as cloneGroupId, count(file) as count " +
			"with cloneGroupId " +
			"create (:CloneGroup{name: cloneGroupId, cloneLevel: \"file\", entityId: -1});\n")
	void createCloneGroupRelations();

	@Query("MATCH (n:CloneGroup) with n match (file:ProjectFile) " +
			"where file.cloneGroupId = n.name " +
			"create (n)-[:CONTAIN]->(file);\n")
	void createCloneGroupContainRelations();

	@Query("MATCH (n:CloneGroup) with n set n.size = size((n)-[:CONTAIN]->());\n")
	void setCloneGroupContainSize();

	@Query("MATCH (n:CloneGroup)-[:CONTAIN]->(file:ProjectFile) where n.language is null with n, file set n.language = file.language;\n")
	void setCloneGroupLanguage();

	/**
	 * 判断是否存在co-change关系
	 * @param
	 * @return
	 */
	@Query("match (n:CloneGroup) return n limit 10")
	List<CloneGroup> findCloneGroupWithLimit();
}
