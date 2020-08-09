var hublike = function(cytoscapeutil) {
	var _hublike = function(projects, files, packages) {
		var html = "";

		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			var hubLikeFiles = files[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>File</th>";
			html += "<th>Fan In</th>";
			html += "<th>Fan Out</th>";
			html += "</tr>";
			for(var fileIndex in hubLikeFiles) {
				var file = hubLikeFiles[fileIndex];
				console.log(file);
				html += "<tr>";
				html += "<td width='50%'>" + file.file.path + "</td>";
				html += "<td width='20%'>" + file.fanIn + "</td>";
				html += "<td width='20%'>" + file.fanOut + "</td>";
				html += "<td width='10%'>" + file.file.score + "</td>";
				html += "</tr>";
			}
			
			var hubLikePackages = packages[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>Package</th>";
			html += "<th>Fan In</th>";
			html += "<th>Fan Out</th>";
			html += "</tr>";
			for(var packageIndex in hubLikePackages) {
				var pck = hubLikePackage[packageIndex];
				html += "<tr>";
				html += "<td width='50%'>" + pck.pck.directoryPath + "</td>";
				html += "<td width='20%'>" + pck.fanIn + "</td>";
				html += "<td width='30%'>" + pck.fanOut + "</td>";
				html += "</tr>";
			}
			html += "</table>";
		}
		
		$("#content").html(html);
	}
	
	var _save = function() {
		
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
