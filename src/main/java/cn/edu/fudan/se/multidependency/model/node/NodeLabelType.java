package cn.edu.fudan.se.multidependency.model.node;

import java.util.ArrayList;
import java.util.List;

public enum NodeLabelType {
	Project, Package, ProjectFile,
	Namespace, Type, Function, Variable, Snippet,
	Library, LibraryAPI, License,
	MicroService, RestfulAPI, Span,
	Scenario, TestCase, Feature, Trace, Bug,
	GitRepository, Branch, Commit, Issue, Label, Developer,
	CloneGroup;
	
	public List<String> indexes() {
		List<String> result = new ArrayList<>();
		switch(this) {
		case Branch:
			break;
		case Bug:
			break;
		case CloneGroup:
			result.add("cloneLevel");
			result.add("name");
			break;
		case Commit:
			result.add("commitId");
			break;
		case Developer:
			break;
		case Feature:
			result.add("name");
			break;
		case Function:
			break;
		case GitRepository:
			break;
		case Issue:
			break;
		case Label:
			break;
		case Library:
			break;
		case LibraryAPI:
			break;
		case License:
			break;
		case MicroService:
			result.add("name");
			break;
		case Namespace:
			break;
		case Package:
			result.add("directoryPath");
			break;
		case Project:
			result.add("name");
			result.add("language");
			break;
		case ProjectFile:
			result.add("path");
			break;
		case RestfulAPI:
			break;
		case Scenario:
			break;
		case Snippet:
			break;
		case Span:
			break;
		case TestCase:
			break;
		case Trace:
			result.add("traceId");
			break;
		case Type:
			break;
		case Variable:
			break;
		default:
			break;
		}
		return result;
	}
	
}
