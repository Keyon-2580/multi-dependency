package cn.edu.fudan.se.multidependency.service.query.as.data;

import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PieFilesData {
	
	Project project;
	
	Set<ProjectFile> normalFiles;
	Set<ProjectFile> onlyIssueFiles;
	Set<ProjectFile> onlySmellFiles;
	Set<ProjectFile> issueAndSmellFiles;
	
	public int getAllFilesSize() {
		return normalFiles.size() + onlyIssueFiles.size() + onlySmellFiles.size() + issueAndSmellFiles.size();
	}
}
