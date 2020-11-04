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

	private int allNodesCount1 = 0;

	private int allNodesCount2 = 0;

	private double similarityValue = 0.00;

	private int cloneNodesCoChangeTimes = 0;

	private int allNodesCoChangeTimes = 0;

	public CloneRelationDataForDoubleNodes(N node1, N node2){
		super(node1, node2);
	}

	public void setDate(int allNodesCount1, int allNodesCount2, int cloneNodesCount1, int cloneNodesCount2) {
		this.allNodesCount1 = allNodesCount1;
		this.allNodesCount2 = allNodesCount2;
		this.cloneNodesCount1 = cloneNodesCount1;
		this.cloneNodesCount2 = cloneNodesCount2;
		similarityValue = (cloneNodesCount1 + cloneNodesCount2 + 0.0) / (allNodesCount1 + allNodesCount2);
	}

	public void setClonePairs(int clonePairs) {
		this.clonePairs = clonePairs;
	}

	public void setCloneNodesCoChangeTimes(int cloneNodesCoChangeTimes) {
		this.cloneNodesCoChangeTimes = cloneNodesCoChangeTimes;
	}

	public void setAllNodesCoChangeTimes(int allNodesCoChangeTimes) {
		this.allNodesCoChangeTimes = allNodesCoChangeTimes;
	}

	@Override
	public RelationType getRelationDataType() {
		return RelationType.CLONE;
	}
}
