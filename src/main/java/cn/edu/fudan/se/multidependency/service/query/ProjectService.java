package cn.edu.fudan.se.multidependency.service.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.data.PackageStructure;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import org.springframework.web.bind.annotation.RequestParam;

public interface ProjectService {
	String getAbsolutePath(Project project);

	void setAbsolutePath(Project project, String path);

	JSONArray getHasJson(Collection<ProjectFile> clonefiles, List<PackageStructure> childrenPackages, String showType);

	JSONArray getMultipleProjectsGraphJson(JSONObject dataList);

	JSONObject cloneGraphAndTableOfChildrenPackages(long package1Id, long package2Id);

	JSONObject getAllProjectsCloneLinks();
}