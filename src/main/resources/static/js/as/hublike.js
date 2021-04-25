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
				html += "<th>Index</th>";
				html += "<th>File</th>";
				html += "<th>FanIn</th>";
				html += "<th>Co-changeFilesIn/FanIn</th>";
				html += "<th>FanOut</th>";
				html += "<th>Co-changeFilesOut/FanOut</th>";
				html += "<th>Co-changeFilesAll/(FanIn+FinOut)</th>";
				html += "<th>Co-changeCommits</th>";
				html += "<th>Score</th>";
				html += "</tr>";
				let index = 1;
				for (let fileIndex in fileHubLikeDependencyList) {
					if (fileHubLikeDependencyList.hasOwnProperty(fileIndex)) {
						let fileHubLikeDependency = fileHubLikeDependencyList[fileIndex];
						html += "<tr>";
						html += "<td>" + index + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + fileHubLikeDependency.file.id + "'>" + fileHubLikeDependency.file.path + "</a></td>";
						html += "<td>" + fileHubLikeDependency.fanIn + "</td>";
						let inRatio = (fileHubLikeDependency.coChangeFilesIn.length / fileHubLikeDependency.fanIn).toFixed(2);
						html += "<td>" + fileHubLikeDependency.coChangeFilesIn.length + "/" + fileHubLikeDependency.fanIn + "=" + inRatio + "</td>";
						html += "<td>" + fileHubLikeDependency.fanOut + "</td>";
						let outRatio = (fileHubLikeDependency.coChangeFilesOut.length / fileHubLikeDependency.fanOut).toFixed(2);
						html += "<td>" + fileHubLikeDependency.coChangeFilesOut.length + "/" + fileHubLikeDependency.fanOut + "=" + outRatio + "</td>";
						let allIORatio = ((fileHubLikeDependency.coChangeFilesIn.length + fileHubLikeDependency.coChangeFilesOut.length) / (fileHubLikeDependency.fanIn + fileHubLikeDependency.fanOut)).toFixed(2);
						html += "<td>(" + fileHubLikeDependency.coChangeFilesIn.length + "+" + fileHubLikeDependency.coChangeFilesOut.length + ")/(" ;
						html += fileHubLikeDependency.fanIn + "+" + fileHubLikeDependency.fanOut + ")=" + allIORatio + "</td>";
						let allFilesIds = fileHubLikeDependency.file.id;
						for (let j = 0; j < fileHubLikeDependency.coChangeFilesIn.length; j++) {
							allFilesIds += "," + fileHubLikeDependency.coChangeFilesIn[j].id;
						}
						for (let j = 0; j < fileHubLikeDependency.coChangeFilesOut.length; j++) {
							allFilesIds += "," + fileHubLikeDependency.coChangeFilesOut[j].id;
						}
						html += "<td>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + fileHubLikeDependency.file.id + "&minCount=2'>commits</a>" + "</td>";
						html += "<td>" + (fileHubLikeDependency.file.score).toFixed(2) + "</td>";
						html += "</tr>";
						index ++;
					}
				}

				let packageHubLikeDependencyList = packageHubLikeDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>Index</th>";
				html += "<th>Package</th>";
				html += "<th>Ca（afferent couplings）</th>";
				html += "<th>Ce（efferent couplings）</th>";
				html += "</tr>";
				index = 1;
				for (let packageIndex in packageHubLikeDependencyList) {
					if (packageHubLikeDependencyList.hasOwnProperty(packageIndex)) {
						let packageHubLikeDependency = packageHubLikeDependencyList[packageIndex];
						html += "<tr>";
						html += "<td>" + packageHubLikeDependency.pck.name + "</td>";
						html += "<td>" + packageHubLikeDependency.fanIn + "</td>";
						html += "<td>" + packageHubLikeDependency.fanOut + "</td>";
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
		let setMinFanIO = function(projectId, hubLikeMinFileFanIn, hubLikeMinFileFanOut, hubLikeMinPackageFanIn, hubLikeMinPackageFanOut) {
			$.ajax({
				type: "post",
				url: "/as/hublike/fanio/" + projectId
					+ "?hubLikeMinFileFanIn=" + hubLikeMinFileFanIn
					+ "&hubLikeMinFileFanOut=" + hubLikeMinFileFanOut
					+ "&hubLikeMinPackageFanIn=" + hubLikeMinPackageFanIn
					+ "&hubLikeMinPackageFanOut=" + hubLikeMinPackageFanOut,
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
			let hubLikeMinFileFanIn = $("#hubLikeMinFileFanIn").val();
			let hubLikeMinFileFanOut = $("#hubLikeMinFileFanOut").val();
			let hubLikeMinPackageFanIn = $("#hubLikeMinPackageFanIn").val();
			let hubLikeMinPackageFanOut = $("#hubLikeMinPackageFanOut").val();
			setMinFanIO(projectId, hubLikeMinFileFanIn, hubLikeMinFileFanOut, hubLikeMinPackageFanIn, hubLikeMinPackageFanOut)
		});
	}
	
	let _get = function() {
		let getMinFanIO = function(projectId) {
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
			getMinFanIO($(this).val())
		})
		if($("#hubLikeDependencyProjects").val() != null) {
			getMinFanIO($("#hubLikeDependencyProjects").val());
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
