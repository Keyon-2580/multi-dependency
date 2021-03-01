var smellMetric = function() {
	var allSmellMetric = function(projects) {
    		$.ajax({
    			type: "get",
    			url: "/metric/smellMetric",
    			success: function(result) {
    				console.log(result);
    				var html = "";
    				for(var id in projects) {
    					html += "<div><h4>" + projects[id].name + " (" + projects[id].language + ")" + "</h4></div>";
    					html += "<div><button name='fileTable_" + id + "' class='btn btn-primary file_excel_button'>输出 excel</button></div>";
    					html += "<div><table id='fileTable_" + id + "' class='table table-bordered'>";
    					html += "<tr>";
    					html += "<th>Index</th>";
    					html += "<th>Project</th>";
    					html += "<th>Smell</th>";
    					html += "<th>Size</th>";
    					html += "<th>LOC</th>";
    					html += "<th>NOC</th>";
    					html += "<th>NOM</th>";
    					html += "<th>Commits</th>";
    					html += "<th>Developers</th>";
    					html += "<th>CoCommits</th>";
    					html += "<th>CoFiles</th>";
    					html += "<th>Issues</th>";
    					html += "<th>Bugs</th>";
    					html += "<th>NewFeatures</th>";
    					html += "<th>Improvements</th>";
    					html += "</tr>";
    					var metrics = result[id];
    					console.log(metrics);
    					for(var i = 0; i < metrics.length; i++) {
    						console.log(metrics[i]);
    						html += "<tr>";
    						html += "<td>" + (i + 1) + "</td>";
    						html += "<td>" + metrics[i].smell.projectName + "</td>";
    						html += "<td>" + metrics[i].smell.name + "</td>";
                            var size = 0;
                            var loc = 0;
                            var noc = 0;
                            var nom = 0;
    						if(metrics[i].structureMetric != null){
    						    size = metrics[i].structureMetric.size;
    						    loc = metrics[i].structureMetric.loc;
    						    noc = metrics[i].structureMetric.noc;
    						    nom = metrics[i].structureMetric.nom;

    						}
    						html += "<td>" + size + "</td>";
    						html += "<td>" + loc + "</td>";
    						html += "<td>" + noc + "</td>";
    						html += "<td>" + nom + "</td>";
                            var commits = 0;
                            var developers = 0;
                            if(metrics[i].evolutionMetric != null){
                                 commits = metrics[i].evolutionMetric.commits;
                                 developers = metrics[i].evolutionMetric.developers;
                            }
                            var coCommits = 0;
                            var CoFiles = 0;
                            if(metrics[i].coChangeMetric != null){
                                 coCommits = metrics[i].coChangeMetric.coChangeCommits;
                                 CoFiles = metrics[i].coChangeMetric.coChangeFileCount;
                            }
    						html += "<td>" + commits + "</td>";
    						html += "<td>" + developers + "</td>";
    						html += "<td>" + coCommits + "</td>";
    						html += "<td>" + CoFiles + "</td>";
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
//    						html += "<td>" + metrics[i].file.score.toFixed(2) + "</td>";
    						html += "</tr>";
    					}
    					html += "</table></div>";
    				}
    				$("#smellMetric").html(html);
//    				$(".smell_excel_button").click(function() {
//    					tableToExcel($(this).attr("name"), "allSmellMetric");
//    				});
    			}
    		})
    	}
	return {
		init: function() {
			$.ajax({
				type: "get",
				url: "/project/all",
				success: function(result) {
					console.log(result);
					allSmellMetric(result);
				}
			});
		}
	}
}