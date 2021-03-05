var similar = function(cytoscapeutil, files) {
	var _similar = function() {
		console.log(files);
		var html = "";
		html += "<table class='table table-bordered'>";
		html += "<tr>";
		html += "<th>File1</th>";
		html += "<th>Module1</th>";
		html += "<th>File2</th>";
		html += "<th>Module2</th>";
		html += "<th>Clone Type</th>";
		html += "<th>Clone Value</th>";
		html += "<th>Node1 Change Times</th>";
		html += "<th>Node2 Change Times</th>";
		html += "<th>Co-Change Times</th>";
		html += "<th>Ratio of Depends-On</th>";
		html += "<th></th>";
		html += "</tr>";
		for(var fileIndex in files) {
			var file = files[fileIndex];
			console.log(file);
			html += "<tr>";
			html += "<td><a target='_blank' href='/relation/file/" + file.node1.id + "'>" + file.node1.path + "</a></td>";
			html += "<td>" + "module_" + file.module1.id + "</td>";
			html += "<td><a target='_blank' href='/relation/file/" + file.node2.id + "'>" + file.node2.path + "</a></td>";
			html += "<td>" + "module_" + file.module2.id + "</td>";
			html += "<td>" + file.cloneType + "</td>";
			html += "<td>" + file.value + "</td>";
			html += "<td>" + file.node1ChangeTimes + "</td>";
			html += "<td>" + file.node2ChangeTimes + "</td>";
			html += "<td>" + file.cochangeTimes + "</td>";
			html += "<td>" + file.sameDependsOnRatio + "</td>";
			html += "<td><a target='_blank' href='/as/matrix?allFiles=" + file.node1.id + "," + file.node2.id + "&specifiedFiles=" + file.node1.id + "," + file.node2.id + "&minCount=2" + "'" + fileIndex + "'>commits</a></td>";
			html += "</tr>";
		}
		html += "</table>";
		
		$("#content").html(html);
	}
	
	return {
		init : function() {
			_similar();
		}
	}
}
