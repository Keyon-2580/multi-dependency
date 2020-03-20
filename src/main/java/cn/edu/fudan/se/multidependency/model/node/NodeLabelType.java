package cn.edu.fudan.se.multidependency.model.node;

public enum NodeLabelType {
	ProjectFile, Function, Package, Type, Project, Variable, Namespace,
	Scenario, TestCase, Feature, Commit, Issue,
	Trace, Span, MicroService, Bug, License;
	
	public String indexName() {
		switch(this) {
		case Commit:
			return cn.edu.fudan.se.multidependency.model.node.testcase.Commit.LABEL_INDEX;
		case Feature:
			return cn.edu.fudan.se.multidependency.model.node.testcase.Feature.LABEL_INDEX;
		case Function:
			return cn.edu.fudan.se.multidependency.model.node.code.Function.LABEL_INDEX;
		case Issue:
			return cn.edu.fudan.se.multidependency.model.node.testcase.Issue.LABEL_INDEX;
		case MicroService:
			return cn.edu.fudan.se.multidependency.model.node.microservice.MicroService.LABEL_INDEX;
		case Namespace:
			return cn.edu.fudan.se.multidependency.model.node.code.Namespace.LABEL_INDEX;
		case Package:
			return cn.edu.fudan.se.multidependency.model.node.Package.LABEL_INDEX;
		case Project:
			return cn.edu.fudan.se.multidependency.model.node.Project.LABEL_INDEX;
		case ProjectFile:
			return cn.edu.fudan.se.multidependency.model.node.ProjectFile.LABEL_INDEX;
		case Scenario:
			return cn.edu.fudan.se.multidependency.model.node.testcase.Scenario.LABEL_INDEX;
		case Span:
			return cn.edu.fudan.se.multidependency.model.node.microservice.Span.LABEL_INDEX;
		case TestCase:
			return cn.edu.fudan.se.multidependency.model.node.testcase.TestCase.LABEL_INDEX;
		case Trace:
			return cn.edu.fudan.se.multidependency.model.node.testcase.Trace.LABEL_INDEX;
		case Type:
			return cn.edu.fudan.se.multidependency.model.node.code.Type.LABEL_INDEX;
		case Variable:
			return cn.edu.fudan.se.multidependency.model.node.code.Variable.LABEL_INDEX;
		case Bug:
			return cn.edu.fudan.se.multidependency.model.node.testcase.Bug.LABEL_INDEX;
		case License:
			return cn.edu.fudan.se.multidependency.model.node.lib.License.LABEL_INDEX;
		default:
			return null;
		}
	}
	
}
