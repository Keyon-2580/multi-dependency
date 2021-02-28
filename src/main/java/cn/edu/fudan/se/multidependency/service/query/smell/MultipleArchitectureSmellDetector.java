package cn.edu.fudan.se.multidependency.service.query.smell;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.service.query.smell.data.CirclePacking;
import cn.edu.fudan.se.multidependency.service.query.smell.data.HistogramAS;
import cn.edu.fudan.se.multidependency.service.query.smell.data.MultipleAS;
import cn.edu.fudan.se.multidependency.service.query.smell.data.MultipleASFile;
import cn.edu.fudan.se.multidependency.service.query.smell.data.PieFilesData;

public interface MultipleArchitectureSmellDetector {
	
	Map<Long, HistogramAS> projectHistogramOnVersion();
	
	Map<Long, List<CirclePacking>> circlePacking(MultipleAS multipleAS);
	
	Map<Long, List<MultipleASFile>> multipleASFiles(boolean removeNoASFile);
	
	Map<Long, PieFilesData> smellAndIssueFiles(MultipleAS multipleAS);
	
	void printMultipleASFiles(OutputStream stream);
}
