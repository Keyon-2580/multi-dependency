let unstable = function(cytoscapeutil) {
	let _unstable = function(projects, fileUnstableDependencyMap, packageUnstableDependencyMap) {
		let html = "";

		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";
				let fileUnstableDependencyList = fileUnstableDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>Index</th>";
				html += "<th>File</th>";
				html += "<th>Instability</th>";
				html += "<th>Score</th>";
				html += "<th>All Outgoing Dependencies</th>";
				html += "<th>Bad Outgoing Dependencies</th>";
				html += "<th>Commits</th>";
				html += "</tr>";
				let index = 1;
				for(let fileIndex in fileUnstableDependencyList) {
					if (fileUnstableDependencyList.hasOwnProperty(fileIndex)) {
						let fileUnstableDependency = fileUnstableDependencyList[fileIndex];
						html += "<tr>";
						html += "<td>" + index + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + fileUnstableDependency.component.id + "'>" + fileUnstableDependency.component.path + "</a></td>";
						html += "<td>" + (fileUnstableDependency.component.instability).toFixed(2) + "</td>";
						html += "<td>" + (fileUnstableDependency.component.score).toFixed(2) + "</td>";
						html += "<td>" + fileUnstableDependency.allDependencies + "</td>";
						html += "<td>" + fileUnstableDependency.badDependencies + "</td>";

						let allFilesIds = fileUnstableDependency.component.id;
						for(let j = 0; j < fileUnstableDependency.badDependsOns.length; j++) {
							allFilesIds += "," + fileUnstableDependency.badDependsOns[j].endNode.id;
						}

						html += "<td>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + fileUnstableDependency.component.id + "&minCount=2'>commits</a>" + "</td>";
						html += "</tr>";
						index ++;
					}
				}

				let packageUnstableDependencyList = packageUnstableDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>Index</th>";
				html += "<th>Package</th>";
				html += "<th>Instability</th>";
				html += "<th>Score</th>";
				html += "<th>All Outgoing Dependencies</th>";
				html += "<th>Bad Outgoing Dependencies</th>";
				html += "</tr>";
				index = 1;
				for(let packageIndex in packageUnstableDependencyList) {
					if (packageUnstableDependencyList.hasOwnProperty(packageIndex)) {
						let packageUnstableDependency = packageUnstableDependencyList[packageIndex];
						html += "<tr>";
						html += "<td>" + index + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + packageUnstableDependency.component.id + "'>" + packageUnstableDependency.component.name + "</a></td>";
						let pck_instability = packageUnstableDependency.component.instability != null ? (packageUnstableDependency.component.instability).toFixed(2) : "NULL"
						html += "<td>" + pck_instability  + "</td>";
						let pck_score = packageUnstableDependency.component.score != null ? (packageUnstableDependency.component.score).toFixed(2) : "NULL"
						html += "<td>" + pck_score + "</td>";
						html += "<td>" + packageUnstableDependency.allDependencies + "</td>";
						html += "<td>" + packageUnstableDependency.badDependencies + "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
			}
		}
		
		$("#content").html(html);
	}
	
	let _save = function() {
		// let setHistoryThreshold = function(projectId, fanInThreshold, cochangeTimesThreshold, cochangeFilesThreshold) {
		// 	$.ajax({
		// 		type: "post",
		// 		url: "/as/unstable/threshold/history/" + projectId 
		// 			+ "?fanInThreshold=" + fanInThreshold
		// 			+ "&cochangeTimesThreshold=" + cochangeTimesThreshold
		// 			+ "&cochangeFilesThreshold=" + cochangeFilesThreshold,
		// 		success: function(result) {
		// 			if (result === true) {
		// 				alert("修改成功");
		// 			}
		// 			else {
		// 				alert("修改失败");
		// 			}
		// 		}
		// 	});
		// };
		let setInstabilityThreshold = function(projectId, unstableFileFanOutThreshold, unstableModuleFanOutThreshold, unstableRatioThreshold) {
			$.ajax({
				type: "post",
				url: "/as/unstable/threshold/instability/" + projectId 
					+ "?fileFanOutThreshold=" + unstableFileFanOutThreshold
					+ "&moduleFanOutThreshold=" + unstableModuleFanOutThreshold
					+ "&ratioThreshold=" + unstableRatioThreshold,
				success: function(result) {
					if (result === true) {
						alert("修改成功");
					}
					else {
						alert("修改失败");
					}
				}
			});
		};
		// $("#unstableHistoryThresholdSave").click(function() {
		// 	let fanInThreshold = $("#unstableFanInThreshold").val();
		// 	let cochangeTimesThreshold = $("#unstableCoChangeTimesThreshold").val();
		// 	let cochangeFilesThreshold = $("#unstableCoChangeFilesThreshold").val();
		// 	let projectId = $("#unstableDependencyProjects").val();
		// 	setHistoryThreshold(projectId, fanInThreshold, cochangeTimesThreshold, cochangeFilesThreshold);
		// });
		
		$("#unstableInstabilityThresholdSave").click(function() {
			let unstableFileFanOutThreshold = $("#unstableFileFanOutThreshold").val();
			let unstableModuleFanOutThreshold = $("#unstableModuleFanOutThreshold").val();
			let unstableRatioThreshold = $("#unstableRatioThreshold").val();
			let projectId = $("#unstableDependencyProjects").val();
			setInstabilityThreshold(projectId, unstableFileFanOutThreshold, unstableModuleFanOutThreshold, unstableRatioThreshold);
		})
	}
	
	let _get = function() {
		let getHistoryThreshold = function(projectId) {
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
		let getInstabilityThreshold = function(projectId) {
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
		unstable: function(projects, fileUnstableDependencyMap, packageUnstableDependencyMap) {
			_unstable(projects, fileUnstableDependencyMap, packageUnstableDependencyMap);
		}
	}
}
