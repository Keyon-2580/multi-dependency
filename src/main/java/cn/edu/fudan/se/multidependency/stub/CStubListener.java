package cn.edu.fudan.se.multidependency.stub;

import java.io.File;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

import depends.extractor.cpp.CElsaBaseListener;
import depends.extractor.cpp.CElsaParser.FunctionBodyContext;
import depends.extractor.cpp.CElsaParser.FunctionDefinitionContext;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CStubListener extends CElsaBaseListener {
	
	protected String projectName;
	protected File listenJavaFile;
	protected TokenStreamRewriter rewriter;
	protected CharStream inputCharStream;
	
	@Override
	public void enterFunctionDefinition(FunctionDefinitionContext ctx) {
		
	}
	
	@Override
	public void enterFunctionBody(FunctionBodyContext ctx) {
		
	}
}
