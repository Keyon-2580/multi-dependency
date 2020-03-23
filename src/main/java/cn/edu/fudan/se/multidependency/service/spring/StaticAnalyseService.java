package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
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
	public List<Type> findTypes();
	
	/**
	 * find Types in File
	 * @param codeFile
	 * @return
	 */
	public List<Type> findTypes(ProjectFile codeFile);
	
	public List<Type> findExtendsType(Type type);
	
	public List<ProjectFile> allFiles();
	
	public List<Function> allFunctions();
	
	public Package findTypeBelongToPackage(Type type);
	
	public Package findFileBelongToPackage(ProjectFile file);
	
	public Type findFunctionBelongToType(Function function);
	
	public ProjectFile findFunctionBelongToFile(Function function);
	
	public ProjectFile findTypeBelongToFile(Type type);
	
	public ProjectFile findVariableBelongToFile(Variable variable);
	
	public Project findFunctionBelongToProject(Function function);
	
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
}
