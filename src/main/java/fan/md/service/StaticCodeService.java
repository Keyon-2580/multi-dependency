package fan.md.service;

import java.util.List;

import fan.md.model.entity.code.CodeFile;
import fan.md.model.entity.code.Type;

public interface StaticCodeService {
	
	List<Type> findAllTypes();
	
	List<Type> findTypesInFile(CodeFile codeFile);
	
	
}
