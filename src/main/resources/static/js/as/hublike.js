var hublike = function(cytoscapeutil) {
	var _hublike = function(projects, files, modules) {
		var html = "";

		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			var hubLikeFiles = files[project.id];
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
			for(var fileIndex in hubLikeFiles) {
				var hubLikeFile = hubLikeFiles[fileIndex];
				console.log(fileIndex);
				index ++;
				html += "<tr>";
				html += "<td>" + index + "</td>";
				html += "<td><a target='_blank' href='/relation/file/" + hubLikeFile.file.id + "'>" + hubLikeFile.file.path + "</a></td>";
				html += "<td>" + hubLikeFile.fanIn + "</td>";
				var inRatio = (hubLikeFile.coChangeFilesIn.length / hubLikeFile.fanIn).toFixed(2);
				html += "<td>" + hubLikeFile.coChangeFilesIn.length + "/" + hubLikeFile.fanIn + "=" + inRatio + "</td>";
				html += "<td>" + hubLikeFile.fanOut + "</td>";
				var outRatio = (hubLikeFile.coChangeFilesOut.length / hubLikeFile.fanOut).toFixed(2);
                html += "<td>" + hubLikeFile.coChangeFilesOut.length + "/" + hubLikeFile.fanOut + "=" + outRatio + "</td>";
                var allIORatio = ((hubLikeFile.coChangeFilesIn.length + hubLikeFile.coChangeFilesOut.length) / (hubLikeFile.fanIn + hubLikeFile.fanOut)).toFixed(2);
                html += "<td>(" + hubLikeFile.coChangeFilesIn.length + "+" + hubLikeFile.coChangeFilesOut.length + ")/(" ;
				html += hubLikeFile.fanIn + "+" + hubLikeFile.fanOut + ")=" + allIORatio + "</td>";

				var allFilesIds = hubLikeFile.file.id;
                for(var j = 0; j < hubLikeFile.coChangeFilesIn.length; j++) {
                    allFilesIds += "," + hubLikeFile.coChangeFilesIn[j].id;
                }
                for(var j = 0; j < hubLikeFile.coChangeFilesOut.length; j++) {
                    allFilesIds += "," + hubLikeFile.coChangeFilesOut[j].id;
                }

                html += "<td>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + hubLikeFile.file.id + "&minCount=2'>commits</a>" + "</td>";

				html += "<td>" + (hubLikeFile.file.score).toFixed(2) + "</td>";
				html += "</tr>";
			}
			
			var hubLikeModules = modules[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th width='50%'>Module</th>";
			html += "<th width='25%'>Ca（afferent couplings）</th>";
			html += "<th width='25%'>Ce（efferent couplings）</th>";
			html += "</tr>";
			for(var moduleIndex in hubLikeModules) {
				var module = hubLikeModules[moduleIndex];
				html += "<tr>";
				html += "<td>" + module.module.name + "</td>";
				html += "<td>" + module.fanIn + "</td>";
				html += "<td>" + module.fanOut + "</td>";
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
			setMinFanIO(projectId, 
					hubLikeMinFileFanIn, hubLikeMinFileFanOut,
					hubLikeMinPackageFanIn, hubLikeMinPackageFanOut)
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
		hublike: function(projects, files, packages) {
			_hublike(projects, files, packages);
		}
	}
}
