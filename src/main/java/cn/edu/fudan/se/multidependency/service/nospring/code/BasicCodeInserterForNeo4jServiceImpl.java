package cn.edu.fudan.se.multidependency.service.nospring.code;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import lombok.Setter;

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
	
	@Setter
	protected RestfulAPIFileExtractor restfulAPIFileExtractor;
	
	@Override
	public void addNodesAndRelations() throws Exception {
		currentProject.setEntityId(generateEntityId().longValue());
		addNode(currentProject, currentProject);
		if(isMicroservice) {
			// 被分析的项目是微服务项目，生成一个MicroService节点
			MicroService microService = this.getNodes().findMicroServiceByName(currentProject.getProjectName());
			if(microService == null) {
				microService = new MicroService();
				microService.setEntityId(generateEntityId().longValue());
				microService.setName(currentProject.getProjectName());
				microService.setServiceGroupName(serviceGroupName);
				addNode(microService, null);
			}
			Contain contain = new Contain(microService, currentProject);
			addRelation(contain);
		}
		
		if(restfulAPIFileExtractor != null) {
			MicroService currentProjectBelongToMicroService = null;
			if(isMicroservice) {
				currentProjectBelongToMicroService = this.getNodes().findMicroServiceByName(currentProject.getProjectName());
			}
			Iterable<RestfulAPI> apis = restfulAPIFileExtractor.extract();
			for(RestfulAPI api : apis) {
				api.setEntityId(generateEntityId());
				System.out.println(generateEntityId());
				addNode(api, currentProject);
				
				Contain projectContainAPI = new Contain(currentProject, api);
				addRelation(projectContainAPI);
				
				if(currentProjectBelongToMicroService != null) {
					Contain microServiceContainAPI = new Contain(currentProjectBelongToMicroService, api);
					addRelation(microServiceContainAPI);
				}
			}
			
		}
	}
	
}
