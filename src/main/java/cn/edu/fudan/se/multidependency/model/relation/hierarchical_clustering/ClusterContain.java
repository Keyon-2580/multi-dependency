package cn.edu.fudan.se.multidependency.model.relation.hierarchical_clustering;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_CLUSTER_CONTAIN)
public class ClusterContain implements Relation {

    private static final long serialVersionUID = 4921385066054085063L;

    @Id
    @GeneratedValue
    private Long id;

    public ClusterContain(Node start, Node end) {
        super();
        this.start = start;
        this.end = end;
    }

    @StartNode
    private Node start;

    @EndNode
    private Node end;

    @Override
    public Node getStartNode() {
        return start;
    }

    @Override
    public Node getEndNode() {
        return end;
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.CLUSTER_CONTAIN;
    }

    @Override
    public Map<String, Object> getProperties() {
        return new HashMap<>();
    }

}

