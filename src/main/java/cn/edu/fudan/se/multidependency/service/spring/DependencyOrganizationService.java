package cn.edu.fudan.se.multidependency.service.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCastType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionParameterType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionReturnType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionThrowType;
import cn.edu.fudan.se.multidependency.model.relation.structure.TypeCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.TypeInheritsType;
import cn.edu.fudan.se.multidependency.model.relation.structure.VariableIsType;
import cn.edu.fudan.se.multidependency.utils.CytoscapeUtil;

@Service
public class DependencyOrganizationService {

	@Autowired
	private StaticAnalyseService staticAnalyseService;
    
    @Autowired
    ContainRelationService containRelationService;
	
	private Map<Long, Function> functions = new HashMap<>();
	private Map<Long, ProjectFile> files = new HashMap<>();
	private Map<Long, Package> packages = new HashMap<>();
	private Map<Function, Map<Function, Integer>> countOfFunctionCall = new HashMap<>();
	private Map<ProjectFile, Map<ProjectFile, Integer>> countOfFileCall = new HashMap<>();
	private Map<Package, Map<Package, Integer>> countOfPackageCall = new HashMap<>();
	private Map<Function, ProjectFile> functionBelongToFile = new HashMap<>();
	private Map<ProjectFile, Package> fileBelongToPackage = new HashMap<>();
	
	public JSONObject projectStaticAndDynamicToCytoscape(Project project, Iterable<FunctionDynamicCallFunction> dynamicCalls) {
		JSONObject result = new JSONObject();
		JSONObject staticResult = projectStaticStructureToCytoscape(project);
		JSONArray nodes = staticResult.getJSONArray("nodes");
		JSONArray edges = staticResult.getJSONArray("edges");
		
		Map<Function, Map<Function, Boolean>> hasFunctionCallFunction = new HashMap<>();
		for(FunctionDynamicCallFunction call : dynamicCalls) {
			Function start = call.getFunction();
			Function end = call.getCallFunction();
			if(!hasFunctionCallFunction.getOrDefault(start, new HashMap<>()).getOrDefault(end, false)) {
				edges.add(CytoscapeUtil.relationToEdge(start, end, "FunctionDynamicCallFunction", "FunctionDynamicCallFunction", false));
				Map<Function, Boolean> temp = hasFunctionCallFunction.getOrDefault(start, new HashMap<>());
				temp.put(end, true);
				hasFunctionCallFunction.put(start, temp);
			}
		}

		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONObject projectStaticStructureToCytoscape(Project project) {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		
		Iterable<Package> packages = containRelationService.findProjectContainPackages(project);
		
		for(Package pck : packages) {
			nodes.add(CytoscapeUtil.toCytoscapeNode(pck, "Package: " + pck.getName(), "Package"));
			Iterable<ProjectFile> files = containRelationService.findPackageContainFiles(pck);
			for(ProjectFile file : files) {
				JSONObject fileJson = CytoscapeUtil.toCytoscapeNode(file, "File: " + file.getName(), "File");
				fileJson.getJSONObject("data").put("parent", pck.getId());
				nodes.add(fileJson);
				
				Iterable<Type> types = containRelationService.findFileContainTypes(file);
				for(Type type : types) {
					JSONObject typeJson = CytoscapeUtil.toCytoscapeNode(type, "Type: " + type.getName(), "Type");
					typeJson.getJSONObject("data").put("parent", file.getId());
					nodes.add(typeJson);
					
					Iterable<Function> functions = containRelationService.findTypeContainFunctions(type);
					for(Function function : functions) {
						JSONObject functionJson = CytoscapeUtil.toCytoscapeNode(function, "Function: " + function.getName(), "Function");
						functionJson.getJSONObject("data").put("parent", type.getId());
						nodes.add(functionJson);
						Iterable<Variable> variables = containRelationService.findFunctionContainVariables(function);
						for(Variable variable : variables) {
							JSONObject variableJson = CytoscapeUtil.toCytoscapeNode(variable, "Variable: " + variable.getName(), "Variable");
							variableJson.getJSONObject("data").put("parent", function.getId());
							nodes.add(variableJson);
						}

					}
					
					Iterable<Variable> variables = containRelationService.findTypeContainVariables(type);
					for(Variable variable : variables) {
						JSONObject variableJson = CytoscapeUtil.toCytoscapeNode(variable, "Variable: " + variable.getName(), "Variable");
						variableJson.getJSONObject("data").put("parent", type.getId());
						nodes.add(variableJson);
					}
				}
				
				Iterable<Function> functions = containRelationService.findFileContainFunctions(file);
				for(Function function : functions) {
					JSONObject functionJson = CytoscapeUtil.toCytoscapeNode(function, "Function: " + function.getName(), "Function");
					functionJson.getJSONObject("data").put("parent", file.getId());
					nodes.add(functionJson);
				}
			}
		}
		
		List<FunctionCallFunction> functionCallFunctions = staticAnalyseService.findFunctionCallFunctionRelations(project);
		for(FunctionCallFunction call : functionCallFunctions) {
			edges.add(CytoscapeUtil.relationToEdge(call.getFunction(), call.getCallFunction(), "FunctionCallFunction", "call", true));
		}
		
		List<TypeInheritsType> typeInheritsType = staticAnalyseService.findProjectContainInheritsRelations(project);
		for(TypeInheritsType inherit : typeInheritsType) {
			if(inherit.isExtends()) {
				edges.add(CytoscapeUtil.relationToEdge(inherit.getStart(), inherit.getEnd(), "TypeExtendsType", "extends", true));
			}
			if(inherit.isImplements()) {
				edges.add(CytoscapeUtil.relationToEdge(inherit.getStart(), inherit.getEnd(), "TypeImplementsType", "implements", true));
			}
		}
		
		List<TypeCallFunction> typeCallFunctions = staticAnalyseService.findProjectContainTypeCallFunctions(project);
		for(TypeCallFunction call : typeCallFunctions) {
			edges.add(CytoscapeUtil.relationToEdge(call.getType(), call.getCallFunction(), "TypeCallFunction", "call", true));
		}
		
		List<FunctionCastType> functionCastTypes = staticAnalyseService.findProjectContainFunctionCastTypeRelations(project);
		for(FunctionCastType relation : functionCastTypes) {
			edges.add(CytoscapeUtil.relationToEdge(relation.getFunction(), relation.getCastType(), "FunctionCastType", "cast", true));
		}
		List<FunctionParameterType> functionParameterTypes = staticAnalyseService.findProjectContainFunctionParameterTypeRelations(project);
		for(FunctionParameterType relation : functionParameterTypes) {
			edges.add(CytoscapeUtil.relationToEdge(relation.getFunction(), relation.getParameterType(), "FunctionParameterType", "parameter", true));
		}
		List<FunctionReturnType> functionReturnTypes = staticAnalyseService.findProjectContainFunctionReturnTypeRelations(project);
		for(FunctionReturnType relation : functionReturnTypes) {
			edges.add(CytoscapeUtil.relationToEdge(relation.getFunction(), relation.getReturnType(), "FunctionReturnType", "return", true));
		}
		List<FunctionThrowType> functionThrowTypes = staticAnalyseService.findProjectContainFunctionThrowTypeRelations(project);
		for(FunctionThrowType relation : functionThrowTypes) {
			edges.add(CytoscapeUtil.relationToEdge(relation.getFunction(), relation.getType(), "FunctionThrowType", "throw", true));
		}
		List<FileImportType> fileImportTypes = staticAnalyseService.findProjectContainFileImportTypeRelations(project);
		for(FileImportType relation : fileImportTypes) {
			edges.add(CytoscapeUtil.relationToEdge(relation.getFile(), relation.getType(), "FileImportType", "import", true));
		}
		List<FileImportFunction> fileImportFunctions = staticAnalyseService.findProjectContainFileImportFunctionRelations(project);
		for(FileImportFunction relation : fileImportFunctions) {
			edges.add(CytoscapeUtil.relationToEdge(relation.getFile(), relation.getFunction(), "FileImportFunction", "import", true));
		}
		List<VariableIsType> variableIsType = staticAnalyseService.findProjectContainVariableIsTypeRelations(project);
		for(VariableIsType relation : variableIsType) {
			edges.add(CytoscapeUtil.relationToEdge(relation.getVariable(), relation.getType(), "VariableIsType", "VariableIsType", false));
		}
		/*List<VariableTypeParameterType> variableTypeParameterTypes = staticAnalyseService.findProjectContainVariableTypeParameterTypeRelations(project);
		for(VariableTypeParameterType relation : variableTypeParameterTypes) {
			edges.add(ProjectUtil.relationToEdge(relation.getVariable(), relation.getType(), "VariableParameterType", "use", false));
		}*/
		
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONObject packageAndFileToCytoscape() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		
		for(Package pck : packages.values()) {
			JSONObject packageJson = new JSONObject();
			JSONObject packageDataValue = new JSONObject();
			packageDataValue.put("id", pck.getId());
			packageDataValue.put("name", pck.getName());
			packageDataValue.put("type", "package");
			packageJson.put("data", packageDataValue);
			nodes.add(packageJson);
		}
		
		for(ProjectFile file : files.values()) {
			JSONObject fileJson = new JSONObject();
			JSONObject fileDataValue = new JSONObject();
			fileDataValue.put("id", file.getId());
			fileDataValue.put("name", file.getName());
			fileDataValue.put("type", "file");
			fileJson.put("data", fileDataValue);
			nodes.add(fileJson);
			
			Package pck = fileBelongToPackage.get(file);
			JSONObject edge = new JSONObject();
			JSONObject value = new JSONObject();
			value.put("id", pck.getId() + "_" + file.getId());
			value.put("value", "contain");
			value.put("source", pck.getId());
			value.put("target", file.getId());
			value.put("type", "contain");
			edge.put("data", value);
			edges.add(edge);	
		}
		
		for(Package callerPackage : countOfPackageCall.keySet()) {
			for(Package calledPackage : countOfPackageCall.get(callerPackage).keySet()) {
				Integer count = countOfPackageCall.get(callerPackage).get(calledPackage);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", callerPackage.getId() + "_" + calledPackage.getId());
				value.put("value", count);
				value.put("source", callerPackage.getId());
				value.put("target", calledPackage.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		
		for(ProjectFile callerFile : countOfFileCall.keySet()) {
			for(ProjectFile calledFile : countOfFileCall.get(callerFile).keySet()) {
				Integer count = countOfFileCall.get(callerFile).get(calledFile);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", callerFile.getId() + "_" + calledFile.getId());
				value.put("value", count);
				value.put("source", callerFile.getId());
				value.put("target", calledFile.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONObject fileCallToCytoscape() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		
		for(ProjectFile file : files.values()) {
			JSONObject fileJson = new JSONObject();
			JSONObject fileDataValue = new JSONObject();
			fileDataValue.put("id", file.getId());
			fileDataValue.put("name", file.getName());
			fileJson.put("data", fileDataValue);
			nodes.add(fileJson);
		}
		
		for(ProjectFile callerFile : countOfFileCall.keySet()) {
			for(ProjectFile calledFile : countOfFileCall.get(callerFile).keySet()) {
				Integer count = countOfFileCall.get(callerFile).get(calledFile);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", callerFile.getId() + "_" + calledFile.getId());
				value.put("value", count);
				value.put("source", callerFile.getId());
				value.put("target", calledFile.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONObject functionCallToCytoscape() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		
		for(Function function : functions.values()) {
			JSONObject functionJson = new JSONObject();
			JSONObject functionDataValue = new JSONObject();
			functionDataValue.put("id", function.getId());
			functionDataValue.put("name", function.getName());
			functionJson.put("data", functionDataValue);
			nodes.add(functionJson);
		}
		
		for(Function callerFunction : countOfFunctionCall.keySet()) {
			for(Function calledFunction : countOfFunctionCall.get(callerFunction).keySet()) {
				Integer count = countOfFunctionCall.get(callerFunction).get(calledFunction);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", callerFunction.getId() + "_" + calledFunction.getId());
				value.put("value", count);
				value.put("source", callerFunction.getId());
				value.put("target", calledFunction.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONObject directoryCallToCytoscape() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		
		for(Package pck : packages.values()) {
			JSONObject packageJson = new JSONObject();
			JSONObject packageDataValue = new JSONObject();
			packageDataValue.put("id", pck.getId());
			packageDataValue.put("name", pck.getName());
			packageJson.put("data", packageDataValue);
			nodes.add(packageJson);
		}
		
		for(Package callerPackage : countOfPackageCall.keySet()) {
			for(Package calledPackage : countOfPackageCall.get(callerPackage).keySet()) {
				Integer count = countOfPackageCall.get(callerPackage).get(calledPackage);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", callerPackage.getId() + "_" + calledPackage.getId());
				value.put("value", count);
				value.put("source", callerPackage.getId());
				value.put("target", calledPackage.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public void dynamicCallDependency(List<FunctionDynamicCallFunction> dynamicCalls) {
		
		countOfFunctionCall = new HashMap<>();
		countOfFileCall = new HashMap<>();
		countOfPackageCall = new HashMap<>();
		functions = new HashMap<>();
		files = new HashMap<>();
		packages = new HashMap<>();
		functionBelongToFile = new HashMap<>();
		fileBelongToPackage = new HashMap<>();

		for(FunctionDynamicCallFunction dynamicCall : dynamicCalls) {
			Function callerFunction = dynamicCall.getFunction();
			Function calledFunction = dynamicCall.getCallFunction();
			if(callerFunction.getId().equals(calledFunction.getId())) {
				continue;
			}
			functions.put(callerFunction.getId(), callerFunction);
			functions.put(calledFunction.getId(), calledFunction);
			Map<Function, Integer> tempFunction = countOfFunctionCall.getOrDefault(callerFunction, new HashMap<>());
			Integer size = tempFunction.getOrDefault(calledFunction, 0);
			size++;
			tempFunction.put(calledFunction, size);
			countOfFunctionCall.put(callerFunction, tempFunction);
			
			ProjectFile callerFile = containRelationService.findFunctionBelongToFile(callerFunction);
			ProjectFile calledFile = containRelationService.findFunctionBelongToFile(calledFunction);
			if(callerFile.getId().equals(calledFile.getId())) {
				continue;
			}
			functionBelongToFile.put(callerFunction, callerFile);
			functionBelongToFile.put(calledFunction, calledFile);
			files.put(callerFile.getId(), callerFile);
			files.put(calledFile.getId(), calledFile);
			Map<ProjectFile, Integer> tempFile = countOfFileCall.getOrDefault(callerFile, new HashMap<>());
			size = tempFile.getOrDefault(calledFile, 0);
			size++;
			tempFile.put(calledFile, size);
			countOfFileCall.put(callerFile, tempFile);
			
			Package callerPackage = containRelationService.findFileBelongToPackage(callerFile);
			Package calledPackage = containRelationService.findFileBelongToPackage(calledFile);
			if(callerPackage.getId().equals(calledPackage.getId())) {
				continue;
			}
			fileBelongToPackage.put(callerFile, callerPackage);
			fileBelongToPackage.put(calledFile, calledPackage);
			packages.put(callerPackage.getId(), callerPackage);
			packages.put(calledPackage.getId(), calledPackage);
			Map<Package, Integer> tempPackage = countOfPackageCall.getOrDefault(callerPackage, new HashMap<>());
			size = tempPackage.getOrDefault(calledPackage, 0);
			size++;
			tempPackage.put(calledPackage, size);
			countOfPackageCall.put(callerPackage, tempPackage);
			
		}
		
	}	
}
