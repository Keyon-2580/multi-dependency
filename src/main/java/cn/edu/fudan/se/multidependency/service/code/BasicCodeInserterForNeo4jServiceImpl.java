package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelationsImpl;

/**
 * @author fan
 *
 */
public abstract class BasicCodeInserterForNeo4jServiceImpl extends ExtractorForNodesAndRelationsImpl {
	
	public BasicCodeInserterForNeo4jServiceImpl(String projectPath, String projectName, Language language, boolean isMicroservice, String serviceGroupName) {
		super();
		this.isMicroservice = isMicroservice;
		this.serviceGroupName = serviceGroupName;
		this.currentProject = new Project(projectName, "/" + projectName, language);
	}

	protected Project currentProject;
	
	protected boolean isMicroservice;
	
	protected String serviceGroupName;
	
	protected void addNodeToNodes(Node node, Long entityId, Project inProject) {
		if(node.getEntityId().longValue() != entityId.longValue()) {
			try {
				throw new Exception("节点id没有对应");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		addNode(node, inProject);
	}
	
	@Override
	public void addNodesAndRelations() {
		currentProject.setEntityId(generateEntityId().longValue());
		addNodeToNodes(currentProject, currentProject.getEntityId(), currentProject);
		if(isMicroservice) {
			MicroService microService = new MicroService();
			microService.setEntityId(generateEntityId().longValue());
			microService.setName(currentProject.getProjectName());
			microService.setServiceGroupName(serviceGroupName);
			addNodeToNodes(microService, microService.getEntityId(), null);
			Contain contain = new Contain(microService, currentProject);
			addRelation(contain);
		}
	}
	
}
