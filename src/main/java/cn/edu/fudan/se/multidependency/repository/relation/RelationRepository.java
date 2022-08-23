package cn.edu.fudan.se.multidependency.repository.relation;


import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface RelationRepository extends Neo4jRepository<Relation, Long> {

    @Query("MATCH p=()-[r:CALL|CAST|CREATE|EXTENDS|IMPLEMENTS|LOCAL_VARIABLE|MEMBER_VARIABLE|PARAMETER]->() RETURN count(p)>0")
    boolean alreadyInserted();

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_RELATE_TO +"$prop]->(b)")
    void insertRelateTo(@Param("startId") long startId, @Param("endId") long endId,
                        @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_EXTENDS +"$prop]->(b)")
    void insertExtends(@Param("startId") long startId, @Param("endId") long endId,
                       @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_IMPLEMENTS +"$prop]->(b)")
    void insertImplements(@Param("startId") long startId, @Param("endId") long endId,
                          @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_CALL +"$prop]->(b)")
    void insertCall(@Param("startId") long startId, @Param("endId") long endId,
                    @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_CREATE +"$prop]->(b)")
    void insertCreate(@Param("startId") long startId, @Param("endId") long endId,
                      @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_CAST +"$prop]->(b)")
    void insertCast(@Param("startId") long startId, @Param("endId") long endId,
                    @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_THROW +"$prop]->(b)")
    void insertThrow(@Param("startId") long startId, @Param("endId") long endId,
                     @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_PARAMETER +"$prop]->(b)")
    void insertParameter(@Param("startId") long startId, @Param("endId") long endId,
                         @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_RETURN +"$prop]->(b)")
    void insertReturn(@Param("startId") long startId, @Param("endId") long endId,
                      @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_USE +"$prop]->(b)")
    void insertUse(@Param("startId") long startId, @Param("endId") long endId,
                   @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_ACCESS +"$prop]->(b)")
    void insertAccess(@Param("startId") long startId, @Param("endId") long endId,
                      @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_MEMBER_VARIABLE +"$prop]->(b)")
    void insertMemberVariable(@Param("startId") long startId, @Param("endId") long endId,
                              @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_LOCAL_VARIABLE +"$prop]->" +
            "(b)")
    void insertLocalVariable(@Param("startId") long startId, @Param("endId") long endId,
                             @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_ANNOTATION +"$prop]->(b)")
    void insertAnnotation(@Param("startId") long startId, @Param("endId") long endId,
                          @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_IMPLLINK +"$prop]->(b)")
    void insertImplink(@Param("startId") long startId, @Param("endId") long endId,
                       @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_IMPLEMENTS_C +"$prop]->(b)")
    void insertImplementsC(@Param("startId") long startId, @Param("endId") long endId,
                           @Param("prop") Map<String, Object> prop);


    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_CONTAIN +"$prop]->(b)")
    void insertContain(@Param("startId") long startId, @Param("endId") long endId,
                       @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_HAS +"$prop]->(b)")
    void insertHas(@Param("startId") long startId, @Param("endId") long endId,
                   @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_VARIABLE_TYPE +"$prop]->(b)")
    void insertVariableType(@Param("startId") long startId, @Param("endId") long endId,
                            @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_GENERIC_PARAMETER +"$prop]->(b)")
    void insertGenericParam(@Param("startId") long startId, @Param("endId") long endId,
                            @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_IMPORT +"$prop]->(b)")
    void insertImport(@Param("startId") long startId, @Param("endId") long endId,
                      @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_INCLUDE +"$prop]->(b)")
    void insertInclude(@Param("startId") long startId, @Param("endId") long endId,
                       @Param("prop") Map<String, Object> prop);

    @Query("match (a) where id(a)=$startId match (b) where id(b)=$endId create (a)-[:"+ RelationType.str_GLOBAL_VARIABLE +"$prop]->(b)")
    void insertGlobalVariable(@Param("startId") long startId, @Param("endId") long endId,
                              @Param("prop") Map<String, Object> prop);
}
