package cn.edu.fudan.se.multidependency.service.spring.show;

import java.util.Collection;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelation;

public interface CloneShowService {

	JSONObject clonesToCytoscape(Collection<? extends CloneRelation> groupRelations);
	
}
