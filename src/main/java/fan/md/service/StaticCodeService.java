package fan.md.service;

import java.util.List;

import fan.md.model.node.code.CodeFile;
import fan.md.model.node.code.Function;
import fan.md.model.node.code.Type;
import fan.md.model.relation.code.TypeExtendsType;

public interface StaticCodeService {
	
	List<Type> findAllTypes();
	
	List<Type> findTypesInFile(CodeFile codeFile);
	
	List<Type> findExtendsType(Type type);
	
	List<TypeExtendsType> findAllExtends();
	
	List<Function> findAllFunctions();
}
