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
					html += "<th>Index</th>";
					html += "<th>Id</th>";
					html += "<th>ProjectFile</th>";
					html += "<th>LOC</th>";
					html += "<th>NOC</th>";
					html += "<th>NOM</th>";
					html += "<th>FanIn</th>";
					html += "<th>FanOut</th>";
//					html += "<th>Instability</th>";
					html += "<th>Commits</th>";
					html += "<th>Developers</th>";
//					html += "<th>Co-ChangeTimes</th>";
					html += "<th>CoChanges</th>";
					html += "<th>Issues</th>";
					html += "<th>Bugs</th>";
					html += "<th>NewFeatures</th>";
					html += "<th>Improvements</th>";
					html += "<th>PageRank</th>";
					html += "</tr>";
					var metrics = result[id];
					console.log(metrics);
					for(var i = 0; i < metrics.length; i++) {
						console.log(metrics[i]);
						html += "<tr>";
						html += "<td>" + (i + 1) + "</td>";
						html += "<td>" + metrics[i].file.id + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + metrics[i].file.id + "'>" + metrics[i].file.path + "</a></td>";
                        var loc = 0;
                        var noc = 0;
                        var nom = 0;
                        var fanIn = 0;
                        var fanOut = 0;
						if(metrics[i].structureMetric != null){
						    loc = metrics[i].structureMetric.loc;
						    noc = metrics[i].structureMetric.noc;
						    nom = metrics[i].structureMetric.nom;
						    fanIn = metrics[i].structureMetric.fanIn;
						    fanOut = metrics[i].structureMetric.fanOut;
						}
						html += "<td>" + loc + "</td>";
						html += "<td>" + noc + "</td>";
						html += "<td>" + nom + "</td>";
						html += "<td>" + fanIn + "</td>";
						html += "<td>" + fanOut + "</td>";
//						html += "<td>" + metrics[i].instability.toFixed(2) + "</td>";
                        var changeTimes = 0;
                        var developers = 0;
//                        var coChangeCommitTimes = 0;
                        var coChangeFileCount = 0;
                        if(metrics[i].evolutionMetric != null){
                             changeTimes = metrics[i].evolutionMetric.changeTimes;
                             developers = metrics[i].evolutionMetric.developers;
//                             coChangeCommitTimes = metrics[i].evolutionMetric.coChangeCommitTimes;
                             coChangeFileCount = metrics[i].evolutionMetric.coChangeFileCount;
                        }
						html += "<td>" + changeTimes + "</td>";
						html += "<td>" + developers + "</td>";
//						html += "<td>" + coChangeCommitTimes + "</td>";
						html += "<td>" + coChangeFileCount + "</td>";
						var issues = 0;
                        var bugIssues = 0;
                        var newFeatureIssues = 0;
                        var improvementIssues = 0;
                        if(metrics[i].debtMetric != null){
                             issues = metrics[i].debtMetric.issues;
                             bugIssues = metrics[i].debtMetric.bugIssues;
                             newFeatureIssues = metrics[i].debtMetric.newFeatureIssues;
                             improvementIssues = metrics[i].debtMetric.improvementIssues;
                        }
                        html += "<td>" + issues + "</td>";
						html += "<td>" + bugIssues + "</td>";
						html += "<td>" + newFeatureIssues + "</td>";
						html += "<td>" + improvementIssues + "</td>";
						html += "<td>" + metrics[i].file.score.toFixed(2) + "</td>";
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
					html += "<th>Index</th>";
					html += "<th>Id</th>";
					html += "<th>Package/Directory</th>";
					html += "<th>NOF</th>";
					html += "<th>NOM</th>";
					html += "<th>LOC</th>";
					html += "<th>Lines</th>";
					html += "<th>Ca</th>";
					html += "<th>Ce</th>";
					html += "<th>Instability</th>";
					html += "</tr>";
					var metrics = result[id];
					console.log(metrics);
					for(var i = 0; i < metrics.length; i++) {
						html += "<tr>";
						html += "<td>" + (i + 1) + "</td>";
						html += "<td>" + metrics[i].pck.id + "</td>";
						html += "<td><a target='_blank' href='/relation/package/" + metrics[i].pck.id + "'>" + metrics[i].pck.directoryPath + "</a></td>";
						html += "<td>" + metrics[i].nof + "</td>";
						html += "<td>" + metrics[i].nom + "</td>";
						html += "<td>" + metrics[i].loc + "</td>";
						html += "<td>" + metrics[i].lines + "</td>";
						html += "<td>" + metrics[i].fanIn + "</td>";
						html += "<td>" + metrics[i].fanOut + "</td>";
						html += "<td>" + (metrics[i].fanOut / (metrics[i].fanIn + metrics[i].fanOut)).toFixed(2) + "</td>";
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
				html += "<th>Index</th>";
				html += "<th>Id</th>";
				html += "<th>Project</th>";
				html += "<th>NOP</th>";
				html += "<th>NOF</th>";
				html += "<th>NOM</th>";
				html += "<th>LOC</th>";
				html += "<th>Lines</th>";
				html += "<th>Commits</th>";
				html += "<th>Modularity</th>";
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
					html += "<td id='modularity_" + result[i].project.id + "'>" + (result[i].modularity < 0 ? "计算中..." : result[i].modularity.toFixed(2)) + "</td>";
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