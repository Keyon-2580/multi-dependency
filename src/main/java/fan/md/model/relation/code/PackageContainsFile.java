package fan.md.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import fan.md.model.node.code.CodeFile;
import fan.md.model.node.code.Package;
import fan.md.model.relation.Relation;
import fan.md.model.relation.RelationType;

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
