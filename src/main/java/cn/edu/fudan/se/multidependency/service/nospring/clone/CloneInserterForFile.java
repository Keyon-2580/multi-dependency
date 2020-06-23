package cn.edu.fudan.se.multidependency.service.nospring.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.utils.clone.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.clone.data.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.FilePathFromCsv;
import lombok.Setter;

public class CloneInserterForFile extends CloneInserter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloneInserterForFile.class);
	@Setter
	private String namePath;
	@Setter
	private String resultPath;
	
	public CloneInserterForFile(String namePath, String resultPath) {
		super();
		this.namePath = namePath;
		this.resultPath = resultPath;
	}
	
	private Map<Integer, FilePathFromCsv> filePaths = new HashMap<>();
	private Collection<CloneResultFromCsv> cloneResults = new ArrayList<>();

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
	protected void extractNodesAndRelations() throws Exception {
		LOGGER.info("文件克隆对数：" + cloneResults.size());
		int sizeOfFileCloneFiles = 0;
		List<FileCloneFile> clones = new ArrayList<>();
		for(CloneResultFromCsv cloneResult : cloneResults) {
			int start = cloneResult.getStart();
			int end = cloneResult.getEnd();
			double value = cloneResult.getValue();
			FilePathFromCsv filePath1 = filePaths.get(start);
			if(filePath1 == null) {
				throw new Exception("filePath1 is null");
			}
			FilePathFromCsv filePath2 = filePaths.get(end);
			if(filePath2 == null) {
				throw new Exception("filePath2 is null");
			}
			ProjectFile file1 = this.getNodes().findFileByPathRecursion(filePath1.getFilePath());
			ProjectFile file2 = this.getNodes().findFileByPathRecursion(filePath2.getFilePath());
			if(file1 == null) {
				LOGGER.warn("file1 is null " + filePath1.getLineId() + " " + filePath1.getFilePath());
				continue;
			}
			if(file2 == null) {
				LOGGER.warn("file2 is null " + filePath2.getLineId() + " " + filePath2.getFilePath());
				continue;
			}
			FileCloneFile clone = new FileCloneFile(file1, file2);
			clone.setNode1Index(start);
			clone.setNode2Index(end);
			clone.setNode1StartLine(filePath1.getStartLine());
			clone.setNode1EndLine(filePath1.getEndLine());
			clone.setNode2StartLine(filePath2.getStartLine());
			clone.setNode2EndLine(filePath2.getEndLine());
			clone.setValue(value);
			addRelation(clone);
			clones.add(clone);
			sizeOfFileCloneFiles++;
		}
		LOGGER.info("插入文件级克隆关系数：" + sizeOfFileCloneFiles);
		Collection<Collection<? extends Node>> groups = CloneUtil.groupCloneNodes(clones);
		long groupCount = cloneGroupNumber;
		for(Collection<? extends Node> nodes : groups) {
			CloneGroup group = new CloneGroup();
			group.setEntityId(generateEntityId());
			group.setName("group_" + cloneGroupNumber++);
			group.setLevel(NodeLabelType.ProjectFile);
			group.setSize(nodes.size());
			addNode(group, null);
			for(Node node : nodes) {
				addRelation(new Contain(group, node));
			}
		}
		LOGGER.info("插入文件级克隆组，组数：" + (cloneGroupNumber - groupCount));
	}

}
