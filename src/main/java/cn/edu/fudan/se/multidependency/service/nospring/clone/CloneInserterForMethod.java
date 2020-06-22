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
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.NodeWithLine;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.model.relation.clone.SnippetCloneSnippet;
import cn.edu.fudan.se.multidependency.model.relation.clone.TypeCloneType;
import cn.edu.fudan.se.multidependency.utils.clone.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.clone.data.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.FilePathFromCsv;
import lombok.Setter;

public class CloneInserterForMethod extends CloneInserter {
	private static final Logger LOGGER = LoggerFactory.getLogger(CloneInserterForMethod.class);
	@Setter
	private String methodNameTablePath;
	@Setter
	private String methodResultPath;

	private Map<Integer, FilePathFromCsv> methodPaths = new HashMap<>();
	private Collection<CloneResultFromCsv> cloneResults = new ArrayList<>();
	
	public CloneInserterForMethod(String methodNameTablePath, String methodResultPath) {
		super();
		this.methodNameTablePath = methodNameTablePath;
		this.methodResultPath = methodResultPath;
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
		List<FunctionCloneFunction> functionClones = new ArrayList<>();
		List<TypeCloneType> typeClones = new ArrayList<>();
		// 既不是方法又不是Type的改为片段级
		List<SnippetCloneSnippet> snippetClones = new ArrayList<>();
		int sizeOfFunctionCloneFunctions = 0;
		int sizeOfTypeCloneTypes = 0;
		int sizeOfSnippetCloneSnippets = 0;
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
				NodeWithLine node1 = this.getNodes().findNodeByEndLineInFile(file1, path1.getEndLine());
				NodeWithLine node2 = this.getNodes().findNodeByEndLineInFile(file2, path2.getEndLine());
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
					
					SnippetCloneSnippet clone = new SnippetCloneSnippet(snippet1, snippet2);
					clone.setNode1Index(start);
					clone.setNode2Index(end);
					clone.setNode1StartLine(snippet1.getStartLine());
					clone.setNode1EndLine(snippet1.getEndLine());
					clone.setNode2StartLine(snippet2.getStartLine());
					clone.setNode2EndLine(snippet2.getEndLine());
					clone.setValue(value);
					addRelation(clone);
					sizeOfSnippetCloneSnippets++;
					snippetClones.add(clone);
					continue;
				}
				if(node1.getClass() != node2.getClass()) {
					LOGGER.warn("有克隆关系的两个节点不是同一个类型的节点");
					continue;
				}
				if(node1 instanceof Function) {
					Function function1 = (Function) node1;
					Function function2 = (Function) node2;
					FunctionCloneFunction clone = new FunctionCloneFunction(function1, function2);
					clone.setNode1Index(start);
					clone.setNode2Index(end);
					clone.setNode1StartLine(function1.getStartLine());
					clone.setNode1EndLine(function1.getEndLine());
					clone.setNode2StartLine(function2.getStartLine());
					clone.setNode2EndLine(function2.getEndLine());
					clone.setValue(value);
					addRelation(clone);
					sizeOfFunctionCloneFunctions++;
					functionClones.add(clone);
					continue;
				}
				if(node1 instanceof Type) {
					Type type1 = (Type) node1;
					Type type2 = (Type) node2;
					TypeCloneType clone = new TypeCloneType(type1, type2);
					clone.setNode1Index(start);
					clone.setNode2Index(end);
					clone.setNode1StartLine(type1.getStartLine());
					clone.setNode1EndLine(type1.getEndLine());
					clone.setNode2StartLine(type2.getStartLine());
					clone.setNode2EndLine(type2.getEndLine());
					clone.setValue(value);
					addRelation(clone);
					sizeOfTypeCloneTypes++;
					typeClones.add(clone);
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.warn(e.getMessage());
		}
		LOGGER.info("插入方法级克隆数：" + sizeOfFunctionCloneFunctions);
		Collection<Collection<? extends Node>> functionGroups = CloneUtil.groupCloneNodes(functionClones);
		long groupCount = cloneGroupNumber;
		for(Collection<? extends Node> nodes : functionGroups) {
			CloneGroup group = new CloneGroup();
			group.setEntityId(generateEntityId());
			group.setName("group_" + cloneGroupNumber++);
			group.setLevel(NodeLabelType.Function);
			group.setSize(nodes.size());
			addNode(group, null);
			for(Node node : nodes) {
				addRelation(new Contain(group, node));
			}
		}
		LOGGER.info("插入方法级克隆组，组数：" + (cloneGroupNumber - groupCount));
		LOGGER.info("插入Type级克隆数：" + sizeOfTypeCloneTypes);
		Collection<Collection<? extends Node>> typeGroups = CloneUtil.groupCloneNodes(typeClones);
		groupCount = cloneGroupNumber;
		for(Collection<? extends Node> nodes : typeGroups) {
			CloneGroup group = new CloneGroup();
			group.setEntityId(generateEntityId());
			group.setName("group_" + cloneGroupNumber++);
			group.setLevel(NodeLabelType.Type);
			group.setSize(nodes.size());
			addNode(group, null);
			for(Node node : nodes) {
				addRelation(new Contain(group, node));
			}
		}
		LOGGER.info("插入Type级克隆组，组数：" + (cloneGroupNumber - groupCount));
		LOGGER.info("插入片段级克隆数：" + sizeOfSnippetCloneSnippets);
		Collection<Collection<? extends Node>> snippetGroups = CloneUtil.groupCloneNodes(snippetClones);
		groupCount = cloneGroupNumber;
		for(Collection<? extends Node> nodes : snippetGroups) {
			CloneGroup group = new CloneGroup();
			group.setEntityId(generateEntityId());
			group.setName("group_" + cloneGroupNumber++);
			group.setLevel(NodeLabelType.Snippet);
			group.setSize(nodes.size());
			addNode(group, null);
			for(Node node : nodes) {
				addRelation(new Contain(group, node));
			}
		}
		LOGGER.info("插入Type级克隆组，组数：" + (cloneGroupNumber - groupCount));
	}
	
}
