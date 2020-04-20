package cn.edu.fudan.se.multidependency.model.relation.git;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_COMMIT_INHERIT_COMMIT)
public class CommitInheritCommit implements Relation {

    private static final long serialVersionUID = 1232885772212330907L;

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private Commit start;

    @EndNode
    private Commit end;

    public CommitInheritCommit(Commit start, Commit end){
        this.start = start;
        this.end = end;
    }

    @Override
    public Long getStartNodeGraphId() {
        return start.getId();
    }

    @Override
    public Long getEndNodeGraphId() {
        return end.getId();
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.COMMIT_INHERIT_COMMIT;
    }

    @Override
    public Map<String, Object> getProperties() {
        return new HashMap<>();
    }
}
