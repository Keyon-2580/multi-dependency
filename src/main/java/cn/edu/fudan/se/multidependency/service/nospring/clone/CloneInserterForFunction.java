package cn.edu.fudan.se.multidependency.service.nospring.clone;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.FunctionUtil;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.MethodNameForJavaFromCsv;
import lombok.Setter;

public class CloneInserterForFunction extends ExtractorForNodesAndRelationsImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloneUtil.class);
	@Setter
	private Language language;
	@Setter
	private String methodNameTablePath;
	@Setter
	private String methodResultPath;
	
	public CloneInserterForFunction(Language language, String methodNameTablePath, String methodResultPath) {
		super();
		this.language = language;
		this.methodNameTablePath = methodNameTablePath;
		this.methodResultPath = methodResultPath;
	}

	@Override
	public void addNodesAndRelations() throws Exception {
		if(Language.java == language) {
			Map<Integer, MethodNameForJavaFromCsv> methodNames = CloneUtil.readJavaCloneCsvForMethodName(methodNameTablePath);
			Iterable<CloneResultFromCsv> cloneResults = CloneUtil.readCloneResultCsv(methodResultPath);
			
			for(CloneResultFromCsv cloneResult : cloneResults) {
				int start = cloneResult.getStart();
				int end = cloneResult.getEnd();
				double value = cloneResult.getValue();
				MethodNameForJavaFromCsv methodName1 = methodNames.get(start);
				if(methodName1 == null) {
					throw new Exception("methofName1 is null");
				}
				MethodNameForJavaFromCsv methodName2 = methodNames.get(end);
				if(methodName2 == null) {
					throw new Exception("methodName2 is null");
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
				FunctionCloneFunction clone = new FunctionCloneFunction(function1, function2);
				clone.setValue(value);
				addRelation(clone);
			}
		}
	}
	
	private Function findJavaFunctionByMethod(MethodNameForJavaFromCsv methodName, Project project) throws Exception {
		Function result = null;
		Map<String, List<Function>> functionNameToFunctions = this.getNodes().findFunctionsInProject(project);
		String functionNameFromCsv = FunctionUtil.extractFunctionNameAndParameters(methodName.getFunctionFullName()).get(0);
		List<Function> functions = functionNameToFunctions.get(functionNameFromCsv);
		if(functions == null) {
			System.out.println(methodName.getFunctionFullName());
		}
		for(Function function : functions) {
			if(FunctionUtil.isSameJavaFunction(function, methodName.getFunctionFullName())) {
				result = function;
			}
		}
		return result;
	}

}
