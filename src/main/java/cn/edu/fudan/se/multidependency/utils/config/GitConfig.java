package cn.edu.fudan.se.multidependency.utils.config;

import lombok.Data;

@Data
public class GitConfig {
	private String path;
	private boolean specifyCommitRange;
	private boolean specifyByCommitId;
	private String commitIdFrom;
	private String commitIdTo;
	private String commitTimeSince;
	private String commitTimeUntil;
	private boolean isAnalyseIssue;
	private String issueFilePath;
	private boolean calculateCochange;
}
