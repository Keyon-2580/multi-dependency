package cn.edu.fudan.se.multidependency.repository.node.git;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitRepository extends Neo4jRepository<Commit, Long> {

}
