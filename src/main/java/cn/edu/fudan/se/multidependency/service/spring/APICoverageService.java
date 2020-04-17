package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanInstanceOfRestfulAPI;

@Service
public class APICoverageService {
	
	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;
	
	public Map<MicroService, TestCaseCoverageMicroServiceAPIs> apiCoverage(Collection<TestCase> testCases) {
		Map<MicroService, TestCaseCoverageMicroServiceAPIs> result = new HashMap<>();
		
		for(MicroService ms : featureOrganizationService.allMicroServices()) {
			TestCaseCoverageMicroServiceAPIs coverage = new TestCaseCoverageMicroServiceAPIs();
			coverage.addTestCases(testCases);
			coverage.setMicroService(ms);
			List<RestfulAPI> apis = staticAnalyseService.findMicroServiceContainRestfulAPI(ms);
			for(RestfulAPI api : apis) {
				coverage.addCallRestfulAPITimes(api, 0);
			}
			result.put(ms, coverage);
		}
		
		Iterable<Span> relatedSpans = featureOrganizationService.relatedSpan(testCases);
		int i = 0;
		for(Span span : relatedSpans) {
			System.out.println(i++);
			MicroService microService = featureOrganizationService.spanBelongToMicroservice(span);
			assert(microService != null);
			TestCaseCoverageMicroServiceAPIs coverage = result.get(microService);
			assert(coverage != null);
			
			SpanInstanceOfRestfulAPI instanceOf = dynamicAnalyseService.findSpanBelongToAPI(span);
			if(instanceOf == null) {
				System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee " + span.getApiFunctionName() + " " + span.getSpanId());
				continue;
			}
			RestfulAPI api = instanceOf.getApi();
			coverage.addCallRestfulAPITimes(api, 1);
		}
		
		return result;
	}
	
}
