package cn.edu.fudan.se.multidependency.service.nospring.clone;

import java.util.Map;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.MethodNameForJava;
import lombok.Setter;

public class CloneInserter extends ExtractorForNodesAndRelationsImpl {
	
	@Setter
	private Language language;
	@Setter
	private String methodNameTablePath;
	@Setter
	private String methodResultPath;
	
	public CloneInserter(Language language, String methodNameTablePath, String methodResultPath) {
		super();
		this.language = language;
		this.methodNameTablePath = methodNameTablePath;
		this.methodResultPath = methodResultPath;
	}

	@Override
	public void addNodesAndRelations() throws Exception {
		if(Language.java == language) {
			Map<Integer, MethodNameForJava> methodNames = CloneUtil.readJavaCloneCsvForMethodName(methodNameTablePath);
			Map<Integer, Map<Integer, Double>> cloneResult = CloneUtil.readCloneResultCsv(methodResultPath);
			
			for(Integer start : cloneResult.keySet()) {
				MethodNameForJava methodName1 = methodNames.get(start);
				for(Integer end : cloneResult.get(start).keySet()) {
					MethodNameForJava methodName2 = methodNames.get(end);
					///FIXME
					Function function1 = null;
					Function function2 = null;
					FunctionCloneFunction clone = new FunctionCloneFunction(function1, function2);
					addRelation(clone);
				}
			}
		}
	}

}
