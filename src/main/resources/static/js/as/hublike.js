var hublike = function(cytoscapeutil) {
	var _hublike = function(projects, files, packages) {
		var html = "";

		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			var hubLikeFiles = files[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th width='80%'>File</th>";
			html += "<th width='10%'>Fan In</th>";
			html += "<th width='10%'>Fan Out</th>";
			html += "</tr>";
			for(var fileIndex in hubLikeFiles) {
				var file = hubLikeFiles[fileIndex];
				console.log(file);
				html += "<tr>";
				html += "<td><a target='_blank' href='/relation/file/" + file.file.id + "'>" + file.file.path + "</a></td>";
				html += "<td>" + file.fanIn + "</td>";
				html += "<td>" + file.fanOut + "</td>";
				html += "</tr>";
			}
			
			var hubLikePackages = packages[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th width='50%'>Package</th>";
			html += "<th width='25%'>Ca（afferent couplings）</th>";
			html += "<th width='25%'>Ce（efferent couplings）</th>";
			html += "</tr>";
			for(var packageIndex in hubLikePackages) {
				var pck = hubLikePackages[packageIndex];
				html += "<tr>";
				html += "<td>" + pck.pck.directoryPath + "</td>";
				html += "<td>" + pck.fanIn + "</td>";
				html += "<td>" + pck.fanOut + "</td>";
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
