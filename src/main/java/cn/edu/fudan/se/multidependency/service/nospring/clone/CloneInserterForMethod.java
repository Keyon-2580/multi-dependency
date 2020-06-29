package cn.edu.fudan.se.multidependency.service.nospring.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.utils.clone.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.clone.data.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.FilePathFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.Group;

public class CloneInserterForMethod extends CloneInserter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CloneInserterForMethod.class);
	private String methodNameTablePath;
	private String methodResultPath;
	private String groupPath;
	private Language language;

	private Map<Integer, FilePathFromCsv> methodPaths = new HashMap<>();
	private Collection<CloneResultFromCsv> cloneResults = new ArrayList<>();
	private Collection<Group> groups = new ArrayList<>();
	private Map<Integer, CodeNode> cloneNodeIdToCodeNode = new HashMap<>();
	
	public CloneInserterForMethod(String methodNameTablePath, String methodResultPath, String groupPath, Language language) {
		super();
		this.methodNameTablePath = methodNameTablePath;
		this.methodResultPath = methodResultPath;
		this.groupPath = groupPath;
		this.language = language;
	}

	@Override
	protected void readMeasureIndex() throws Exception {
		methodPaths = CloneUtil.readCloneCsvForFilePath(methodNameTablePath);
	}

	@Override
	protected void readResult() throws Exception {
		cloneResults = CloneUtil.readCloneResultCsv(methodResultPath);
	}

	@Override
	protected void readGroup() throws Exception {
		groups = CloneUtil.readGroupFile(groupPath);
	}

	@Override
	protected void extractNodesAndRelations() throws Exception {
		LOGGER.info("方法级克隆对数：" + cloneResults.size());
		List<Clone> clones = new ArrayList<>();
		int sizeOfClones = 0;
		for(CloneResultFromCsv cloneResult : cloneResults) {
			int start = cloneResult.getStart();
			int end = cloneResult.getEnd();
			double value = cloneResult.getValue();
			String type = cloneResult.getType();
			FilePathFromCsv filePath1 = methodPaths.get(start);
			if(filePath1 == null) {
				LOGGER.error("path1 is null");
				continue;
			}
			FilePathFromCsv filePath2 = methodPaths.get(end);
			if(filePath2 == null) {
				LOGGER.error("path2 is null");
				continue;
			}
			ProjectFile file1 = this.getNodes().findFileByPathRecursion(filePath1.getFilePath());
			ProjectFile file2 = this.getNodes().findFileByPathRecursion(filePath2.getFilePath());
			if(file1 == null) {
				LOGGER.error("file1 is null " + filePath1.getLineId() + " " + filePath1.getFilePath());
				continue;
			}
			if(file2 == null) {
				LOGGER.error("file2 is null " + filePath2.getLineId() + " " + filePath2.getFilePath());
				continue;
			}
			CodeNode node1 = this.cloneNodeIdToCodeNode.get(filePath1.getLineId());
			CodeNode node2 = this.cloneNodeIdToCodeNode.get(filePath2.getLineId());
			if(node1 == null) {
				node1 = this.getNodes().findNodeByEndLineInFile(file1, filePath1.getEndLine());
			}
			if(node2 == null) {
				node2 = this.getNodes().findNodeByEndLineInFile(file2, filePath2.getEndLine());
			}
			if(node1 == null || node2 == null) {
				// 既不是方法也不是Type的，改为片段
				if(node1 == null) {
					LOGGER.warn("node1 is null "  + filePath1.getLineId() + " " + filePath1.getFilePath() + " " + filePath1.getStartLine() + " " + filePath1.getEndLine());
				}
				if(node2 == null) {
					LOGGER.warn("node2 is null "  + filePath2.getLineId() + " " + filePath2.getFilePath() + " " + filePath2.getStartLine() + " " + filePath2.getEndLine());
				}
				Snippet snippet1 = new Snippet();
				snippet1.setEntityId(generateEntityId());
				snippet1.setStartLine(filePath1.getStartLine());
				snippet1.setEndLine(filePath1.getEndLine());
				snippet1.setIdentifier(String.join(",", file1.getPath(), String.valueOf(filePath1.getStartLine()), String.valueOf(filePath1.getEndLine())));
				snippet1.setName(String.join(",", file1.getName(), String.valueOf(filePath1.getStartLine()), String.valueOf(filePath1.getEndLine())));
				addNode(snippet1, null);
				addRelation(new Contain(file1, snippet1));
				Snippet snippet2 = new Snippet();
				snippet2.setEntityId(generateEntityId());
				snippet2.setStartLine(filePath2.getStartLine());
				snippet2.setEndLine(filePath2.getEndLine());
				snippet2.setIdentifier(String.join(",", file2.getPath(), String.valueOf(filePath2.getStartLine()), String.valueOf(filePath2.getEndLine())));
				snippet2.setName(String.join(",", file2.getName(), String.valueOf(filePath2.getStartLine()), String.valueOf(filePath2.getEndLine())));
				addNode(snippet2, null);
				addRelation(new Contain(file2, snippet2));
				
				node1 = snippet1;
				node2 = snippet2;
			}
			if(node1.getClass() != node2.getClass()) {
				LOGGER.warn("有克隆关系的两个节点不是同一个类型的节点" + filePath1.getLineId() + " " + filePath2.getLineId());
			}
			CloneRelationType cloneType = CloneRelationType.getCloneType(node1, node2);
			if(cloneType == null) {
				LOGGER.error("克隆类型为null：");
				continue;
			}
			cloneNodeIdToCodeNode.put(filePath1.getLineId(), node1);
			cloneNodeIdToCodeNode.put(filePath2.getLineId(), node2);
			Clone clone = new Clone(node1, node2);
			clone.setNode1Index(start);
			clone.setNode2Index(end);
			clone.setNode1StartLine(node1.getStartLine());
			clone.setNode1EndLine(node1.getEndLine());
			clone.setNode2StartLine(node2.getStartLine());
			clone.setNode2EndLine(node2.getEndLine());
			clone.setValue(value);
			clone.setCloneRelationType(cloneType.toString());
			clone.setCloneType(String.join("_", "type", type));
			clones.add(clone);
			addRelation(clone);
			sizeOfClones++;
		}
		LOGGER.info("插入克隆数：" + sizeOfClones);
		long groupCount = cloneGroupNumber;
		for(Group group : this.groups) {
			CloneGroup cloneGroup = new CloneGroup();
			cloneGroup.setLanguage(language.toString());
			cloneGroup.setEntityId(generateEntityId());
			cloneGroup.setName(String.join("_", "file", "group", String.valueOf(cloneGroupNumber++)));
			cloneGroup.setSize(group.getGroupIds().size());
			addNode(cloneGroup, null);
			for(int id : group.getGroupIds()) {
				CodeNode node = this.cloneNodeIdToCodeNode.get(id);
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
