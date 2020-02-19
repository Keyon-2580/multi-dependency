package cn.edu.fudan.se.multidependency.model.node;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 文件：目录
 * java：包
 * c/c++：目录
 * @author fan
 *
 */
@NodeEntity
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Package implements Node {

    @Id
    @GeneratedValue
    private Long id;
    
	private String packageName;
	
	private String directoryPath;

    private Long entityId;

	private static final long serialVersionUID = -4892461872164624064L;

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("packageName", getPackageName() == null ? "" : getPackageName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("directoryPath", getDirectoryPath() == null ? "" : getDirectoryPath());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Package;
	}

}
