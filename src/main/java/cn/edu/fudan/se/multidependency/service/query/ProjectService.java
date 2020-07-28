package cn.edu.fudan.se.multidependency.service.query;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;

@Service
public class ProjectService {
	
	private Map<Project, String> projectToAbsolutePath = new ConcurrentHashMap<>();
	
	public String getAbsolutePath(Project project) {
		if(project == null) {
			return "";
		}
		return projectToAbsolutePath.getOrDefault(project, "");
	}
	
	public void setAbsolutePath(Project project, String path) {
		this.projectToAbsolutePath.put(project, path);
	}
	
}
