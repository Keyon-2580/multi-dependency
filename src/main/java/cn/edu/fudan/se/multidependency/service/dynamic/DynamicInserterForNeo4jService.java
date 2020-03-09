package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.service.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.JavaDynamicUtil;
import cn.edu.fudan.se.multidependency.utils.JavaDynamicUtil.JavaDynamicFunctionExecution;

public abstract class DynamicInserterForNeo4jService extends ExtractorForNodesAndRelationsImpl {
	
	private File[] dynamicFunctionCallFiles;
	
	protected abstract void extractNodesAndRelations() throws Exception;
	
	protected Map<String, List<JavaDynamicFunctionExecution>> javaExecutionsGroupByProject;
	
	public DynamicInserterForNeo4jService(File[] dynamicFunctionCallFiles) {
		this.dynamicFunctionCallFiles = dynamicFunctionCallFiles;
	}
	
	@Override
	public void addNodesAndRelations() throws Exception {
		if(dynamicFunctionCallFiles == null) {
			throw new Exception("动态运行日志dynamicFunctionCallFiles不能为null！");
		}
		javaExecutionsGroupByProject = JavaDynamicUtil.readDynamicLogs(dynamicFunctionCallFiles);
		extractNodesAndRelations();
	}

	public void setDynamicFunctionCallFiles(File... dynamicFunctionCallFiles) {
		this.dynamicFunctionCallFiles = dynamicFunctionCallFiles;
	}

}
