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

public class CloneInserterForMethod extends CloneInserter {
	private static final Logger LOGGER = LoggerFactory.getLogger(CloneInserterForMethod.class);
	private String methodNameTablePath;
	private String methodResultPath;
	private Language language;

	private Map<Integer, FilePathFromCsv> methodPaths = new HashMap<>();
	private Collection<CloneResultFromCsv> cloneResults = new ArrayList<>();
	
	public CloneInserterForMethod(String methodNameTablePath, String methodResultPath, Language language) {
		super();
		this.methodNameTablePath = methodNameTablePath;
		this.methodResultPath = methodResultPath;
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
	protected void extractNodesAndRelations() throws Exception {
		LOGGER.info("方法级克隆对数：" + cloneResults.size());
		List<Clone> clones = new ArrayList<>();
		int sizeOfClones = 0;
		try {
			for(CloneResultFromCsv cloneResult : cloneResults) {
				int start = cloneResult.getStart();
				int end = cloneResult.getEnd();
				double value = cloneResult.getValue();
				FilePathFromCsv path1 = methodPaths.get(start);
				if(path1 == null) {
					LOGGER.warn("path1 is null");
					continue;
				}
				FilePathFromCsv path2 = methodPaths.get(end);
				if(path2 == null) {
					LOGGER.warn("path2 is null");
					continue;
				}
				ProjectFile file1 = this.getNodes().findFileByPathRecursion(path1.getFilePath());
				ProjectFile file2 = this.getNodes().findFileByPathRecursion(path2.getFilePath());
				if(file1 == null) {
					LOGGER.warn("file1 is null " + path1.getLineId() + " " + path1.getFilePath());
					continue;
				}
				if(file2 == null) {
					LOGGER.warn("file2 is null " + path2.getLineId() + " " + path2.getFilePath());
					continue;
				}
				CodeNode node1 = this.getNodes().findNodeByEndLineInFile(file1, path1.getEndLine());
				CodeNode node2 = this.getNodes().findNodeByEndLineInFile(file2, path2.getEndLine());
				if(node1 == null || node2 == null) {
					if(node1 == null) {
						LOGGER.warn("node1 is null "  + path1.getLineId() + " " + path1.getFilePath() + " " + path1.getStartLine() + " " + path1.getEndLine());
					}
					if(node2 == null) {
						LOGGER.warn("node2 is null "  + path2.getLineId() + " " + path2.getFilePath() + " " + path2.getStartLine() + " " + path2.getEndLine());
					}
					Snippet snippet1 = new Snippet();
					snippet1.setEntityId(generateEntityId());
					snippet1.setStartLine(path1.getStartLine());
					snippet1.setEndLine(path1.getEndLine());
					snippet1.setName(String.join(",", file1.getPath(), String.valueOf(path1.getStartLine()), String.valueOf(path1.getEndLine())));
					addNode(snippet1, null);
					addRelation(new Contain(file1, snippet1));
					Snippet snippet2 = new Snippet();
					snippet2.setEntityId(generateEntityId());
					snippet2.setStartLine(path2.getStartLine());
					snippet2.setEndLine(path2.getEndLine());
					snippet2.setName(String.join(",", file2.getPath(), String.valueOf(path2.getStartLine()), String.valueOf(path2.getEndLine())));
					addNode(snippet2, null);
					addRelation(new Contain(file2, snippet2));
					
					Clone clone = new Clone(snippet1, snippet2);
					clone.setNode1Index(start);
					clone.setNode2Index(end);
					clone.setNode1StartLine(snippet1.getStartLine());
					clone.setNode1EndLine(snippet1.getEndLine());
					clone.setNode2StartLine(snippet2.getStartLine());
					clone.setNode2EndLine(snippet2.getEndLine());
					clone.setValue(value);
					clone.setCloneRelationType(CloneRelationType.str_SNIPPET_CLONE_SNIPPET);
					addRelation(clone);
					clones.add(clone);
					sizeOfClones++;
					continue;
				}
				if(node1.getClass() != node2.getClass()) {
					LOGGER.warn("有克隆关系的两个节点不是同一个类型的节点");
				}
				CloneRelationType cloneType = CloneRelationType.getCloneType(node1, node2);
				if(cloneType == null) {
					LOGGER.warn("克隆类型为null：");
					continue;
				}
				Clone clone = new Clone(node1, node2);
				clone.setNode1Index(start);
				clone.setNode2Index(end);
				clone.setNode1StartLine(node1.getStartLine());
				clone.setNode1EndLine(node1.getEndLine());
				clone.setNode2StartLine(node2.getStartLine());
				clone.setNode2EndLine(node2.getEndLine());
				clone.setValue(value);
				clone.setCloneRelationType(cloneType.toString());
				clones.add(clone);
				addRelation(clone);
				sizeOfClones++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.warn(e.getMessage());
		}
		LOGGER.info("插入克隆数：" + sizeOfClones);
		Collection<Collection<CodeNode>> groups = CloneUtil.groupCloneNodes(clones);
		long groupCount = cloneGroupNumber;
		for(Collection<? extends Node> nodes : groups) {
			CloneGroup group = new CloneGroup();
			group.setLanguage(language.toString());
			group.setEntityId(generateEntityId());
			group.setName(String.join("_", "code", "group", String.valueOf(cloneGroupNumber++)));
			group.setSize(nodes.size());
			addNode(group, null);
			for(Node node : nodes) {
				addRelation(new Contain(group, node));
			}
		}
		LOGGER.info("插入克隆组，组数：" + (cloneGroupNumber - groupCount));
	}
	
}
