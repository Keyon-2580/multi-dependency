var multiple = function(cytoscapeutil) {
	
	var _multiple = function() {
		$.ajax({
			type: "get",
			url: "/as/api/multiple",
			success: function(result) {
				console.log(result);
				var html = "";
				for(var project in result) {
					console.log(project);
					console.log(result[project]);
					var p = result[project][0].project;
					html += "<div>";
					html += "<div>";
					html += "<h4>" + p.name + " (" + p.language + ") ";
					html += "</div>";
					html += "<div>";
					html += "<table class='table table-bordered'>";
					html += "<tr>";
					html += "<th>文件</th>";
					html += "<th>cycle</th>";
					html += "<th>hublike</th>";
					html += "<th>unstable</th>";
					html += "<th>logic coupling</th>";
					html += "<th>simiar</th>";
					html += "<th>cyclicHierarchy</th>"
					html += "<th>page rank</th>";
					html += "</tr>";
					for(var i = 0 ; i < result[project].length; i++) {
						var value = result[project][i];
						html += "<tr>";
						html += "<td>" + value.file.path + "</td>";
						html += "<td>" + (value.cycle == true ? "T" : "") + "</td>";
						html += "<td>" + (value.hublike == true ? "T" : "") + "</td>";
						html += "<td>" + (value.unstable == true ? "T" : "") + "</td>";
						html += "<td>" + (value.logicCoupling == true ? "T" : "") + "</td>";
						html += "<td>" + (value.similar == true ? "T" : "") + "</td>";
						html += "<td>" + (value.cyclicHierarchy == true ? "T" : "") + "</td>";
						html += "<td>" + value.file.score + "</td>";
						html += "</tr>";
					}
					html += "</table>";
					html += "</div>";
					html += "</div>";
				}
				$("#content").html(html);
			}
		})
	}
	
	return {
		init : function() {
			_multiple();
		}
	}
}
