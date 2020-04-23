package cn.edu.fudan.se.multidependency.service.nospring.git;

import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class IssueExtractor {

    private String issueFilePath;

    public IssueExtractor(String issueFilePath) {
        this.issueFilePath = issueFilePath;
    }

    public Map<Integer,Issue> extract() throws Exception {
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
