package cn.edu.fudan.se.multidependency.service.query.as.data;

import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PieFilesData {
	Set<ProjectFile> normalFiles;
	Set<ProjectFile> onlyIssueFiles;
	Set<ProjectFile> onlySmellFiles;
	Set<ProjectFile> issueAndSmellFiles;
}
