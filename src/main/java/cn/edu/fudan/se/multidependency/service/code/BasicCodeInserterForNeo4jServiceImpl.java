package cn.edu.fudan.se.multidependency.service.code;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.FileUtils;

/**
 * @author fan
 *
 */
public abstract class BasicCodeInserterForNeo4jServiceImpl extends ExtractorForNodesAndRelationsImpl {
	
	public BasicCodeInserterForNeo4jServiceImpl(String projectPath, Language language) {
		super();
		this.language = language;
		String projectName = FileUtils.extractFileName(projectPath);
		currentProject = new Project(projectName, "/" + projectName, language);
	}

	protected Project currentProject;
	
	protected Language language;
	
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
	
	public abstract void addNodesAndRelations();
	
	public void setLanguage(Language language) {
		this.language = language;
	}

}
