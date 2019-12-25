package cn.edu.fudan.se.multidependency.service;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.code.CodeFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeExtendsType;
import cn.edu.fudan.se.multidependency.model.node.code.Type;

public interface StaticCodeService {
	
	List<Type> findAllTypes();
	
	List<Type> findTypesInFile(CodeFile codeFile);
	
	List<Type> findExtendsType(Type type);
	
	List<TypeExtendsType> findAllExtends();
	
	List<Function> findAllFunctions();
}
