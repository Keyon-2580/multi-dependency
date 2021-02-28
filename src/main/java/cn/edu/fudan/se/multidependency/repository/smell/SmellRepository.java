package cn.edu.fudan.se.multidependency.repository.smell;

import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmellRepository extends Neo4jRepository<Smell, Long> {

	@Query("match p= (g:Smell) where g.level = $level return g")
	public List<Smell> findGroups(@Param("cloneLevel") String level);
	
	@Query("match (smell:Smell) where smell.name=$name return smell")
	Smell querySmell(@Param("name") String name);

	@Query("match (n:ProjectFile) where n.suffix=\".java\" set n.language = \"java\";")
	void setJavaLanguageBySuffix();

	@Query("match (n:ProjectFile) where n.suffix<>\".java\" set n.language = \"cpp\";")
	void setCppLanguageBySuffix();

	@Query("match p = (n:Smell)-[r:CONTAIN]-() delete r;")
	void deleteSmellContainRelations();

	@Query("match (n:CloneGroup) delete n;")
	void deleteSmellRelations();

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
	void createSmellRelations();

	@Query("MATCH (n:Smell) with n match (file:ProjectFile) " +
			"where file.cloneGroupId = n.name " +
			"create (n)-[:CONTAIN]->(file);\n")
	void createSmellContainRelations();

	@Query("MATCH (n:Smell) with n set n.size = size((n)-[:CONTAIN]->());\n")
	void setSmellContainSize();

	@Query("MATCH (n:Smell)-[:CONTAIN]->(file:ProjectFile) where n.language is null with n, file set n.language = file.language;\n")
	void setSmellLanguage();

	/**
	 * 判断是否存在co-change关系
	 * @param
	 * @return
	 */
	@Query("match (n:Smell) return n limit 10")
	List<Smell> findSmellWithLimit();
}
