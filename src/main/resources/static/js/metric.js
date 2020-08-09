var metric = function() {
	var fileMetric = function(projects) {
		$.ajax({
			type: "get",
			url: "/metric/file",
			success: function(result) {
				console.log(result);
				var html = "";
				for(var id in projects) {
					html += "<div><h4>" + projects[id].name + " (" + projects[id].language + ")" + "</h4></div>";
					html += "<div><button name='fileTable_" + id + "' class='btn btn-primary file_excel_button'>输出 excel</button></div>";
					html += "<div><table id='fileTable_" + id + "' class='table table-bordered'>";
					html += "<tr>";
					html += "<th>index</th>";
					html += "<th>id</th>";
					html += "<th>文件</th>";
					html += "<th>LOC（代码行）</th>";
					html += "<th>NOM（方法数）</th>";
					html += "<th>Fan In</th>";
					html += "<th>Fan Out</th>";
					html += "<th>修改次数</th>";
					html += "<th>协同修改的commit次数</th>";
					html += "<th>协同修改的文件数</th>";
					html += "<th>PageRank Score</th>";
					html += "</tr>";
					var metrics = result[id];
					console.log(metrics);
					for(var i = 0; i < metrics.length; i++) {
						html += "<tr>";
						html += "<td>" + (i + 1) + "</td>";
						html += "<td>" + metrics[i].file.id + "</td>";
						html += "<td>" + metrics[i].file.path + "</td>";
						html += "<td>" + metrics[i].loc + "</td>";
						html += "<td>" + metrics[i].nom + "</td>";
						html += "<td>" + metrics[i].fanIn + "</td>";
						html += "<td>" + metrics[i].fanOut + "</td>";
						html += "<td>" + metrics[i].changeTimes + "</td>";
						html += "<td>" + metrics[i].cochangeCommitTimes + "</td>";
						html += "<td>" + metrics[i].cochangeFileCount + "</td>";
						html += "<td>" + metrics[i].file.score + "</td>";
						html += "</tr>";
					}
					html += "</table></div>";
				}
				$("#fileMetrics").html(html);
				$(".file_excel_button").click(function() {
					tableToExcel($(this).attr("name"), "fileMetrics");
				});
			}
		})
	}
	
	var packageMetric = function(projects) {
		$.ajax({
			type: "get",
			url: "/metric/package",
			success: function(result) {
				console.log(result);
				var html = "";
				for(var id in projects) {
					html += "<div><h4>" + projects[id].name + " (" + projects[id].language + ")" + "</h4></div>";
					html += "<div><button name='packageTable_" + id + "' class='btn btn-primary package_excel_button'>输出 excel</button></div>";
					html += "<div><table id='packageTable_" + i + "' class='table table-bordered'>";
					html += "<tr>";
					html += "<th>index</th>";
					html += "<th>id</th>";
					html += "<th>目录</th>";
					html += "<th>NOF（文件数）</th>";
					html += "<th>NOM（方法数）</th>";
					html += "<th>LOC（代码行）</th>";
					html += "<th>Fan In</th>";
					html += "<th>Fan Out</th>";
					html += "</tr>";
					var metrics = result[id];
					console.log(metrics);
					for(var i = 0; i < metrics.length; i++) {
						html += "<tr>";
						html += "<td>" + (i + 1) + "</td>";
						html += "<td>" + metrics[i].pck.id + "</td>";
						html += "<td>" + metrics[i].pck.directoryPath + "</td>";
						html += "<td>" + metrics[i].nof + "</td>";
						html += "<td>" + metrics[i].nom + "</td>";
						html += "<td>" + metrics[i].loc + "</td>";
						html += "<td>" + metrics[i].fanIn + "</td>";
						html += "<td>" + metrics[i].fanOut + "</td>";
						html += "</tr>";
					}
					html += "</table></div>";
				}
				$("#packageMetrics").html(html);
				$(".package_excel_button").click(function() {
					tableToExcel($(this).attr("name"), "packageMetrics");
				});
			}
		});
	}
	
	var projectMetric = function() {
		$.ajax({
			type: "get",
			url: "/metric/project",
			success: function(result) {
				console.log(result);
				var html = "<table id='projectTable' class='table table-bordered'>";
				html += "<tr>";
				html += "<th>index</th>";
				html += "<th>id</th>";
				html += "<th>项目</th>";
				html += "<th>NOP（包数）</th>";
				html += "<th>NOF（文件数）</th>";
				html += "<th>NOM（方法数）</th>";
				html += "<th>LOC（代码行）</th>";
				html += "<th>Commit次数</th>";
				html += "<th>Modularity（模块度）</th>";
				html += "</tr>";
				for(var i = 0; i < result.length; i++) {
					html += "<tr>";
					html += "<td>" + (i + 1) + "</td>";
					html += "<td>" + result[i].project.id + "</td>";
					html += "<td>" + result[i].project.name + " (" + result[i].project.language + ") " + "</td>";
					html += "<td>" + result[i].nop + "</td>";
					html += "<td>" + result[i].nof + "</td>";
					html += "<td>" + result[i].nom + "</td>";
					html += "<td>" + result[i].loc + "</td>";
					html += "<td>" + result[i].commitTimes + "</td>";
					html += "<td>" + result[i].modularity + "</td>";
					html += "</tr>";
				}
				html += "</table>";
				$("#projectMetrics").html(html);
			}
		});
	}
	return {
		init: function() {
			projectMetric();
			$.ajax({
				type: "get",
				url: "/project/all",
				success: function(result) {
					console.log(result);
					packageMetric(result);
					fileMetric(result);
				}
			});
			$("#projectButton").click(function() {
				tableToExcel("projectTable", "projectMetrics");
			});
			$("#fileButton").click(function() {
//				window.href="/";
				$.ajax({
					type: "get",
					url: "/metric/excel/file"
				});
			});
		}
	}
}