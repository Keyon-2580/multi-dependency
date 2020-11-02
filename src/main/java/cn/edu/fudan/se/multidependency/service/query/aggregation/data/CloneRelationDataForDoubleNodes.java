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
public class CloneRelationDataForDoubleNodes<N extends Node, R extends Relation> extends BasicDataForDoubleNodes {

	private static final long serialVersionUID = -7780703024314601425L;

	private int clonePairs = 0;

	private int cloneNodesCount1 = 0;

	private int cloneNodesCount2 = 0;

	private int cloneNodesCoChangeTimes = 0;

	public CloneRelationDataForDoubleNodes(N node1, N node2){
		super(node1, node2);
	}

	public void setDate(int clonePairs, int cloneNodesCount1, int cloneNodesCount2, int cloneNodesCoChangeTimes) {
		this.clonePairs = clonePairs;
		this.cloneNodesCount1 = cloneNodesCount1;
		this.cloneNodesCount2 = cloneNodesCount2;
		this.cloneNodesCoChangeTimes = cloneNodesCoChangeTimes;
	}

	@Override
	protected RelationType getRelationDataType() {
		return RelationType.CLONE;
	}
}
