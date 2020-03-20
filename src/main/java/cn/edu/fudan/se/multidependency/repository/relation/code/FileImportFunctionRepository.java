package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.code.FileImportFunction;

@Repository
public interface FileImportFunctionRepository extends Neo4jRepository<FileImportFunction, Long> {

	@Query("MATCH result=(file:ProjectFile)-[r:" + RelationType.str_FILE_IMPORT_FUNCTION + "]->(:Function) with file,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*2]->(file) where id(project)={projectId} RETURN result")
	List<FileImportFunction> findProjectContainFileImportFunctionRelations(@Param("projectId") Long projectId);

}
