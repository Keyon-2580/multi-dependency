package cn.edu.fudan.se.multidependency.service.nospring.code;

import org.apache.commons.lang3.StringUtils;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.ProjectConfigUtil.ProjectConfig;
import depends.entity.Entity;
import lombok.Setter;

/**
 * @author fan
 *
 */
public abstract class BasicCodeInserterForNeo4jServiceImpl extends ExtractorForNodesAndRelationsImpl {
	
	public BasicCodeInserterForNeo4jServiceImpl(ProjectConfig projectConfig) {
		super();
		this.isMicroservice = projectConfig.isMicroService();
		this.serviceGroupName = projectConfig.getServiceGroupName();
		this.currentProject = new Project(projectConfig.getProject(), "/" + projectConfig.getProject(), projectConfig.getLanguage());
		this.microserviceName = projectConfig.getMicroserviceName();
		if(StringUtils.isBlank(microserviceName)) {
			this.microserviceName = projectConfig.getProject();
		}
		this.currentProject.setMicroserviceName(microserviceName);
	}

	protected Project currentProject;
	
	protected String microserviceName;
	
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
			MicroService microService = this.getNodes().findMicroServiceByName(microserviceName);
			if(microService == null) {
				microService = new MicroService();
				microService.setEntityId(generateEntityId().longValue());
				microService.setName(microserviceName);
				microService.setServiceGroupName(serviceGroupName);
				addNode(microService, null);
			}
			Contain contain = new Contain(microService, currentProject);
			addRelation(contain);
		}
		
		if(restfulAPIFileExtractor != null) {
			MicroService currentProjectBelongToMicroService = null;
			if(isMicroservice) {
				currentProjectBelongToMicroService = this.getNodes().findMicroServiceByName(microserviceName);
			}
			Iterable<RestfulAPI> apis = restfulAPIFileExtractor.extract();
			for(RestfulAPI api : apis) {
				api.setEntityId(generateEntityId());
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
	protected Node findNodeByEntityIdInProject(Entity entity) {
		if(entity == null) {
			return null;
		}
		return this.getNodes().findNodeByEntityIdInProject(entity.getId().longValue(), currentProject);
	}
}
