package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.CodeFile;
import cn.edu.fudan.se.multidependency.model.node.code.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity("PACKAGE_CONTAINS_FILE")
public class PackageContainsFile implements Relation {
	
	private static final long serialVersionUID = 6671650000417159863L;

	@Id
    @GeneratedValue
    private Long id;
    
	public PackageContainsFile() {
		super();
	}

	public PackageContainsFile(Package pck, CodeFile file) {
		super();
		this.pck = pck;
		this.file = file;
	}

	@StartNode
	private Package pck;
	
	@EndNode
	private CodeFile file;

	public Package getPck() {
		return pck;
	}

	public void setPck(Package pck) {
		this.pck = pck;
	}

	public CodeFile getFile() {
		return file;
	}

	public void setFile(CodeFile file) {
		this.file = file;
	}

	@Override
	public Long getStartNodeGraphId() {
		return pck.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return file.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.PACKAGE_CONTAINS_FILE;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}
}
