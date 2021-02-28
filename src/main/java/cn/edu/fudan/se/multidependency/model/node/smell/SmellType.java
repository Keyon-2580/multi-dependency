package cn.edu.fudan.se.multidependency.model.node.smell;

public enum SmellType {
	Clone,
	CyclicDependency,HubLike,UnstableDependency,
	UnusedComponent,
	ImplicitCrossModuleDependency,GodComponent;
}
