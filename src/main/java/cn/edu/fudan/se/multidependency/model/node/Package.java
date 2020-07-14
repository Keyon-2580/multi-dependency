package cn.edu.fudan.se.multidependency.model.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

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
@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Package implements Node {

    @Id
    @GeneratedValue
    private Long id;
    
    private String directoryPath;
	
    private String name;

    private Long entityId;

	private static final long serialVersionUID = -4892461872164624064L;
	
	public static final String JAVA_PACKAGE_DEFAULT = "default";

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("directoryPath", getDirectoryPath() == null ? "" : getDirectoryPath());
		properties.put("name", getName() == null ? "" : getName());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Package;
	}
	
	@Transient
	private Set<ProjectFile> files = new HashSet<>();
	public synchronized void addFiles(Collection<ProjectFile> files) {
		this.files.addAll(files);
	}
	
}
