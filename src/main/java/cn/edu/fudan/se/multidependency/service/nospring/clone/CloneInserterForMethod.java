package cn.edu.fudan.se.multidependency.service.nospring.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.clone.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.clone.data.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.MethodIdentifierFromCsv;
import lombok.Setter;

public class CloneInserterForMethod extends ExtractorForNodesAndRelationsImpl {
private static final Executor executor = Executors.newCachedThreadPool();
	
	private static long functionCloneGroupNumber = 0;
	
	private CountDownLatch latch;

	private static final Logger LOGGER = LoggerFactory.getLogger(CloneInserterForMethod.class);
	@Setter
	private String methodNameTablePath;
	@Setter
	private String methodResultPath;

	private Map<Integer, MethodIdentifierFromCsv> methodIdentifiers;
	private Collection<CloneResultFromCsv> cloneResults;
	
	public CloneInserterForMethod(String methodNameTablePath, String methodResultPath) {
		super();
		this.methodNameTablePath = methodNameTablePath;
		this.methodResultPath = methodResultPath;
		this.latch = new CountDownLatch(2);
	}
	
	private void process() throws Exception {
		executor.execute(() -> {
			try {
				methodIdentifiers = CloneUtil.readMethodIdentifiersCsv(methodNameTablePath);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});
		
		executor.execute(() -> {
			try {
				cloneResults = CloneUtil.readCloneResultCsv(methodResultPath);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});
		
		latch.await();
		LOGGER.info("方法克隆对数：" + cloneResults.size());
		List<FunctionCloneFunction> clones = new ArrayList<>();
		int sizeOfFunctionCloneFunctions = 0;
		try {
			for(CloneResultFromCsv cloneResult : cloneResults) {
				int start = cloneResult.getStart();
				int end = cloneResult.getEnd();
				double value = cloneResult.getValue();
				MethodIdentifierFromCsv methodName1 = methodIdentifiers.get(start);
				if(methodName1 == null) {
					LOGGER.warn("methodName1 is null, index: " + start);
					continue;
				}
				MethodIdentifierFromCsv methodName2 = methodIdentifiers.get(end);
				if(methodName2 == null) {
					LOGGER.warn("methodName2 is null, index: " + end);
					continue;
				}
				Node node1 = this.getNodes().findCodeNodeByIdentifier(methodName1.getIdentifier());
				if(node1 == null) {
					LOGGER.warn("function1 is null " + methodName1.getIdentifier());
					continue;
				} 
				Function function1 = (Function) node1;
				Node node2 = this.getNodes().findCodeNodeByIdentifier(methodName2.getIdentifier());
				if(node2 == null) {
					LOGGER.warn("function2 is null " + methodName2.getIdentifier());
					continue;
				}
				Function function2 = (Function) node2;
				if(function1.equals(function2)) {
					LOGGER.warn("方法相同：" + methodName1 + " " + methodName2);
					continue;
				}
				function1.setStartLine(methodName1.getStartLine());
				function1.setEndLine(methodName1.getEndLine());
				function2.setStartLine(methodName2.getStartLine());
				function2.setEndLine(methodName2.getEndLine());
				FunctionCloneFunction clone = new FunctionCloneFunction(function1, function2);
				clone.setFunction1Index(start);
				clone.setFunction2Index(end);
				clone.setFunction1StartLine(methodName1.getStartLine());
				clone.setFunction1EndLine(methodName1.getEndLine());
				clone.setFunction2StartLine(methodName2.getStartLine());
				clone.setFunction2EndLine(methodName2.getEndLine());
				clone.setValue(value);
				addRelation(clone);
				sizeOfFunctionCloneFunctions++;
				clones.add(clone);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.warn(e.getMessage());
		}
		LOGGER.info("插入方法级克隆数：" + sizeOfFunctionCloneFunctions);
		Collection<Collection<? extends Node>> groups = CloneUtil.groupCloneNodes(clones);
		long groupCount = functionCloneGroupNumber;
		for(Collection<? extends Node> nodes : groups) {
			CloneGroup group = new CloneGroup();
			group.setEntityId(generateEntityId());
			group.setName("group_" + functionCloneGroupNumber++);
			group.setLevel(NodeLabelType.Function);
			group.setSize(nodes.size());
			addNode(group, null);
			for(Node node : nodes) {
				addRelation(new Contain(group, node));
			}
		}
		LOGGER.info("插入方法级克隆组，组数：" + (functionCloneGroupNumber - groupCount));
	}

	@Override
	public void addNodesAndRelations() throws Exception {
		process();
	}
	
}
