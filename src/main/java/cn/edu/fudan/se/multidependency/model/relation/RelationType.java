package cn.edu.fudan.se.multidependency.model.relation;

import org.neo4j.graphdb.RelationshipType;

public enum RelationType implements RelationshipType {
	CONTAIN(RelationType.str_CONTAIN),
	TYPE_INHERITS_TYPE(RelationType.str_TYPE_INHERITS_TYPE),
	TYPE_CALL_FUNCTION(RelationType.str_TYPE_CALL_FUNCTION),
	FILE_IMPORT_FUNCTION(RelationType.str_FILE_IMPORT_FUNCTION),
	FILE_IMPORT_TYPE(RelationType.str_FILE_IMPORT_TYPE),
	FILE_IMPORT_VARIABLE(RelationType.str_FILE_IMPORT_VARIABLE),
	FILE_INCLUDE_FILE(RelationType.str_FILE_INCLUDE_FILE),
	FUNCTION_ACCESS_FIELD(RelationType.str_FUNCTION_ACCESS_FIELD),
	FUNCTION_CALL_FUNCTION(RelationType.str_FUNCTION_CALL_FUNCTION),
	FUNCTION_IMPLEMENT_FUNCTION(RelationType.str_FUNCTION_IMPLEMENT_FUNCTION),
	FUNCTION_IMPLLINK_FUNCTION(RelationType.str_FUNCTION_IMPLLINK_FUNCTION),
	FUNCTION_PARAMETER_TYPE(RelationType.str_FUNCTION_PARAMETER_TYPE),
	FUNCTION_RETURN_TYPE(RelationType.str_FUNCTION_RETURN_TYPE),
	FUNCTION_THROW_TYPE(RelationType.str_FUNCTION_THORW_TYPE),
	FUNCTION_CAST_TYPE(RelationType.str_FUNCTION_CAST_TYPE),
	VARIABLE_IS_TYPE(RelationType.str_VARIABLE_IS_TYPE),
	VARIABLE_TYPE_PARAMETER_TYPE(RelationType.str_VARIABLE_TYPE_PARAMETER_TYPE),
	DYNAMIC_FUNCTION_CALL_FUNCTION(RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION),
	NODE_ANNOTATION_TYPE(RelationType.str_NODE_ANNOTATION_TYPE),
	TESTCASE_EXECUTE_FEATURE(RelationType.str_TESTCASE_EXECUTE_FEATURE),
	TESTCASE_RUN_TRACE(RelationType.str_TESTCASE_RUN_TRACE),
	SCENARIO_DEFINE_TESTCASE(RelationType.str_SCENARIO_DEFINE_TESTCASE),
	NODE_IS_SCENARIO(RelationType.str_NODE_IS_SCENARIO),
	NODE_IS_FEATURE(RelationType.str_NODE_IS_FEATURE),
	NODE_IS_TESTCASE(RelationType.str_NODE_IS_TESTCASE),

	FILE_BUILD_DEPENDS_FILE(RelationType.str_FILE_BUILD_DEPENDS_FILE),
	
	MICRO_SERVICE_CREATE_SPAN(RelationType.str_MICRO_SERVICE_CREATE_SPAN),
	SPAN_CALL_SPAN(RelationType.str_SPAN_CALL_SPAN),
	MICROSERVICE_CALL_MICROSERVICE(RelationType.str_MICROSERVICE_CALL_MICROSERVICE),
	
	SPAN_START_WITH_FUNCTION(RelationType.str_SPAN_START_WITH_FUNCTION),
	
	TRACE_RUN_WITH_FUNCTION(RelationType.str_TRACE_RUN_WITH_FUNCTION),
	
	FILE_DEPEND_ON_FILE(RelationType.str_FILE_DEPEND_ON_FILE),
	SPAN_INSTANCE_OF_RESTFUL_API(RelationType.str_SPAN_INSTANCE_OF_RESTFUL_API),
	
	MICROSERVICE_DEPEND_ON_MICROSERVICE(RelationType.str_MICROSERVICE_DEPEND_ON_MICROSERVICE),

	COMMIT_UPDATE_FILE(RelationType.str_COMMIT_UPDATE_FILE),
	COMMIT_ADDRESS_ISSUE(RelationType.str_COMMIT_ADDRESS_ISSUE),
	DEVELOPER_REPORT_ISSUE(RelationType.str_DEVELOPER_REPORT_ISSUE),
	DEVELOPER_SUBMIT_COMMIT(RelationType.str_DEVELOPER_SUBMIT_COMMIT),
	COMMIT_INHERIT_COMMIT(RelationType.str_COMMIT_INHERIT_COMMIT),
	
	FUNCTION_CALL_LIBRARY_API(RelationType.str_FUNCTION_CALL_LIBRARY_API),
	
	FUNCTION_CLONE_FUNCTION(RelationType.str_FUNCTION_CLONE_FUNCTION),
	FILE_CLONE_FILE(RelationType.str_FILE_CLONE_FILE);

	/**
	 * 结构关系
	 */
	public static final String str_CONTAIN = "CONTAIN";

	/**
	 * 依赖关系
	 */
	public static final String str_TYPE_INHERITS_TYPE = "TYPE_INHERITS_TYPE";
	public static final String str_TYPE_CALL_FUNCTION = "TYPE_CALL_FUNCTION";
	public static final String str_FILE_IMPORT_FUNCTION = "FILE_IMPORT_FUNCTION";
	public static final String str_FILE_IMPORT_TYPE = "FILE_IMPORT_TYPE";
	public static final String str_FILE_IMPORT_VARIABLE = "FILE_IMPORT_VARIABLE";
	public static final String str_FILE_INCLUDE_FILE = "FILE_INCLUDE_FILE";
	public static final String str_FUNCTION_ACCESS_FIELD = "FUNCTION_ACCESS_FIELD";
	public static final String str_FUNCTION_CALL_FUNCTION = "FUNCTION_CALL_FUNCTION";
	public static final String str_FUNCTION_IMPLEMENT_FUNCTION = "FUNCTION_IMPLEMENT_FUNCTION";
	public static final String str_FUNCTION_IMPLLINK_FUNCTION = "FUNCTION_IMPLLINK_FUNCTION";
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

	public static final String str_FILE_BUILD_DEPENDS_FILE = "FILE_BUILD_DEPENDS_FILE";
	
	public static final String str_SPAN_CALL_SPAN = "SPAN_CALL_SPAN";
	public static final String str_SPAN_INSTANCE_OF_RESTFUL_API = "SPAN_INSTANCE_OF_RESTFUL_API";
	public static final String str_MICRO_SERVICE_CREATE_SPAN = "MICRO_SERVICE_CREATE_SPAN";
	public static final String str_SPAN_START_WITH_FUNCTION = "SPAN_START_WITH_FUNCTION";
	
	public static final String str_TESTCASE_RUN_TRACE = "TESTCASE_RUN_TRACE";
	
	public static final String str_TRACE_RUN_WITH_FUNCTION = "TRACE_RUN_WITH_FUNCTION";
	
	public static final String str_MICROSERVICE_CALL_MICROSERVICE = "MICROSERVICE_CALL_MICROSERVICE";
	public static final String str_MICROSERVICE_DEPEND_ON_MICROSERVICE = "MICROSERVICE_DEPEND_ON_MICROSERVICE";
	public static final String str_FILE_DEPEND_ON_FILE = "FILE_DEPEND_ON_FILE";

	public static final String str_COMMIT_UPDATE_FILE = "COMMIT_UPDATE_FILE";
	public static final String str_COMMIT_ADDRESS_ISSUE = "COMMIT_ADDRESS_ISSUE";
	public static final String str_DEVELOPER_REPORT_ISSUE = "DEVELOPER_REPORT_ISSUE";
	public static final String str_DEVELOPER_SUBMIT_COMMIT = "DEVELOPER_SUBMIT_COMMIT";
	public static final String str_COMMIT_INHERIT_COMMIT = "COMMIT_INHERIT_COMMIT";
	public static final String str_FUNCTION_CALL_LIBRARY_API = "FUNCTION_CALL_LIBRARY_API";
	
	public static final String str_FUNCTION_CLONE_FUNCTION = "FUNCTION_CLONE_FUNCTION";
	public static final String str_FILE_CLONE_FILE = "FILE_CLONE_FILE";
	
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
