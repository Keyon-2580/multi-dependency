package cn.edu.fudan.se.multidependency.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FeatureExecuteTrace;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;

public class FeatureInserter extends ExtractorForNodesAndRelationsImpl {
	
	public FeatureInserter(String featureConfigPath) {
		this.featureConfigPath = featureConfigPath;
	}
	
	private String featureConfigPath;
	
	private void extract() throws Exception {
		JSONObject featureJson = JSONUtil.extractJson(new File(featureConfigPath));
		System.out.println(featureJson);
		JSONArray featuresArray = featureJson.getJSONArray("features");
		for(int i = 0; i < featuresArray.size(); i++) {
			JSONObject featureTemp = featuresArray.getJSONObject(i);
			Feature feature = new Feature();
			feature.setEntityId(generateEntityId());
			feature.setFeatureId(featureTemp.getInteger("id"));
			feature.setFeatureName(featureTemp.getString("name"));
			feature.setDescription(featureTemp.getString("description"));
			String traceId = featureTemp.getString("traceId");
			feature.setTraceId(traceId);
			Trace trace = this.getNodes().findTraces().get(traceId);
			if(trace == null) {
				throw new Exception("traceId " + traceId + " 不存在");
			}
			FeatureExecuteTrace featureExecuteTrace = new FeatureExecuteTrace(feature, trace);
			addNode(feature, null);
			addRelation(featureExecuteTrace);
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
