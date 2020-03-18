package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.MicroServiceCallMicroServiceRepository;

@Service
public class RelationInserterService {

	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Autowired
	private MicroServiceCallMicroServiceRepository msCallMsRepository;
	
	@Bean
	public void addMsCallMsRelation() {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls = featureOrganizationService.findMsCallMsByTraces(featureOrganizationService.allTraces());
		for(MicroService ms : calls.keySet()) {
			for(MicroService callMs : calls.get(ms).keySet()) {
				MicroServiceCallMicroService call = calls.get(ms).get(callMs);
				msCallMsRepository.save(call);
			}
		}
	}
	
}
