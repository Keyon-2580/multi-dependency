package cn.edu.fudan.se.multidependency.service.spring.clone;

import java.util.function.Predicate;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;

public class PredicateForDataFile implements Predicate<CloneGroup> {
	
	private StaticAnalyseService staticAnalyseService;
	
	public PredicateForDataFile(StaticAnalyseService staticAnalyseService) {
		this.staticAnalyseService = staticAnalyseService;
	}
	
	/**
	 * 如果该克隆组内的文件只有一个Type并且这个Type是DataType，则去除
	 */
	@Override
	public boolean test(CloneGroup t) {
		boolean flag = true;
		for(CodeNode node : t.getNodes()) {
			if(!(node instanceof ProjectFile)) {
				return false;
			}
			if(!staticAnalyseService.isDataFile((ProjectFile) node)) {
				flag = false;
				break;
			}
		}
		return flag;
	}

}
