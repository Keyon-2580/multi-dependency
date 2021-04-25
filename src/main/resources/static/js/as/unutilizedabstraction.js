let unutilizedAbstraction = function() {
	let _unutilizedAbstraction = function(projects, fileUnutilizedAbstractionMap) {
		let html = "";
		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";

				let fileUnutilizedAbstractionList = fileUnutilizedAbstractionMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='vertical-align: middle'>unutilizedAbstractionFile</th>";
				html += "</tr>";
				let index = 1;
				for(let fileIndex in fileUnutilizedAbstractionList) {
					if (fileUnutilizedAbstractionList.hasOwnProperty(fileIndex)) {
						let fileUnutilizedAbstraction = fileUnutilizedAbstractionList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td style='vertical-align: middle'>" + fileUnutilizedAbstraction.component.path + "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
			}
		}
		$("#content").html(html);
	}

	return {
		unutilizedAbstraction: function(projects, fileUnutilizedAbstractionMap) {
			_unutilizedAbstraction(projects, fileUnutilizedAbstractionMap);
		}
	}
}
