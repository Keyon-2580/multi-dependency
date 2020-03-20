package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import cn.edu.fudan.se.multidependency.model.relation.Relation;

public interface DynamicCallFunction extends Relation {
	
	String getTraceId();
	
	Integer getTestcaseId();
	
	void setTestcaseId(Integer testcaseId);
	
}
