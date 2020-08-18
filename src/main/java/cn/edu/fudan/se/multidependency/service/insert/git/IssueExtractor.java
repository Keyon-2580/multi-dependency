package cn.edu.fudan.se.multidependency.service.insert.git;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;

public class IssueExtractor {

    private Collection<String> issueFilePathes;

    public IssueExtractor(Collection<String> issueFilePathes) {
    	this.issueFilePathes = issueFilePathes;
    }
    
    public Map<Integer, Issue> extract() throws Exception {
    	Map<Integer, Issue> result = new HashMap<>();
    	for(String issueFilePath : this.issueFilePathes) {
    		result.putAll(extract(issueFilePath));
    	}
    	return result;
    }

    private Map<Integer, Issue> extract(String issueFilePath) throws Exception {
        Map<Integer,Issue> result = new HashMap<>();
        File file = new File(issueFilePath);
        JSONArray issues = JSONUtil.extractJSONArray(file);
        for(int i = 0; i < issues.size(); i++) {
            JSONObject issueJson = issues.getJSONObject(i);
            Issue issue = new Issue(issueJson.getInteger("number"), issueJson.getString("title"), issueJson.getString("state"),
                    issueJson.getString("html_url"), issueJson.getString("created_at"), issueJson.getString("updated_at"),
                    issueJson.getString("closed_at"), issueJson.getString("body"));
            JSONObject user = issueJson.getJSONObject("user");
            if(user == null) {
                continue;
            }
            issue.setDeveloperName(user.getString("login"));
            result.put(issue.getNumber(),issue);
        }
        return result;
    }
}
