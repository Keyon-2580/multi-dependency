package cn.edu.fudan.se.multidependency.service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;

public abstract class ExtractorForNodesAndRelationsImpl implements ExtractorForNodesAndRelations {
	
	protected InserterForNeo4j repository = RepositoryService.getInstance();
	
	protected Long generateEntityId() {
		return repository.generateEntityId();
	}

	protected void setCurrentEntityId(Long entityId) {
		repository.setCurrentEntityId(entityId);
	}
	
	@Override
	public abstract void addNodesAndRelations();
	
	protected boolean addNode(Node node) {
		return this.repository.addNode(node);
	}

	protected boolean addRelation(Relation relation) {
		return this.repository.addRelation(relation);
	}
	
	@Override
	public Nodes getNodes() {
		return repository.getNodes();
	}

	@Override
	public Relations getRelations() {
		return repository.getRelations();
	}

}