package cn.edu.fudan.se.multidependency.repository.relation;

import cn.edu.fudan.se.multidependency.model.relation.RelateTo;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;


@Repository
public interface RelateToRepository extends Neo4jRepository<RelateTo, Long> {

    @Query("match p = ()-[r:" + RelationType.str_RELATE_TO + "] -> () delete r;")
    void clearRelateToRelation();
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
}
