package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.model.MetricType;
import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.relation.Has;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.relation.HasRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.data.SmellMetric;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SmellMetricCalculatorService {

	@Autowired
	private CacheService cache;

	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private HasRepository hasRepository;

	@Autowired
	private ContainRelationService containRelationService;

	public Map<Smell, Metric> generateSmellMetricNodesInFileLevel(){
		Map<Smell, Metric> result = new HashMap<>();
		Map<Smell, SmellMetric> smellMetricsMap = calculateSmellMetricInFileLevel();
		if(smellMetricsMap != null && !smellMetricsMap.isEmpty()){
			smellMetricsMap.forEach((smell, smellMetric) ->{
				Metric metric = new Metric();
				metric.setEntityId((long) -1);
				metric.setLanguage(smell.getLanguage());
				metric.setName(smell.getName());
				metric.setNodeType(smell.getNodeType());

				Map<MetricType, Object> metricValues =  new HashMap<>();
				metricValues.put(MetricType.SIZE, smellMetric.getStructureMetric().getSize());
				metricValues.put(MetricType.NOC, smellMetric.getStructureMetric().getNoc());
				metricValues.put(MetricType.NOM, smellMetric.getStructureMetric().getNom());
				metricValues.put(MetricType.LOC, smellMetric.getStructureMetric().getLoc());

				SmellMetric.EvolutionMetric evolutionMetric = smellMetric.getEvolutionMetric();
				if (evolutionMetric != null){
					metricValues.put(MetricType.COMMITS, evolutionMetric.getCommits());
					metricValues.put(MetricType.DEVELOPERS, evolutionMetric.getDevelopers());
				}

				SmellMetric.CoChangeMetric coChangeMetric = smellMetric.getCoChangeMetric();
				if (coChangeMetric != null){
					metricValues.put(MetricType.CO_CHANGE_COMMITS, coChangeMetric.getCoChangeCommits());
					metricValues.put(MetricType.CO_CHANGE_FILE_COUNT, coChangeMetric.getCoChangeFileCount());
				}

				SmellMetric.DebtMetric detMetric = smellMetric.getDebtMetric();
				if(detMetric != null){
					metricValues.put(MetricType.ISSUES, detMetric.getIssues());
					metricValues.put(MetricType.BUG_ISSUES, detMetric.getBugIssues());
					metricValues.put(MetricType.NEW_FEATURE_ISSUES, detMetric.getNewFeatureIssues());
					metricValues.put(MetricType.IMPROVEMENT_ISSUES, detMetric.getImprovementIssues());
				}

				metric.setMetricValues(metricValues);
				result.put(smell, metric);
			});
		}

		return result;
	}

	public void createSmellMetricNodesInFileLevel(){
		Map<Smell, Metric> smellMetricMap = generateSmellMetricNodesInFileLevel();
		if(smellMetricMap != null && !smellMetricMap.isEmpty()){
			Collection<Metric> fileMetricNodes = smellMetricMap.values();
			metricRepository.saveAll(fileMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			int size = 0;
			for(Map.Entry<Smell, Metric> entry : smellMetricMap.entrySet()){
				Has has = new Has(entry.getKey(), entry.getValue());
				hasMetrics.add(has);
				if(++size > 500){
					hasRepository.saveAll(hasMetrics);
					hasMetrics.clear();
					size = 0;
				}
			}
			hasRepository.saveAll(hasMetrics);
		}
	}

	public SmellMetric calculateSmellMetricInFileLevel(Smell smell) {
		SmellMetric smellMetric = new SmellMetric();
		SmellMetric.StructureMetric structureMetric = smellRepository.calculateSmellStructureMetricInFileLevel(smell.getId());
		SmellMetric.EvolutionMetric evolutionMetric = smellRepository.calculateSmellEvolutionMetricInFileLevel(smell.getId());
		SmellMetric.CoChangeMetric coChangeMetric = smellRepository.calculateSmellCoChangeMetricInFileLevel(smell.getId());
		SmellMetric.DebtMetric smellDebtMetrics = smellRepository.calculateSmellDebtMetricInFileLevel(smell.getId());

		smellMetric.setStructureMetric(structureMetric);
		smellMetric.setEvolutionMetric(evolutionMetric);
		smellMetric.setCoChangeMetric(coChangeMetric);
		smellMetric.setDebtMetric(smellDebtMetrics);
		return smellMetric;
	}

	public Map<Smell, SmellMetric> calculateSmellMetricInFileLevel() {
		String key = "calculateSmellMetricInFileLevel";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Smell, SmellMetric> result = new HashMap<>();
		List<SmellMetric.StructureMetric> smellStructureMetrics = smellRepository.calculateSmellStructureMetricInFileLevel();
		if(smellStructureMetrics != null && !smellStructureMetrics.isEmpty()){
			smellStructureMetrics.forEach(structureMetric -> {
				Smell smell = structureMetric.getSmell();
				SmellMetric.EvolutionMetric evolutionMetric = smellRepository.calculateSmellEvolutionMetricInFileLevel(smell.getId());
				SmellMetric.CoChangeMetric coChangeMetric = smellRepository.calculateSmellCoChangeMetricInFileLevel(smell.getId());
				SmellMetric.DebtMetric debtMetric = smellRepository.calculateSmellDebtMetricInFileLevel(smell.getId());
				SmellMetric smellMetric = new SmellMetric();
				smellMetric.setSmell(smell);
				smellMetric.setStructureMetric(structureMetric);
				smellMetric.setEvolutionMetric(evolutionMetric);
				smellMetric.setCoChangeMetric(coChangeMetric);
				smellMetric.setDebtMetric(debtMetric);
				result.put(smell, smellMetric);
			});
			cache.cache(getClass(), key, result);
		}
		return result;
	}

	public Map<Long, List<SmellMetric>> calculateProjectSmellMetricsInFileLevel() {
		String key = "calculateProjectSmellMetricsInFileLevel";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<SmellMetric>> result = new HashMap<>();
		Map<Smell, SmellMetric> smellMetricsCache = new HashMap<>(calculateSmellMetricInFileLevel());
		if(smellMetricsCache != null && !smellMetricsCache.isEmpty()){
			smellMetricsCache.forEach((smell,smellMetrics)->{
				Long projectId = smell.getProjectId();
				if (projectId != null){
					List<SmellMetric> temp = result.getOrDefault(projectId, new ArrayList<>());
					temp.add(smellMetrics);
					result.put(projectId, temp);
				}
			});
			cache.cache(getClass(), key, result);
		}
		return result;
	}
}
