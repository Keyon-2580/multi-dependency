package cn.edu.fudan.se.multidependency.model.relation.clone;

import java.util.HashMap;
import java.util.Map;

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
	
	private int file1Index;
	
	private int file2Index;
	
	private int file1StartLine;
	
	private int file1EndLine;
	
	private int file2StartLine;
	
	private int file2EndLine;
	
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

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("value", getValue());
		properties.put("file1StartLine", getFile1StartLine());
		properties.put("file1EndLine", getFile1EndLine());
		properties.put("file2StartLine", getFile2StartLine());
		properties.put("file2EndLine", getFile2EndLine());
		return properties;
	}

	@Override
	public int getNode1StartLine() {
		return file1StartLine;
	}

	@Override
	public int getNode1EndLine() {
		return file1EndLine;
	}

	@Override
	public int getNode2StartLine() {
		return file2StartLine;
	}

	@Override
	public int getNode2EndLine() {
		return file2EndLine;
	}

	@Override
	public int getNode1Index() {
		return file1Index;
	}

	@Override
	public int getNode2Index() {
		return file2Index;
	}

}
