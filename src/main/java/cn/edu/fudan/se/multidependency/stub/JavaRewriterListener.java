package cn.edu.fudan.se.multidependency.stub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

import depends.extractor.java.JavaParser.BlockContext;
import depends.extractor.java.JavaParser.ClassDeclarationContext;
import depends.extractor.java.JavaParser.FormalParameterContext;
import depends.extractor.java.JavaParser.FormalParameterListContext;
import depends.extractor.java.JavaParser.MethodDeclarationContext;
import depends.extractor.java.JavaParser.PackageDeclarationContext;
import depends.extractor.java.JavaParserBaseListener;

public class JavaRewriterListener extends JavaParserBaseListener {
	private File listenFile;
	private TokenStreamRewriter rewriter;
	
	public File getListenFile() {
		return this.listenFile;
	}
	
	public TokenStreamRewriter getRewriter() {
		return this.rewriter;
	}
	
	public JavaRewriterListener(TokenStream tokens, File listenFile) {
		this.rewriter = new TokenStreamRewriter(tokens);
		this.listenFile = listenFile;
	}
	
	private String currentPackageName;
	private Stack<String> currentClassNames = new Stack<>();
	
	@Override
	public void enterPackageDeclaration(PackageDeclarationContext ctx) {
		currentPackageName = ctx.qualifiedName().getText();
	}
	
	@Override
	public void enterClassDeclaration(ClassDeclarationContext ctx) {
		String className = ctx.IDENTIFIER().getText();
		currentClassNames.push(className);
	}
	
	@Override
	public void exitClassDeclaration(ClassDeclarationContext ctx) {
		currentClassNames.pop();
	}
	
	@Override
	public void enterMethodDeclaration(MethodDeclarationContext ctx) {
		System.out.println(getCurrentFullClassNameWithPackageName());
		BlockContext block = ctx.methodBody().block();
		if(block == null) {
			return ;
		}
		String methodName = ctx.IDENTIFIER().getText();
		FormalParameterListContext parameterList = ctx.formalParameters().formalParameterList();
		List<String> parameterNames = new ArrayList<>();
		if(parameterList == null) {
			rewriter.insertAfter(block.getStart(), getRewriteStr(methodName, parameterNames));
		} else {
			List<FormalParameterContext> parameters = parameterList.formalParameter();
			parameters.forEach(parameter -> {
				parameterNames.add(parameter.typeType().getText());
			});
			if(parameterList.lastFormalParameter() != null) {
				parameterNames.add(parameterList.lastFormalParameter().typeType().getText() + "...");
			}
			rewriter.insertAfter(block.getStart(), getRewriteStr(methodName, parameterNames));
		}
	}
	
	private String getRewriteStr(String methodName, List<String> parameterNames) {
		return "\n\t\t"
				+ "System.out.println(\"" 
				+ this.listenFile.getAbsolutePath() + "-"
				+ getMethodFullName(methodName, parameterNames) + "\");";
	}
	
	private String getCurrentFullClassNameWithPackageName() {
		StringBuilder builder = new StringBuilder();
		builder.append(currentPackageName);
		currentClassNames.forEach(name -> {
			builder.append(".");
			builder.append(name);
		});
		return builder.toString();
	}
	
	private String getMethodFullName(String methodName, List<String> parameters) {
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
	
}
