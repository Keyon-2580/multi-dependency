package cn.edu.fudan.se.multidependency.service;

import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.relation.Relations;

public interface ExtractorForNodesAndRelations {
	
	public void addNodesAndRelations() throws Exception ;
	
	public Nodes getNodes();
	
	public Relations getRelations();
	
}
