package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeExtendsType;

public interface StaticAnalyseService {
	
	List<Type> findAllTypes();
	
	List<Type> findTypesInFile(ProjectFile codeFile);
	
	List<Type> findExtendsType(Type type);
	
	List<TypeExtendsType> findAllExtends();
	
	List<Function> findAllFunctions();
	
	Package findTypeInPackage(Type type);
	
	ProjectFile findFunctionBelongToCodeFile(Function function);
	
}
