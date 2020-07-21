var metric = function() {
	var projectMetric = function() {
		$.ajax({
			type: "get",
			url: "/metric/project",
			success: function(result) {
				console.log(result);
				var html = "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>index</th>";
				html += "<th>项目</th>";
				html += "<th>NOP（包数）</th>";
				html += "<th>NOF（文件数）</th>";
				html += "<th>NOM（方法数）</th>";
				html += "<th>LOC（代码行）</th>";
				html += "</tr>";
				for(var i = 0; i < result.length; i++) {
					html += "<tr>";
					html += "<td>" + (i + 1) + "</td>";
					html += "<td>" + result[i].project.name + " (" + result[i].project.language + ") " + "</td>";
					html += "<td>" + result[i].nop + "</td>";
					html += "<td>" + result[i].nof + "</td>";
					html += "<td>" + result[i].nom + "</td>";
					html += "<td>" + result[i].loc + "</td>";
					html += "</tr>";
				}
				html += "</table>";
				$("#projectMetrics").html(html);
			}
		});
	}
	var fileMetric = function() {
		$.ajax({
			type: "get",
			url: "/metric/file",
			success: function(result) {
				console.log(result);
				var html = "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>index</th>";
				html += "<th>文件</th>";
				html += "<th>LOC（代码行）</th>";
				html += "<th>NOM（方法数）</th>";
				html += "<th>Fan In</th>";
				html += "<th>Fan Out</th>";
				html += "<th>修改次数</th>";
				html += "</tr>";
				for(var i = 0; i < result.length; i++) {
					html += "<tr>";
					html += "<td>" + (i + 1) + "</td>";
					html += "<td>" + result[i].file.path + "</td>";
					html += "<td>" + result[i].loc + "</td>";
					html += "<td>" + result[i].nom + "</td>";
					html += "<td>" + result[i].fanIn + "</td>";
					html += "<td>" + result[i].fanOut + "</td>";
					html += "<td>" + result[i].changeTimes + "</td>";
					html += "</tr>";
				}
				html += "</table>";
				$("#fileMetrics").html(html);
			}
		})
	}
	
	return {
		init: function() {
			projectMetric();
			fileMetric();
		}
	}
}