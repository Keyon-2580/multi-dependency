let hubLikeDependency = function() {
	let _hubLikeDependency = function(projects, fileHubLikeDependencyMap, packageHubLikeDependencyMap) {
		let html = "";
		for (let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";

				let fileHubLikeDependencyList = fileHubLikeDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>File</th>";
				html += "<th style='text-align: center; vertical-align: middle'>FanIn</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-ChangeFilesIn/FanIn</th>";
				html += "<th style='text-align: center; vertical-align: middle'>FanOut</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-ChangeFilesOut/FanOut</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-ChangeFilesAll/(FanIn+FinOut)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-ChangeCommits</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Score</th>";
				html += "</tr>";
				let index = 1;
				for (let fileIndex in fileHubLikeDependencyList) {
					if (fileHubLikeDependencyList.hasOwnProperty(fileIndex)) {
						let fileHubLikeDependency = fileHubLikeDependencyList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + fileHubLikeDependency.file.id + "'>" + fileHubLikeDependency.file.path + "</a></td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileHubLikeDependency.fanIn + "</td>";
						let inRatio = (fileHubLikeDependency.coChangeFilesIn.length / fileHubLikeDependency.fanIn).toFixed(2);
						html += "<td style='text-align: center; vertical-align: middle'>" + fileHubLikeDependency.coChangeFilesIn.length + "/" + fileHubLikeDependency.fanIn + "=" + inRatio + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileHubLikeDependency.fanOut + "</td>";
						let outRatio = (fileHubLikeDependency.coChangeFilesOut.length / fileHubLikeDependency.fanOut).toFixed(2);
						html += "<td style='text-align: center; vertical-align: middle'>" + fileHubLikeDependency.coChangeFilesOut.length + "/" + fileHubLikeDependency.fanOut + "=" + outRatio + "</td>";
						let allIORatio = ((fileHubLikeDependency.coChangeFilesIn.length + fileHubLikeDependency.coChangeFilesOut.length) / (fileHubLikeDependency.fanIn + fileHubLikeDependency.fanOut)).toFixed(2);
						html += "<td style='text-align: center; vertical-align: middle'>(" + fileHubLikeDependency.coChangeFilesIn.length + "+" + fileHubLikeDependency.coChangeFilesOut.length + ")/(" ;
						html += fileHubLikeDependency.fanIn + "+" + fileHubLikeDependency.fanOut + ")=" + allIORatio + "</td>";
						let allFilesIds = fileHubLikeDependency.file.id;
						for (let j = 0; j < fileHubLikeDependency.coChangeFilesIn.length; j++) {
							allFilesIds += "," + fileHubLikeDependency.coChangeFilesIn[j].id;
						}
						for (let j = 0; j < fileHubLikeDependency.coChangeFilesOut.length; j++) {
							allFilesIds += "," + fileHubLikeDependency.coChangeFilesOut[j].id;
						}
						html += "<td style='text-align: center; vertical-align: middle'>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + fileHubLikeDependency.file.id + "&minCount=2'>commits</a>" + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (fileHubLikeDependency.file.score).toFixed(2) + "</td>";
						html += "</tr>";
						index ++;
					}
				}

				let packageHubLikeDependencyList = packageHubLikeDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>Package</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Ca（afferent couplings）</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Ce（efferent couplings）</th>";
				html += "</tr>";
				index = 1;
				for (let packageIndex in packageHubLikeDependencyList) {
					if (packageHubLikeDependencyList.hasOwnProperty(packageIndex)) {
						let packageHubLikeDependency = packageHubLikeDependencyList[packageIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td>" + packageHubLikeDependency.pck.name + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + packageHubLikeDependency.fanIn + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + packageHubLikeDependency.fanOut + "</td>";
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
		let setProjectMinFanIO = function(projectId, minFileFanIn, minFileFanOut, minPackageFanIn, minPackageFanOut) {
			$.ajax({
				type: "post",
				url: "/as/hublike/fanio/" + projectId
					+ "?minFileFanIn=" + minFileFanIn
					+ "&minFileFanOut=" + minFileFanOut
					+ "&minPackageFanIn=" + minPackageFanIn
					+ "&minPackageFanOut=" + minPackageFanOut,
				success: function(result) {
					if (result === true) {
						alert("修改成功");
					}
					else {
						alert("修改失败");
					}
				}
			})
		}
		$("#hubLikeMinFanIOSave").click(function() {
			let projectId = $("#hubLikeDependencyProjects").val();
			let minFileFanIn = $("#hubLikeMinFileFanIn").val();
			let minFileFanOut = $("#hubLikeMinFileFanOut").val();
			let minPackageFanIn = $("#hubLikeMinPackageFanIn").val();
			let minPackageFanOut = $("#hubLikeMinPackageFanOut").val();
			setProjectMinFanIO(projectId, minFileFanIn, minFileFanOut, minPackageFanIn, minPackageFanOut)
		});
	}
	
	let _get = function() {
		let getProjectMinFanIO = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/hublike/fanio/" + projectId,
				success: function(result) {
					$("#hubLikeMinFileFanIn").val(result[0]);
					$("#hubLikeMinFileFanOut").val(result[1]);
					$("#hubLikeMinPackageFanIn").val(result[2]);
					$("#hubLikeMinPackageFanOut").val(result[3]);
				}
			})
		}
		$("#hubLikeDependencyProjects").change(function() {
			getProjectMinFanIO($(this).val())
		})
		if($("#hubLikeDependencyProjects").val() != null) {
			getProjectMinFanIO($("#hubLikeDependencyProjects").val());
		}
	}
	
	return {
		init : function() {
			_save();
			_get();
		},
		hubLikeDependency: function(projects, fileHubLikeDependencyMap, packageHubLikeDependencyMap) {
			_hubLikeDependency(projects, fileHubLikeDependencyMap, packageHubLikeDependencyMap);
		}
	}
}
