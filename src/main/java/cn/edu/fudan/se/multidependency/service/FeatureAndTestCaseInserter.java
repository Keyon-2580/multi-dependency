package cn.edu.fudan.se.multidependency.service;

import java.io.File;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;

public class FeatureAndTestCaseInserter extends ExtractorForNodesAndRelationsImpl {
	
	public FeatureAndTestCaseInserter(String featureConfigPath) {
		this.featureConfigPath = featureConfigPath;
	}
	
	private String featureConfigPath;
	
	private void extract() throws Exception {
		JSONObject featureJsonFile = JSONUtil.extractJson(new File(featureConfigPath));
		System.out.println(featureJsonFile);
		JSONArray featuresArray = featureJsonFile.getJSONArray("features");
		JSONArray testcasesArray = featureJsonFile.getJSONArray("testcases");
		for(int i = 0; i < featuresArray.size(); i++) {
			JSONObject featureTemp = featuresArray.getJSONObject(i);
			Feature feature = new Feature();
			feature.setEntityId(generateEntityId());
			feature.setFeatureId(featureTemp.getInteger("id"));
			feature.setFeatureName(featureTemp.getString("name"));
			feature.setDescription(featureTemp.getString("description"));
			addNode(feature, null);
		}
		
		for(int i = 0; i < testcasesArray.size(); i++) {
			JSONObject testcaseTemp = testcasesArray.getJSONObject(i);
			TestCase testcase = new TestCase();
			testcase.setEntityId(generateEntityId());
			testcase.setTestCaseId(testcaseTemp.getLong("id"));
			testcase.setInputContent(testcaseTemp.getString("input"));
			testcase.setSuccess(testcaseTemp.getBooleanValue("success"));
			testcase.setTestCaseName(testcaseTemp.getString("name"));
			addNode(testcase, null);
			
			JSONArray featureIds = testcaseTemp.getJSONArray("features");
			for(int j = 0; j < featureIds.size(); j++) {
				Integer featureId = featureIds.getInteger(j);
				Feature feature = this.getNodes().findFeatures().get(featureId);
				if(feature == null) {
					throw new Exception("featureId " + featureId + " 不存在");
				}
				TestCaseExecuteFeature testCaseExecuteFeature = new TestCaseExecuteFeature(testcase, feature);
				addRelation(testCaseExecuteFeature);
			}
			JSONArray traceIds = testcaseTemp.getJSONArray("traces");
			for(int j = 0; j < traceIds.size(); j++) {
				String traceId = traceIds.getString(j);
				Trace trace = this.getNodes().findTraces().get(traceId);
				if(trace == null) {
					throw new Exception("traceId " + traceId + " 不存在");
				}
				TestCaseRunTrace testcaseRunTrace = new TestCaseRunTrace(testcase, trace);
				addRelation(testcaseRunTrace);
				
			}
			
		}
	}
	
	@Override
	public void addNodesAndRelations() {
		try {
			extract();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
