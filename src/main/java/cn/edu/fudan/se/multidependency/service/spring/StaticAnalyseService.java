package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.lib.FunctionCallLibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileImportVariable;
import cn.edu.fudan.se.multidependency.model.relation.structure.FileIncludeFile;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCastType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionParameterType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionReturnType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionThrowType;
import cn.edu.fudan.se.multidependency.model.relation.structure.NodeAnnotationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.TypeCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.TypeInheritsType;
import cn.edu.fudan.se.multidependency.model.relation.structure.VariableIsType;
import cn.edu.fudan.se.multidependency.model.relation.structure.VariableTypeParameterType;

public interface StaticAnalyseService {
	
	public Iterable<Package> allPackagesInProject(Project project);
	
	public Iterable<ProjectFile> allFilesInPackage(Package pck);
	
	public Iterable<Type> allTypesInFile(ProjectFile codeFile);
	
	public Iterable<Variable> allVariablesInType(Type type);
	
	public Iterable<Variable> allVariablesInFunction(Function function);
	
	public Iterable<Function> allFunctionsInFile(ProjectFile codeFile);
	
	public Iterable<Function> allFunctionsInType(Type type);
	
	/**
	 * all projects
	 * @return
	 */
	public Map<Long, Project> allProjects();
	
	/**
	 * find project using id
	 * @param id
	 * @return
	 */
	public Project findProject(Long id);
	
	/**
	 * all types
	 * @return
	 */
	public Map<Long, Type> findTypes();
	
	
	/**
	 * 找出Type继承哪些Type
	 * @param type
	 * @return
	 */
	public List<Type> findExtendsType(Type type);
	
	public Map<Long, ProjectFile> allFiles();
	
	public List<Function> allFunctions();
	
	/**
	 * Type属于哪个Package
	 * @param type
	 * @return
	 */
	public Package findTypeBelongToPackage(Type type);
	
	/**
	 * 文件属于哪个Package
	 * @param file
	 * @return
	 */
	public Package findFileBelongToPackage(ProjectFile file);
	
	/**
	 * Function属于哪个Type
	 * @param function
	 * @return
	 */
	public Type findFunctionBelongToType(Function function);
	
	/**
	 * Function属于哪个File
	 * @param function
	 * @return
	 */
	public ProjectFile findFunctionBelongToFile(Function function);
	
	/**
	 * Type属于哪个文件
	 * @param type
	 * @return
	 */
	public ProjectFile findTypeBelongToFile(Type type);
	
	/**
	 * Variable属于哪个文件
	 * @param variable
	 * @return
	 */
	public ProjectFile findVariableBelongToFile(Variable variable);
	
	/**
	 * Function属于哪个Project
	 * @param function
	 * @return
	 */
	public Project findFunctionBelongToProject(Function function);
	
	/**
	 * Project包含的Function
	 * @param project
	 * @return
	 */
	public List<Function> findProjectContainFunctions(Project project);
	
	public Map<Function, List<FunctionCallFunction>> findAllFunctionCallRelationsGroupByCaller();
	
	public Iterable<TypeInheritsType> findAllInheritsRelations();
	public Iterable<FileIncludeFile> findAllFileIncludeFileRelations();
	public Iterable<FileImportType> findAllFileImportTypeRelations();
	public Iterable<FileImportFunction> findAllFileImportFunctionRelations();
	public Iterable<FileImportVariable> findAllFileImportVariableRelations();
	public Iterable<FunctionCallFunction> findAllFunctionCallFunctionRelations();
	public Iterable<TypeCallFunction> findAllTypeCallFunctions();
	public Iterable<FunctionCastType> findAllFunctionCastTypeRelations();
	public Iterable<FunctionParameterType> findAllFunctionParameterTypeRelations();
	public Iterable<FunctionReturnType> findAllFunctionReturnTypeRelations();
	public Iterable<FunctionThrowType> findAllFunctionThrowTypeRelations();
	public Iterable<NodeAnnotationType> findAllNodeAnnotationTypeRelations();
	public Iterable<VariableIsType> findAllVariableIsTypeRelations();
	public Iterable<VariableTypeParameterType> findAllVariableTypeParameterTypeRelations();

	public List<TypeInheritsType> findProjectContainInheritsRelations(Project project);
	public List<FileIncludeFile> findProjectContainFileIncludeFileRelations(Project project);
	public List<FileImportType> findProjectContainFileImportTypeRelations(Project project);
	public List<FileImportFunction> findProjectContainFileImportFunctionRelations(Project project);
	public List<FileImportVariable> findProjectContainFileImportVariableRelations(Project project);
	public List<FunctionCallFunction> findFunctionCallFunctionRelations(Project project);
	public List<TypeCallFunction> findProjectContainTypeCallFunctions(Project project);
	public List<FunctionCastType> findProjectContainFunctionCastTypeRelations(Project project);
	public List<FunctionParameterType> findProjectContainFunctionParameterTypeRelations(Project project);
	public List<FunctionReturnType> findProjectContainFunctionReturnTypeRelations(Project project);
	public List<FunctionThrowType> findProjectContainFunctionThrowTypeRelations(Project project);
	public List<NodeAnnotationType> findProjectContainNodeAnnotationTypeRelations(Project project);
	public List<VariableIsType> findProjectContainVariableIsTypeRelations(Project project);
	public List<VariableTypeParameterType> findProjectContainVariableTypeParameterTypeRelations(Project project);

	public boolean isSubType(Type subType, Type superType);
	
	/**
	 * 找出所有函数调用第三方库数据
	 * @return
	 */
	public Iterable<FunctionCallLibraryAPI> findAllFunctionCallLibraryAPIs();
}
