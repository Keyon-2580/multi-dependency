package cn.edu.fudan.se.multidependency.stub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;

import depends.extractor.java.JavaParser.BlockContext;
import depends.extractor.java.JavaParser.BlockStatementContext;
import depends.extractor.java.JavaParser.ConstructorDeclarationContext;
import depends.extractor.java.JavaParser.ExpressionContext;
import depends.extractor.java.JavaParser.FormalParameterListContext;
import depends.extractor.java.JavaParser.MethodCallContext;
import depends.extractor.java.JavaParser.MethodDeclarationContext;
import depends.extractor.java.JavaParser.StatementContext;

public class JavaStubListenerUsingTryFinally extends JavaStubListener {

	public JavaStubListenerUsingTryFinally(TokenStream tokens, File listenFile, CharStream input, String className) {
		super(tokens, listenFile, input, className);
	}
	
	@Override
	public void enterConstructorDeclaration(ConstructorDeclarationContext ctx) {
		String methodName = ctx.IDENTIFIER().getText();
		
		BlockContext block = ctx.constructorBody;
		FormalParameterListContext parameterList = ctx.formalParameters().formalParameterList();
		List<String> parameterNames = new ArrayList<>();
		if(parameterList != null) {
			parameterNames = extractParameterNames(parameterList);
		}
		List<BlockStatementContext> blockStatements = block.blockStatement();
		for(BlockStatementContext blockStatement : blockStatements) {
			StatementContext statement = blockStatement.statement();
			if(statement == null) {
				continue;
			}
			ExpressionContext expression = statement.statementExpression;
			if(expression == null) {
				continue;
			}
			MethodCallContext methodCall = expression.methodCall();
			if(methodCall == null) {
				continue;
			}
			if(methodCall.SUPER() != null || methodCall.THIS() != null) {
				// 添加try-finally
				rewriter.insertAfter(statement.stop, getRewriteStr(methodName, parameterNames) + "\ntry {");
				rewriter.insertBefore(block.stop, "\n} finally{\n" + endMethod() + "\n}");
				return ;
			}
		}
		rewriter.insertAfter(block.getStart(), getRewriteStr(methodName, parameterNames));
		// 添加try-finally
		rewriter.insertAfter(block.start, "\ntry {");
		rewriter.insertBefore(block.stop, "\n} finally{\n" + endMethod() + "\n}");
	}

	@Override
	public void enterMethodDeclaration(MethodDeclarationContext ctx) {
		BlockContext block = ctx.methodBody().block();
		if(block == null) {
			return ;
		}
		String methodName = ctx.IDENTIFIER().getText();
		FormalParameterListContext parameterList = ctx.formalParameters().formalParameterList();
		List<String> parameterNames = extractParameterNames(parameterList);
		// 开头
		rewriter.insertAfter(block.getStart(), getRewriteStr(methodName, parameterNames));
		
		// 添加try-finally
		rewriter.insertAfter(block.start, "\ntry {");
		rewriter.insertBefore(block.stop, "\n} finally{\n" + endMethod() + "\n}");
	}
	
}
