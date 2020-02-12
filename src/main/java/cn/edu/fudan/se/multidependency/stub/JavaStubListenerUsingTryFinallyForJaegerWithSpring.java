package cn.edu.fudan.se.multidependency.stub;

import java.io.File;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;

import depends.extractor.java.JavaParser.AnnotationContext;
import depends.extractor.java.JavaParser.BlockContext;
import depends.extractor.java.JavaParser.ClassBodyContext;
import depends.extractor.java.JavaParser.ClassBodyDeclarationContext;
import depends.extractor.java.JavaParser.ClassOrInterfaceModifierContext;
import depends.extractor.java.JavaParser.FormalParameterListContext;
import depends.extractor.java.JavaParser.MethodDeclarationContext;
import depends.extractor.java.JavaParser.ModifierContext;
import depends.extractor.java.JavaParser.QualifiedNameContext;
import depends.extractor.java.JavaParser.TypeDeclarationContext;

public class JavaStubListenerUsingTryFinallyForJaegerWithSpring extends JavaStubListenerUsingTryFinally {
	protected static final String MULTIPLE_STUB_TRACE_ID = "MULTIPLE_STUB_TRACE_ID";
	protected static final String MULTIPLE_STUB_SPAN_ID = "MULTIPLE_STUB_SPAN_ID";
	protected static final String MULTIPLE_STUB_JAEGER_AUTOWIRED = "MULTIPLE_STUB_JAEGER_AUTOWIRED";
	
	protected boolean isClassController;
	
	protected boolean isMethodRequestMapping;
	
	public JavaStubListenerUsingTryFinallyForJaegerWithSpring(TokenStream tokens, String projectName, File listenFile,
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
			builder.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STRING_BUILDER)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_PRINT_TO_FILE)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_TRACE_ID)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_SPAN_ID)
				.append(";\n")				
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_REMARKS)
				.append(";\n")
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
			builder.append(" public static long ")
			.append(MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH)
			.append(" = 0L;")
			.append(" public static long ")
			.append(MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER)
			.append(" = 0L;")
			.append(" public static java.lang.String ")
			.append(MULTIPLE_STUB_TRACE_ID)
			.append(" = \"\";")
			.append(" public static java.lang.String ")
			.append(MULTIPLE_STUB_SPAN_ID)
			.append(" = \"\";")
			.append(" public static java.lang.String ")
			.append(MULTIPLE_STUB_REMARKS)
			.append(" = ")
			.append(remarks == null ? null : ("\"" + remarks.replace("\"", "\\\"") + "\""))
			.append(";")
			.append(" public static final java.lang.StringBuffer ")
			.append(MULTIPLE_STRING_BUILDER)
			.append(" = new java.lang.StringBuffer();")
			.append(" public static final java.lang.String ")
			.append(MULTIPLE_OUTPUT_FILE)
			.append(" = \"").append(outputFilePath.replace("\\", "\\\\")).append("\";")
			.append("public static void ").append(MULTIPLE_PRINT_TO_FILE).append("() {")
			.append("if(\"\".equals(").append(MULTIPLE_STRING_BUILDER).append(".toString())) { return; }")
			.append("try(java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileOutputStream(").append(MULTIPLE_OUTPUT_FILE).append(", true), true)) {")
			.append("writer.print(").append(MULTIPLE_STRING_BUILDER).append(".toString());")
			.append("writer.flush();")
			.append(MULTIPLE_STRING_BUILDER).append(".delete(0,").append(MULTIPLE_STRING_BUILDER).append(".length());")
			.append("} catch (java.lang.Exception e) {e.printStackTrace();}")
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
			.append(MULTIPLE_STUB_TRACE_ID)
			.append(" = java.lang.Long.toHexString(((io.jaegertracing.internal.JaegerSpan) ")
			.append(MULTIPLE_STUB_JAEGER_AUTOWIRED)
			.append(".activeSpan()).context().getTraceId());")
			.append(MULTIPLE_STUB_SPAN_ID)
			.append(" = java.lang.Long.toHexString(((io.jaegertracing.internal.JaegerSpan) ")
			.append(MULTIPLE_STUB_JAEGER_AUTOWIRED)
			.append(".activeSpan()).context().getSpanId());");
			isMethodRequestMapping = false;
		}
		builder.append(MULTIPLE_STRING_BUILDER)
		.append(".append(\"{\\\"language\\\" : \\\"java\\\", \")")
		.append(".append(\"\\\"time\\\" : \\\"")
		.append("\")")
		.append(".append(")
		.append("new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss,SSS\").format(new java.sql.Timestamp(java.lang.System.currentTimeMillis()))")
		.append(")")
		.append(".append(\"\\\"\")")
		.append(".append(\", \\\"project\\\" : \\\"\")")
		.append(".append(\"")
		.append(projectName)
		.append("\")")
		.append(".append(\"\\\", \\\"inFile\\\" : \\\"\")")
		.append(".append(\"")
		.append(this.listenJavaFile.getAbsolutePath().replace("\\", "\\\\\\\\"))
		.append("\")")
		.append(".append(\"\\\", \\\"function\\\" : \\\"\")")
		.append(".append(\"")
		.append(getMethodFullName(methodName, parameterNames))
		.append("\")")
		.append(".append(\"\\\", \\\"order\\\" : \\\"\")")
		.append(".append(")
		.append(MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER)
		.append("++")
		.append(")")
		.append(".append(\"\\\", \\\"depth\\\" : \\\"\")")
		.append(".append(")
		.append(MULTIPLE_STUB_VARIABLE_EXECUTION_DEPTH)
		.append("++")
		.append(")")
		.append(".append(\"\\\", \\\"traceId\\\" : \\\"\")")
		.append(".append(")
		.append(MULTIPLE_STUB_TRACE_ID)
		.append(")")
		.append(".append(\"\\\", \\\"spanId\\\" : \\\"\")")
		.append(".append(")
		.append(MULTIPLE_STUB_SPAN_ID)
		.append(")");
		if(remarks == null) {
			builder.append(".append(\"\\\", \\\"remarks\\\" : {}}\")")
			.append(".append(\"\\n\");");
		} else {
			builder.append(".append(\"\\\", \\\"remarks\\\" : \")")
			.append(".append(")
			.append("\"")
			.append(remarks.replace("\"", "\\\""))
			.append("\"")
			.append(")")
			.append(".append(\"}\")")
			.append(".append(\"\\n\");");
		}
		return builder.toString();
	}

}
