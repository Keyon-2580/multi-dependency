package cn.edu.fudan.se.multidependency.stub;

import java.io.File;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;

import depends.extractor.java.JavaParser.AnnotationContext;
import depends.extractor.java.JavaParser.ClassBodyContext;
import depends.extractor.java.JavaParser.ClassBodyDeclarationContext;
import depends.extractor.java.JavaParser.ClassOrInterfaceModifierContext;
import depends.extractor.java.JavaParser.ModifierContext;
import depends.extractor.java.JavaParser.QualifiedNameContext;
import depends.extractor.java.JavaParser.TypeDeclarationContext;

public class JavaStubListenerUsingTryFinallyForJaegerWithSpringMultiThread extends JavaStubListenerUsingTryFinally {
	protected static final String MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_DEPTH = "MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_DEPTH";
	protected static final String MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_ORDER = "MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_ORDER";
	protected static final String MULTIPLE_STUB_VARIABLE_MAP_TRACE_ID = "MULTIPLE_STUB_VARIABLE_MAP_TRACE_ID";
	protected static final String MULTIPLE_STUB_VARIABLE_MAP_SPAN_ID = "MULTIPLE_STUB_VARIABLE_MAP_SPAN_ID";
	protected static final String MULTIPLE_STUB_TRACE_ID = "MULTIPLE_STUB_TRACE_ID";
	protected static final String MULTIPLE_STUB_SPAN_ID = "MULTIPLE_STUB_SPAN_ID";
	protected static final String MULTIPLE_STUB_JAEGER_AUTOWIRED = "MULTIPLE_STUB_JAEGER_AUTOWIRED";
	
	protected static final String MULTIPLE_CALL = "MULTIPLE_CALL";
	
	protected boolean isClassController;
	
	protected boolean isMethodRequestMapping;
	
	public JavaStubListenerUsingTryFinallyForJaegerWithSpringMultiThread(TokenStream tokens, String projectName, File listenFile,
			CharStream input, String className, String outputFilePath, String remarks) {
		super(tokens, projectName, listenFile, input, className, outputFilePath, remarks);
	}
	
	@Override
	public void enterTypeDeclaration(TypeDeclarationContext ctx) {
		List<ClassOrInterfaceModifierContext> modifiers = ctx.classOrInterfaceModifier();
		for(ClassOrInterfaceModifierContext modifier : modifiers) {
			AnnotationContext annotation = modifier.annotation();
			if(annotation == null) {
				continue;
			}
			QualifiedNameContext name = annotation.qualifiedName();
			String annotationName = name.getText();
			String[] split = annotationName.split("\\.");
			String lastAnnotationName = split[split.length - 1];
			if("Controller".equals(lastAnnotationName)
					|| "RestController".equals(lastAnnotationName)) {
				isClassController = true;
				break;
			}
		}
		if(this.importGlobalVariable == false) {
			StringBuilder builder = new StringBuilder();
			builder
				/*.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_DEPTH)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_ORDER)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STRING_BUILDER)
				.append(";\n")*/
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_PRINT_TO_FILE)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_CALL)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_VARIABLE_MAP_TRACE_ID)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_VARIABLE_MAP_SPAN_ID)
				.append(";\n")				
				/*.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_REMARKS)
				.append(";\n")*/
				.append(ctx.start.getText());
			rewriter.replace(ctx.start, 
					builder.toString());
		}
		this.importGlobalVariable = true;
	}
	
	@Override
	public void enterClassBodyDeclaration(ClassBodyDeclarationContext ctx) {
		if(ctx.memberDeclaration() == null) {
			return;
		}
		if(ctx.memberDeclaration().methodDeclaration() == null) {
			return;
		}
		List<ModifierContext> modifiers = ctx.modifier();
		for(ModifierContext modifier : modifiers) {
			ClassOrInterfaceModifierContext modifierContext = modifier.classOrInterfaceModifier();
			if(modifierContext == null) {
				continue;
			}
			AnnotationContext annotation = modifierContext.annotation();
			if(annotation == null) {
				continue;
			}
			QualifiedNameContext name = annotation.qualifiedName();
			String annotationName = name.getText();
			String[] split = annotationName.split("\\.");
			String lastAnnotationName = split[split.length - 1];
			if(lastAnnotationName.endsWith("Mapping")) {
				isMethodRequestMapping = true;
				break;
			}
		}
		
	}
	
	@Override
	public void enterClassBody(ClassBodyContext ctx) {
		StringBuilder builder = new StringBuilder();
		builder.append(ctx.start.getText());
		if(isClassController) {
			builder
				.append("@org.springframework.beans.factory.annotation.Autowired private io.opentracing.Tracer ")
				.append(MULTIPLE_STUB_JAEGER_AUTOWIRED)
				.append(";");
			isClassController = false;
		}
		if(getMethodContainerName().equals(globalClass)) {
			builder.append(" public static final java.util.Map<java.lang.Long, java.lang.Long> ")
			.append(MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_DEPTH)
			.append(" = new java.util.HashMap<>();")
			.append(" public static final java.util.Map<java.lang.Long, java.lang.Long> ")
			.append(MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_ORDER)
			.append(" = new java.util.HashMap<>();")
			.append(" public static final java.util.Map<java.lang.Long, java.lang.String> ")
			.append(MULTIPLE_STUB_VARIABLE_MAP_TRACE_ID)
			.append(" = new java.util.HashMap<>();")
			.append(" public static final java.util.Map<java.lang.Long, java.lang.String> ")
			.append(MULTIPLE_STUB_VARIABLE_MAP_SPAN_ID)
			.append(" = new java.util.HashMap<>();")
			.append(" public static java.lang.String ")
			.append(MULTIPLE_STUB_REMARKS)
			.append(" = ")
			.append(remarks == null ? "\"{}\"" : ("\"" + remarks.replace("\"", "\\\"") + "\""))
			.append(";")
			.append(" public static final java.lang.StringBuffer ")
			.append(MULTIPLE_STRING_BUILDER)
			.append(" = new java.lang.StringBuffer();")
			.append(" public static final java.lang.String ")
			.append(MULTIPLE_OUTPUT_FILE)
			.append(" = \"").append(outputFilePath.replace("\\", "\\\\")).append("\";")
			.append("public static void ").append(MULTIPLE_PRINT_TO_FILE).append("() {")
			.append("java.lang.Long currentThreadId = java.lang.Thread.currentThread().getId();")
			.append("java.lang.Long MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH = ").append(MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_DEPTH).append(".get(currentThreadId);")
			.append(MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH).append(" = ").append(MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH).append(" == null ? 0L : ").append(MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH).append(";")
			.append(MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH).append("--;")
			.append("if(\"\".equals(").append(MULTIPLE_STRING_BUILDER).append(".toString())) { return; }")
			.append("try(java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileOutputStream(").append(MULTIPLE_OUTPUT_FILE).append(", true), true)) {")
			.append("writer.print(").append(MULTIPLE_STRING_BUILDER).append(".toString());")
			.append("writer.flush();")
			.append(MULTIPLE_STRING_BUILDER).append(".delete(0,").append(MULTIPLE_STRING_BUILDER).append(".length());")
			.append("} catch (java.lang.Exception e) {e.printStackTrace();}")
			.append("}")
			.append("public static void ").append(MULTIPLE_CALL).append("(java.lang.String project, java.lang.String inFile, java.lang.String function) {")
			.append("java.lang.Long currentThreadId = java.lang.Thread.currentThread().getId();")
			.append("java.lang.Long MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH = ").append(MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_DEPTH).append(".get(currentThreadId);")
			.append("MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH = MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH == null ? 0L : MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH;")
			.append("java.lang.Long MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER = ").append(MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_ORDER).append(".get(currentThreadId);")
			.append("MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER = MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER == null ? 0L : MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER;")
			.append("java.lang.String MULTIPLE_STUB_TRACE_ID = ").append(MULTIPLE_STUB_VARIABLE_MAP_TRACE_ID).append(".get(currentThreadId);")
			.append("MULTIPLE_STUB_TRACE_ID = MULTIPLE_STUB_TRACE_ID == null ? \"\" : MULTIPLE_STUB_TRACE_ID;")
			.append("java.lang.String MULTIPLE_STUB_SPAN_ID = ").append(MULTIPLE_STUB_VARIABLE_MAP_SPAN_ID).append(".get(currentThreadId);")
			.append("MULTIPLE_STUB_SPAN_ID = MULTIPLE_STUB_SPAN_ID == null ? \"\" : MULTIPLE_STUB_SPAN_ID;")
			.append("java.lang.String v_MUKTIPLE_STUB_REMARKS = ").append(MULTIPLE_STUB_REMARKS).append(";")
			.append(MULTIPLE_STRING_BUILDER).append(".append(\"{\\\"language\\\" : \\\"java\\\", \").append(\"\\\"time\\\" : \\\"\")\r\n" + 
					"				.append(new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss,SSS\")\r\n" + 
					"						.format(new java.sql.Timestamp(java.lang.System.currentTimeMillis()))).append(\"\\\"\")\r\n" + 
					"				.append(\", \\\"project\\\" : \\\"\")\r\n" + 
					"				.append(project)\r\n" + 
					"				.append(\"\\\", \\\"inFile\\\" : \\\"\")\r\n" + 
					"				.append(inFile)\r\n" + 
					"				.append(\"\\\", \\\"function\\\" : \\\"\")\r\n" + 
					"				.append(function)\r\n" + 
					"				.append(\"\\\", \\\"order\\\" : \\\"\").append(MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER++)\r\n" + 
					"				.append(\"\\\", \\\"depth\\\" : \\\"\").append(MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH++)\r\n" + 
					"				.append(\"\\\", \\\"traceId\\\" : \\\"\").append(MULTIPLE_STUB_TRACE_ID)\r\n" + 
					"				.append(\"\\\", \\\"spanId\\\" : \\\"\").append(MULTIPLE_STUB_SPAN_ID)\r\n" + 
					"				.append(\"\\\", \\\"remarks\\\" : \").append(v_MUKTIPLE_STUB_REMARKS).append(\"}\").append(\"\\n\");")
			.append(MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_DEPTH).append(".put(currentThreadId, MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH);")
			.append(MULTIPLE_STUB_VARIABLE_MAP_EXECUTION_ORDER).append(".put(currentThreadId, MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER);")
			.append("}")
			;
		}
		rewriter.replace(ctx.start, builder.toString());
	}
	
	@Override
	protected String startMethod(String methodName, List<String> parameterNames) {
		StringBuilder builder = new StringBuilder();
		if(isMethodRequestMapping) {
			builder
			.append("java.lang.String MULTIPLE_STUB_TRACE_ID = ").append(MULTIPLE_STUB_VARIABLE_MAP_TRACE_ID).append(".get(java.lang.Thread.currentThread().getId());")
			.append("java.lang.String MULTIPLE_STUB_SPAN_ID = ").append(MULTIPLE_STUB_VARIABLE_MAP_SPAN_ID).append(".get(java.lang.Thread.currentThread().getId());")
			.append("MULTIPLE_STUB_TRACE_ID = MULTIPLE_STUB_TRACE_ID == null ? java.lang.Long.toHexString(((io.jaegertracing.internal.JaegerSpan) ").append(MULTIPLE_STUB_JAEGER_AUTOWIRED).append(".activeSpan()).context().getTraceId()) : MULTIPLE_STUB_TRACE_ID;")
			.append("MULTIPLE_STUB_SPAN_ID = MULTIPLE_STUB_SPAN_ID == null ? java.lang.Long.toHexString(((io.jaegertracing.internal.JaegerSpan) ").append(MULTIPLE_STUB_JAEGER_AUTOWIRED).append(".activeSpan()).context().getSpanId()) : MULTIPLE_STUB_SPAN_ID;")
			.append(MULTIPLE_STUB_VARIABLE_MAP_TRACE_ID).append(".put(java.lang.Thread.currentThread().getId(), MULTIPLE_STUB_TRACE_ID);")
			.append(MULTIPLE_STUB_VARIABLE_MAP_SPAN_ID).append(".put(java.lang.Thread.currentThread().getId(), MULTIPLE_STUB_SPAN_ID);");
			isMethodRequestMapping = false;
		}
		builder.append(MULTIPLE_CALL)
		.append("(")
		// peoject
		.append("\"").append(projectName).append("\", ")
		// inFile
		.append("\"").append(this.listenJavaFile.getAbsolutePath().replace("\\", "\\\\")).append("\", ")
		// function
		.append("\"").append(getMethodFullName(methodName, parameterNames)).append("\"")
		.append(");");
		return builder.toString();
	}
	protected String endMethod() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n")
		.append(MULTIPLE_PRINT_TO_FILE).append("();");
		return builder.toString();
	}
}
