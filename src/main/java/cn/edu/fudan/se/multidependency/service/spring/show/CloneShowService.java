package cn.edu.fudan.se.multidependency.service.spring.show;

import java.util.Collection;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.service.spring.data.HistogramWithProjectsSize;

public interface CloneShowService {

	JSONObject clonesGroupsToCytoscape(Language language, Collection<CloneGroup> groups, CloneLevel level, boolean showGroupNode, boolean removeFileLevelClone, boolean removeDataClass);
	
	Collection<HistogramWithProjectsSize> withProjectsSizeToHistogram(Language language, CloneLevel level, boolean removeDataClass, boolean removeFileClone);
	
}
