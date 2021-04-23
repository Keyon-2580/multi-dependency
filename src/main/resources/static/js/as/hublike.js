var hublike = function(cytoscapeutil) {
	var _hublike = function(projects, fileHubLikes, packageHubLikes) {
		var html = "";

		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			var fileHubLikeList = fileHubLikes[project.id];
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
			var index = 0;
			for(var fileIndex in fileHubLikeList) {
				var fileHubLike = fileHubLikeList[fileIndex];
				console.log(fileIndex);
				index ++;
				html += "<tr>";
				html += "<td>" + index + "</td>";
				html += "<td><a target='_blank' href='/relation/file/" + fileHubLike.file.id + "'>" + fileHubLike.file.path + "</a></td>";
				html += "<td>" + fileHubLike.fanIn + "</td>";
				var inRatio = (fileHubLike.coChangeFilesIn.length / fileHubLike.fanIn).toFixed(2);
				html += "<td>" + fileHubLike.coChangeFilesIn.length + "/" + fileHubLike.fanIn + "=" + inRatio + "</td>";
				html += "<td>" + fileHubLike.fanOut + "</td>";
				var outRatio = (fileHubLike.coChangeFilesOut.length / fileHubLike.fanOut).toFixed(2);
                html += "<td>" + fileHubLike.coChangeFilesOut.length + "/" + fileHubLike.fanOut + "=" + outRatio + "</td>";
                var allIORatio = ((fileHubLike.coChangeFilesIn.length + fileHubLike.coChangeFilesOut.length) / (fileHubLike.fanIn + fileHubLike.fanOut)).toFixed(2);
                html += "<td>(" + fileHubLike.coChangeFilesIn.length + "+" + fileHubLike.coChangeFilesOut.length + ")/(" ;
				html += fileHubLike.fanIn + "+" + fileHubLike.fanOut + ")=" + allIORatio + "</td>";

				var allFilesIds = fileHubLike.file.id;
                for(var j = 0; j < fileHubLike.coChangeFilesIn.length; j++) {
                    allFilesIds += "," + fileHubLike.coChangeFilesIn[j].id;
                }
                for(var j = 0; j < fileHubLike.coChangeFilesOut.length; j++) {
                    allFilesIds += "," + fileHubLike.coChangeFilesOut[j].id;
                }

                html += "<td>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + fileHubLike.file.id + "&minCount=2'>commits</a>" + "</td>";

				html += "<td>" + (fileHubLike.file.score).toFixed(2) + "</td>";
				html += "</tr>";
			}
			
			var packageHubLikeList = packageHubLikes[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th width='50%'>Package</th>";
			html += "<th width='25%'>Ca（afferent couplings）</th>";
			html += "<th width='25%'>Ce（efferent couplings）</th>";
			html += "</tr>";
			for(var packageIndex in packageHubLikeList) {
				var packageHubLike = packageHubLikeList[packageIndex];
				html += "<tr>";
				html += "<td>" + packageHubLike.pck.name + "</td>";
				html += "<td>" + packageHubLike.fanIn + "</td>";
				html += "<td>" + packageHubLike.fanOut + "</td>";
				html += "</tr>";
			}
			html += "</table>";
		}
		
		$("#content").html(html);
	}
	
	var _save = function() {
		var setMinFanIO = function(projectId, 
				hubLikeMinFileFanIn, hubLikeMinFileFanOut,
				hubLikeMinPackageFanIn, hubLikeMinPackageFanOut) {
			$.ajax({
				type: "post",
				url: "/as/hublike/fanio/" + projectId
					+ "?hubLikeMinFileFanIn=" + hubLikeMinFileFanIn
					+ "&hubLikeMinFileFanOut=" + hubLikeMinFileFanOut
					+ "&hubLikeMinPackageFanIn=" + hubLikeMinPackageFanIn
					+ "&hubLikeMinPackageFanOut=" + hubLikeMinPackageFanOut,
				success: function(result) {
					if(result == true) {
						alert("修改成功");
					} else {
						alert("修改失败");
					}
				}
			})
		}
		$("#hubLikeMinFanIOSave").click(function() {
			var projectId = $("#hubLikeDependencyProjects").val();
			var hubLikeMinFileFanIn = $("#hubLikeMinFileFanIn").val();
			var hubLikeMinFileFanOut = $("#hubLikeMinFileFanOut").val();
			var hubLikeMinPackageFanIn = $("#hubLikeMinPackageFanIn").val();
			var hubLikeMinPackageFanOut = $("#hubLikeMinPackageFanOut").val();
			setMinFanIO(projectId, hubLikeMinFileFanIn, hubLikeMinFileFanOut, hubLikeMinPackageFanIn, hubLikeMinPackageFanOut)
		});
	}
	
	var _get = function() {
		var getMinFanIO = function(projectId) {
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
