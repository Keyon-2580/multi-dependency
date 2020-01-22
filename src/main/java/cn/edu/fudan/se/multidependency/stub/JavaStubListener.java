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
import org.antlr.v4.runtime.tree.TerminalNode;

import depends.extractor.java.JavaParser.AnnotationTypeDeclarationContext;
import depends.extractor.java.JavaParser.BlockContext;
import depends.extractor.java.JavaParser.BlockStatementContext;
import depends.extractor.java.JavaParser.ClassBodyContext;
import depends.extractor.java.JavaParser.ClassDeclarationContext;
import depends.extractor.java.JavaParser.ConstructorDeclarationContext;
import depends.extractor.java.JavaParser.EnumDeclarationContext;
import depends.extractor.java.JavaParser.ExpressionContext;
import depends.extractor.java.JavaParser.FormalParameterContext;
import depends.extractor.java.JavaParser.FormalParameterListContext;
import depends.extractor.java.JavaParser.InterfaceBodyContext;
import depends.extractor.java.JavaParser.InterfaceDeclarationContext;
import depends.extractor.java.JavaParser.MethodCallContext;
import depends.extractor.java.JavaParser.MethodDeclarationContext;
import depends.extractor.java.JavaParser.PackageDeclarationContext;
import depends.extractor.java.JavaParser.StatementContext;
import depends.extractor.java.JavaParser.TypeDeclarationContext;
import depends.extractor.java.JavaParserBaseListener;

public class JavaStubListener extends JavaParserBaseListener {
	private File listenFile;
	private TokenStreamRewriter rewriter;
	private CharStream input;
	private String globalClass;
	private static final String MULTIPLE_STUB_RETURN = "MULTIPLE_STUB_RETURN";
	private static final String MULTIPLE_STUB_VARIABLE_BREADTH = "MULTIPLE_STUB_VARIABLE_BREADTH";
	private boolean importBreadth = false;
	
	public File getListenFile() {
		return this.listenFile;
	}
	
	public TokenStreamRewriter getRewriter() {
		return this.rewriter;
	}
	
	public JavaStubListener(TokenStream tokens, File listenFile, CharStream input, String className) {
		this.rewriter = new TokenStreamRewriter(tokens);
		this.listenFile = listenFile;
		this.input = input;
		this.globalClass = className;
	}
	
	private String currentPackageName;
	private Stack<String> currentClassNames = new Stack<>();
	private String currentReturnType;
	
	@Override
	public void enterPackageDeclaration(PackageDeclarationContext ctx) {
		currentPackageName = ctx.qualifiedName().getText();
	}
	@Override
	public void enterTypeDeclaration(TypeDeclarationContext ctx) {
		if(this.importBreadth == false) {
			rewriter.replace(ctx.start, "import static " + globalClass + "." + MULTIPLE_STUB_VARIABLE_BREADTH + ";\n" + ctx.start.getText());
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
	
	@Override
	public void enterClassBody(ClassBodyContext ctx) {
		if(!getCurrentFullClassNameWithPackageName().equals(globalClass)) {
			return ;
		}
		rewriter.replace(ctx.start, ctx.start.getText() + " public static long " + MULTIPLE_STUB_VARIABLE_BREADTH + " = 1L;");
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
				rewriter.insertAfter(statement.stop, getRewriteStr(methodName, parameterNames));
				rewriter.insertBefore(block.getStop(), endMethod());
				return ;
			}
		}
		rewriter.insertAfter(block.getStart(), getRewriteStr(methodName, parameterNames));
		rewriter.insertBefore(block.getStop(), endMethod());
	}
	
	private String endMethod() {
		return "\n" + MULTIPLE_STUB_VARIABLE_BREADTH + "--;\n";
	}
	
	private void addBlockForConditionStatement(StatementContext mainStatement) {
		if(mainStatement == null) {
			return ;
		}
		if(!(mainStatement.IF() != null || mainStatement.FOR() != null 
				|| mainStatement.WHILE() != null 
				|| mainStatement.DO() != null 
				|| mainStatement.ELSE() != null)) {
			return ;
		}
		for(StatementContext childStatement : mainStatement.statement()) {
			if(childStatement.blockLabel == null) {
//				if(childStatement.RETURN() != null || childStatement.THROW() != null) {
				if(childStatement.RETURN() != null) {
					List<ExpressionContext> expressions = childStatement.expression();
					if(expressions.size() == 0) {
						rewriter.replace(childStatement.start, "{\n" + endMethod() + childStatement.start.getText());
					} else if(expressions.size() == 1) {
						ExpressionContext expression = expressions.get(0);
						if(childStatement.RETURN() != null) {
							rewriter.replace(childStatement.start, 
									"{" + currentReturnType + " MULTIPLE_STUB_RETURN = " + getText(expression) + ";\n" + endMethod() + childStatement.start.getText());
						} else if(childStatement.THROW() != null) {
							rewriter.replace(childStatement.start, 
									"{ Exception MULTIPLE_STUB_RETURN = " + getText(expression) + ";\n" + childStatement.start.getText());
						}
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
//			if(statement.RETURN() != null || statement.THROW() != null) {
			if(statement.RETURN() != null) {
				List<ExpressionContext> expressions = statement.expression();
				if(expressions.size() == 0) {
					// return ;
					rewriter.replace(statement.start, "\n" + endMethod() + statement.start.getText());
				} else if(expressions.size() == 1) {
					ExpressionContext expression = expressions.get(0);
					if(statement.RETURN() != null) {
						rewriter.replace(statement.start, currentReturnType + " MULTIPLE_STUB_RETURN = " + getText(expression) + ";\n" + endMethod() + statement.start.getText());
					} else if(statement.THROW() != null) {
						rewriter.replace(statement.start, "Exception MULTIPLE_STUB_RETURN = " + getText(expression) + ";\n" + statement.start.getText());
					}
					rewriter.replace(expression.start, expression.stop, "MULTIPLE_STUB_RETURN");
				}
			} else {
				addBlockForConditionStatement(statement);
			}
		}
	}
	
	private String getText(ParserRuleContext ruleContext) {
		int start = ruleContext.start.getStartIndex();
		int stop = ruleContext.stop.getStopIndex();
		Interval interval = new Interval(start, stop);
		return input.getText(interval);
	}
	
	@Override
	public void enterMethodDeclaration(MethodDeclarationContext ctx) {
		BlockContext block = ctx.methodBody().block();
		if(block == null) {
			return ;
		}
		String methodName = ctx.IDENTIFIER().getText();
		FormalParameterListContext parameterList = ctx.formalParameters().formalParameterList();
		List<String> parameterNames = new ArrayList<>();
		// 开头
		if(parameterList == null) {
			rewriter.insertAfter(block.getStart(), getRewriteStr(methodName, parameterNames));
		} else {
			parameterNames = extractParameterNames(parameterList);
			rewriter.insertAfter(block.getStart(), getRewriteStr(methodName, parameterNames));
		}
		
		// 末尾
		String returnType = ctx.typeTypeOrVoid().getText();
		currentReturnType = returnType;
		addBlockForBlockStatement(block);
		if("void".equals(currentReturnType)) {
			if(block.blockStatement().size() == 0) {
				rewriter.insertBefore(block.stop, endMethod());
			} else {
				BlockStatementContext lastBlockStatement = block.blockStatement(block.blockStatement().size() - 1);
				if(lastBlockStatement.statement() != null && lastBlockStatement.statement().RETURN() == null) {
//					System.out.println(methodName + " " + lastBlockStatement.getText());
					rewriter.insertBefore(block.stop, endMethod());
				}
			}
		}
	}
	
	@Override
	public void exitMethodDeclaration(MethodDeclarationContext ctx) {
		currentReturnType = null;
	}
	
	private String getRewriteStr(String methodName, List<String> parameterNames) {
		return "\n\t\t"
				+ "System.out.println(\"" 
				+ this.listenFile.getAbsolutePath().replace("\\", "\\\\") + "-"
				+ getMethodFullName(methodName, parameterNames) + "\" + " + MULTIPLE_STUB_VARIABLE_BREADTH + "++);";
	}
	
	private String getCurrentFullClassNameWithPackageName() {
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
	
	private List<String> extractParameterNames(FormalParameterListContext parameterList) {
		List<String> parameterNames = new ArrayList<>();
		List<FormalParameterContext> parameters = parameterList.formalParameter();
		parameters.forEach(parameter -> {
			parameterNames.add(parameter.typeType().getText());
		});
		if(parameterList.lastFormalParameter() != null) {
			parameterNames.add(parameterList.lastFormalParameter().typeType().getText() + "...");
		}
		return parameterNames;
	}
	
}
