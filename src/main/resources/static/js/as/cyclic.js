var cyclic = function(cytoscapeutil) {
	var showTable = function(projects, types, files, packages) {
		console.log("types");
		console.log("files");
		console.log("packages");
		var html = "";
		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";

			var typeCycles = types[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th style='text-align: center; vertical-align: middle'>Partition</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
			html += "<th>Types</th>";
			html += "</tr>";
			let index = 1;
			for(var typeIndex in typeCycles) {
				var cycle = typeCycles[typeIndex];
				html += "<tr>";
				html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + cycle.components.length + "</td>";
				html += "<td>";
				for(var i = 0; i < cycle.components.length; i++) {
					html += cycle.components[i].name + "<br/>";
				}
				html += "</td>";
				html += "</tr>";
				index ++;
			}
			html += "</table>";

			var fileCycles = files[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th style='text-align: center; vertical-align: middle'>Partition</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
			html += "<th>Files</th>";
			html += "</tr>";
			index = 1;
			for(var cycleIndex in fileCycles) {
				var cycle = fileCycles[cycleIndex];
				html += "<tr>";
				html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + cycle.components.length + "</td>";
				html += "<td>";
				for(var i = 0; i < cycle.components.length; i++) {
					html += cycle.components[i].path + "<br/>";
				}
				html += "</td>";
				html += "</tr>";
				index ++;
			}

			var cyclicPackages = packages[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th style='text-align: center; vertical-align: middle'>Partition</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
			html += "<th>Packages</th>";
			html += "</tr>";
			index = 1;
			for(var packageIndex in cyclicPackages) {
				var cycle = cyclicPackages[packageIndex];
				html += "<tr>";
				html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + cycle.components.length + "</td>";
				html += "<td>";
				for(var i = 0; i < cycle.components.length; i++) {
					html += cycle.components[i].name + "<br/>";
				}
				html += "</td>";
				html += "</tr>";
				index ++;
			}
		}
		
		$("#content").html(html);
	}
	var _cyclic = function(projects, types, files, packages) {
		showTable(projects, types, files, packages);
	}
	
	return {
		cyclic: function(projects, types, files, packages) {
			_cyclic(projects, types, files, packages);
		}
	}
}
