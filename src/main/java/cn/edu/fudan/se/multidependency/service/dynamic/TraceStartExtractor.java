package cn.edu.fudan.se.multidependency.service.dynamic;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.utils.JavaDynamicUtil.JavaDynamicFunctionExecution;
import cn.edu.fudan.se.multidependency.utils.TimeUtil;
import lombok.Data;
import lombok.ToString;

/**
 * 不做插入数据库操作，只从日志文件中找出parentSpanId为-1的trace
 * @author fan
 *
 */
public class TraceStartExtractor extends DynamicInserterForNeo4jService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TraceStartExtractor.class);
	
	public TraceStartExtractor(File[] dynamicFunctionCallFiles) {
		super(dynamicFunctionCallFiles);
	}

	@Data
	@ToString
	public static class TraceStartProject {
		String project;
		String language;
		String traceId;
		String time;
		String functionName;
		
		public TraceStartProject(JavaDynamicFunctionExecution execution) {
			this.project = execution.getProject();
			this.language = execution.getLanguage();
			this.traceId = execution.getTraceId();
			this.time = execution.getTime();
			this.functionName = execution.getFunctionName();
		}
	}
	
	private Map<String, TraceStartProject> traceStartProjects = new HashMap<>();	
	
	public List<TraceStartProject> getTraceStartProjects() {
		List<TraceStartProject> result = new ArrayList<>();
		for(String traceId : traceStartProjects.keySet()) {
			result.add(traceStartProjects.get(traceId));
		}
		result.sort(new Comparator<TraceStartProject>() {
			@Override
			public int compare(TraceStartProject o1, TraceStartProject o2) {
				Timestamp o1time = new Timestamp(TimeUtil.changeTimeStrToLong(o1.getTime()));
				Timestamp o2time = new Timestamp(TimeUtil.changeTimeStrToLong(o2.getTime()));
				
//				return o1.getProject().compareTo(o2.getProject());
				return o1time.compareTo(o2time);
			}
		});
		return result;
	}

	@Override
	protected void extractNodesAndRelations() throws Exception {
		for(String projectName : this.javaExecutionsGroupByProject.keySet()) {
			List<JavaDynamicFunctionExecution> executions = this.javaExecutionsGroupByProject.get(projectName);
			for(JavaDynamicFunctionExecution execution : executions) {
				if(JavaDynamicFunctionExecution.TRACE_START_PARENT_SPAN_ID.equals(execution.getParentSpanId())) {
					String traceId = execution.getTraceId();
					if(traceStartProjects.get(traceId) == null) {
						TraceStartProject project = new TraceStartProject(execution);
						traceStartProjects.put(traceId, project);
					}
				}
			}
		}
		for(TraceStartProject project : getTraceStartProjects()) {
			LOGGER.info(project.toString());
		}
	}

}
