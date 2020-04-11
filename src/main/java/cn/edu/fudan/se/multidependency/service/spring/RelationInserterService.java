package cn.edu.fudan.se.multidependency.service.spring;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.MicroServiceCallMicroServiceRepository;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

@Service
public class RelationInserterService {

	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@Autowired
	private MicroServiceCallMicroServiceRepository msCallMsRepository;
	
	@Autowired
	private FileDependOnFileExtractService fileDependOnFileExtractor;
	
	@Autowired
	private MicroserviceService microserviceService;
	
	/*@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private DynamicAnalyseService dynamicAnalyseService;*/
	
//	@Bean
	public void addMsCallMsRelation() {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls 
			= featureOrganizationService.findMsCallMsByTraces(featureOrganizationService.allTraces()).getCalls();
		for(MicroService ms : calls.keySet()) {
			for(MicroService callMs : calls.get(ms).keySet()) {
				MicroServiceCallMicroService call = calls.get(ms).get(callMs);
				msCallMsRepository.save(call);
			}
		}
	}
	
//	@Bean
	public void printGraphviz() {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls = microserviceService.msCalls();
		Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> dependons = microserviceService.msDependOns();
		try {
			MutableGraph g = mutGraph("example1").setDirected(true);
			for(Map<MicroService, MicroServiceDependOnMicroService> temp : dependons.values()) {
				for(MicroServiceDependOnMicroService depend : temp.values()) {
					MutableNode startNode = mutNode(depend.getStart().getName()).add(Shape.RECTANGLE);
					MutableNode endNode = mutNode(depend.getEnd().getName()).add(Shape.RECTANGLE);
					g.add(startNode.addLink(endNode));
				}
			}
			for(Map<MicroService, MicroServiceCallMicroService> temp : calls.values()) {
				for(MicroServiceCallMicroService call : temp.values()) {
					MicroService start = call.getMs();
					MicroService end = call.getCallMs();
					Map<MicroService, MicroServiceDependOnMicroService> depends = dependons.getOrDefault(start, new HashMap<>());
					MicroServiceDependOnMicroService depend = depends.get(end);
					if(depend == null) {
						MutableNode startNode = mutNode(call.getMs().getName()).add(Shape.RECTANGLE);
						MutableNode endNode = mutNode(call.getCallMs().getName()).add(Shape.RECTANGLE);
						startNode.links().add(startNode.linkTo(endNode).add(Color.RED));
						g.add(startNode);
					}
				}
			}
			Graphviz.fromGraph(g).render(Format.PNG).toFile(new File("D:\\testdot.png"));
//			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
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

}
