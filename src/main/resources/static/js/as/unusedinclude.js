let unusedInclude = function(cytoscapeutil) {
	let showTable = function(projects, unusedIncludeMap) {
		console.log("projects");
		console.log("unusedIncludes");
		let html = "";
		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";

				let unusedIncludeList = unusedIncludeMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='vertical-align: middle'>CoreFile</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th style='vertical-align: middle'>UnusedIncludeFiles</th>";
				html += "</tr>";
				let index = 1;
				for(let fileIndex in unusedIncludeList) {
					if (unusedIncludeList.hasOwnProperty(fileIndex)) {
						let unusedInclude = unusedIncludeList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td style='vertical-align: middle'>" + unusedInclude.coreFile.path + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + unusedInclude.unusedIncludeFiles.length + "</td>";
						html += "<td style='vertical-align: middle'>";
						for(let i = 0; i < unusedInclude.unusedIncludeFiles.length; i++) {
							html += unusedInclude.unusedIncludeFiles[i].path + "<br/>";
						}
						html += "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
			}
		}
		
		$("#unusedIncludeTable").html(html);
	}

	return {
		include: function(projects, unusedIncludeMap) {
			showTable(projects, unusedIncludeMap);
		}
	}
}
