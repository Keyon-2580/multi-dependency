package cn.edu.fudan.se.multidependency.model;

public enum IssueType {
	Bug, Improvement, NewFeature,DefaultIssue;

	public static IssueType valueOfIssue(String type) {
		switch(type) {
			case "Bug":
				return Bug;
			case "Improvement":
				return Improvement;
			case "New Feature":
				return NewFeature;
		}
		return DefaultIssue;
	}

	/**
	 * 代码总规模， 包括空行和注释
	 */
	public static final String str_Bug = "Bug";
	public static final String str_Improvement = "Improvement";
	public static final String str_New_Feature = "NewFeature";
	public static final String str_Default_Issue = "DefaultIssue";

}
