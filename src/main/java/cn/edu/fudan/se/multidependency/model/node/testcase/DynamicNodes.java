package cn.edu.fudan.se.multidependency.model.node.testcase;

import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.NodeType;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.code.Type;

public class DynamicNodes extends Nodes {
	
	public Map<Integer, Scenario> findScenarios() {
		Map<Integer, Scenario> scenarios = (Map<Integer, Scenario>) findNodesMap(NodeType.Scenario);
		return scenarios;
	}
	
	public Scenario findScenarioByName(String scenarioName) {
		for(Scenario s : findScenarios().values()) {
			if(s.getScenarioName().equals(scenarioName)) {
				return s;
			}
		}
		return null;
	}
	
	public Map<Integer, Feature> findFeatures() {
		Map<Integer, Feature> features = (Map<Integer, Feature>) findNodesMap(NodeType.Feature);
		return features;
	}
	
	public Feature findFeatureByFeature(String featureName) {
		for(Feature f : findFeatures().values()) {
			if(f.getFeatureName().equals(featureName)) {
				return f;
			}
		}
		return null;
	}
	
	public Map<Integer, TestCase> findTestCases() {
		Map<Integer, TestCase> testCases = (Map<Integer, TestCase>) findNodesMap(NodeType.TestCase);
		return testCases;
	}
	
	
}
