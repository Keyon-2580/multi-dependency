package cn.edu.fudan.se.multidependency.service.query.metric;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class MetricShowService {
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private MetricCalculatorService metricCalculatorService;
	
	public void printPackageMetricExcel(OutputStream stream) {
		Workbook hwb = new XSSFWorkbook();
		Map<Long, List<PackageMetrics>> allPackageMetrics = metricCalculatorService.calculatePackageMetrics();
		Collection<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			Sheet sheet = hwb.createSheet(new StringBuilder().append(project.getName()).append("(").append(project.getLanguage()).append(")").toString());
			List<PackageMetrics> packageMetrics = allPackageMetrics.get(project.getId());
			Row row = sheet.createRow(0);
			CellStyle style = hwb.createCellStyle();
			style.setAlignment(HorizontalAlignment.CENTER);
//			sheet.setColumnWidth(0, "xxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(1, "xxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(2, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(3, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(4, "xxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(5, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(6, "xxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(7, "xxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(8, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(9, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(10, "xxxxxx".length() * 256);
//			sheet.setColumnWidth(11, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(12, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(13, "xxxxxxxxxx".length() * 256);
			Cell cell = null;
			cell = row.createCell(0);
			cell.setCellValue("id");
			cell.setCellStyle(style);
			cell = row.createCell(1);
			cell.setCellValue("目录");
			cell.setCellStyle(style);
			cell = row.createCell(2);
			cell.setCellValue("NOF");
			cell.setCellStyle(style);
			cell = row.createCell(3);
			cell.setCellValue("NOM");
			cell.setCellStyle(style);
			cell = row.createCell(4);
			cell.setCellValue("LOC");
			cell.setCellStyle(style);
			cell = row.createCell(5);
			cell.setCellValue("Fan In");
			cell.setCellStyle(style);
			cell = row.createCell(6);
			cell.setCellValue("Fan Out");
			cell.setCellStyle(style);
			for (int i = 0; i < packageMetrics.size(); i++) {
				PackageMetrics packageMetric = packageMetrics.get(i);
				row = sheet.createRow(i + 1);
				row.createCell(0).setCellValue(packageMetric.getPck().getId());
				row.createCell(1).setCellValue(packageMetric.getPck().getDirectoryPath());
				row.createCell(2).setCellValue(packageMetric.getNof());
				row.createCell(3).setCellValue(packageMetric.getNom());
				row.createCell(4).setCellValue(packageMetric.getLoc());
				row.createCell(5).setCellValue(packageMetric.getFanIn());
				row.createCell(6).setCellValue(packageMetric.getFanOut());
			}			
		}
		try {
			hwb.write(stream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
				hwb.close();
			} catch (IOException e) {
			}
		}
	}

	public void printFileMetricExcel(OutputStream stream) {
		Workbook hwb = new XSSFWorkbook();
		Map<Long, List<FileMetrics>> allFileMetrics = metricCalculatorService.calculateFileMetricsWithProjectIdIndex();
		Collection<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			Sheet sheet = hwb.createSheet(new StringBuilder().append(project.getName()).append("(").append(project.getLanguage()).append(")").toString());
			List<FileMetrics> fileMetrics = allFileMetrics.get(project.getId());
			Row row = sheet.createRow(0);
			CellStyle style = hwb.createCellStyle();
			style.setAlignment(HorizontalAlignment.CENTER);
//			sheet.setColumnWidth(0, "xxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(1, "xxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(2, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(3, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(4, "xxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(5, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(6, "xxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(7, "xxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(8, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(9, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(10, "xxxxxx".length() * 256);
//			sheet.setColumnWidth(11, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(12, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(13, "xxxxxxxxxx".length() * 256);
			Cell cell = null;
			cell = row.createCell(0);
			cell.setCellValue("id");
			cell.setCellStyle(style);
			cell = row.createCell(1);
			cell.setCellValue("文件");
			cell.setCellStyle(style);
			cell = row.createCell(2);
			cell.setCellValue("LOC（代码行）");
			cell.setCellStyle(style);
			cell = row.createCell(3);
			cell.setCellValue("NOM（方法数）");
			cell.setCellStyle(style);
			cell = row.createCell(4);
			cell.setCellValue("Fan In");
			cell.setCellStyle(style);
			cell = row.createCell(5);
			cell.setCellValue("Fan Out");
			cell.setCellStyle(style);
			cell = row.createCell(6);
			cell.setCellValue("修改次数");
			cell.setCellStyle(style);
			cell = row.createCell(7);
			cell.setCellValue("协同修改的commit次数");
			cell.setCellStyle(style);
			cell = row.createCell(8);
			cell.setCellValue("协同修改的文件数");
			cell.setCellStyle(style);
			cell = row.createCell(9);
			cell.setCellValue("Page Rank");
			cell.setCellStyle(style);
			for (int i = 0; i < fileMetrics.size(); i++) {
				FileMetrics fileMetric = fileMetrics.get(i);
				row = sheet.createRow(i + 1);
				row.createCell(0).setCellValue(fileMetric.getFile().getId());
				row.createCell(1).setCellValue(fileMetric.getFile().getPath());
				row.createCell(2).setCellValue(fileMetric.getLoc());
				row.createCell(3).setCellValue(fileMetric.getNom());
				row.createCell(4).setCellValue(fileMetric.getFanIn());
				row.createCell(5).setCellValue(fileMetric.getFanOut());
				row.createCell(6).setCellValue(fileMetric.getChangeTimes());
				row.createCell(7).setCellValue(fileMetric.getCochangeCommitTimes());
				row.createCell(8).setCellValue(fileMetric.getCochangeFileCount());
				row.createCell(9).setCellValue(fileMetric.getFile().getScore());
			}			
		}
		try {
			hwb.write(stream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
				hwb.close();
			} catch (IOException e) {
			}
		}
	}
	
}
