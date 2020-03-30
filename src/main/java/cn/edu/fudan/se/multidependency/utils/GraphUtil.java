package cn.edu.fudan.se.multidependency.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.Graph;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

public class GraphUtil {

	public static Graph sameSubGraphBetweenGraphsWithSameRelationExcludeRelationProperty(Graph graph1, Graph graph2, RelationType relationType) {
		Graph result = new Graph();
		
		List<Node> sameNodes = new ArrayList<>();
		
		for(Node node : graph1.allNodes().values()) {
			if(graph2.findNode(node.getId()) != null) {
				sameNodes.add(node);
			}
		}
		
		for(Node startNode : sameNodes) {
			Map<Node, List<Relation>> startToRelations1 = graph1.allEdges().get(startNode);
			Map<Node, List<Relation>> startToRelations2 = graph2.allEdges().get(startNode);
			for(Node endNode : startToRelations1.keySet()) {
				List<Relation> relations1 = startToRelations1.get(endNode);
				List<Relation> relations2 = startToRelations2.get(endNode);
				if(relations2 == null) {
					break;
				}
				Relation relationInType1 = null;
				for(Relation relation1 : relations1) {
					if(relation1.getRelationType() == relationType) {
						relationInType1 = relation1;
						break;
					}
				}
				if(relationInType1 == null) {
					break;
				}
				Relation relationInType2 = null;
				for(Relation relation2 : relations2) {
					if(relation2.getRelationType() == relationType) {
						relationInType2 = relation2;
						break;
					}
				}
				if(relationInType2 == null) {
					break;
				}
				result.addNode(startNode, graph1.nodeName(startNode));
				result.addNode(endNode, graph2.nodeName(endNode));
				result.addEdge(relationInType1);
			}
			
		}
		
		
		return result;
	}
	
}
