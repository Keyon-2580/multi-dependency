var unused = function(cytoscapeutil) {
	var _unused = function(projects, packages) {
		var html = "";

		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			var unusedPackages = packages[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>Package</th>";
			html += "</tr>";
			for(var pckIndex in unusedPackages) {
				var pck = unusedPackages[pckIndex];
				console.log(pck);
				html += "<tr>";
				html += "<td width='50%'>" + pck.directoryPath + "</td>";
				html += "</tr>";
			}
			
			html += "</table>";
		}
		
		$("#content").html(html);
	}
	
	return {
		unused: function(projects, packages) {
			_unused(projects, packages);
		}
	}
}
