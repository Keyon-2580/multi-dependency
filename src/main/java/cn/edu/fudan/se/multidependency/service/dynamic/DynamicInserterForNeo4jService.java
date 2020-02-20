package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;

import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelationsImpl;

public abstract class DynamicInserterForNeo4jService extends ExtractorForNodesAndRelationsImpl {
	
	protected File[] dynamicFunctionCallFiles;
	
	protected abstract void extractNodesAndRelations() throws Exception;
	
	@Override
	public void addNodesAndRelations() {
		try {
			extractNodesAndRelations();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File[] getDynamicFunctionCallFiles() {
		return dynamicFunctionCallFiles;
	}

	public void setDynamicFunctionCallFiles(File... dynamicFunctionCallFiles) {
		this.dynamicFunctionCallFiles = dynamicFunctionCallFiles;
	}

}
