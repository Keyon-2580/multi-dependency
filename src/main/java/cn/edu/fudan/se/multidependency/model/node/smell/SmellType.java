package cn.edu.fudan.se.multidependency.model.node.smell;

public enum SmellType {
	Clone(SmellType.str_CLONE),
	CyclicDependency(SmellType.str_CyclicDependency),
	HubLike(SmellType.str_HubLike),
	UnstableDependency(SmellType.str_UnstableDependency),
	UnusedComponent(SmellType.str_UnusedComponent),
	ImplicitCrossModuleDependency(SmellType.str_ImplicitCrossModuleDependency),
	GodComponent(SmellType.str_GodComponent);

	public static final String str_CLONE = "Clone";

	public static final String str_CyclicDependency = "CyclicDependency";
	public static final String str_HubLike = "HubLike";
	public static final String str_UnstableDependency = "UnstableDependency";
	public static final String str_UnusedComponent = "UnusedComponent";
	public static final String str_ImplicitCrossModuleDependency = "ImplicitCrossModuleDependency";
	public static final String str_GodComponent = "GodComponent";

	private String name;

	SmellType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return getName();
	}
}
