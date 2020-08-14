package cn.edu.fudan.se.multidependency.model.relation;

import org.neo4j.graphdb.RelationshipType;

public enum RelationType implements RelationshipType {
	CONTAIN(RelationType.str_CONTAIN),
	HAS(RelationType.str_HAS),
	EXTENDS(RelationType.str_EXTENDS),
	IMPORT(RelationType.str_IMPORT),
	INCLUDE(RelationType.str_INCLUDE),
	ACCESS(RelationType.str_ACCESS),
	CALL(RelationType.str_CALL),
	CREATE(RelationType.str_CREATE),
	IMPLEMENTS(RelationType.str_IMPLEMENTS),
	IMPLLINK(RelationType.str_IMPLLINK),
	PARAMETER(RelationType.str_PARAMETER),
	RETURN(RelationType.str_RETURN),
	THROW(RelationType.str_THROW),
	CAST(RelationType.str_CAST),
	VARIABLE_TYPE(RelationType.str_VARIABLE_TYPE),
	ANNOTATION(RelationType.str_ANNOTATION),
	
	DYNAMIC_CALL(RelationType.str_DYNAMIC_CALL),
	
	TESTCASE_EXECUTE_FEATURE(RelationType.str_TESTCASE_EXECUTE_FEATURE),
	TESTCASE_RUN_TRACE(RelationType.str_TESTCASE_RUN_TRACE),
	SCENARIO_DEFINE_TESTCASE(RelationType.str_SCENARIO_DEFINE_TESTCASE),
	
	MICRO_SERVICE_CREATE_SPAN(RelationType.str_MICRO_SERVICE_CREATE_SPAN),
	SPAN_CALL_SPAN(RelationType.str_SPAN_CALL_SPAN),
	MICROSERVICE_CALL_MICROSERVICE(RelationType.str_MICROSERVICE_CALL_MICROSERVICE),
	
	SPAN_START_WITH_FUNCTION(RelationType.str_SPAN_START_WITH_FUNCTION),
	
	TRACE_RUN_WITH_FUNCTION(RelationType.str_TRACE_RUN_WITH_FUNCTION),

	FILE_BUILD_DEPENDS_FILE(RelationType.str_FILE_BUILD_DEPENDS_FILE),
	
	SPAN_INSTANCE_OF_RESTFUL_API(RelationType.str_SPAN_INSTANCE_OF_RESTFUL_API),
	
	MICROSERVICE_DEPEND_ON_MICROSERVICE(RelationType.str_MICROSERVICE_DEPEND_ON_MICROSERVICE),

	COMMIT_UPDATE_FILE(RelationType.str_COMMIT_UPDATE_FILE),
	COMMIT_ADDRESS_ISSUE(RelationType.str_COMMIT_ADDRESS_ISSUE),
	DEVELOPER_REPORT_ISSUE(RelationType.str_DEVELOPER_REPORT_ISSUE),
	DEVELOPER_SUBMIT_COMMIT(RelationType.str_DEVELOPER_SUBMIT_COMMIT),
	COMMIT_INHERIT_COMMIT(RelationType.str_COMMIT_INHERIT_COMMIT),
	CO_CHANGE(RelationType.str_CO_CHANGE),
	
	FUNCTION_CALL_LIBRARY_API(RelationType.str_FUNCTION_CALL_LIBRARY_API),
	
	CLONE(RelationType.str_CLONE),
	
	DEPENDS_ON(RelationType.str_DEPENDS_ON);

	/**
	 * 结构关系
	 */
	public static final String str_CONTAIN = "CONTAIN";
	public static final String str_HAS = "HAS";

	/**
	 * 依赖关系
	 */
	public static final String str_EXTENDS = "EXTENDS";
	public static final String str_IMPORT = "IMPORT";
	public static final String str_INCLUDE = "INCLUDE";
	public static final String str_ACCESS = "ACCESS";
	public static final String str_CALL = "CALL";
	public static final String str_CREATE = "CREATE";
	public static final String str_IMPLEMENTS = "IMPLEMENTS";
	public static final String str_IMPLLINK = "IMPLLINK";
	public static final String str_PARAMETER = "PARAMETER";
	public static final String str_CAST = "CAST";
	public static final String str_RETURN = "RETURN";
	public static final String str_VARIABLE_TYPE = "VARIABLE_TYPE";
	public static final String str_DYNAMIC_CALL = "DYNAMIC_CALL";
	public static final String str_THROW = "THROW";
	public static final String str_ANNOTATION = "ANNOTATION";
	
	public static final String str_TESTCASE_EXECUTE_FEATURE = "TESTCASE_EXECUTE_FEATURE";
	public static final String str_SCENARIO_DEFINE_TESTCASE = "SCENARIO_DEFINE_TESTCASE";
	
	public static final String str_SPAN_CALL_SPAN = "SPAN_CALL_SPAN";
	public static final String str_SPAN_INSTANCE_OF_RESTFUL_API = "SPAN_INSTANCE_OF_RESTFUL_API";
	public static final String str_MICRO_SERVICE_CREATE_SPAN = "MICRO_SERVICE_CREATE_SPAN";
	public static final String str_SPAN_START_WITH_FUNCTION = "SPAN_START_WITH_FUNCTION";
	public static final String str_TESTCASE_RUN_TRACE = "TESTCASE_RUN_TRACE";
	
	public static final String str_TRACE_RUN_WITH_FUNCTION = "TRACE_RUN_WITH_FUNCTION";

	public static final String str_FILE_BUILD_DEPENDS_FILE = "FILE_BUILD_DEPENDS_FILE";
	
	public static final String str_MICROSERVICE_CALL_MICROSERVICE = "MICROSERVICE_CALL_MICROSERVICE";
	public static final String str_MICROSERVICE_DEPEND_ON_MICROSERVICE = "MICROSERVICE_DEPEND_ON_MICROSERVICE";

	public static final String str_COMMIT_UPDATE_FILE = "COMMIT_UPDATE_FILE";
	public static final String str_COMMIT_ADDRESS_ISSUE = "COMMIT_ADDRESS_ISSUE";
	public static final String str_DEVELOPER_REPORT_ISSUE = "DEVELOPER_REPORT_ISSUE";
	public static final String str_DEVELOPER_SUBMIT_COMMIT = "DEVELOPER_SUBMIT_COMMIT";
	public static final String str_COMMIT_INHERIT_COMMIT = "COMMIT_INHERIT_COMMIT";
	public static final String str_CO_CHANGE = "CO_CHANGE";
	
	public static final String str_FUNCTION_CALL_LIBRARY_API = "FUNCTION_CALL_LIBRARY_API";
	
	public static final String str_CLONE = "CLONE";
	
	/**
	 * 聚合关系
	 */
	
	public static final String str_DEPENDS_ON = "DEPENDS_ON";
	
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
