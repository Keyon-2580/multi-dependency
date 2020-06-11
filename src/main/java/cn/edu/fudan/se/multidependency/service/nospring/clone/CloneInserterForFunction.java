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

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.MethodNameForJavaFromCsv;
import cn.edu.fudan.se.multidependency.utils.FunctionUtil;
import lombok.Setter;

public class CloneInserterForFunction extends ExtractorForNodesAndRelationsImpl {

	private static final Executor executor = Executors.newCachedThreadPool();
	
	private static long functionCloneGroupNumber = 0;
	
	private CountDownLatch latch;

	private static final Logger LOGGER = LoggerFactory.getLogger(CloneInserterForFunction.class);
	@Setter
	private Language language;
	@Setter
	private String methodNameTablePath;
	@Setter
	private String methodResultPath;

	private Map<Integer, MethodNameForJavaFromCsv> methodNames;
	private Collection<CloneResultFromCsv> cloneResults;
	
	public CloneInserterForFunction(Language language, String methodNameTablePath, String methodResultPath) {
		super();
		this.language = language;
		this.methodNameTablePath = methodNameTablePath;
		this.methodResultPath = methodResultPath;
		this.latch = new CountDownLatch(2);
	}
	
	private void processJava() throws Exception {
		executor.execute(() -> {
			try {
				methodNames = CloneUtil.readJavaCloneCsvForMethodName(methodNameTablePath);
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
				MethodNameForJavaFromCsv methodName1 = methodNames.get(start);
				if(methodName1 == null) {
					LOGGER.warn("methodName1 is null, index: " + start);
					continue;
				}
				MethodNameForJavaFromCsv methodName2 = methodNames.get(end);
				if(methodName2 == null) {
					LOGGER.warn("methodName2 is null, index: " + end);
					continue;
				}
				Project project1 = this.getNodes().findProject(methodName1.getProjectName(), language);
				if(project1 == null) {
					LOGGER.warn("project1 is null " + methodName1.getProjectName());
					continue;
				}
				Project project2 = this.getNodes().findProject(methodName2.getProjectName(), language);
				if(project2 == null) {
					LOGGER.warn("project2 is null " + methodName2.getProjectName());
					continue;
				}
				Function function1 = findJavaFunctionByMethod(methodName1, project1);
				if(function1 == null) {
					LOGGER.warn("function1 is null " + methodName1);
					continue;
				}
				Function function2 = findJavaFunctionByMethod(methodName2, project2);
				if(function2 == null) {
					LOGGER.warn("function2 is null " + methodName2);
					continue;
				}
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
		if(Language.java == language) {
			processJava();
		}
	}
	
	private Function findJavaFunctionByMethod(MethodNameForJavaFromCsv methodName, Project project) throws Exception {
		Function result = null;
		Map<String, List<Function>> functionNameToFunctions = this.getNodes().findFunctionsInProject(project);
		String functionNameFromCsv = FunctionUtil.extractFunctionNameAndParameters(methodName.getFunctionFullName()).get(0);
		List<Function> functions = functionNameToFunctions.get(functionNameFromCsv);
		if(functions == null) {
			LOGGER.warn("没有找到该方法名的方法：" + methodName.getFunctionFullName());
			functions = new ArrayList<>();
		}
		for(Function function : functions) {
			if(FunctionUtil.isSameJavaFunction(function, methodName.getFunctionFullName())) {
				result = function;
			}
		}
		return result;
	}

}
