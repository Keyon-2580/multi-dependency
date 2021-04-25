let hublike = function(cytoscapeutil) {
	let _hublike = function(projects, fileHubLikes, packageHubLikes) {
		let html = "";

		for (let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";
				let fileHubLikeList = fileHubLikes[project.id];
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
				let index = 0;
				for (let fileIndex in fileHubLikeList) {
					if (fileHubLikeList.hasOwnProperty(fileIndex)) {
						let fileHubLike = fileHubLikeList[fileIndex];
						index ++;
						html += "<tr>";
						html += "<td>" + index + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + fileHubLike.file.id + "'>" + fileHubLike.file.path + "</a></td>";
						html += "<td>" + fileHubLike.fanIn + "</td>";
						let inRatio = (fileHubLike.coChangeFilesIn.length / fileHubLike.fanIn).toFixed(2);
						html += "<td>" + fileHubLike.coChangeFilesIn.length + "/" + fileHubLike.fanIn + "=" + inRatio + "</td>";
						html += "<td>" + fileHubLike.fanOut + "</td>";
						let outRatio = (fileHubLike.coChangeFilesOut.length / fileHubLike.fanOut).toFixed(2);
						html += "<td>" + fileHubLike.coChangeFilesOut.length + "/" + fileHubLike.fanOut + "=" + outRatio + "</td>";
						let allIORatio = ((fileHubLike.coChangeFilesIn.length + fileHubLike.coChangeFilesOut.length) / (fileHubLike.fanIn + fileHubLike.fanOut)).toFixed(2);
						html += "<td>(" + fileHubLike.coChangeFilesIn.length + "+" + fileHubLike.coChangeFilesOut.length + ")/(" ;
						html += fileHubLike.fanIn + "+" + fileHubLike.fanOut + ")=" + allIORatio + "</td>";

						let allFilesIds = fileHubLike.file.id;
						for (let j = 0; j < fileHubLike.coChangeFilesIn.length; j++) {
							allFilesIds += "," + fileHubLike.coChangeFilesIn[j].id;
						}
						for (let j = 0; j < fileHubLike.coChangeFilesOut.length; j++) {
							allFilesIds += "," + fileHubLike.coChangeFilesOut[j].id;
						}

						html += "<td>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + fileHubLike.file.id + "&minCount=2'>commits</a>" + "</td>";

						html += "<td>" + (fileHubLike.file.score).toFixed(2) + "</td>";
						html += "</tr>";
					}
				}

				let packageHubLikeList = packageHubLikes[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>Package</th>";
				html += "<th>Ca（afferent couplings）</th>";
				html += "<th>Ce（efferent couplings）</th>";
				html += "</tr>";
				for (let packageIndex in packageHubLikeList) {
					if (packageHubLikeList.hasOwnProperty(packageIndex)) {
						let packageHubLike = packageHubLikeList[packageIndex];
						html += "<tr>";
						html += "<td>" + packageHubLike.pck.name + "</td>";
						html += "<td>" + packageHubLike.fanIn + "</td>";
						html += "<td>" + packageHubLike.fanOut + "</td>";
						html += "</tr>";
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
					console.log(result);
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
		hublike: function(projects, fileHubLikes, packageHubLikes) {
			_hublike(projects, fileHubLikes, packageHubLikes);
		}
	}
}
