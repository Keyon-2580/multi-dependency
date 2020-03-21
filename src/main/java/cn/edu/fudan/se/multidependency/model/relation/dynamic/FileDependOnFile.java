package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.Transient;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependOnType;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationTimes;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FILE_DEPEND_ON_FILE)
public class FileDependOnFile implements Relation {
	
	private static final long serialVersionUID = 1041792061024395307L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private ProjectFile start;
	
	@EndNode
	private ProjectFile end;
	
	public FileDependOnFile(ProjectFile start, ProjectFile end) {
		this.start = start;
		this.end = end;
	}
	
	private Integer times = 0;
	
	private String detail;

	@Transient
	private Map<DependOnType, RelationTimes> dependOnTimes = new HashMap<>();
	
	public void addTimes(DependOnType type, boolean addTimes, Relation relation) {
		RelationTimes times = dependOnTimes.getOrDefault(type, new RelationTimes());
		times.addRelation(relation);
		dependOnTimes.put(type, times);
		if(addTimes) {
			times.addTimes();
			this.times++;
		}
		detail = generateDetail();
	}
	
	public boolean hasDependOnType(DependOnType type) {
		return dependOnTimes.get(type) != null;
	}
	
	private String generateDetail() {
		JSONObject result = new JSONObject();
		for(DependOnType dependOnType : dependOnTimes.keySet()) {
			result.put(dependOnType.name(), dependOnTimes.get(dependOnType).getTimes());
		}
		return result.toJSONString();
	}
	
	@Override
	public Long getStartNodeGraphId() {
		return start.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return end.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FILE_DEPEND_ON_FILE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes() == null ? 0 : getTimes());
		properties.put("", getDetail() == null ? "" : getDetail());
		return properties;
	}

}
