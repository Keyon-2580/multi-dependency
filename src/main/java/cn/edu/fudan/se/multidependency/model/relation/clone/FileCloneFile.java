package cn.edu.fudan.se.multidependency.model.relation.clone;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FILE_CLONE_FILE)
public class FileCloneFile implements CloneRelation {
	private static final long serialVersionUID = -8166989684654207651L;

	@Id
    @GeneratedValue
    private Long id;
	 	
	@StartNode
	private ProjectFile file1;
	
	@EndNode
	private ProjectFile file2;
	
	private double value;
	
	private int node1Index;
	
	private int node2Index;
	
	private int node1StartLine;
	
	private int node1EndLine;
	
	private int node2StartLine;
	
	private int node2EndLine;
	
	public FileCloneFile(ProjectFile file1, ProjectFile file2) {
		this.file1 = file1;
		this.file2 = file2;
	}

	@Override
	public Node getStartNode() {
		return file1;
	}

	@Override
	public Node getEndNode() {
		return file2;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FILE_CLONE_FILE;
	}

}
