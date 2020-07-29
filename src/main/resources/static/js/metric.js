var metric = function() {
	var packageMetric = function() {
		$.ajax({
			type: "get",
			url: "/metric/package",
			success: function(result) {
				console.log(result);
				var html = "<table id='packageTable' class='table table-bordered'>";
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
				for(var i = 0; i < result.length; i++) {
					html += "<tr>";
					html += "<td>" + (i + 1) + "</td>";
					html += "<td>" + result[i].pck.id + "</td>";
					html += "<td>" + result[i].pck.directoryPath + "</td>";
					html += "<td>" + result[i].nof + "</td>";
					html += "<td>" + result[i].nom + "</td>";
					html += "<td>" + result[i].loc + "</td>";
					html += "<td>" + result[i].fanIn + "</td>";
					html += "<td>" + result[i].fanOut + "</td>";
					html += "</tr>";
				}
				html += "</table>";
				$("#packageMetrics").html(html);
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
	var fileMetric = function() {
		$.ajax({
			type: "get",
			url: "/metric/file",
			success: function(result) {
				console.log(result);
				var html = "<table id='fileTable' class='table table-bordered'>";
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
				for(var i = 0; i < result.length; i++) {
					html += "<tr>";
					html += "<td>" + (i + 1) + "</td>";
					html += "<td>" + result[i].file.id + "</td>";
					html += "<td>" + result[i].file.path + "</td>";
					html += "<td>" + result[i].loc + "</td>";
					html += "<td>" + result[i].nom + "</td>";
					html += "<td>" + result[i].fanIn + "</td>";
					html += "<td>" + result[i].fanOut + "</td>";
					html += "<td>" + result[i].changeTimes + "</td>";
					html += "<td>" + result[i].cochangeCommitTimes + "</td>";
					html += "<td>" + result[i].cochangeFileCount + "</td>";
					html += "<td>" + result[i].score + "</td>";
					html += "</tr>";
				}
				html += "</table>";
				$("#fileMetrics").html(html);
			}
		})
	}
	
	return {
		init: function() {
			projectMetric();
			packageMetric();
			fileMetric();
			$("#projectButton").click(function() {
				tableToExcel("projectTable", "projectMetrics");
			});
			$("#packageButton").click(function() {
				tableToExcel("packageTable", "packageMetrics");
			});
			$("#fileButton").click(function() {
				tableToExcel("fileTable", "fileMetrics");
			});
		}
	}
}