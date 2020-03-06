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

	public JavaStubListenerUsingTryFinally(TokenStream tokens, String projectName, File listenFile, CharStream input,
			String className, String outputFilePath,
			String remarks) {
		super(tokens, projectName, listenFile, input, className, outputFilePath, remarks);
	}

	@Override
	public void enterConstructorDeclaration(ConstructorDeclarationContext ctx) {
		String methodName = ctx.IDENTIFIER().getText();
		methodContainer.push(methodName);

		BlockContext block = ctx.constructorBody;
		FormalParameterListContext parameterList = ctx.formalParameters().formalParameterList();
		List<String> parameterNames = new ArrayList<>();
		if (parameterList != null) {
			parameterNames = extractParameterNames(parameterList);
		}
		List<BlockStatementContext> blockStatements = block.blockStatement();
		for (BlockStatementContext blockStatement : blockStatements) {
			StatementContext statement = blockStatement.statement();
			if (statement == null) {
				continue;
			}
			ExpressionContext expression = statement.statementExpression;
			if (expression == null) {
				continue;
			}
			MethodCallContext methodCall = expression.methodCall();
			if (methodCall == null) {
				continue;
			}
			if (methodCall.SUPER() != null || methodCall.THIS() != null) {
				// 添加try-finally
				rewriter.insertAfter(statement.stop, startMethod(methodName, parameterNames) + "\ntry {");
				rewriter.insertBefore(block.stop, "\n} finally{\n" + endMethod() + "\n}");
				return;
			}
		}
		rewriter.insertAfter(block.getStart(), startMethod(methodName, parameterNames));
		// 添加try-finally
		rewriter.insertAfter(block.start, "\ntry {");
		rewriter.insertBefore(block.stop, "\n} finally{\n" + endMethod() + "\n}");
	}

	@Override
	public void enterMethodDeclaration(MethodDeclarationContext ctx) {
		BlockContext block = ctx.methodBody().block();
		if (block == null) {
			return;
		}
		String methodName = ctx.IDENTIFIER().getText();
		methodContainer.push(methodName);
		FormalParameterListContext parameterList = ctx.formalParameters().formalParameterList();
		List<String> parameterNames = extractParameterNames(parameterList);
		// 开头
		rewriter.insertAfter(block.getStart(), startMethod(methodName, parameterNames));

		// 添加try-finally
		rewriter.insertAfter(block.start, "\ntry {");
		rewriter.insertBefore(block.stop, "\n} finally{\n" + endMethod() + "\n}");
	}

	@Override
	public void exitMethodDeclaration(MethodDeclarationContext ctx) {
		if (ctx.methodBody().block() != null) {
			methodContainer.pop();
		}
	}

	@Override
	public void exitConstructorDeclaration(ConstructorDeclarationContext ctx) {
		methodContainer.pop();
	}

}
