package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
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
	
	public ProjectFile findFunctionBelongToFile(Function function);
	
	public ProjectFile findTypeBelongToFile(Type type);
	
	public ProjectFile findVariableBelongToFile(Variable variable);
	
	
	public Iterable<TypeExtendsType> findAllExtends();
	public Iterable<TypeExtendsType> findAllExtendsRelations();
	public Iterable<TypeImplementsType> findAllImplementsRelations();
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

	public List<TypeExtendsType> findProjectContainExtendsRelations(Project project);
	public List<TypeImplementsType> findProjectContainImplementsRelations(Project project);
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
}
