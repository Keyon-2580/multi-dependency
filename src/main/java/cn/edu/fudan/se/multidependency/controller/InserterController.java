package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.spring.NodeService;
import cn.edu.fudan.se.multidependency.utils.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.FilePathFromCsv;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.MethodNameForJavaFromCsv;

@Controller
@RequestMapping("/insert")
public class InserterController {
	private static final Logger LOGGER = LoggerFactory.getLogger(InserterController.class);
	
	@Autowired
	private NodeService nodeService;

	@GetMapping("/")
	public String index() {
		return "insert/insert";
	}
	
	public Collection<FunctionCloneFunction> extractJavaFunctionClones(String namePath, String resultPath) throws Exception {
		List<FunctionCloneFunction> result = new ArrayList<>();
		Map<Integer, MethodNameForJavaFromCsv> methodNames = CloneUtil.readJavaCloneCsvForMethodName(namePath);
		Collection<CloneResultFromCsv> cloneResults = CloneUtil.readCloneResultCsv(resultPath);
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
			Project project1 = nodeService.queryProject(methodName1.getProjectName(), Language.java);
			if(project1 == null) {
				LOGGER.warn("project1 is null " + methodName1.getProjectName());
				continue;
			}
			Project project2 = nodeService.queryProject(methodName2.getProjectName(), Language.java);
			if(project2 == null) {
				LOGGER.warn("project2 is null " + methodName2.getProjectName());
				continue;
			}
//			Function function1 = findJavaFunctionByMethod(methodName1, project1);
//			if(function1 == null) {
//				LOGGER.warn("function1 is null " + methodName1);
//				continue;
//			}
//			function1.setStartLine(methodName1.getStartLine());
//			function1.setEndLine(methodName1.getEndLine());
//			Function function2 = findJavaFunctionByMethod(methodName2, project2);
//			if(function2 == null) {
//				LOGGER.warn("function2 is null " + methodName2);
//				continue;
//			}
//			function2.setStartLine(methodName2.getStartLine());
//			function2.setEndLine(methodName2.getEndLine());
//			FunctionCloneFunction clone = new FunctionCloneFunction(function1, function2);
//			clone.setFunction1Index(start);
//			clone.setFunction2Index(end);
//			clone.setFunction1StartLine(methodName1.getStartLine());
//			clone.setFunction1EndLine(methodName1.getEndLine());
//			clone.setFunction2StartLine(methodName2.getStartLine());
//			clone.setFunction2EndLine(methodName2.getEndLine());
//			clone.setValue(value);
//			result.add(clone);
		}
		return result;
	}
	
	public Collection<FileCloneFile> extractFileClones(String namePath, String resultPath) throws Exception {
		List<FileCloneFile> result = new ArrayList<>();
		Map<Integer, FilePathFromCsv> filePaths = CloneUtil.readJavaCloneCsvForFilePath(namePath);;
		Collection<CloneResultFromCsv> cloneResults = CloneUtil.readCloneResultCsv(resultPath);
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
			ProjectFile file1 = nodeService.queryFile(filePath1.getFilePath());
			ProjectFile file2 = nodeService.queryFile(filePath2.getFilePath());
			if(file1 == null) {
				LOGGER.warn("file1 is null " + filePath1.getFilePath());
				continue;
			}
			if(file2 == null) {
				LOGGER.warn("file2 is null " + filePath2.getFilePath());
				continue;
			}
			FileCloneFile clone = new FileCloneFile(file1, file2);
			clone.setFile1Index(start);
			clone.setFile2Index(end);
			clone.setFile1StartLine(filePath1.getStartLine());
			clone.setFile1EndLine(filePath1.getEndLine());
			clone.setFile2StartLine(filePath2.getStartLine());
			clone.setFile2EndLine(filePath2.getEndLine());
			clone.setValue(value);
			result.add(clone);
		}
		return result;
	}
	
	
}
