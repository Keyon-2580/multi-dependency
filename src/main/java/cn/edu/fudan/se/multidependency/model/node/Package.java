package cn.edu.fudan.se.multidependency.model.node;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.utils.FileUtil;
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
public class Package implements Node, Module {

    @Id
    @GeneratedValue
    private Long id;
    
    private String directoryPath;
	
    private String name;

    private Long entityId;
    
    private long loc = -1;
    
    private long lines = -1;

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
	
	public String lastPackageDirectoryPath() {
		return FileUtil.extractDirectoryFromFile(FileUtil.extractDirectoryFromFile(directoryPath)) + "/";
	}
	
}
