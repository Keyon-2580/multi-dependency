package cn.edu.fudan.se.multidependency.service.nospring.clone;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.FilePathForJavaFromCsv;
import lombok.Setter;

public class CloneInserterForFile extends ExtractorForNodesAndRelationsImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloneUtil.class);
	@Setter
	private Language language;
	@Setter
	private String namePath;
	@Setter
	private String resultPath;
	
	public CloneInserterForFile(Language language, String namePath, String resultPath) {
		super();
		this.language = language;
		this.namePath = namePath;
		this.resultPath = resultPath;
	}

	@Override
	public void addNodesAndRelations() throws Exception {
		if(Language.java == language) {
			Map<Integer, FilePathForJavaFromCsv> filePaths = CloneUtil.readJavaCloneCsvForFilePath(namePath);
			Iterable<CloneResultFromCsv> cloneResults = CloneUtil.readCloneResultCsv(resultPath);
			
			for(CloneResultFromCsv cloneResult : cloneResults) {
				int start = cloneResult.getStart();
				int end = cloneResult.getEnd();
				double value = cloneResult.getValue();
				FilePathForJavaFromCsv filePath1 = filePaths.get(start);
				if(filePath1 == null) {
					throw new Exception("filePath1 is null");
				}
				FilePathForJavaFromCsv filePath2 = filePaths.get(end);
				if(filePath2 == null) {
					throw new Exception("filePath2 is null");
				}
				ProjectFile file1 = this.getNodes().findFileByPathRecursion(filePath1.getFilePath());
				ProjectFile file2 = this.getNodes().findFileByPathRecursion(filePath2.getFilePath());
				if(file1 == null) {
					LOGGER.warn("file1 is null " + filePath1.getFilePath());
					continue;
				}
				if(file2 == null) {
					LOGGER.warn("file2 is null " + filePath2.getFilePath());
					continue;
				}
				FileCloneFile clone = new FileCloneFile(file1, file2);
				clone.setValue(value);
				addRelation(clone);
			}
		}
	}

}
