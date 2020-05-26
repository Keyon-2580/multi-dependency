package cn.edu.fudan.se.multidependency.utils.config;

import lombok.Data;

@Data
public class GitConfig {
	private String path;
	private String commitIdFrom;
	private String commitIdTo;
	private String issueFilePath;
}
