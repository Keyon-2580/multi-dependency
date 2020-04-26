package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCreateSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanCallSpan;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanInstanceOfRestfulAPI;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanStartWithFunction;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.lib.FunctionCallLibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;
import cn.edu.fudan.se.multidependency.repository.node.microservice.MicroServiceRepository;
import cn.edu.fudan.se.multidependency.repository.node.microservice.SpanRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.MicroServiceCallMicroServiceRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.MicroServiceCreateSpanRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.MicroServiceDependOnMicroServiceRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.SpanCallSpanRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.SpanInstanceOfRestfulAPIRepository;
import cn.edu.fudan.se.multidependency.repository.relation.microservice.SpanStartWithFunctionRepository;
import cn.edu.fudan.se.multidependency.utils.ProjectUtil;

@Service
public class MicroserviceServiceImpl implements MicroserviceService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceServiceImpl.class);
	
	@Autowired
	private SpanCallSpanRepository spanCallSpanRepository;
	
	@Autowired
	private ContainRepository containRepository;
	
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
	
	@Override
	public List<Span> findSpansByTrace(Trace trace) {
		List<Span> spans = containRepository.findTraceContainSpansByTraceId(trace.getTraceId());
		spans.sort(new Comparator<Span>() {
			@Override
			public int compare(Span o1, Span o2) {
				return o1.getOrder().compareTo(o2.getOrder());
			}
		});
		return spans;
	}

	@Override
	public List<SpanCallSpan> findSpanCallSpans(Span span) {
		return spanCallSpanRepository.findSpanCallSpansBySpanId(span.getSpanId());
	}

	@Override
	public MicroServiceCreateSpan findMicroServiceCreateSpan(Span span) {
		return microserviceCreateSpanRepository.findMicroServiceCreateSpan(span.getSpanId());
	}

	private Map<String, MicroService> allMicroServicesGroupByServiceNameCache = new HashMap<>();
	private Map<Long, MicroService> allMicroServicesGroupByServiceIdCache = new HashMap<>();
	@Override
	public Map<String, MicroService> findAllMicroService() {
		if(allMicroServicesGroupByServiceNameCache.size() == 0) {
			microServiceRepository.findAll().forEach(ms -> {
				allMicroServicesGroupByServiceNameCache.put(ms.getName(), ms);
				allMicroServicesGroupByServiceIdCache.put(ms.getId(), ms);
			});
		}
		return allMicroServicesGroupByServiceNameCache;
	}

	@Override
	public MicroService findMicroServiceById(Long id) {
		MicroService result = allMicroServicesGroupByServiceIdCache.get(id);
		if(result == null) {
			result = microServiceRepository.findById(id).get();
			allMicroServicesGroupByServiceIdCache.put(id, result);
		}
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

	@Override
	public Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls() {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> result = new HashMap<>();
		Iterable<MicroServiceCallMicroService> list = microServiceCallMicroServiceRepository.findAll();
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
		return ProjectUtil.isMicroServiceCall(start, end, msCalls());
	}

	@Override
	public boolean isMicroServiceDependOn(MicroService start, MicroService end) {
		return ProjectUtil.isMicroServiceDependOn(start, end, msDependOns());
	}

	@Override
	public Map<MicroService, List<RestfulAPI>> microServiceContainsAPIs() {
		Map<MicroService, List<RestfulAPI>> result = new HashMap<>();
		for(MicroService ms : findAllMicroService().values()) {
			List<RestfulAPI> apis = containRepository.findMicroServiceContainRestfulAPI(ms.getId());
			result.put(ms, apis);
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
	public CallLibrary microServiceCallLibraries(MicroService microService) {
		CallLibrary result = new CallLibrary();
		result.setCaller(microService);
	
		Iterable<FunctionCallLibraryAPI> allCalls = staticAnalyseService.findAllFunctionCallLibraryAPIs();
		
		
		return result;
	}

	@Override
	public List<RestfulAPI> findMicroServiceContainRestfulAPI(MicroService microService) {
		return containRepository.findMicroServiceContainRestfulAPI(microService.getId());
	}

	@Override
	public Iterable<Project> microServiceContainProjects(MicroService ms) {
		return null;
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

}
