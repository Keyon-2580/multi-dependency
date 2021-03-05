var unstable = function(cytoscapeutil) {
	var _unstable = function(projects, filesUsingInstability, filesUsingHistory, modules) {
		var html = "";

		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			var fileUnstables = filesUsingInstability[project.id];
			console.log(fileUnstables);
			html += "<h5>Instability</h5>";
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>File</th>";
			html += "<th>Instability</th>";
			html += "<th>Score</th>";
			html += "<th>All Outgoing Dependencies</th>";
			html += "<th>Bad Outgoing Dependencies</th>";
			html += "<th></th>";
			html += "</tr>";
			for(var fileIndex in fileUnstables) {
				var file = fileUnstables[fileIndex];
				console.log(file);
				html += "<tr>";
				html += "<td><a target='_blank' href='/relation/file/" + file.component.id + "'>" + file.component.path + "</a></td>";
				html += "<td>" + (file.component.instability).toFixed(2) + "</td>";
				html += "<td>" + (file.component.score).toFixed(2) + "</td>";
				html += "<td>" + file.allDependencies + "</td>";
				html += "<td>" + file.badDependencies + "</td>";
				
				var allFilesIds = file.component.id;
				console.log(file.badDependsOns);
				for(var j = 0; j < file.badDependsOns.length; j++) {
					allFilesIds += "," + file.badDependsOns[j].endNode.id;
				}
				
				html += "<td>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + file.component.id + "&minCount=2'>commits</a>" + "</td>";
				html += "</tr>";
			}
			html += "</table>";
			fileUnstables = filesUsingHistory[project.id];
			html += "<h5>History</h5>";
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>File</th>";
			html += "<th>Instability</th>";
			html += "<th>Score</th>";
			html += "<th>Fan In </th>";
			html += "<th>Co-change Files</th>";
			html += "<th>Co-changeFiles/FanIn</th>";
			html += "<th>commits</th>";
			html += "</tr>";
			for(var fileIndex in fileUnstables) {
				var file = fileUnstables[fileIndex];
				var count = 0;
				for(var i in file.cochangeTimesWithFile) {
					count++;
				}
				console.log(file);
				html += "<tr>";
				html += "<td><a target='_blank' href='/relation/file/" + file.component.id + "'>" + file.component.path + "</a></td>";
				html += "<td>" + (file.component.instability).toFixed(2) + "</td>";
				html += "<td>" + (file.component.score).toFixed(2) + "</td>";
				html += "<td>" + file.fanIn + "</td>";
				html += "<td>" + file.cochangeFiles.length + "</td>";
				html += "<td>" + (file.cochangeFiles.length / file.fanIn).toFixed(2) + "</td>";
				
				var allFilesIds = file.component.id;
				for(var j = 0; j < file.cochangeFiles.length; j++) {
					allFilesIds += "," + file.cochangeFiles[j].id;
				}
				
				html += "<td>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + file.component.id + "&minCount=2'>commits</a>" + "</td>";
				
				html += "</tr>";
			}
			html += "</table>";

			var moduleUnstables = modules[project.id];
			html += "<h5>Unstable Modules</h5>";
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>Module</th>";
			html += "<th>Instability</th>";
			html += "<th>Score</th>";
			html += "<th>All Outgoing Dependencies</th>";
			html += "<th>Bad Outgoing Dependencies</th>";
			html += "</tr>";
			for(var moduleIndex in moduleUnstables) {
				var module = moduleUnstables[moduleIndex];
				console.log(module);
				html += "<tr>";
				html += "<td><a target='_blank' href='/relation/file/" + module.component.id + "'>" + module.component.name + "</a></td>";
				var pck_instability = module.component.instability != null ? (module.component.instability).toFixed(2) : "NULL"
				html += "<td>" + pck_instability  + "</td>";
				var pck_score = module.component.score != null ? (module.component.score).toFixed(2) : "NULL"
				html += "<td>" + pck_score + "</td>";
				html += "<td>" + module.allDependencies + "</td>";
				html += "<td>" + module.badDependencies + "</td>";
				html += "</tr>";
			}
			html += "</table>";
		}
		
		$("#content").html(html);
	}
	
	var _save = function() {
		var setHistoryThreshold = function(projectId, fanInThreshold, cochangeTimesThreshold, cochangeFilesThreshold) {
			$.ajax({
				type: "post",
				url: "/as/unstable/threshold/history/" + projectId 
					+ "?fanInThreshold=" + fanInThreshold
					+ "&cochangeTimesThreshold=" + cochangeTimesThreshold
					+ "&cochangeFilesThreshold=" + cochangeFilesThreshold,
				success: function(result) {
					if(result == true) {
						alert("修改成功");
					} else {
						alert("修改失败");
					}
				}
			});
		};
		var setInstabilityThreshold = function(projectId, unstableFileFanOutThreshold, unstableModuleFanOutThreshold, unstableRatioThreshold) {
			$.ajax({
				type: "post",
				url: "/as/unstable/threshold/instability/" + projectId 
					+ "?fileFanOutThreshold=" + unstableFileFanOutThreshold
					+ "&moduleFanOutThreshold=" + unstableModuleFanOutThreshold
					+ "&ratioThreshold=" + unstableRatioThreshold,
				success: function(result) {
					if(result == true) {
						alert("修改成功");
					} else {
						alert("修改失败");
					}
				}
			});
		};
		$("#unstableHistoryThresholdSave").click(function() {
			var fanInThreshold = $("#unstableFanInThreshold").val();
			var cochangeTimesThreshold = $("#unstableCoChangeTimesThreshold").val();
			var cochangeFilesThreshold = $("#unstableCoChangeFilesThreshold").val();
			var projectId = $("#unstableDependencyProjects").val();
			setHistoryThreshold(projectId, fanInThreshold, cochangeTimesThreshold, cochangeFilesThreshold);
		});
		
		$("#unstableInstabilityThresholdSave").click(function() {
			var unstableFileFanOutThreshold = $("#unstableFileFanOutThreshold").val();
			var unstableModuleFanOutThreshold = $("#unstableModuleFanOutThreshold").val();
			var unstableRatioThreshold = $("#unstableRatioThreshold").val();
			var projectId = $("#unstableDependencyProjects").val();
			setInstabilityThreshold(projectId, unstableFileFanOutThreshold, unstableModuleFanOutThreshold, unstableRatioThreshold);
		})
	}
	
	var _get = function() {
		var getHistoryThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/unstable/threshold/history/" + projectId,
				success: function(result) {
					console.log(result);
					$("#unstableFanInThreshold").val(result[0]);
					$("#unstableCoChangeTimesThreshold").val(result[1]);
					$("#unstableCoChangeFilesThreshold").val(result[2]);
				}
			})
		};
		var getInstabilityThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/unstable/threshold/instability/" + projectId,
				success: function(result) {
					console.log(result);
					$("#unstableFileFanOutThreshold").val(result[0]);
					$("#unstableModuleFanOutThreshold").val(result[1]);
					$("#unstableRatioThreshold").val(result[2]);
				}
			})
		};
		$("#unstableDependencyProjects").change(function() {
			getHistoryThreshold($(this).val())
			getInstabilityThreshold($(this).val());
		})
		if($("#unstableDependencyProjects").val() != null) {
			getHistoryThreshold($("#unstableDependencyProjects").val());
			getInstabilityThreshold($("#unstableDependencyProjects").val());
		}
		
	}
	
	return {
		init : function() {
			_save();
			_get();
		},
		unstable: function(projects, filesUsingInstability, filesUsingHistory, modules) {
			_unstable(projects, filesUsingInstability, filesUsingHistory, modules);
		}
	}
}
