package cn.edu.fudan.se.multidependency.service.spring;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.MicroServiceCallMicroServiceRepository;

@Service
public class RelationInserterService {

	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Autowired
	private MicroServiceCallMicroServiceRepository msCallMsRepository;
	
	@Autowired
	private FileDependOnFileExtractService fileDependOnFileExtractor;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;
	
//	@Bean
	public void addMsCallMsRelation() {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls = featureOrganizationService.findMsCallMsByTraces(featureOrganizationService.allTraces());
		for(MicroService ms : calls.keySet()) {
			for(MicroService callMs : calls.get(ms).keySet()) {
				MicroServiceCallMicroService call = calls.get(ms).get(callMs);
				msCallMsRepository.save(call);
			}
		}
	}
	
//	@Bean
	public void testFileDependOnFileExtractService() {
		try {
			fileDependOnFileExtractor.extractFileDependOnFiles();
			fileDependOnFileExtractor.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*Map<Long, Project> allProjects = staticAnalyseService.allProjects();
		for(Long id : allProjects.keySet()) {
			try {
				fileDependOnFileExtractor.setProject(id);
				System.out.println("extractFileDependOnFiles");
				fileDependOnFileExtractor.extractFileDependOnFiles();
				fileDependOnFileExtractor.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
	}

	@Bean
	public void findFunctionCallFunctionNotDynamicCalled() {
		System.out.println("findFunctionCallFunctionNotDynamicCalled");
		// 所有静态调用
		Map<Function, List<FunctionCallFunction>> staticCalls = staticAnalyseService.findAllFunctionCallRelationsGroupByCaller();
		
		// 没有被动态调用的静态调用
		Map<Function, List<FunctionCallFunction>> notDynamicCalls 
			= dynamicAnalyseService.findFunctionCallFunctionNotDynamicCalled(true, null);
		
		for(Function caller : staticCalls.keySet()) {
			List<FunctionCallFunction> staticCall = staticCalls.get(caller);
			List<FunctionCallFunction> notDynamicCall = notDynamicCalls.get(caller);
//			System.out.println(caller.getFunctionName() + " " + notDynamicCall);
		}
		
	}
	
}
