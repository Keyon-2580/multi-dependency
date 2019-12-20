package fan.md.model.relation.code;

import java.io.Serializable;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import fan.md.model.entity.code.CodeFile;
import fan.md.model.entity.code.Package;

@RelationshipEntity("PACKAGE_CONTAIN_FILE")
public class PackageContainFile implements Serializable {
	
	private static final long serialVersionUID = 6671650000417159863L;

	@Id
    @GeneratedValue
    private Long id;
    
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
	
	
}
