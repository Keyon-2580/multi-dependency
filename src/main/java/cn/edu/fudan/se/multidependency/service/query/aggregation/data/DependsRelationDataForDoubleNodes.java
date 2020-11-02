package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;


/**
 * 节点node1和node2不分方向
 * 若Node为Package，则表示两个Package之间的**关系，children为pck1和pck2内的文件**关系或方法**关系等，**关系的两个节点分别在两个包里，不计同一包下的文件**
 * @author fan
 * 
 * @param <N>
 */
@Data
public class DependsRelationDataForDoubleNodes<N extends Node, R extends Relation> extends BasicDataForDoubleNodes {

	private static final long serialVersionUID = -3921207838863336582L;

	private String dependsOnTypes = "";

	private String dependsByTypes = "";

	private int dependsOnTimes = 0;

	private int dependsByTimes = 0;

	public DependsRelationDataForDoubleNodes(N node1, N node2){
		super(node1, node2);
	}

	public void setDependsData(String dependsOnTypes, String dependsByTypes, int dependsOnTimes, int dependsByTimes) {
		this.dependsOnTypes = dependsOnTypes;
		this.dependsByTypes = dependsByTypes;
		this.dependsOnTimes = dependsOnTimes;
		this.dependsByTimes = dependsByTimes;
	}

	@Override
	protected RelationType getRelationDataType() {
		return RelationType.DEPENDS_ON;
	}
}
