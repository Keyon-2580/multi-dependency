var gitRepoMetric = function() {
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
						html += "<td><a target='_blank' href='/relation/file/" + metrics[i].file.id + "'>" + metrics[i].file.path + "</a></td>";
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
					html += "<th>lines（文件总行数）</th>";
					html += "<th>Ca（afferent couplings）</th>";
					html += "<th>Ce（efferent couplings）</th>";
					html += "<th>instability</th>";
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
						html += "<td>" + metrics[i].lines + "</td>";
						html += "<td>" + metrics[i].fanIn + "</td>";
						html += "<td>" + metrics[i].fanOut + "</td>";
						html += "<td>" + metrics[i].fanOut / (metrics[i].fanIn + metrics[i].fanOut) + "</td>";
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
	
	var showModularity = function(container, projectId) {
		$.ajax({
			type: "get",
			url: "/metric/project/modularity?projectId=" + projectId,
			success: function(modularity) {
				container.text(modularity);
			}
		});
	}
	
	var showCommitTimes = function(container, projectId) {
		$.ajax({
			type: "get",
			url: "/metric/project/commitTimes?projectId=" + projectId,
			success: function(commitTimes) {
				container.text(commitTimes);
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
				html += "<th>lines（文件总行数）</th>";
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
					html += "<td>" + result[i].lines + "</td>";
					html += "<td id='commitTimes_" + result[i].project.id + "'>" + (result[i].commitTimes < 0 ? "计算中..." : result[i].commitTimes) + "</td>";
					html += "<td id='modularity_" + result[i].project.id + "'>" + (result[i].modularity < 0 ? "计算中..." : result[i].modularity)  + "</td>";
					html += "</tr>";
				}
				html += "</table>";
				$("#projectMetrics").html(html);
				for(var i = 0; i < result.length; i++) {
					var projectId = result[i].project.id;
					showModularity($("#modularity_" + result[i].project.id), result[i].project.id);
					showCommitTimes($("#commitTimes_" + result[i].project.id), result[i].project.id);
				}
			}
		});
	}

	var repoMetric = function (){
		$.ajax({
			type: "get",
			url: "/git/repoMetric",
			success: function(result) {
				console.log(result);
				var html = "<table id='repoTable' class='table table-bordered'>";
				html += "<tr>";
				html += "<th>index</th>";
				html += "<th>id</th>";
				html += "<th>Repo</th>";
				html += "<th>Language</th>";
				html += "<th>NOP（包数）</th>";
				html += "<th>NOF（文件数）</th>";
				html += "<th>NOM（方法数）</th>";
				html += "<th>LOC（代码行）</th>";
				html += "<th>lines（文件总行数）</th>";
				html += "<th>Commits</th>";
				html += "<th>Issues</th>";
				html += "</tr>";
				for(var i = 0; i < result.length; i++) {
					for (var j = 0; j < result[i].projectMetricsList.length; j++){
						html += "<tr>";
						html += "<td>" + (i + 1) + "</td>";
						html += "<td>" + result[i].gitRepository.id + "</td>";
						html += "<td>" + result[i].gitRepository.name + "</td>";
						projectMetricTmp = result[i].projectMetricsList[j];
						html += "<td>" + projectMetricTmp.project.name + " ("  + projectMetricTmp.project.language + ") " + "</td>";
						html += "<td>" + projectMetricTmp.nop + "</td>";
						html += "<td>" + projectMetricTmp.nof + "</td>";
						html += "<td>" + projectMetricTmp.nom + "</td>";
						html += "<td>" + projectMetricTmp.loc + "</td>";
						html += "<td>" + projectMetricTmp.lines + "</td>";
						target='_blank'
						html += "<td id='numOfCommits_" + result[i].gitRepository.id + "'>" + "<a target='_blank' href='/commit'>" +  (result[i].numOfCommits < 0 ? "计算中..." : result[i].numOfCommits) + "</a>" + "</td>";
						html += "<td id='numOfIssues_" + result[i].gitRepository.id + "'>" + "<a target='_blank' href='/issue'>" + (result[i].numOfIssues < 0 ? "计算中..." : result[i].numOfIssues) + "</a>" + "</td>";
						html += "</tr>";
					}
				}
				html += "</table>";
				$("#gitRepoMetric").html(html);
			}
		});
	}
	return {
		init: function() {
			repoMetric();
		}
	}
}