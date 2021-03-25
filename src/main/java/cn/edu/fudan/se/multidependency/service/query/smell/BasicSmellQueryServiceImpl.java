package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Service
public class BasicSmellQueryServiceImpl implements BasicSmellQueryService {

    @Autowired
	private SmellRepository smellRepository;

	@Override
	public Collection<Smell> findSmellsByLevel(String level) {
		Collection<Smell> result = smellRepository.findSmells(level);
		for(Smell smell : result){
			List<Node> nodes = smellRepository.findSmellContains(smell.getId());
			Set<Node> set = new HashSet<>(nodes);
			smell.setNodes(set);
		}
		return result;
	}

	@Override
	public JSONArray smellsToTreemap(){
		JSONArray smellArray = new JSONArray();
		try {
			Collection<Smell> smellGroups = findSmellsByLevel(SmellLevel.FILE);

			for(Smell smell : smellGroups){
				JSONObject temp_smell = new JSONObject();
				JSONArray temp_nodes = new JSONArray();
				temp_smell.put("name", smell.getName());
				temp_smell.put("id", smell.getId());
				temp_smell.put("smell_type", smell.getType());
				temp_smell.put("smell_level", smell.getLevel());

				for(Node node : smell.getNodes()){
					ProjectFile file = (ProjectFile)node;
					JSONObject temp_node = new JSONObject();
					temp_node.put("id", "id_" + file.getId());
					temp_node.put("path", file.getPath());
					temp_node.put("name", file.getName());

					temp_nodes.add(temp_node);
				}

				temp_smell.put("nodes", temp_nodes);
				smellArray.add(temp_smell);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return smellArray;
	}

	@Override
	public Metric findMetricBySmellId(long smellId){
		return smellRepository.findSmellMetric(smellId);
	}
}
