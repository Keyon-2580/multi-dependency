package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.lib.FunctionCallLibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.structure.Access;
import cn.edu.fudan.se.multidependency.model.relation.structure.Annotation;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;
import cn.edu.fudan.se.multidependency.model.relation.structure.Cast;
import cn.edu.fudan.se.multidependency.model.relation.structure.Import;
import cn.edu.fudan.se.multidependency.model.relation.structure.Include;
import cn.edu.fudan.se.multidependency.model.relation.structure.Inherits;
import cn.edu.fudan.se.multidependency.model.relation.structure.Parameter;
import cn.edu.fudan.se.multidependency.model.relation.structure.Return;
import cn.edu.fudan.se.multidependency.model.relation.structure.Throw;
import cn.edu.fudan.se.multidependency.model.relation.structure.VariableType;
import cn.edu.fudan.se.multidependency.service.spring.metric.Fan_IO;

public interface StaticAnalyseService {
	
	List<Project> queryAllProjectsByPage(int page, int size, String... sortByProperties);
	
	public Iterable<Project> allProjects();
	
	public Collection<Type> findExtendsType(Type type);
	
	/**
	 * 找出type继承的Type
	 * @param type
	 * @return
	 */
	public Collection<Type> findInheritsType(Type type);
	
	/**
	 * 找出哪些Type继承该type
	 * @param type
	 * @return
	 */
	public Collection<Type> findInheritsFromType(Type type);
	
	public List<Call> findAllFunctionCallFunctionRelations();
	
	public Map<Function, List<Call>> findAllFunctionCallRelationsGroupByCaller();
	public Map<Function, List<Call>> findAllFunctionCallRelationsGroupByCaller(Project project);
	public Map<Function, List<Access>> findAllFunctionAccessRelationsGroupByCaller(Project project);

	public List<Inherits> findProjectContainInheritsRelations(Project project);
	public List<Include> findProjectContainFileIncludeFileRelations(Project project);
	public List<Import> findProjectContainImportRelations(Project project);
	public List<Import> findProjectContainFileImportTypeRelations(Project project);
	public List<Import> findProjectContainFileImportFunctionRelations(Project project);
	public List<Import> findProjectContainFileImportVariableRelations(Project project);
	public List<Call> findFunctionCallFunctionRelations(Project project);
	public List<Call> findProjectContainTypeCallFunctions(Project project);
	public List<Cast> findProjectContainFunctionCastTypeRelations(Project project);
	public List<Return> findProjectContainFunctionReturnTypeRelations(Project project);
	public List<Throw> findProjectContainFunctionThrowTypeRelations(Project project);
	public List<Annotation> findProjectContainNodeAnnotationTypeRelations(Project project);
	public List<VariableType> findProjectContainVariableIsTypeRelations(Project project);
	public List<Parameter> findProjectContainParameterRelations(Project project);
	public List<Parameter> findProjectContainFunctionParameterTypeRelations(Project project);
	public List<Parameter> findProjectContainVariableTypeParameterTypeRelations(Project project);
	public List<Access> findProjectContainFunctionAccessVariableRelations(Project project);

	public boolean isSubType(Type subType, Type superType);
	
	/**
	 * 找出所有函数调用第三方库数据
	 * @return
	 */
	public Map<Function, List<FunctionCallLibraryAPI>> findAllFunctionCallLibraryAPIs();
	
	/**
	 * 找出Project调用了哪些三方
	 * @param project
	 * @return
	 */
	public CallLibrary<Project> findProjectCallLibraries(Project project);
	
	public Iterable<Library> findAllLibraries();

	long countOfAllProjects();
	
	List<Fan_IO<ProjectFile>> queryAllFileFanIOs(Project project);
	
	Fan_IO<ProjectFile> queryJavaFileFanIO(ProjectFile file);
	
	Collection<Call> queryFunctionCallFunctions(Function function);

	Collection<Call> queryFunctionCallByFunctions(Function function);
	
	boolean isDataClass(Type type);
	
	boolean isDataFile(ProjectFile file);
	
}
