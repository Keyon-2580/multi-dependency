let multipleSmell = function(project, files) {
	let _multipleSmell = function() {
		if (project != null) {
			let html = "";
			html += "<div>";
			html += "<div>";
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			html += "</div>";
			html += "<div>";
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th style='text-align: center; vertical-align: middle'>ID</th>";
			html += "<th>File</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Cyclic Dependency</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Hub-Like Dependency</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Unstable dependency</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Implicit Cross Module Dependency</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Unutilized Abstraction</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Unused Include</th>";
			html += "</tr>";
			for(let j = 0 ; j < files[project.id].length; j++) {
				let value = files[project.id][j];
				html += "<tr>";
				html += "<td style='text-align: center; vertical-align: middle'>" + value.file.id + "</td>";
				html += "<td><a target='_blank' href='/relation/file/" + value.file.id + "'>" + value.file.path + "</a></td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.cyclicDependency === true ? "T" : "") + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.hubLikeDependency === true ? "T" : "") + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.unstableDependency === true ? "T" : "") + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.implicitCrossModuleDependency === true ? "T" : "") + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.unutilizedAbstraction === true ? "T" : "") + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.unusedInclude === true ? "T" : "") + "</td>";
				html += "</tr>";
			}
			html += "</table>";
			html += "</div>";
			html += "</div>";
			$("#content").html(html);
		}
	}
	
	return {
		init : function() {
			_multipleSmell();
		}
	}
}
