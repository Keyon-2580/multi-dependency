package cn.edu.fudan.se.multidependency.model;

public enum IssueType {
	Bug(IssueType.str_Bug),
	Improvement(IssueType.str_Improvement),
	NewFeature(IssueType.str_New_Feature),
	DefaultIssue(IssueType.str_Default_Issue);

	public static String typeOfIssue(String type) {
		switch(type) {
			case "Bug":
				return str_Bug;
			case "Improvement":
				return str_Improvement;
			case "New Feature":
				return str_New_Feature;
		}
		return str_Default_Issue;
	}

	/**
	 * 代码总规模， 包括空行和注释
	 */
	public static final String str_Bug = "Bug";
	public static final String str_Improvement = "Improvement";
	public static final String str_New_Feature = "NewFeature";
	public static final String str_Default_Issue = "DefaultIssue";

	public static final String str_DEFAULT = "DEFAULT";

	private String name;

	IssueType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return getName();
	}
}
