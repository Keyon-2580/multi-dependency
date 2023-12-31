package cn.edu.fudan.se.multidependency.repository.relation.coupling;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.Coupling;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CouplingRepository extends Neo4jRepository<Coupling, Long> {
    @Query("match p=()-[r:" + RelationType.str_COUPLING + "]->() return r;")
    List<Coupling> queryAllCouplings();

    @Query("match p=()-[r:" + RelationType.str_COUPLING + "]->() return p order by r.dist asc;")
    List<Coupling> queryAllCouplingsOrderByDist();

    @Query("match p=()-[r:" + RelationType.str_COUPLING + "]->() where id(r)=$couplingId set r.clusterDist=$dist;")
    void setCouplingClusterDistance(@Param("couplingId") long couplingId, @Param("dist") int dist);

    @Query("match (f1:ProjectFile)-[:" + RelationType.str_CONTAIN + "]-(:Type)-[:" + RelationType.str_CONTAIN + "]-" +
            "(:Function)-[]->(m2:Function)-[:" + RelationType.str_CONTAIN + "]-(:Type)-[:" + RelationType.str_CONTAIN + "]" +
            "-(f2:ProjectFile) where id(f1)=$file1Id and id(f2)=$file2Id return count(distinct m2);")
    int queryTwoFilesDependsByFunctionsNum(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);

    @Query("match (f1:ProjectFile)-[:" + RelationType.str_CONTAIN + "]-(:Type)-[:" + RelationType.str_CONTAIN + "]-" +
            "(m1:Function)-[]->(m2:Function)-[:" + RelationType.str_CONTAIN + "]-(:Type)-[:" + RelationType.str_CONTAIN + "]" +
            "-(f2:ProjectFile) where id(f1)=$file1Id and id(f2)=$file2Id return count(distinct m1);")
    int queryTwoFilesDependsOnFunctionsNum(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);//

    @Query("match (f1:ProjectFile)-[:" + RelationType.str_CONTAIN + "]-(:Type)-[:" + RelationType.str_CONTAIN + "*]->" +
            "()-[]->(v2:Variable)<-[:" + RelationType.str_CONTAIN + "*]-(f2:ProjectFile) " +
            "where id(f1)=$file1Id and id(f2)=$file2Id return count(distinct v2);")
    int queryTwoFilesDependsByVariablesNum(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);

    @Query("match (f1:ProjectFile)-[:" + RelationType.str_CONTAIN + "]-(:Type)-[:" + RelationType.str_CONTAIN + "*]->" +
            "(v1:Variable)-[]->()<-[:" + RelationType.str_CONTAIN + "*]-(f2:ProjectFile) " +
            "where id(f1)=$file1Id and id(f2)=$file2Id return count(distinct v1);")
    int queryTwoFilesDependsOnVariablesNum(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);

    @Query("match p=(:ProjectFile)-[:" + RelationType.str_COUPLING + "]->(:ProjectFile) return p limit 10;")
    List<Coupling> findFileDependsWithLimit();

    @Query("match p=(:Package)-[:" + RelationType.str_COUPLING + "]->(:Package) return p limit 10;")
    List<Coupling> findPkgDependsWithLimit();

    @Query("match p=(f1:ProjectFile)-[r:" + RelationType.str_COUPLING + "]-(f2:ProjectFile) where id(f1)=$file1Id " +
            "and id(f2)=$file2Id return p;")
    Coupling queryCouplingBetweenTwoFiles(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);

    @Query("match p=(p1:Package)-[r:" + RelationType.str_COUPLING + "]-(p2:Package) where id(p1)=$pkgId1 " +
            "and id(p2)=$pkgId2 return p;")
    List<Coupling> queryCouplingBetweenTwoPkgs(@Param("pkgId1") long pkgId1, @Param("pkgId2") long pkgId2);

    @Query("MATCH p=(n1:Package)-[:" + RelationType.str_CONTAIN + "*]->(f1:ProjectFile)" +
            "-[r:" + RelationType.str_COUPLING + "]-(f2:ProjectFile)<-[:"
            + RelationType.str_CONTAIN + "*]-(n1:Package) where id(n1)=$pckId return p")
    List<Coupling> queryAllCouplingsWithinPackage(@Param("pckId") long pckId);

    @Query("MATCH p=(n1:Package)-[:" + RelationType.str_CONTAIN + "*]->(f1:ProjectFile)" +
            "-[r:" + RelationType.str_COUPLING + "]-(f2:ProjectFile)<-[:"
            + RelationType.str_CONTAIN + "*]-(n1:Package) where id(n1)=$pckId return count(r);")
    Integer queryAllCouplingsWithinPackageCouplingsNum(@Param("pckId") long pckId);

    @Query("MATCH p=(n1:Package)-[:" + RelationType.str_CONTAIN + "*]->(f1:ProjectFile)" +
            "-[r:" + RelationType.str_COUPLING + "]-(f2:ProjectFile)<-[:"
            + RelationType.str_CONTAIN + "*]-(n1:Package) where id(n1)=$pckId return count(distinct f1);")
    Integer queryAllCouplingsWithinPackageFilesNum(@Param("pckId") long pckId);

    @Query("MATCH p=(n1:Package)-[:" + RelationType.str_CONTAIN + "*]->(f1:ProjectFile)" +
            "-[r:" + RelationType.str_COUPLING + "]-(f2:ProjectFile)<-[:"
            + RelationType.str_CONTAIN + "*]-(n2:Package) where id(n1)=$pckId and n1<>n2 return p")
    List<Coupling> queryAllCouplingsOutOfPackage(@Param("pckId") long pckId);

    @Query("MATCH p=(n1:Package)-[:" + RelationType.str_CONTAIN + "*]->(f1:ProjectFile)" +
            "-[r:" + RelationType.str_COUPLING + "]-(f2:ProjectFile)<-[:"
            + RelationType.str_CONTAIN + "*]-(n2:Package) where id(n1)=$pckId and n1<>n2 return count(distinct f1);")
    Integer queryAllCouplingsOutOfPackageFilesNum(@Param("pckId") long pckId);
}
