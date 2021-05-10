package cn.edu.fudan.se.multidependency.service.query.smell;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.service.query.smell.data.CirclePacking;
import cn.edu.fudan.se.multidependency.service.query.smell.data.HistogramAS;
import cn.edu.fudan.se.multidependency.service.query.smell.data.MultipleAS;
import cn.edu.fudan.se.multidependency.service.query.smell.data.MultipleASFile;
import cn.edu.fudan.se.multidependency.service.query.smell.data.PieFilesData;
import com.alibaba.fastjson.JSONObject;

public interface MultipleSmellDetector {
	
	Map<Long, HistogramAS> projectHistogramOnVersion();
	
	Map<Long, List<CirclePacking>> circlePacking(MultipleAS multipleAS);
	
	Map<Long, List<MultipleASFile>> queryMultipleSmellASFiles(boolean removeNoASFile);

	Map<Long, List<MultipleASFile>> detectMultipleSmellASFiles(boolean removeNoASFile);

	Map<Long, PieFilesData> smellAndIssueFiles(MultipleAS multipleAS);

	Map<Long, JSONObject> getProjectTotal();

	Map<Long, JSONObject> getFileSmellOverview();

	Map<Long, JSONObject> getPackageSmellOverview();

	void printMultipleASFiles(OutputStream stream);
}
