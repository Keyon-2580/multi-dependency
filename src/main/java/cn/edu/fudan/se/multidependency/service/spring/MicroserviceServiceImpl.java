package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanCallSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanInstanceOfRestfulAPI;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;
import cn.edu.fudan.se.multidependency.repository.node.microservice.MicroServiceRepository;
import cn.edu.fudan.se.multidependency.repository.node.microservice.SpanRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.MicroServiceCallMicroServiceRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.MicroServiceCreateSpanRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.MicroServiceDependOnMicroServiceRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.SpanCallSpanRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.SpanInstanceOfRestfulAPIRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.SpanStartWithFunctionRepository;
import cn.edu.fudan.se.multidependency.service.spring.data.Clone;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneValueCalculatorForMicroService;
import cn.edu.fudan.se.multidependency.service.spring.data.Fan_IO;
import cn.edu.fudan.se.multidependency.utils.MicroServiceUtil;
import cn.edu.fudan.se.multidependency.utils.PageUtil;
import cn.edu.fudan.se.multidependency.utils.ZTreeUtil.ZTreeNode;

@Service
public class MicroserviceServiceImpl implements MicroserviceService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceServiceImpl.class);
	
	@Autowired
	private SpanCallSpanRepository spanCallSpanRepository;
	
	@Autowired
	private MicroServiceCreateSpanRepository microserviceCreateSpanRepository;
	
	@Autowired
	private MicroServiceRepository microServiceRepository;
	
	@Autowired
	private SpanStartWithFunctionRepository spanStartWithFunctionRepository;
	
	@Autowired
	private SpanRepository spanRepository;
	
	@Autowired
	private MicroServiceCallMicroServiceRepository microServiceCallMicroServiceRepository;
	
	@Autowired
	private MicroServiceDependOnMicroServiceRepository microServiceDependOnMicroServiceRepository;	
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;

	@Autowired
	private SpanInstanceOfRestfulAPIRepository spanInstanceOfRestfulAPIRepository;
    
    @Autowired
    private ContainRelationService containRelationService;
    
    @Autowired
    private CacheService cache;
	
	@Override
	public List<SpanCallSpan> findSpanCallSpans(Span span) {
		return spanCallSpanRepository.findSpanCallSpansBySpanId(span.getSpanId());
	}

	@Override
	public MicroServiceCreateSpan findMicroServiceCreateSpan(Span span) {
		return microserviceCreateSpanRepository.findMicroServiceCreateSpan(span.getSpanId());
	}

	private Map<String, MicroService> allMicroServicesGroupByServiceNameCache = new HashMap<>();
	@Override
	public Map<String, MicroService> findAllMicroService() {
		if(allMicroServicesGroupByServiceNameCache.size() == 0) {
			microServiceRepository.findAll().forEach(ms -> {
				allMicroServicesGroupByServiceNameCache.put(ms.getName(), ms);
				cache.cacheNodeById(ms);
			});
		}
		return allMicroServicesGroupByServiceNameCache;
	}

	@Override
	public MicroService findMicroServiceById(Long id) {
		Node node = cache.findNodeById(id);
		MicroService result = node == null ? microServiceRepository.findById(id).get() : (node instanceof MicroService ? (MicroService) node : microServiceRepository.findById(id).get());
		cache.cacheNodeById(result);
		return result;
	}

	@Override
	public SpanCallSpan findSpanCallSpanById(Long id) {
		return spanCallSpanRepository.findById(id).get();
	}

	@Override
	public SpanStartWithFunction findSpanStartWithFunctionByTraceIdAndSpanId(String requestTraceId,
			String requestSpanId) {
		return spanStartWithFunctionRepository.findSpanStartWIthFunctionByTraceIdAndSpanId(requestTraceId, requestSpanId);
	}

	@Override
	public Span findSpanById(Long id) {
		return spanRepository.findById(id).get();
	}

	@Override
	public List<Span> findSpansByMicroserviceAndTraceId(MicroService ms, String traceId) {
		List<MicroServiceCreateSpan> createSpans = microserviceCreateSpanRepository.findMicroServiceCreateSpansInTrace(ms.getId(), traceId);
		List<Span> result = new ArrayList<>();
		for(MicroServiceCreateSpan createSpan : createSpans) {
			result.add(createSpan.getSpan());
		}
		return result;
	}
	
	@Override
	public Trace findTraceByFeature(Feature feature) {
		return null;
	}

	private Iterable<MicroServiceCallMicroService> msCallMsCache = null;
	/**
	 * 依赖于RelationInserterService
	 */
	@Override
	public Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls() {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> result = new HashMap<>();
		Iterable<MicroServiceCallMicroService> list = msCallMsCache == null ? microServiceCallMicroServiceRepository.findAll() : msCallMsCache;
		msCallMsCache = list;
		for(MicroServiceCallMicroService call : list) {
			MicroService start = call.getMs();
			Map<MicroService, MicroServiceCallMicroService> temp = result.getOrDefault(start, new HashMap<>());
			temp.put(call.getCallMs(), call);
			result.put(start, temp);
		}
		return result;
	}

	@Override
	public Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns() {
		Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> result = new HashMap<>();
		Iterable<MicroServiceDependOnMicroService> list = microServiceDependOnMicroServiceRepository.findAll();
		for(MicroServiceDependOnMicroService call : list) {
			MicroService start = call.getStart();
			Map<MicroService, MicroServiceDependOnMicroService> temp = result.getOrDefault(start, new HashMap<>());
			temp.put(call.getEnd(), call);
			result.put(start, temp);
		}
		return result;
	}

	@Override
	public boolean isMicroServiceCall(MicroService start, MicroService end) {
		return MicroServiceUtil.isMicroServiceCall(start, end, msCalls());
	}

	@Override
	public boolean isMicroServiceDependOn(MicroService start, MicroService end) {
		return MicroServiceUtil.isMicroServiceDependOn(start, end, msDependOns());
	}

	@Override
	public Map<MicroService, List<RestfulAPI>> microServiceContainsAPIs() {
		Map<MicroService, List<RestfulAPI>> result = new HashMap<>();
		for(MicroService ms : findAllMicroService().values()) {
			List<RestfulAPI> temp = new ArrayList<>();
			Iterable<RestfulAPI> apis = containRelationService.findMicroServiceContainRestfulAPI(ms);
			for(RestfulAPI api : apis) {
				temp.add(api);
			}
			result.put(ms, temp);
		}
		return result;
	}

	
	
	@Override
	public Iterable<MicroServiceCallMicroService> findAllMicroServiceCallMicroServices() {
		LOGGER.info("findAllMicroServiceCallMicroServices");
		return microServiceCallMicroServiceRepository.findAll();
	}

	@Override
	public void deleteAllMicroServiceCallMicroService() {
		LOGGER.info("deleteAllMicroServiceCallMicroService");
		microServiceCallMicroServiceRepository.deleteAll();
	}

	@Override
	public void saveMicroServiceCallMicroService(MicroServiceCallMicroService call) {
		LOGGER.info("saveMicroServiceCallMicroServic");
		microServiceCallMicroServiceRepository.save(call);
	}
	
	@Override
	public SpanInstanceOfRestfulAPI findSpanBelongToAPI(Span span) {
		return spanInstanceOfRestfulAPIRepository.findSpanBelongToAPI(span.getSpanId());
	}

	@Override
	public Map<Span, SpanInstanceOfRestfulAPI> findAllSpanInstanceOfRestfulAPIs() {
		Map<Span, SpanInstanceOfRestfulAPI> result = new HashMap<>();
		Iterable<SpanInstanceOfRestfulAPI> instanceOfs = spanInstanceOfRestfulAPIRepository.findAll();
		for(SpanInstanceOfRestfulAPI instanceOf : instanceOfs) {
			result.put(instanceOf.getSpan(), instanceOf);
		}
		return result;
	}
	
	private Clone<MicroService> hasClone(Map<MicroService, Map<MicroService, Clone<MicroService>>> msToMsClones, MicroService ms1, MicroService ms2) {
		Map<MicroService, Clone<MicroService>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
		Clone<MicroService> clone = ms1ToClones.get(ms2);
		if(clone != null) {
			return clone;
		}
		Map<MicroService, Clone<MicroService>> ms2ToClones = msToMsClones.getOrDefault(ms2, new HashMap<>());
		clone = ms2ToClones.get(ms1);
		return clone;
	}

	@Override
	public Iterable<Clone<MicroService>> findMicroServiceClone(Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode) {
		Iterable<Clone<Project>> projectClones = staticAnalyseService.findProjectClone(functionClones, removeSameNode);
		List<Clone<MicroService>> result = new ArrayList<>();
		Map<MicroService, Map<MicroService, Clone<MicroService>>> msToMsClones = new HashMap<>();
		for(Clone<Project> projectClone : projectClones) {
			Project project1 = projectClone.getNode1();
			Project project2 = projectClone.getNode2();
			if(removeSameNode && project1.equals(project2)) {
				continue;
			}
			MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
			MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
			if(ms1 == null || ms2 == null) {
				continue;
			}
			if(removeSameNode && ms1.equals(ms2)) {
				continue;
			}
			Clone<MicroService> clone = hasClone(msToMsClones, ms1, ms2);
			if(clone == null) {
				clone = new Clone<MicroService>();
				clone.setNode1(ms1);
				clone.setNode2(ms2);
				result.add(clone);
				CloneValueCalculatorForMicroService calculator = new CloneValueCalculatorForMicroService();
				Iterable<Function> functions1 = containRelationService.findMicroServiceContainFunctions(ms1);
				Iterable<Function> functions2 = containRelationService.findMicroServiceContainFunctions(ms2);
				calculator.addFunctions(functions1, ms1);
				calculator.addFunctions(functions2, ms2);
				clone.setCalculator(calculator);
			}
			clone.addChildren(projectClone.getChildren());
			Map<MicroService, Clone<MicroService>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
			ms1ToClones.put(ms2, clone);
			msToMsClones.put(ms1, ms1ToClones);
			
		}
		return result;
	}

	@Override
	public CallLibrary<MicroService> findMicroServiceCallLibraries(MicroService ms) {
		CallLibrary<MicroService> result = new CallLibrary<MicroService>();
		result.setCaller(ms);
		Iterable<Project> projects = containRelationService.findMicroServiceContainProjects(ms);
		for(Project project : projects) {
			CallLibrary<Project> projectCallLibrary = staticAnalyseService.findProjectCallLibraries(project);
			Map<Library, Set<LibraryAPI>> callLibraryToAPIs = projectCallLibrary.getCallLibraryToAPIs();
			for(Library lib : callLibraryToAPIs.keySet()) {
				Set<LibraryAPI> apis = callLibraryToAPIs.getOrDefault(lib, new HashSet<>());
				for(LibraryAPI api : apis) {
					result.addLibraryAPI(api, lib, projectCallLibrary.timesOfCallAPI(api));
				}
			}
		}
		return result;
	}

	@Override
	public Iterable<CallLibrary<MicroService>> findAllMicroServiceCallLibraries() {
		List<CallLibrary<MicroService>> result = new ArrayList<>();
		for(MicroService ms : findAllMicroService().values()) {
			result.add(findMicroServiceCallLibraries(ms));
		}
		return result;
	}
	
	private Map<Integer, List<MicroService>> pageMicroServicesCache = new ConcurrentHashMap<>();
	@Override
	public List<MicroService> queryAllMicroServicesByPage(int page, int size, String... sortByProperties) {
		List<MicroService> result = pageMicroServicesCache.get(page);
		if(result == null || result.size() == 0) {
			result = new ArrayList<>();
			Pageable pageable = PageUtil.generatePageable(page, size, sortByProperties);
			Page<MicroService> pageMS = microServiceRepository.findAll(pageable);
			for(MicroService ms : pageMS) {
				result.add(ms);
			}
			pageMicroServicesCache.put(page, result);
		}
		return result;
	}

	@Override
	public List<ZTreeNode> queryMicroServiceContainProjectsZTree(Iterable<MicroService> mss) {
		List<ZTreeNode> result = new ArrayList<>();
		for(MicroService ms : mss) {
			ZTreeNode msNode = new ZTreeNode(ms, true);
			Iterable<Project> projects = containRelationService.findMicroServiceContainProjects(ms);
			for(Project project : projects) {
				ZTreeNode projectNode = new ZTreeNode(project, true);
				msNode.addChild(projectNode);
			}
			
			Iterable<RestfulAPI> apis = containRelationService.findMicroServiceContainRestfulAPI(ms);
			ZTreeNode apisNode = null;
			for(RestfulAPI api : apis) {
				if(apisNode == null) {
					apisNode = new ZTreeNode(ZTreeNode.DEFAULT_ID, "RestfulAPI", false, ZTreeNode.DEFAULT_TYPE, true);
					msNode.addChild(apisNode);
				}
				apisNode.addChild(new ZTreeNode(api, false));
			}
			
			result.add(msNode);
		}
		return result;
	}

	@Override
	public long countOfAllMicroServices() {
		return microServiceRepository.count();
	}

	@Override
	public Fan_IO<MicroService> microServiceDependencyFanIOInDynamicCall(MicroService ms) {
		if(ms == null) {
			return null;
		}
		Fan_IO<MicroService> result = new Fan_IO<>(ms);
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> allCalls = msCalls();
		for(Map.Entry<MicroService, Map<MicroService, MicroServiceCallMicroService>> calls : allCalls.entrySet()) {
			MicroService caller = calls.getKey();
			for(MicroService called : calls.getValue().keySet()) {
				if(caller.equals(ms)) {
					result.addFanOut(called);
				}
				if(called.equals(ms)) {
					result.addFanIn(caller);
				}
			}
		}
		return result;
	}

}
