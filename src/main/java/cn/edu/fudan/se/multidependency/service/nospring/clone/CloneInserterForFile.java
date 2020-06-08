package cn.edu.fudan.se.multidependency.service.nospring.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.CloneUtil.FilePathFromCsv;
import lombok.Setter;

public class CloneInserterForFile extends ExtractorForNodesAndRelationsImpl {

	private static final Executor executor = Executors.newCachedThreadPool();
	
	private CountDownLatch latch;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CloneInserterForFile.class);
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
		this.latch = new CountDownLatch(2);
	}
	
	private Map<Integer, FilePathFromCsv> filePaths;
	private Collection<CloneResultFromCsv> cloneResults;

	@Override
	public void addNodesAndRelations() throws Exception {
		executor.execute(() -> {
			try {
				filePaths = CloneUtil.readJavaCloneCsvForFilePath(namePath);
				for(Map.Entry<Integer, FilePathFromCsv> entry : filePaths.entrySet()) {
					FilePathFromCsv filePath = entry.getValue();
					ProjectFile file = this.getNodes().findFileByPathRecursion(filePath.getFilePath());
					if(file == null) {
						LOGGER.warn("file is null " + filePath.getFilePath());
						continue;
					}
					file.setLine(filePath.getEndLine());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});
		
		executor.execute(() -> {
			try {
				cloneResults = CloneUtil.readCloneResultCsv(resultPath);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});
		
		latch.await();
		LOGGER.info("文件克隆对数：" + cloneResults.size());
		int sizeOfFileCloneFiles = 0;
		List<FileCloneFile> clones = new ArrayList<>();
		for(CloneResultFromCsv cloneResult : cloneResults) {
			int start = cloneResult.getStart();
			int end = cloneResult.getEnd();
			double value = cloneResult.getValue();
			FilePathFromCsv filePath1 = filePaths.get(start);
			if(filePath1 == null) {
				throw new Exception("filePath1 is null");
			}
			FilePathFromCsv filePath2 = filePaths.get(end);
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
			clone.setFile1Index(start);
			clone.setFile2Index(end);
			clone.setFile1StartLine(filePath1.getStartLine());
			clone.setFile1EndLine(filePath1.getEndLine());
			clone.setFile2StartLine(filePath2.getStartLine());
			clone.setFile2EndLine(filePath2.getEndLine());
			clone.setValue(value);
			addRelation(clone);
			clones.add(clone);
			sizeOfFileCloneFiles++;
		}
		LOGGER.info("插入文件级克隆关系数：" + sizeOfFileCloneFiles);
		Collection<Collection<? extends Node>> groups = CloneUtil.groupCloneNodes(clones);
		int groupCount = 0;
		for(Collection<? extends Node> nodes : groups) {
			CloneGroup group = new CloneGroup();
			group.setEntityId(generateEntityId());
			group.setGroup(String.valueOf(groupCount++));
			group.setName("group_" + group.getGroup());
			group.setLevel(NodeLabelType.ProjectFile);
			addNode(group, null);
			for(Node node : nodes) {
				addRelation(new Contain(group, node));
			}
		}
		LOGGER.info("插入文件级克隆组，组数：" + groupCount);
	}
}
