package cn.edu.fudan.se.multidependency.service.nospring.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.utils.clone.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.clone.data.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.FilePathFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.Group;

public class CloneInserterForFile extends CloneInserter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloneInserterForFile.class);
	private String namePath;
	private String resultPath;
	private String groupPath;
	private Language language;
	
	public CloneInserterForFile(String namePath, String resultPath, String groupPath, Language language) {
		super();
		this.namePath = namePath;
		this.resultPath = resultPath;
		this.groupPath = groupPath;
		this.language = language;
	}
	
	private Map<Integer, FilePathFromCsv> filePaths = new HashMap<>();
	private Collection<CloneResultFromCsv> cloneResults = new ArrayList<>();
	private Collection<Group> groups = new ArrayList<>();
	private Map<Integer, ProjectFile> cloneFileIdToCodeNode = new HashMap<>();

	@Override
	protected void readMeasureIndex() throws Exception {
		filePaths = CloneUtil.readCloneCsvForFilePath(namePath);
		for(Map.Entry<Integer, FilePathFromCsv> entry : filePaths.entrySet()) {
			FilePathFromCsv filePath = entry.getValue();
			ProjectFile file = this.getNodes().findFileByPathRecursion(filePath.getFilePath());
			if(file == null) {
				LOGGER.warn("file is null " + filePath.getFilePath());
				continue;
			}
			file.setEndLine(filePath.getEndLine());
		}
	}

	@Override
	protected void readResult() throws Exception {
		cloneResults = CloneUtil.readCloneResultCsv(resultPath);
	}

	@Override
	protected void readGroup() throws Exception {
		groups = CloneUtil.readGroupFile(groupPath);
	}

	@Override
	protected void extractNodesAndRelations() throws Exception {
		LOGGER.info("文件克隆对数：" + cloneResults.size());
		int sizeOfFileCloneFiles = 0;
		List<Clone> clones = new ArrayList<>();
		for(CloneResultFromCsv cloneResult : cloneResults) {
			int start = cloneResult.getStart();
			int end = cloneResult.getEnd();
			double value = cloneResult.getValue();
			String type = cloneResult.getType();
			FilePathFromCsv filePath1 = filePaths.get(start);
			if(filePath1 == null) {
				LOGGER.error("path1 is null");
				continue;
			}
			FilePathFromCsv filePath2 = filePaths.get(end);
			if(filePath2 == null) {
				LOGGER.error("path2 is null");
				continue;
			}
			ProjectFile file1 = this.cloneFileIdToCodeNode.get(filePath1.getLineId());
			if(file1 == null) {
				file1 = this.getNodes().findFileByPathRecursion(filePath1.getFilePath());
			}
			ProjectFile file2 = this.cloneFileIdToCodeNode.get(filePath2.getLineId());
			if(file2 == null) {
				file2 = this.getNodes().findFileByPathRecursion(filePath2.getFilePath());
			}
			if(file1 == null) {
				LOGGER.error("file1 is null " + filePath1.getLineId() + " " + filePath1.getFilePath());
				continue;
			}
			cloneFileIdToCodeNode.put(filePath1.getLineId(), file1);
			if(file2 == null) {
				LOGGER.error("file2 is null " + filePath2.getLineId() + " " + filePath2.getFilePath());
				continue;
			}
			cloneFileIdToCodeNode.put(filePath2.getLineId(), file2);
			Clone clone = new Clone(file1, file2);
			clone.setNode1Index(start);
			clone.setNode2Index(end);
			clone.setNode1StartLine(filePath1.getStartLine());
			clone.setNode1EndLine(filePath1.getEndLine());
			clone.setNode2StartLine(filePath2.getStartLine());
			clone.setNode2EndLine(filePath2.getEndLine());
			clone.setValue(value);
			clone.setCloneRelationType(CloneRelationType.str_FILE_CLONE_FILE);
			clone.setCloneType(String.join("_", "type", type));
			addRelation(clone);
			clones.add(clone);
			sizeOfFileCloneFiles++;
		}
		LOGGER.info("插入文件级克隆关系数：" + sizeOfFileCloneFiles);
		long groupCount = cloneGroupNumber;
		for(Group group : this.groups) {
			CloneGroup cloneGroup = new CloneGroup();
			cloneGroup.setLanguage(language.toString());
			cloneGroup.setEntityId(generateEntityId());
			cloneGroup.setName(String.join("_", "file", "group", String.valueOf(cloneGroupNumber++)));
			cloneGroup.setSize(group.getGroupIds().size());
			cloneGroup.setCloneLevel(CloneLevel.file.toString());
			addNode(cloneGroup, null);
			for(int id : group.getGroupIds()) {
				CodeNode node = this.cloneFileIdToCodeNode.get(id);
				if(node == null) {
					LOGGER.error("找不到clone id为 " + id + " 的节点");
					continue;
				}
				addRelation(new Contain(cloneGroup, node));
			}
		}
		LOGGER.info("插入文件级克隆组，组数：" + (cloneGroupNumber - groupCount));
	}

}
