var hierarchy = function(cytoscapeutil) {
	var _hierarchy = function(projects, types) {
		var html = "";

		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			var cyclicTypes = types[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th width='50%'>Type</th>";
			html += "<th width='50%'>Sub Types</th>";
			html += "</tr>";
			for(var cycleIndex in cyclicTypes) {
				var cycle = cyclicTypes[cycleIndex];
				console.log(cycle);
				html += "<tr>";
				html += "<td>" + cycle.superType.name + "</td>";
				html += "<td>";
				for(var i = 0; i < cycle.subTypes.length; i++) {
					html += cycle.subTypes[i].name + "<br/>";
				}
				html += "</td>";
				html += "</tr>";
			}
			
			html += "</table>";
		}
		
		$("#content").html(html);
	}
	
	return {
		hierarchy: function(projects, types) {
			_hierarchy(projects, types);
		}
	}
}
