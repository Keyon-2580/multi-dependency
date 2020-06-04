package cn.edu.fudan.se.multidependency.service.spring.show;

import java.util.Collection;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;

public interface CloneShowService {

//	JSONObject clonesToCytoscape(Collection<? extends CloneRelation> groupRelations);
	
	JSONObject clonesGroupsToCytoscape(Collection<Integer> groups, CloneLevel level, boolean showGroupNode, boolean removeFileLevelClone, boolean removeDataClass);
	
}
