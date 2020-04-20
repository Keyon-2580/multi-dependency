package cn.edu.fudan.se.multidependency.model.relation.git;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_DEVELOPER_SUBMIT_COMMIT)
public class DeveloperSubmitCommit implements Relation {

    private static final long serialVersionUID = 3429048638016401498L;

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private Developer developer;

    @EndNode
    private Commit commit;

    public DeveloperSubmitCommit(Developer developer, Commit commit){
        this.developer = developer;
        this.commit = commit;
    }

    @Override
    public Long getStartNodeGraphId() {
        return developer.getId();
    }

    @Override
    public Long getEndNodeGraphId() {
        return commit.getId();
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.DEVELOPER_SUBMIT_COMMIT;
    }

    @Override
    public Map<String, Object> getProperties() {
        return new HashMap<>();
    }
}
