package cn.edu.fudan.se.multidependency.repository.smell;

import cn.edu.fudan.se.multidependency.model.IssueType;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.query.smell.data.SmellMetric;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmellRepository extends Neo4jRepository<Smell, Long> {

	@Query("match p= (smell:Smell) where smell.level = $level return smell")
	public List<Smell> findSmells(@Param("level") String level);

	@Query("match p= (smell:Smell) where smell.level = $level and smell.type = $type return smell")
	public List<Smell> findSmells(@Param("level") String level,@Param("type") String type);
	
	@Query("match (smell:Smell) where smell.name=$name return smell")
	Smell querySmell(@Param("name") String name);

	@Query("match (smell:Smell) -[:" + RelationType.str_CONTAIN + "]-(node) " +
			"where id(smell)=$smellId return node;")
	List<Node> findSmellContains(@Param("smellId") long smellId);

	@Query("match (n:ProjectFile) where n.suffix=\".java\" set n.language = \"java\";")
	void setJavaLanguageBySuffix();

	@Query("match (n:ProjectFile) where n.suffix<>\".java\" set n.language = \"cpp\";")
	void setCppLanguageBySuffix();

	@Query("match p = (:Smell)-[r:CONTAIN]-() delete r;")
	void deleteSmellContainRelations();

	@Query("match p = (smell:Smell)-[r:CONTAIN]-() where smell.type = $smellType delete r;")
	void deleteSmellContainRelations(@Param("smellType") String smellType);

	@Query("match p = (:Smell)-[r:" + RelationType.str_HAS + "] -> (:Metric) delete r;")
	void deleteSmellHasMetricRelation();

	@Query("match p = (smell:Smell)-[r:" + RelationType.str_HAS + "] -> (:Metric) where smell.type = $smellType delete r;")
	void deleteSmellHasMetricRelation(@Param("smellType") String smellType);

	@Query("match (n:Smell) delete n;")
	void deleteSmells();

	@Query("match (n:Smell) where n.type = $smellType delete n;")
	void deleteSmells(@Param("smellType") String smellType);

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

	@Query("match (cloneGroup:CloneGroup) \r\n" +
			"create (:Smell{name: cloneGroup.name, size: cloneGroup.size,language: cloneGroup.language,level: \'" +
			SmellLevel.FILE + "\', type:\'" + SmellType.CLONE + "\',entityId: -1});\n")
	void createCloneSmells();

	@Query("MATCH (smell:Smell) with smell \r\n" +
			"match (cloneGroup:CloneGroup)-[:" + RelationType.str_CONTAIN + "]-(file:ProjectFile)\r\n" +
			"where cloneGroup.name = smell.name " +
			"create (smell)-[:" + RelationType.str_CONTAIN + "]->(file);\n")
	void createCloneSmellContains();

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)<-[:" +
			RelationType.str_CONTAIN + "*2]-(p:Project)\r\n" +
			"with smell, p \r\n" +
			"set smell += {projectId : id(p), projectName : p.name};\n")
	void setSmellProject();

	@Query("MATCH (project:Project) with project " +
			"where id(project) = $projectId " +
			"create (smell:Smell{name: $name, size: $size, language: $language, projectId: id(project), projectName: project.name, level: \'" +
			SmellLevel.FILE + "\', type:\'" + SmellType.CYCLIC_DEPENDENCY + "\', entityId: -1}) " +
			"return smell;")
	Smell createFileCyclicDependencySmell(@Param("name") String name, @Param("size") int size, @Param("language") String language, @Param("projectId") long projectId);

	@Query("MATCH (smell:Smell) with smell " +
			"match (file:ProjectFile) " +
			"where id(smell) = $smellId and id(file) = $fileId " +
			"create (smell)-[:" + RelationType.str_CONTAIN + "]->(file);")
	void createFileCyclicDependencySmellContains(@Param("smellId") long smellId, @Param("fileId") long fileId);

	/**
	 * 判断是否存在co-change关系
	 * @param
	 * @return
	 */
	@Query("match (n:Smell) return n limit 10")
	List<Smell> findSmellWithLimit();

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) \r\n" +
			"WITH smell, count(file) as size, sum(file.noc) as noc, sum(file.nom) as nom, sum(file.loc) as loc \r\n" +
			"RETURN  smell,size,noc,nom,loc;")
	public List<SmellMetric.StructureMetric> calculateSmellStructureMetricInFileLevel();

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) \r\n" +
			"where id(smell)= $smellId\r\n" +
			"WITH smell, count(file) as size, sum(file.noc) as noc, sum(file.nom) as nom, sum(file.loc) as loc \r\n" +
			"RETURN  smell,size,noc,nom,loc;")
	public SmellMetric.StructureMetric calculateSmellStructureMetricInFileLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
			"]-(c:Commit)<-[:" + RelationType.str_DEVELOPER_SUBMIT_COMMIT + "]-(d:Developer) \r\n" +
			"where id(smell)= $smellId\r\n" +
			"WITH smell, count(distinct c) as commits,count(c) as totalCommits,count(distinct d) as developers,count(d) as totalDevelopers \r\n" +
			"RETURN  smell,commits,totalCommits,developers,totalDevelopers;")
	public SmellMetric.EvolutionMetric calculateSmellEvolutionMetricInFileLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file1:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
			"]-(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(file2:ProjectFile)<- [:" +
			RelationType.str_CONTAIN + "]-(smell) \r\n" +
			"where id(smell)= $smellId and id(file1) < id(file2) and size((file1)-[:" + RelationType.str_CO_CHANGE +"]-(file2)) > 0 \r\n" +
			"WITH smell, count(distinct c) as coChangeCommits,count(c) as totalCoChangeCommits,collect(distinct file1) as coFiles1," +
			"     collect(distinct file2) as coFiles2\r\n" +
			"WITH smell,coChangeCommits,totalCoChangeCommits," +
			"     reduce(tmp=size(coFiles1), file in coFiles2 | tmp + (case when file in coFiles1 then 0 else 1 end)) as coChangeFiles\r\n" +
			"RETURN  smell,coChangeCommits,totalCoChangeCommits,coChangeFiles;")
	public SmellMetric.CoChangeMetric calculateSmellCoChangeMetricInFileLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
			"]-(c:Commit)-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]->(issue:Issue) \r\n" +
			"where id(smell)= $smellId\r\n" +
			"WITH smell, collect(distinct issue) as issueList \r\n" +
			"with smell, size(issueList) as issues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.BUG + "\' then 1 else 0 end)) as bugIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.NEW_FEATURE + "\' then 1 else 0 end)) as newFeatureIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.IMPROVEMENT + "\' then 1 else 0 end)) as improvementIssues \r\n" +
			"RETURN  smell,issues,bugIssues,newFeatureIssues,improvementIssues;")
	public SmellMetric.DebtMetric calculateSmellDebtMetricInFileLevel(@Param("smellId") long smellId);
}
