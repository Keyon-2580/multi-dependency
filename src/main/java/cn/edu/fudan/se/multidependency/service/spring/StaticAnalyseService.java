package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
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
	
	List<Project> queryAllProjectsByPage(int page, int size, String... sortByProperties);
	
	public Iterable<Project> allProjects();
	
	public Project queryProject(Long id);
	
	public Map<Long, Type> findTypes();
	
	
	public List<Type> findExtendsType(Type type);
	
	public Map<Long, ProjectFile> allFiles();
	
	public List<Function> allFunctions();
	
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
	public Map<Function, List<FunctionCallLibraryAPI>> findAllFunctionCallLibraryAPIs();
	
	public Iterable<FunctionCloneFunction> findAllFunctionCloneFunctions();
	
	public Iterable<FunctionCloneFunction> findProjectContainFunctionCloneFunctions(Project project);
	
	/**
	 * 根据函数间的克隆找出项目间的克隆
	 * @param functionClones
	 * @return
	 */
	public Iterable<Clone<Project>> findProjectClone(Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode);
	
	/**
	 * 找出Project调用了哪些三方
	 * @param project
	 * @return
	 */
	public CallLibrary<Project> findProjectCallLibraries(Project project);
	
	public Iterable<Library> findAllLibraries();

	long countOfAllProjects();
	
}
