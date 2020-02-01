package cn.edu.fudan.se.multidependency.stub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.misc.Interval;

import depends.extractor.java.JavaParser.AnnotationTypeDeclarationContext;
import depends.extractor.java.JavaParser.ClassBodyContext;
import depends.extractor.java.JavaParser.ClassDeclarationContext;
import depends.extractor.java.JavaParser.ConstructorDeclarationContext;
import depends.extractor.java.JavaParser.CreatorContext;
import depends.extractor.java.JavaParser.EnumDeclarationContext;
import depends.extractor.java.JavaParser.FormalParameterContext;
import depends.extractor.java.JavaParser.FormalParameterListContext;
import depends.extractor.java.JavaParser.InnerCreatorContext;
import depends.extractor.java.JavaParser.InterfaceDeclarationContext;
import depends.extractor.java.JavaParser.MethodDeclarationContext;
import depends.extractor.java.JavaParser.PackageDeclarationContext;
import depends.extractor.java.JavaParser.TypeDeclarationContext;
import depends.extractor.java.JavaParserBaseListener;

public abstract class JavaStubListener extends JavaParserBaseListener {
	
	public JavaStubListener(TokenStream tokens, File listenFile, CharStream input, String className) {
		this.rewriter = new TokenStreamRewriter(tokens);
		this.listenFile = listenFile;
		this.input = input;
		this.globalClass = className;
	}

	protected File listenFile;
	protected TokenStreamRewriter rewriter;
	protected CharStream input;
	protected String globalClass;
	protected static final String MULTIPLE_STUB_VARIABLE_EXECUTION_LAYER = "MULTIPLE_STUB_VARIABLE_EXECUTION_LAYER";
	protected static final String MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER = "MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER";
	
	protected boolean importGlobalVariable = false;
	
	protected String currentPackageName;
	protected Stack<String> methodContainer = new Stack<>();
	
	public File getListenFile() {
		return this.listenFile;
	}
	
	public TokenStreamRewriter getRewriter() {
		return this.rewriter;
	}

	protected String endMethod() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n")
			.append(MULTIPLE_STUB_VARIABLE_EXECUTION_LAYER)
			.append("--;\n");
//		builder.append("System.out.println(" + MULTIPLE_STUB_VARIABLE_BREADTH + ");");
		return builder.toString();
	}
	
	protected String startMethod(String methodName, List<String> parameterNames) {
		StringBuilder builder = new StringBuilder();
		builder.append("\n\t\t")
			.append("System.out.println(")
			.append("\"")
			.append(this.listenFile.getAbsolutePath().replace("\\", "\\\\"))
			.append("|")
			.append(getMethodFullName(methodName, parameterNames))
			.append("-\" + ")
			.append(MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER).append("++")
			.append(" + \"-\" + ")
			.append(MULTIPLE_STUB_VARIABLE_EXECUTION_LAYER).append("++")
			.append(");");
		
		return builder.toString();
	}

	protected String getMethodFullName(String methodName, List<String> parameters) {
		StringBuilder builder = new StringBuilder();
		builder.append(getMethodContainerName());
		// 参数
		builder.append("-(");
		for(int i = 0; i < parameters.size(); i++) {
			String parameter = parameters.get(i);
			builder.append(parameter);
			if(i != parameters.size() - 1) {
				builder.append(", ");
			}
		}
		builder.append(")");
		return builder.toString();
	}

	protected String getMethodContainerName() {
		StringBuilder builder = new StringBuilder();
		if(currentPackageName != null) {
			builder.append(currentPackageName);
			builder.append(".");
		}
		for(int i = 0; i < methodContainer.size(); i++) {
			String name = methodContainer.get(i);
			builder.append(name);
			if(i != methodContainer.size() - 1) {
				builder.append(".");
			}
		}
		return builder.toString();
	}
	
	protected List<String> extractParameterNames(FormalParameterListContext parameterList) {
		List<String> parameterNames = new ArrayList<>();
		if(parameterList == null) {
			return parameterNames;
		}
		List<FormalParameterContext> parameters = parameterList.formalParameter();
		parameters.forEach(parameter -> {
			parameterNames.add(parameter.typeType().getText());
		});
		if(parameterList.lastFormalParameter() != null) {
			parameterNames.add(parameterList.lastFormalParameter().typeType().getText() + "...");
		}
		return parameterNames;
	}
	
	@Override
	public void enterPackageDeclaration(PackageDeclarationContext ctx) {
		currentPackageName = ctx.qualifiedName().getText();
	}
	
	@Override
	public void enterTypeDeclaration(TypeDeclarationContext ctx) {
		if(this.importGlobalVariable == false) {
			StringBuilder builder = new StringBuilder();
			builder.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_VARIABLE_EXECUTION_LAYER)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER)
				.append(";\n")
				.append(ctx.start.getText());
			rewriter.replace(ctx.start, 
					builder.toString());
		}
		this.importGlobalVariable = true;
	}
	
	@Override
	public void enterClassDeclaration(ClassDeclarationContext ctx) {
		methodContainer.push(ctx.IDENTIFIER().getText());
	}
	@Override
	public void exitClassDeclaration(ClassDeclarationContext ctx) {
		methodContainer.pop();
	}
	@Override
	public void enterEnumDeclaration(EnumDeclarationContext ctx) {
		methodContainer.push(ctx.IDENTIFIER().getText());
	}
	@Override
	public void exitEnumDeclaration(EnumDeclarationContext ctx) {
		methodContainer.pop();
	}
	@Override
	public void enterInterfaceDeclaration(InterfaceDeclarationContext ctx) {
		methodContainer.push(ctx.IDENTIFIER().getText());
	}
	@Override
	public void exitInterfaceDeclaration(InterfaceDeclarationContext ctx) {
		methodContainer.pop();
	}
	@Override
	public void enterAnnotationTypeDeclaration(AnnotationTypeDeclarationContext ctx) {
		methodContainer.push(ctx.IDENTIFIER().getText());
	}
	@Override
	public void exitAnnotationTypeDeclaration(AnnotationTypeDeclarationContext ctx) {
		methodContainer.pop();
	}
	///FIXME
	// 匿名类
	private boolean hasCreatorClassBody(CreatorContext ctx) {
		return false;
	}
	private boolean hasCreatorClassBody(InnerCreatorContext ctx) {
		return false;
	}
	@Override
	public void enterCreator(CreatorContext ctx) {
		
	}

	@Override
	public void exitCreator(CreatorContext ctx) {
		
	}
	
	@Override
	public void enterInnerCreator(InnerCreatorContext ctx) {
		
	}
	
	@Override
	public void exitInnerCreator(InnerCreatorContext ctx) {
		
	}
	
	
	protected String getText(ParserRuleContext ruleContext) {
		int start = ruleContext.start.getStartIndex();
		int stop = ruleContext.stop.getStopIndex();
		Interval interval = new Interval(start, stop);
		return input.getText(interval);
	}
	

	@Override
	public void enterClassBody(ClassBodyContext ctx) {
		if(!getMethodContainerName().equals(globalClass)) {
			return ;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(ctx.start.getText())
			.append(" public static long ")
			.append(MULTIPLE_STUB_VARIABLE_EXECUTION_LAYER)
			.append(" = 0L;")
			.append(" public static long ")
			.append(MULTIPLE_STUB_VARIABLE_EXECUTION_ORDER)
			.append(" = 0L;")
			;
		rewriter.replace(ctx.start, builder.toString());
	}
	
	@Override
	public abstract void enterConstructorDeclaration(ConstructorDeclarationContext ctx);

	@Override
	public abstract void enterMethodDeclaration(MethodDeclarationContext ctx);
	
}
