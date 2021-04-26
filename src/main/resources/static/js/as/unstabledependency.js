let unstableDependency = function() {
	let _unstableDependency = function(projects, fileUnstableDependencyMap, packageUnstableDependencyMap) {
		let html = "";

		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";

				let fileUnstableDependencyList = fileUnstableDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>File</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Instability</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Score</th>";
				html += "<th style='text-align: center; vertical-align: middle'>All Outgoing Dependencies</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Bad Outgoing Dependencies</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Commits</th>";
				html += "</tr>";
				let index = 1;
				for(let fileIndex in fileUnstableDependencyList) {
					if (fileUnstableDependencyList.hasOwnProperty(fileIndex)) {
						let fileUnstableDependency = fileUnstableDependencyList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + fileUnstableDependency.component.id + "'>" + fileUnstableDependency.component.path + "</a></td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (fileUnstableDependency.component.instability).toFixed(2) + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (fileUnstableDependency.component.score).toFixed(2) + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileUnstableDependency.allDependencies + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileUnstableDependency.badDependencies + "</td>";

						let allFilesIds = fileUnstableDependency.component.id;
						for(let j = 0; j < fileUnstableDependency.badDependsOns.length; j++) {
							allFilesIds += "," + fileUnstableDependency.badDependsOns[j].endNode.id;
						}

						html += "<td style='text-align: center; vertical-align: middle'>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + fileUnstableDependency.component.id + "&minCount=2'>commits</a>" + "</td>";
						html += "</tr>";
						index ++;
					}
				}

				let packageUnstableDependencyList = packageUnstableDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>Package</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Instability</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Score</th>";
				html += "<th style='text-align: center; vertical-align: middle'>All Outgoing Dependencies</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Bad Outgoing Dependencies</th>";
				html += "</tr>";
				index = 1;
				for(let packageIndex in packageUnstableDependencyList) {
					if (packageUnstableDependencyList.hasOwnProperty(packageIndex)) {
						let packageUnstableDependency = packageUnstableDependencyList[packageIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + packageUnstableDependency.component.id + "'>" + packageUnstableDependency.component.name + "</a></td>";
						let pck_instability = packageUnstableDependency.component.instability != null ? (packageUnstableDependency.component.instability).toFixed(2) : "NULL"
						html += "<td style='text-align: center; vertical-align: middle'>" + pck_instability  + "</td>";
						let pck_score = packageUnstableDependency.component.score != null ? (packageUnstableDependency.component.score).toFixed(2) : "NULL"
						html += "<td style='text-align: center; vertical-align: middle'>" + pck_score + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + packageUnstableDependency.allDependencies + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + packageUnstableDependency.badDependencies + "</td>";
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
		let setProjectMinFanOutInstability = function(projectId, minFileFanOut, minPackageFanOut, minRatio) {
			$.ajax({
				type: "post",
				url: "/as/unstable/threshold/instability/" + projectId 
					+ "?minFileFanOut=" + minFileFanOut
					+ "&minPackageFanOut=" + minPackageFanOut
					+ "&minRatio=" + minRatio,
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
		$("#unstableInstabilityThresholdSave").click(function() {
			let projectId = $("#unstableDependencyProjects").val();
			let minFileFanOut = $("#unstableMinFileFanOut").val();
			let minPackageFanOut = $("#unstableMinPackageFanOut").val();
			let minRatio = $("#unstableMinRatio").val();
			setProjectMinFanOutInstability(projectId, minFileFanOut, minPackageFanOut, minRatio);
		})
	}
	
	let _get = function() {
		let getProjectMinFanOutInstability = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/unstable/threshold/instability/" + projectId,
				success: function(result) {
					$("#unstableMinFileFanOut").val(result[0]);
					$("#unstableMinPackageFanOut").val(result[1]);
					$("#unstableMinRatio").val(result[2]);
				}
			})
		};
		$("#unstableDependencyProjects").change(function() {
			getProjectMinFanOutInstability($(this).val());
		})
		if($("#unstableDependencyProjects").val() != null) {
			getProjectMinFanOutInstability($("#unstableDependencyProjects").val());
		}
		
	}
	
	return {
		init : function() {
			_save();
			_get();
		},
		unstableDependency: function(projects, fileUnstableDependencyMap, packageUnstableDependencyMap) {
			_unstableDependency(projects, fileUnstableDependencyMap, packageUnstableDependencyMap);
		}
	}
}
