var cyclic = function(cytoscapeutil) {
	var _cyclic = function(projects, files, packages) {
		var html = "";

		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			var cyclicFiles = files[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>Partition</th>";
			html += "<th>Files</th>";
			html += "</tr>";
			for(var cycleIndex in cyclicFiles) {
				var cycle = cyclicFiles[cycleIndex];
				console.log(cycle);
				html += "<tr>";
				html += "<td>" + cycle.partition + "</td>";
				html += "<td>";
				for(var i = 0; i < cycle.components.length; i++) {
					html += "<a target='_blank' href='/relation/file/" + cycle.components[i].id + "'>" + cycle.components[i].path + "</a><br/>";
				}
				html += "</td>";
				html += "</tr>";
			}
			
			var cyclicPackages = packages[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>Partition</th>";
			html += "<th>Packages</th>";
			html += "</tr>";
			for(var packageIndex in cyclicPackages) {
				var pcks = cyclicPackages[packageIndex];
				html += "<tr>";
				html += "<td>" + pcks.partition + "</td>";
				html += "<td>";
				for(var i = 0; i < pcks.components.length; i++) {
					html += pcks.components[i].directoryPath + "<br/>";
				}
				html += "</td>";
				html += "</tr>";
			}
			html += "</table>";
		}
		
		$("#content").html(html);
	}
	
	return {
		cyclic: function(projects, files, packages) {
			_cyclic(projects, files, packages);
		}
	}
}
