package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.DependOnType;
import cn.edu.fudan.se.multidependency.model.relation.FileDependOnFile;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.code.FileImportFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.FileImportType;
import cn.edu.fudan.se.multidependency.model.relation.code.FileImportVariable;
import cn.edu.fudan.se.multidependency.model.relation.code.FileIncludeFile;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionCastType;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionParameterType;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionReturnType;
import cn.edu.fudan.se.multidependency.model.relation.code.FunctionThrowType;
import cn.edu.fudan.se.multidependency.model.relation.code.NodeAnnotationType;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeExtendsType;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeImplementsType;
import cn.edu.fudan.se.multidependency.model.relation.code.VariableIsType;
import cn.edu.fudan.se.multidependency.model.relation.code.VariableTypeParameterType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.repository.relation.FileDependOnFileRepository;

@Service
public class FileDependOnFileExtractServiceImpl implements FileDependOnFileExtractService {
	
	private Project project;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;
	
	@Autowired
	private FileDependOnFileRepository fileDependOnFileRepository;
	
	private Iterable<FileImportType> fileImportTypes = new ArrayList<>();
	private Iterable<FileImportFunction> fileImportFunctions = new ArrayList<>();
	private Iterable<FileImportVariable> fileImportVariables = new ArrayList<>();
	private Iterable<FileIncludeFile> fileIncludeFiles = new ArrayList<>();
	
	private Iterable<TypeExtendsType> typeExtendsTypes = new ArrayList<>();
	private Iterable<TypeImplementsType> typeImplementsTypes = new ArrayList<>();
	
	private Iterable<TypeCallFunction> typeCallFunctions = new ArrayList<>();
	private Iterable<FunctionCallFunction> functionCallFunctions = new ArrayList<>();
	private Iterable<FunctionDynamicCallFunction> functionDynamicCallFunctions = new ArrayList<>();
	
	private Iterable<FunctionCastType> functionCastTypes = new ArrayList<>();
	private Iterable<FunctionParameterType> functionParameterTypes = new ArrayList<>();
	private Iterable<FunctionReturnType> functionReturnTypes = new ArrayList<>();
	private Iterable<FunctionThrowType> functionThrowTypes = new ArrayList<>();
	private Iterable<NodeAnnotationType> nodeAnnotationTypes = new ArrayList<>();
	private Iterable<VariableIsType> variableIsTypes = new ArrayList<>();
	private Iterable<VariableTypeParameterType> variableTypeParameterTypes = new ArrayList<>();

	private Map<ProjectFile, Map<ProjectFile, FileDependOnFile>> relations = new HashMap<>();
	
	@Override
	public void setProject(Long projectGraphId) {
		this.project = staticAnalyseService.findProject(projectGraphId);
		clearCache();
	}

	@Override
	public void setProject(Project project) {
		this.project = project;
		clearCache();
	}

	private Map<Node, ProjectFile> cache = new HashMap<>();
	
	private void clearCache() {
		cache.clear();
	}
	
	private void cacheNode(Node node, ProjectFile file) {
		cache.put(node, file);
	}
	
	private ProjectFile findNodeBelongToFile(Node node) throws Exception {
		ProjectFile file = cache.get(node);
		if(file == null) {
			if(node instanceof Type) {
				file = staticAnalyseService.findTypeBelongToFile((Type) node);
			} else if(node instanceof Function) {
				file = staticAnalyseService.findFunctionBelongToFile((Function) node);
			} else if(node instanceof Variable) {
				file = staticAnalyseService.findVariableBelongToFile((Variable) node);
			} else {
				throw new Exception("error node type : " + node.getClass());
			}
			cacheNode(node, file);
		}
		return file;
	}
	
	@Override
	public Map<ProjectFile, Map<ProjectFile, FileDependOnFile>> extractFileDependOnFiles() throws Exception {
//		if(this.project == null) {
			extractAllProject();
//		} else {
//			extractSpecifiedProject();
//		}
		process();
		return relations;
	}
	
	private void extractAllProject() throws Exception {
		int i = 0;
		relations = new HashMap<>();
		System.out.println("extractAll " + i++);
		fileIncludeFiles = staticAnalyseService.findAllFileIncludeFileRelations();
		System.out.println("extract " + i++);
		fileImportTypes = staticAnalyseService.findAllFileImportTypeRelations();
		System.out.println("extract " + i++);
		fileImportFunctions = staticAnalyseService.findAllFileImportFunctionRelations();
		System.out.println("extract " + i++);
		fileImportVariables = staticAnalyseService.findAllFileImportVariableRelations();
		System.out.println("extract " + i++);
		typeImplementsTypes = staticAnalyseService.findAllImplementsRelations();
		System.out.println("extract " + i++);
		typeExtendsTypes = staticAnalyseService.findAllExtendsRelations();
		System.out.println("extract " + i++);
		typeCallFunctions = staticAnalyseService.findAllTypeCallFunctions();
		System.out.println("extract " + i++);
		functionCallFunctions = staticAnalyseService.findAllFunctionCallFunctionRelations();
		System.out.println("extract " + i++);
		functionCastTypes = staticAnalyseService.findAllFunctionCastTypeRelations();
		System.out.println("extract " + i++);
		functionParameterTypes = staticAnalyseService.findAllFunctionParameterTypeRelations();
		System.out.println("extract " + i++);
		functionReturnTypes = staticAnalyseService.findAllFunctionReturnTypeRelations();
		System.out.println("extract " + i++);
		functionThrowTypes = staticAnalyseService.findAllFunctionThrowTypeRelations();
		System.out.println("extract " + i++);
		nodeAnnotationTypes = staticAnalyseService.findAllNodeAnnotationTypeRelations();
		System.out.println("extract " + i++);
		variableIsTypes = staticAnalyseService.findAllVariableIsTypeRelations();
		System.out.println("extract " + i++);
		variableTypeParameterTypes = staticAnalyseService.findAllVariableTypeParameterTypeRelations();
		
		System.out.println("extract " + i++);
		functionDynamicCallFunctions = dynamicAnalyseService.findAllFunctionDynamicCallFunctionRelations(true);
	}
	
	/*private void extractSpecifiedProject() throws Exception {
		int i = 0;
		relations = new HashMap<>();
		System.out.println("extract " + i++);
		fileIncludeFiles = staticAnalyseService.findProjectContainFileIncludeFileRelations(project);
		System.out.println("extract " + i++);
		fileImportTypes = staticAnalyseService.findProjectContainFileImportTypeRelations(project);
		System.out.println("extract " + i++);
		fileImportFunctions = staticAnalyseService.findProjectContainFileImportFunctionRelations(project);
		System.out.println("extract " + i++);
		fileImportVariables = staticAnalyseService.findProjectContainFileImportVariableRelations(project);
		System.out.println("extract " + i++);
		typeImplementsTypes = staticAnalyseService.findProjectContainImplementsRelations(project);
		System.out.println("extract " + i++);
		typeExtendsTypes = staticAnalyseService.findProjectContainExtendsRelations(project);
		System.out.println("extract " + i++);
		typeCallFunctions = staticAnalyseService.findProjectContainTypeCallFunctions(project);
		System.out.println("extract " + i++);
		functionCallFunctions = staticAnalyseService.findFunctionCallFunctionRelations(project);
		System.out.println("extract " + i++);
		functionCastTypes = staticAnalyseService.findProjectContainFunctionCastTypeRelations(project);
		System.out.println("extract " + i++);
		functionParameterTypes = staticAnalyseService.findProjectContainFunctionParameterTypeRelations(project);
		System.out.println("extract " + i++);
		functionReturnTypes = staticAnalyseService.findProjectContainFunctionReturnTypeRelations(project);
		System.out.println("extract " + i++);
		functionThrowTypes = staticAnalyseService.findProjectContainFunctionThrowTypeRelations(project);
		System.out.println("extract " + i++);
		nodeAnnotationTypes = staticAnalyseService.findProjectContainNodeAnnotationTypeRelations(project);
		System.out.println("extract " + i++);
		variableIsTypes = staticAnalyseService.findProjectContainVariableIsTypeRelations(project);
		System.out.println("extract " + i++);
		variableTypeParameterTypes = staticAnalyseService.findProjectContainVariableTypeParameterTypeRelations(project);
		
		System.out.println("extract " + i++);
		functionDynamicCallFunctions = dynamicAnalyseService.findFunctionDynamicCallFunctionRelations(project, true);
	}*/
	
	private void process() throws Exception {
		int i = 0;
		System.out.println("process " + i++);
		processFileIncludeFiles();
		System.out.println("process " + i++);
		processFileImportTypes();
		System.out.println("process " + i++);
		processFileImportFunctions();
		System.out.println("process " + i++);
		processFileImportVariables();
		System.out.println("process " + i++);
		processTypeExtendsTypes();
		System.out.println("process " + i++);
		processTypeImplementsTypes();
		System.out.println("process " + i++);
		processTypeCallFunctions();
		System.out.println("process " + i++);
		processFunctionCallFunctions();
		System.out.println("process " + i++);
		processFunctionCastTypes();
		System.out.println("process " + i++);
		processFunctionParameterTypes();
		System.out.println("process " + i++);
		processFunctionReturnTypes();
		System.out.println("process " + i++);
		processFunctionThrowTypes();
		System.out.println("process " + i++);
		processNodeAnnotationTypes();
		System.out.println("process " + i++);
		processVariableIsTypes();
		System.out.println("process " + i++);
		processVariableTypeParameterTypes();
		System.out.println("process " + i++);
		processFunctionDynamicFunctions();
	}
	
	private void processFunctionCastTypes() throws Exception {
		for(FunctionCastType relation : functionCastTypes) {
			ProjectFile fileStart = findNodeBelongToFile(relation.getFunction());
			ProjectFile fileEnd = findNodeBelongToFile(relation.getCastType());
			addDependTimes(fileStart, fileEnd, DependOnType.USE, relation);
		}
	}

	private void processFunctionParameterTypes() throws Exception {
		for(FunctionParameterType relation : functionParameterTypes) {
			ProjectFile fileStart = findNodeBelongToFile(relation.getFunction());
			ProjectFile fileEnd = findNodeBelongToFile(relation.getParameterType());
			addDependTimes(fileStart, fileEnd, DependOnType.USE, relation);
		}
	}

	private void processFunctionReturnTypes() throws Exception {
		for(FunctionReturnType relation : functionReturnTypes) {
			ProjectFile fileStart = findNodeBelongToFile(relation.getFunction());
			ProjectFile fileEnd = findNodeBelongToFile(relation.getReturnType());
			addDependTimes(fileStart, fileEnd, DependOnType.USE, relation);
		}
	}

	private void processFunctionThrowTypes() throws Exception {
		for(FunctionThrowType relation : functionThrowTypes) {
			ProjectFile fileStart = findNodeBelongToFile(relation.getFunction());
			ProjectFile fileEnd = findNodeBelongToFile(relation.getType());
			addDependTimes(fileStart, fileEnd, DependOnType.USE, relation);
		}
	}

	private void processNodeAnnotationTypes() throws Exception {
		for(NodeAnnotationType relation : nodeAnnotationTypes) {
			ProjectFile fileStart = findNodeBelongToFile(relation.getStartNode());
			ProjectFile fileEnd = findNodeBelongToFile(relation.getAnnotationType());
			addDependTimes(fileStart, fileEnd, DependOnType.USE, relation);
		}
	}
	
	private void processVariableIsTypes() throws Exception {
		for(VariableIsType relation : variableIsTypes) {
			ProjectFile fileStart = findNodeBelongToFile(relation.getVariable());
			ProjectFile fileEnd = findNodeBelongToFile(relation.getType());
			addDependTimes(fileStart, fileEnd, DependOnType.USE, relation);
		}
	}

	private void processVariableTypeParameterTypes() throws Exception {
		for(VariableTypeParameterType relation : variableTypeParameterTypes) {
			ProjectFile fileStart = findNodeBelongToFile(relation.getVariable());
			ProjectFile fileEnd = findNodeBelongToFile(relation.getType());
			addDependTimes(fileStart, fileEnd, DependOnType.USE, relation);
		}
	}

	private void processTypeImplementsTypes() throws Exception {
		for(TypeImplementsType relation : typeImplementsTypes) {
			ProjectFile fileStart = findNodeBelongToFile(relation.getStart());
			ProjectFile fileEnd = findNodeBelongToFile(relation.getEnd());
			addDependTimes(fileStart, fileEnd, DependOnType.EXTENDS_OR_IMPLEMENTS, relation);
		}
	}

	private void processTypeExtendsTypes() throws Exception {
		for(TypeExtendsType relation : typeExtendsTypes) {
			ProjectFile fileStart = findNodeBelongToFile(relation.getStart());
			ProjectFile fileEnd = findNodeBelongToFile(relation.getEnd());
			addDependTimes(fileStart, fileEnd, DependOnType.EXTENDS_OR_IMPLEMENTS, relation);
		}
	}

	private void processTypeCallFunctions() throws Exception {
		for(TypeCallFunction call : typeCallFunctions) {
			ProjectFile fileCaller = findNodeBelongToFile(call.getType());
			ProjectFile fileCalled = findNodeBelongToFile(call.getCallFunction());
			addDependTimes(fileCaller, fileCalled, DependOnType.CALL, call);
		}
	}

	private void processFunctionDynamicFunctions() throws Exception {
		for(FunctionDynamicCallFunction call : functionDynamicCallFunctions) {
			ProjectFile fileCaller = findNodeBelongToFile(call.getFunction());
			ProjectFile fileCalled = findNodeBelongToFile(call.getCallFunction());
			addDependTimes(fileCaller, fileCalled, DependOnType.DYNAMIC_CALL, call);
		}		
	}

	private void processFunctionCallFunctions() throws Exception {
		for(FunctionCallFunction call : functionCallFunctions) {
			ProjectFile fileCaller = findNodeBelongToFile(call.getFunction());
			ProjectFile fileCalled = findNodeBelongToFile(call.getCallFunction());
			addDependTimes(fileCaller, fileCalled, DependOnType.CALL, call);
		}
	}

	@Override
	public void save() {
		System.out.println("saveStart");
		System.out.println(relations.size());
		for(ProjectFile start : relations.keySet()) {
			Map<ProjectFile, FileDependOnFile> temp = relations.get(start);
			if(temp == null) {
				System.out.println(start.getFileName());
			}
			for(ProjectFile end : temp.keySet()) {
				FileDependOnFile dependRelation = temp.get(end);
				System.out.println(project.getProjectName() + " " + dependRelation.getStart().getFileName() + " " + dependRelation.getEnd().getFileName() + " " + dependRelation.getDependOnTimes());
				fileDependOnFileRepository.save(dependRelation);
			}
		}
		System.out.println("saveEnd");
	}

	private void addDependTimes(ProjectFile startFile, ProjectFile endFile, DependOnType type, Relation relation) {
		if(startFile.equals(endFile)) {
			// 去掉自身调用
			return;
		}
		Map<ProjectFile, FileDependOnFile> temp = relations.get(startFile);
		temp = temp == null ? new HashMap<>() : temp;
		FileDependOnFile depend = temp.get(endFile);
		depend = depend == null ? new FileDependOnFile(startFile, endFile) : depend;
		switch(type) {
		case IMPORT_OR_INCLUDE:
			depend.addTimes(DependOnType.IMPORT_OR_INCLUDE, !depend.hasDependOnType(DependOnType.IMPORT_OR_INCLUDE), relation);
			break;
		case CALL:
			
		case DYNAMIC_CALL:
			
		case EXTENDS_OR_IMPLEMENTS:
			
		case USE:
			depend.addTimes(type, true, relation);
		}
		temp.put(endFile, depend);
		relations.put(startFile, temp);
	}

	private void processFileIncludeFiles() throws Exception {
		for(FileIncludeFile includeFile : fileIncludeFiles) {
			ProjectFile start = includeFile.getStart();
			ProjectFile end = includeFile.getEnd();
			addDependTimes(start, end, DependOnType.IMPORT_OR_INCLUDE, includeFile);
		}
	}
	private void processFileImportTypes() throws Exception {
		for(FileImportType importType : fileImportTypes) {
			ProjectFile typeBelongToFile = findNodeBelongToFile(importType.getType());
			addDependTimes(importType.getFile(), typeBelongToFile, DependOnType.IMPORT_OR_INCLUDE, importType);
		}
	}
	private void processFileImportFunctions() throws Exception {
		for(FileImportFunction importFunction : fileImportFunctions) {
			ProjectFile functionBelongToFile = findNodeBelongToFile(importFunction.getFunction());
			addDependTimes(importFunction.getFile(), functionBelongToFile, DependOnType.IMPORT_OR_INCLUDE, importFunction);
		}
	}
	private void processFileImportVariables() throws Exception {
		for(FileImportVariable importVariable : fileImportVariables) {
			ProjectFile varBelongToFile = findNodeBelongToFile(importVariable.getVariable());
			addDependTimes(importVariable.getFile(), varBelongToFile, DependOnType.IMPORT_OR_INCLUDE, importVariable);
		}
	}
}
