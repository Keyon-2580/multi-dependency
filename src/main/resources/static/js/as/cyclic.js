let cyclic = function(cytoscapeutil) {
	let showTable = function(projects, types, files, packages) {
		let html = "";
		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";

				let typeCycles = types[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th>Types</th>";
				html += "</tr>";
				let index = 1;
				for(let typeIndex in typeCycles) {
					if (typeCycles.hasOwnProperty(typeIndex)) {
						let cycle = typeCycles[typeIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + cycle.components.length + "</td>";
						html += "<td>";
						for(let i = 0; i < cycle.components.length; i++) {
							html += cycle.components[i].name + "<br/>";
						}
						html += "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";

				let fileCycles = files[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th>Files</th>";
				html += "</tr>";
				index = 1;
				for(let fileIndex in fileCycles) {
					if (fileCycles.hasOwnProperty(fileIndex)) {
						let cycle = fileCycles[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + cycle.components.length + "</td>";
						html += "<td>";
						for(let i = 0; i < cycle.components.length; i++) {
							html += cycle.components[i].path + "<br/>";
						}
						html += "</td>";
						html += "</tr>";
						index ++;
					}
				}

				let packageCycles = packages[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th>Packages</th>";
				html += "</tr>";
				index = 1;
				for(let packageIndex in packageCycles) {
					if (packageCycles.hasOwnProperty(packageIndex)) {
						let cycle = packageCycles[packageIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + cycle.components.length + "</td>";
						html += "<td>";
						for(let i = 0; i < cycle.components.length; i++) {
							html += cycle.components[i].name + "<br/>";
						}
						html += "</td>";
						html += "</tr>";
						index ++;
					}
				}
			}
		}
		
		$("#content").html(html);
	}
	let _cyclic = function(projects, types, files, packages) {
		showTable(projects, types, files, packages);
	}
	
	return {
		cyclic: function(projects, types, files, packages) {
			_cyclic(projects, types, files, packages);
		}
	}
}
