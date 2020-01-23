package cn.edu.fudan.se.multidependency.stub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;

import depends.extractor.java.JavaParser.BlockContext;
import depends.extractor.java.JavaParser.BlockStatementContext;
import depends.extractor.java.JavaParser.CatchClauseContext;
import depends.extractor.java.JavaParser.ClassBodyContext;
import depends.extractor.java.JavaParser.ConstructorDeclarationContext;
import depends.extractor.java.JavaParser.ExpressionContext;
import depends.extractor.java.JavaParser.FormalParameterListContext;
import depends.extractor.java.JavaParser.MethodCallContext;
import depends.extractor.java.JavaParser.MethodDeclarationContext;
import depends.extractor.java.JavaParser.StatementContext;
import depends.extractor.java.JavaParser.TypeDeclarationContext;

public class JavaStubListenerIamStupid extends JavaStubListener {
	
	protected static final String MULTIPLE_STUB_VARIABLE_BREADTH_TEMP = "MULTIPLE_STUB_VARIABLE_BREADTH_TEMP";

	protected static final String MULTIPLE_STUB_RETURN = "MULTIPLE_STUB_RETURN";
	public JavaStubListenerIamStupid(TokenStream tokens, File listenFile, CharStream input, String className) {
		super(tokens, listenFile, input, className);
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
			.append(MULTIPLE_STUB_VARIABLE_BREADTH).append("++);")
			.append(MULTIPLE_STUB_VARIABLE_BREADTH_TEMP)
			.append(" = ")
			.append(MULTIPLE_STUB_VARIABLE_BREADTH)
			.append(";");
		
		return builder.toString();
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
				.append(MULTIPLE_STUB_VARIABLE_BREADTH_TEMP)
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
			.append(MULTIPLE_STUB_VARIABLE_BREADTH_TEMP)
			.append(" = 1L;")
			.append(" public static long ")
			.append(MULTIPLE_STUB_VARIABLE_ORDER)
			.append(" = 1L;")
			;
		rewriter.replace(ctx.start, builder.toString());
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
			// 构造方法中如果有super()或this()，必须放在第一行
			if(methodCall.SUPER() != null || methodCall.THIS() != null) {
				rewriter.insertAfter(statement.stop, getRewriteStr(methodName, parameterNames));
				rewriter.insertBefore(block.getStop(), endMethod());
				return ;
			}
		}
		rewriter.insertAfter(block.getStart(), getRewriteStr(methodName, parameterNames));
		rewriter.insertBefore(block.getStop(), endMethod());
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

		// 末尾&异常
		String returnType = ctx.typeTypeOrVoid().getText();
		currentReturnType = returnType;
		addBlockForBlockStatement(block);
		if("void".equals(currentReturnType)) {
			if(block.blockStatement().size() == 0) {
				rewriter.insertBefore(block.stop, endMethod());
			} else {
				BlockStatementContext lastBlockStatement = block.blockStatement(block.blockStatement().size() - 1);
				if(lastBlockStatement.statement() != null && lastBlockStatement.statement().RETURN() == null) {
					rewriter.insertBefore(block.stop, endMethod());
				}
			}
		}
	}
	
	private boolean isContiditionStatement(StatementContext statement) {
		return statement.IF() != null || statement.FOR() != null 
				|| statement.WHILE() != null 
				|| statement.DO() != null 
				|| statement.ELSE() != null;
	}
	
	private void addBlockForConditionStatement(StatementContext mainStatement) {
		if(mainStatement == null) {
			return ;
		}
		if(!isContiditionStatement(mainStatement)) {
			return ;
		}
		for(StatementContext childStatement : mainStatement.statement()) {
			if(childStatement.blockLabel == null) {
				if(childStatement.RETURN() != null) {
					List<ExpressionContext> expressions = childStatement.expression();
					if(expressions.size() == 0) {
						rewriter.replace(childStatement.start, "{\n" + endMethod() + childStatement.start.getText());
					} else if(expressions.size() == 1) {
						ExpressionContext expression = expressions.get(0);
						rewriter.replace(childStatement.start, 
								"{" + currentReturnType + " MULTIPLE_STUB_RETURN = " + getText(expression) + ";\n" + endMethod() + childStatement.start.getText());
						rewriter.replace(expression.start, expression.stop, "MULTIPLE_STUB_RETURN");
					}
					rewriter.replace(childStatement.stop, childStatement.stop.getText() + "}");
				} else {
					addBlockForConditionStatement(childStatement);
				}
			} else {
				BlockContext block = childStatement.blockLabel;
				addBlockForBlockStatement(block);
			}
		}
	}
	
	private void addBlockForBlockStatement(BlockContext block) {
		List<BlockStatementContext> blockStatements = block.blockStatement();
		for(BlockStatementContext blockStatement : blockStatements) {
			StatementContext statement = blockStatement.statement();
			if(statement == null) {
				continue;
			}
			if(statement.blockLabel != null) {
				// 方法内的语句块
				addBlockForBlockStatement(statement.blockLabel);
			}
			if(statement.RETURN() != null) {
				List<ExpressionContext> expressions = statement.expression();
				if(expressions.size() == 0) {
					// return ;
					rewriter.replace(statement.start, "\n" + endMethod() + statement.start.getText());
				} else if(expressions.size() == 1) {
					ExpressionContext expression = expressions.get(0);
					rewriter.replace(statement.start, currentReturnType + " MULTIPLE_STUB_RETURN = " + getText(expression) + ";\n" + endMethod() + statement.start.getText());
					rewriter.replace(expression.start, expression.stop, "MULTIPLE_STUB_RETURN");
				}
			} else if(isContiditionStatement(statement)) {
				addBlockForConditionStatement(statement);
			} else {
				List<CatchClauseContext> catchClauses = statement.catchClause();
				for(CatchClauseContext catchClause : catchClauses) {
					BlockContext catchBlock = catchClause.block();
					rewriter.insertAfter(catchBlock.start, MULTIPLE_STUB_VARIABLE_BREADTH + " = " + MULTIPLE_STUB_VARIABLE_BREADTH_TEMP + ";");
				}
			}
		}
	}
	
}
