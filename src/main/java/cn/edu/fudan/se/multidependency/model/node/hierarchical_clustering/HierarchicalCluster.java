package cn.edu.fudan.se.multidependency.model.node.hierarchical_clustering;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.HashMap;
import java.util.Map;


@Data
@NodeEntity
@NoArgsConstructor
public class HierarchicalCluster implements Node {

    private static final long serialVersionUID = -3122500303170151924L;

    @Id
    @GeneratedValue
    private Long id;

    private Long entityId;

    private String name;

    private boolean isBottom = false;

    public HierarchicalCluster(boolean isBottom){
        this.isBottom = isBottom;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
        properties.put("name", getName() == null ? "" : getName());
        return properties;
    }

    @Override
    public NodeLabelType getNodeType() {
        return NodeLabelType.HierarchicalCluster;
    }

}
