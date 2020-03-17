package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelationsImpl;

/**
 * @author fan
 *
 */
public abstract class BasicCodeInserterForNeo4jServiceImpl extends ExtractorForNodesAndRelationsImpl {
	
	public BasicCodeInserterForNeo4jServiceImpl(String projectPath, String projectName, 
			Language language, boolean isMicroservice, String serviceGroupName) {
		super();
		this.isMicroservice = isMicroservice;
		this.serviceGroupName = serviceGroupName;
		this.currentProject = new Project(projectName, "/" + projectName, language);
	}

	protected Project currentProject;
	
	protected boolean isMicroservice;
	
	protected String serviceGroupName;
	
	@Override
	public void addNodesAndRelations() throws Exception {
		currentProject.setEntityId(generateEntityId().longValue());
		addNode(currentProject, currentProject);
		if(isMicroservice) {
			// 被分析的项目是微服务项目，生成一个MicroService节点
			MicroService microService = new MicroService();
			microService.setEntityId(generateEntityId().longValue());
			microService.setName(currentProject.getProjectName());
			microService.setServiceGroupName(serviceGroupName);
			addNode(microService, null);
			Contain contain = new Contain(microService, currentProject);
			addRelation(contain);
		}
	}
	
}
