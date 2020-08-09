var unstable = function(cytoscapeutil) {
	var _unstable = function(projects, files) {
		var html = "";

		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			var unstableFiles = files[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>File</th>";
			html += "<th>Fan In</th>";
			html += "<th>Co-change Files</th>";
			html += "<th>Co-change Times</th>";
			html += "</tr>";
			for(var fileIndex in unstableFiles) {
				var file = unstableFiles[fileIndex];
				console.log(file);
				html += "<tr>";
				html += "<td width='50%'>" + file.file.path + "</td>";
				html += "<td width='20%'>" + file.fanIn + "</td>";
				html += "<td width='10%'>" + file.cochangeFiles.length + "</td>";
				html += "<td width='10%'>" + file.cochangeTimesWithFile.length + "</td>";
				html += "</tr>";
			}
			
			html += "</table>";
		}
		
		$("#content").html(html);
	}
	
	var _save = function() {
		
	}
	
	var _get = function() {
		var getThreshold = function(projectId) {
			console.log("rrrr");
			$.ajax({
				type: "get",
				url: "/as/unstable/threshold/" + projectId,
				success: function(result) {
					console.log(result);
					$("#unstableFanInThreshold").val(result[0]);
					$("#unstableCoChangeTimesThreshold").val(result[1]);
					$("#unstableCoChangeFilesThreshold").val(result[2]);
				}
			})
		}
		$("#unstableDependencyProjects").change(function() {
			getThreshold($(this).val())
		})
		if($("#unstableDependencyProjects").val() != null) {
			getThreshold($("#unstableDependencyProjects").val());
		}
		
	}
	
	return {
		init : function() {
			_save();
			_get();
		},
		unstable: function(projects, files) {
			_unstable(projects, files);
		}
	}
}
