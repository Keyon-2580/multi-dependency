package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;

public class CppDynamicInserter extends DynamicInserterForNeo4jService {

	public CppDynamicInserter(File[] dynamicFunctionCallFiles) {
		super(dynamicFunctionCallFiles);
	}

	@Override
	protected void extractNodesAndRelations() throws Exception {
		/// FIXME
	}

}
