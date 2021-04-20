var unutilizedabstraction = function(cytoscapeutil) {
	var showTable = function(projects, unutilizedAbstractionMap) {
		console.log("projects");
		console.log("unutilizedAbstractionMap");
		var html = "";
		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";

			var unutilizedAbstractionList = unutilizedAbstractionMap[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
			html += "<th style='vertical-align: middle'>unutilizedAbstractionFile</th>";
			html += "</tr>";
			let index = 1;
			for(var unutilizedAbstractionIndex in unutilizedAbstractionList) {
				var unutilizedAbstraction = unutilizedAbstractionList[unutilizedAbstractionIndex];
				html += "<tr>";
				html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
				html += "<td style='vertical-align: middle'>" + unutilizedAbstraction.component.path + "</td>";
				html += "</tr>";
				index ++;
			}
			html += "</table>";
		}
		
		$("#unutilizedAbstraction").html(html);
	}

	return {
		abstraction: function(projects, unutilizedAbstractionMap) {
			showTable(projects, unutilizedAbstractionMap);
		}
	}
}
