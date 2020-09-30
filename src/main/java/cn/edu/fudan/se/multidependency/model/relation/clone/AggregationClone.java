package cn.edu.fudan.se.multidependency.model.relation.clone;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.*;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_AGGREGATION_CLONE)
public class AggregationClone implements Relation {
    private static final long serialVersionUID = 8708817258770543568L;

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private Node node1;

    @EndNode
    private Node node2;

    public AggregationClone(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    private double value = 0;

    private int clonePairs = 0;

    private int nodesInNode1 = 0;

    private int nodesInNode2 = 0;

    private int allNodesInNode1 = 0;

    private int allNodesInNode2 = 0;

    /**
     * 克隆关系类型：文件间克隆，方法间克隆等
     */
    private String cloneRelationType;

    /**
     * 克隆类型，type1，type2等
     */
    private String cloneType;

    @Override
    public Node getStartNode() {
        return node1;
    }

    @Override
    public Node getEndNode() {
        return node2;
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.AGGREGATION_CLONE;
    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }
}
