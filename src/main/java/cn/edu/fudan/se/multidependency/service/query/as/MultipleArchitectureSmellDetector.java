package cn.edu.fudan.se.multidependency.service.query.as;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.service.query.as.data.HistogramAS;
import cn.edu.fudan.se.multidependency.service.query.as.data.MultipleAS;
import cn.edu.fudan.se.multidependency.service.query.as.data.MultipleASFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.PieFilesData;

public interface MultipleArchitectureSmellDetector {
	
	Map<Long, HistogramAS> projectHistogramOnVersion();
	
	Map<Long, List<MultipleASFile>> multipleASFiles(boolean removeNoASFile);
	
	Map<Long, PieFilesData> smellAndIssueFiles(MultipleAS multipleAS);
	
	void printMultipleASFiles(OutputStream stream);
}
