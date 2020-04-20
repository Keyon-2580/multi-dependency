package cn.edu.fudan.se.multidependency.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import lombok.Data;

public class IssueUtil {
	
	public static void main(String[] args) {
		try {
			System.out.println(test("D:\\git\\multi-dependency\\src\\main\\resources\\git\\train-ticket-issues.json"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Issues test(String issueFilePath) throws Exception {
		Issues result = new Issues();
		File file = new File(issueFilePath);
		JSONArray issues = JSONUtil.extractJSONArray(file);
		for(int i = 0; i < issues.size(); i++) {
			JSONObject issueJson = issues.getJSONObject(i);
			Issue issue = new Issue();
			issue.setUrl(issueJson.getString("url"));
			issue.setRepositoryUrl(issueJson.getString("repository_url"));
			issue.setLabelsUrl(issueJson.getString("labels_url"));
			issue.setCommentsUrl(issueJson.getString("comments_url"));
			issue.setEventsUrl(issueJson.getString("events_url"));
			issue.setBody(issueJson.getString("body"));
			issue.setState(issueJson.getString("state"));
			issue.setCreateTime(issueJson.getString("created_at"));
			issue.setUpdateTime(issueJson.getString("updated_at"));
			issue.setCloseTime(issueJson.getString("closed_at"));
			issue.setTitle(issueJson.getString("title"));
			issue.setHtmlUrl(issueJson.getString("html_url"));
			issue.setIssueId(issueJson.getLongValue("id"));
			issue.setIssueNodeId(issueJson.getString("node_id"));
			result.addIssue(issue);
			
			JSONObject user = issueJson.getJSONObject("user");
			if(user == null) {
				continue;
			}
			Developer developer = new Developer();
			developer.setName(user.getString("login"));
			result.addDeveloper(issue, developer);
		}
		return result;
	}
	
	@Data
	public static class Issues {
		private List<Issue> issues = new ArrayList<>();
		private Map<Issue, Developer> issuePutByDeveloper = new HashMap<>();
		
		public void addIssue(Issue issue) {
			this.issues.add(issue);
		}
		
		public void addDeveloper(Issue issue, Developer developer) {
			this.issuePutByDeveloper.put(issue, developer);
		}
		
	}

}
