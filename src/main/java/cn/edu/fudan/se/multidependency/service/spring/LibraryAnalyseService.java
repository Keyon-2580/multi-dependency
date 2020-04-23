package cn.edu.fudan.se.multidependency.service.spring;

import cn.edu.fudan.se.multidependency.model.relation.lib.FunctionCallLibraryAPI;

public interface LibraryAnalyseService {
	
	Iterable<FunctionCallLibraryAPI> findAllFunctionCallLibraryAPIs();
	
}
