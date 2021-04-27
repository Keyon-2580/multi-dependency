let unusedInclude = function() {
	const smellType = "UnusedInclude";
	const smellFileLevel = "File";
	let _unusedInclude = function(projects, fileUnusedIncludeMap) {
		let html = "";
		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<div>";
				html += "<div>";
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";
				html += "</div>";

				let fileUnusedIncludeList = fileUnusedIncludeMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='vertical-align: middle'>CoreFile</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th style='vertical-align: middle'>UnusedIncludeFiles</th>";
				html += "</tr>";
				let index = 1;
				for(let fileIndex in fileUnusedIncludeList) {
					if (fileUnusedIncludeList.hasOwnProperty(fileIndex)) {
						let fileUnusedInclude = fileUnusedIncludeList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" +
							"<a target='_blank' href='/as/smellgraph/" + project.id + "?smelltype=" + smellType + "&smelllevel=" + smellFileLevel + "&smellindex=" + index + "'>" + index + "</a>" +
							"</td>";
						html += "<td style='vertical-align: middle'>" + fileUnusedInclude.coreFile.path + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileUnusedInclude.unusedIncludeFiles.length + "</td>";
						html += "<td style='vertical-align: middle'>";
						for(let i = 0; i < fileUnusedInclude.unusedIncludeFiles.length; i++) {
							html += fileUnusedInclude.unusedIncludeFiles[i].path + "<br/>";
						}
						html += "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
				html += "</div>";
				html += "</div>";
			}
		}
		$("#content").html(html);
	}

	return {
		unusedInclude: function(projects, fileUnusedIncludeMap) {
			_unusedInclude(projects, fileUnusedIncludeMap);
		}
	}
}
