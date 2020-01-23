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
import depends.extractor.java.JavaParser.EnumDeclarationContext;
import depends.extractor.java.JavaParser.FormalParameterContext;
import depends.extractor.java.JavaParser.FormalParameterListContext;
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
	protected static final String MULTIPLE_STUB_VARIABLE_BREADTH = "MULTIPLE_STUB_VARIABLE_BREADTH";
	protected static final String MULTIPLE_STUB_VARIABLE_ORDER = "MULTIPLE_STUB_VARIABLE_ORDER";
	
	protected boolean importBreadth = false;
	
	protected String currentPackageName;
	protected Stack<String> currentClassNames = new Stack<>();
	protected String currentReturnType;
	
	public File getListenFile() {
		return this.listenFile;
	}
	
	public TokenStreamRewriter getRewriter() {
		return this.rewriter;
	}

	protected String endMethod() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n")
			.append(MULTIPLE_STUB_VARIABLE_BREADTH)
			.append("--;\n");
		builder.append("System.out.println(" + MULTIPLE_STUB_VARIABLE_BREADTH + ");");
		return builder.toString();
	}
	
	protected String getRewriteStr(String methodName, List<String> parameterNames) {
		StringBuilder builder = new StringBuilder();
		builder.append("\n\t\t")
			.append("System.out.println(\"")
			.append(this.listenFile.getAbsolutePath().replace("\\", "\\\\"))
			.append("-")
			.append(getMethodFullName(methodName, parameterNames))
			.append("-\" + ")
			.append(MULTIPLE_STUB_VARIABLE_ORDER).append("++")
			.append(" + \"-\" + ")
			.append(MULTIPLE_STUB_VARIABLE_BREADTH).append("++);");
		
		return builder.toString();
	}

	protected String getMethodFullName(String methodName, List<String> parameters) {
		StringBuilder builder = new StringBuilder();
		builder.append(getCurrentFullClassNameWithPackageName());
		builder.append(".");
		builder.append(methodName);
		builder.append("(");
		for(int i = 0; i < parameters.size(); i++) {
			String parameter = parameters.get(i);
			if(i == parameters.size() - 1) {
				builder.append(parameter);
			} else {
				builder.append(parameter);
				builder.append(", ");
			}
		}
		builder.append(")");
		return builder.toString();
	}

	@Override
	public void exitMethodDeclaration(MethodDeclarationContext ctx) {
		currentReturnType = null;
	}
	
	protected String getCurrentFullClassNameWithPackageName() {
		StringBuilder builder = new StringBuilder();
		if(currentPackageName != null) {
			builder.append(currentPackageName);
			currentClassNames.forEach(name -> {
				builder.append(".");
				builder.append(name);
			});
		} else {
			///FIXME
			// 没有包名
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
		if(this.importBreadth == false) {
			StringBuilder builder = new StringBuilder();
			builder.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_VARIABLE_BREADTH)
				.append(";\n")
				.append("import static ")
				.append(globalClass).append(".")
				.append(MULTIPLE_STUB_VARIABLE_ORDER)
				.append(";\n")
				.append(ctx.start.getText());
			rewriter.replace(ctx.start, 
					builder.toString());
		}
		this.importBreadth = true;
	}
	
	@Override
	public void enterClassDeclaration(ClassDeclarationContext ctx) {
		currentClassNames.push(ctx.IDENTIFIER().getText());
	}
	@Override
	public void exitClassDeclaration(ClassDeclarationContext ctx) {
		currentClassNames.pop();
	}
	@Override
	public void enterEnumDeclaration(EnumDeclarationContext ctx) {
		currentClassNames.push(ctx.IDENTIFIER().getText());
	}
	@Override
	public void exitEnumDeclaration(EnumDeclarationContext ctx) {
		currentClassNames.pop();
	}
	@Override
	public void enterInterfaceDeclaration(InterfaceDeclarationContext ctx) {
		currentClassNames.push(ctx.IDENTIFIER().getText());
	}
	@Override
	public void exitInterfaceDeclaration(InterfaceDeclarationContext ctx) {
		currentClassNames.pop();
	}
	@Override
	public void enterAnnotationTypeDeclaration(AnnotationTypeDeclarationContext ctx) {
		currentClassNames.push(ctx.IDENTIFIER().getText());
	}
	@Override
	public void exitAnnotationTypeDeclaration(AnnotationTypeDeclarationContext ctx) {
		currentClassNames.pop();
	}

	protected String getText(ParserRuleContext ruleContext) {
		int start = ruleContext.start.getStartIndex();
		int stop = ruleContext.stop.getStopIndex();
		Interval interval = new Interval(start, stop);
		return input.getText(interval);
	}
	

	@Override
	public void enterClassBody(ClassBodyContext ctx) {
		if(!getCurrentFullClassNameWithPackageName().equals(globalClass)) {
			return ;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(ctx.start.getText())
			.append(" public static long ")
			.append(MULTIPLE_STUB_VARIABLE_BREADTH)
			.append(" = 1L;")
			.append(" public static long ")
			.append(MULTIPLE_STUB_VARIABLE_ORDER)
			.append(" = 1L;")
			;
		rewriter.replace(ctx.start, builder.toString());
	}
	
	@Override
	public abstract void enterConstructorDeclaration(ConstructorDeclarationContext ctx);

	@Override
	public abstract void enterMethodDeclaration(MethodDeclarationContext ctx);
}
