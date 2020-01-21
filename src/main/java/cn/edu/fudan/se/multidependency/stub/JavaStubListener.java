package cn.edu.fudan.se.multidependency.stub;

import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.TerminalNode;

import depends.extractor.java.JavaParserBaseListener;
import depends.extractor.java.JavaParser.LambdaBodyContext;
import depends.extractor.java.JavaParser.MethodBodyContext;

public class JavaStubListener extends JavaParserBaseListener {
	
	@Override
	public void enterMethodBody(MethodBodyContext ctx) {
		System.out.println(ctx.getText());
		
	}
	
	@Override
	public void enterLambdaBody(LambdaBodyContext ctx) {
	}
	
}
