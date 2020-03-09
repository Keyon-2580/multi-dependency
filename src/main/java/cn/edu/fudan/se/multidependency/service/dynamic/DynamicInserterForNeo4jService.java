package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;

import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelationsImpl;

public abstract class DynamicInserterForNeo4jService extends ExtractorForNodesAndRelationsImpl {
	
	protected File[] dynamicFunctionCallFiles;
	
	protected abstract void extractNodesAndRelations() throws Exception;
	
	public DynamicInserterForNeo4jService(File[] dynamicFunctionCallFiles) {
		this.dynamicFunctionCallFiles = dynamicFunctionCallFiles;
	}
	
	@Override
	public void addNodesAndRelations() throws Exception {
		if(dynamicFunctionCallFiles == null) {
			throw new Exception("动态运行日志dynamicFunctionCallFiles不能为null！");
		}
		extractNodesAndRelations();
	}

	public File[] getDynamicFunctionCallFiles() {
		return dynamicFunctionCallFiles;
	}

	public void setDynamicFunctionCallFiles(File... dynamicFunctionCallFiles) {
		this.dynamicFunctionCallFiles = dynamicFunctionCallFiles;
	}

}
