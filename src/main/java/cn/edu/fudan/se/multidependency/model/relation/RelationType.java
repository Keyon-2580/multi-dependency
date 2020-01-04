package cn.edu.fudan.se.multidependency.model.relation;

import org.neo4j.graphdb.RelationshipType;

public enum RelationType implements RelationshipType {
	CONTAIN(RelationType.str_CONTAIN),
	TYPE_IMPLEMENTS_TYPE(RelationType.str_TYPE_IMPLEMENTS_TYPE),
	TYPE_EXTENDS_TYPE(RelationType.str_TYPE_EXTENDS_TYPE),
	TYPE_CALL_FUNCTION(RelationType.str_TYPE_CALL_FUNCTION),
	FILE_IMPORT_FUNCTION(RelationType.str_FILE_IMPORT_FUNCTION),
	FILE_IMPORT_TYPE(RelationType.str_FILE_IMPORT_TYPE),
	FILE_IMPORT_VARIABLE(RelationType.str_FILE_IMPORT_VARIABLE),
	FILE_INCLUDE_FILE(RelationType.str_FILE_INCLUDE_FILE),
	FUNCTION_CALL_FUNCTION(RelationType.str_FUNCTION_CALL_FUNCTION),
	FUNCTION_PARAMETER_TYPE(RelationType.str_FUNCTION_PARAMETER_TYPE),
	FUNCTION_RETURN_TYPE(RelationType.str_FUNCTION_RETURN_TYPE),
	FUNCTION_THROW_TYPE(RelationType.str_FUNCTION_THORW_TYPE),
	FUNCTION_CAST_TYPE(RelationType.str_FUNCTION_CAST_TYPE),
	VARIABLE_IS_TYPE(RelationType.str_VARIABLE_IS_TYPE),
	VARIABLE_TYPE_PARAMETER_TYPE(RelationType.str_VARIABLE_TYPE_PARAMETER_TYPE),
	DYNAMIC_FUNCTION_CALL_FUNCTION(RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION),
	NODE_ANNOTATION_TYPE(RelationType.str_NODE_ANNOTATION_TYPE),
	
	TESTCASE_EXECUTE_FEATURE(RelationType.str_TESTCASE_EXECUTE_FEATURE),
	SCENARIO_DEFINE_TESTCASE(RelationType.str_SCENARIO_DEFINE_TESTCASE),
	NODE_IS_SCENARIO(RelationType.str_NODE_IS_SCENARIO),
	NODE_IS_FEATURE(RelationType.str_NODE_IS_FEATURE),
	NODE_IS_TESTCASE(RelationType.str_NODE_IS_TESTCASE);

	/**
	 * 结构关系
	 */
	public static final String str_CONTAIN = "CONTAIN";

	/**
	 * 依赖关系
	 */
	public static final String str_TYPE_EXTENDS_TYPE = "TYPE_EXTENDS_TYPE";
	public static final String str_TYPE_IMPLEMENTS_TYPE = "TYPE_IMPLEMENTS_TYPE";
	public static final String str_TYPE_CALL_FUNCTION = "TYPE_CALL_FUNCTION";
	public static final String str_FILE_IMPORT_FUNCTION = "FILE_IMPORT_FUNCTION";
	public static final String str_FILE_IMPORT_TYPE = "FILE_IMPORT_TYPE";
	public static final String str_FILE_IMPORT_VARIABLE = "FILE_IMPORT_VARIABLE";
	public static final String str_FILE_INCLUDE_FILE = "FILE_INCLUDE_FILE";
	public static final String str_FUNCTION_CALL_FUNCTION = "FUNCTION_CALL_FUNCTION";
	public static final String str_FUNCTION_PARAMETER_TYPE = "FUNCTION_PARAMETER_TYPE";
	public static final String str_FUNCTION_CAST_TYPE = "FUNCTION_CAST_TYPE";
	public static final String str_FUNCTION_RETURN_TYPE = "FUNCTION_RETURN_TYPE";
	public static final String str_VARIABLE_IS_TYPE = "VARIABLE_IS_TYPE";
	public static final String str_DYNAMIC_FUNCTION_CALL_FUNCTION = "DYNAMIC_FUNCTION_CALL_FUNCTION";
	public static final String str_FUNCTION_THORW_TYPE = "FUNCTION_THORW_TYPE";
	public static final String str_NODE_ANNOTATION_TYPE = "NODE_ANNOTATION_TYPE";
	public static final String str_VARIABLE_TYPE_PARAMETER_TYPE = "VARIABLE_TYPE_PARAMETER_TYPE";
	
	public static final String str_TESTCASE_EXECUTE_FEATURE = "TESTCASE_EXECUTE_FEATURE";
	public static final String str_SCENARIO_DEFINE_TESTCASE = "SCENARIO_DEFINE_TESTCASE";
	
	public static final String str_NODE_IS_SCENARIO = "NODE_IS_SCENARIO";
	public static final String str_NODE_IS_FEATURE = "NODE_IS_FEATURE";
	public static final String str_NODE_IS_TESTCASE = "NODE_IS_TESTCASE";

	private String name;

	RelationType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return getName();
	}
}
