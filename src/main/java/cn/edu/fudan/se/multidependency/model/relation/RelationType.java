package cn.edu.fudan.se.multidependency.model.relation;

public enum RelationType {
	PACKAGE_CONTAINS_FILE, 
	
	FILE_CONTAINS_TYPE, 
	FILE_CONTAINS_FUNCTION, 
	FILE_CONTAINS_VARIABLE, 
	
	TYPE_CONTAINS_FUNCTION,
	TYPE_CONTAINS_TYPE, 
	TYPE_CONTAINS_VARIABLE, 
	TYPE_EXTENDS_TYPE, 
	TYPE_IMPLEMENTS_TYPE, 
	
	FUNCTION_CONTAINS_TYPE, 
	FUNCTION_CONTAINS_VARIABLE, 
	FUNCTION_RETURN_TYPE,
	FUNCTION_PARAMETER_TYPE, 
	
	VARIABLE_IS_TYPE, 
	
	DEPENDENCY_FILE_IMPORT_TYPE, 
	DEPENDENCY_FILE_IMPORT_FUNCTION, 
	DEPENDENCY_FILE_IMPORT_VARIABLE, 
	DEPENDENCY_FILE_INCLUDE_FILE, 
	DEPENDENCY_FUNCTION_CALL_FUNCTION, 
	
	DYNAMIC_FUNCTION_CALL_FUNCTION;
}
