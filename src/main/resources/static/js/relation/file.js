var rFile = function(fileId, cytoscapeutil) {
	var _file = function() {
		containType(fileId);
		metric(fileId);
		depends(fileId);
		issues(fileId);
	};
	
	var issues = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/issue",
			success: function(result) {
				console.log(result);
				var html = "<ol>";
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/issue/" + result[i].id + "'>Issue: " + result[i].number + " " + result[i].title + "</a></li>";
				}
				html += "</ol>";
				$("#issue_content").html(html);
			}
		})
	}
	
	var depends = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/dependedBy",
			success: function(result) {
				console.log(result);
				var html = "<ol>";
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/file/" + result[i].startNode.id + "'>" + result[i].startNode.path + "</a></li>";
				}
				html += "</ol>";
				$("#dependedBy_content").html(html);
			}
		})
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/dependsOn",
			success: function(result) {
				console.log(result);
				var html = "<ol>";
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/file/" + result[i].endNode.id + "'>" + result[i].endNode.path + "</a></li>";
				}
				html += "</ol>";
				$("#dependsOn_content").html(html);
			}
		});
	}
	
	var metric = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/metric",
			success: function(result) {
				console.log(result);
				var html = "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<td width='12.5%'>LOC</td>";
				html += "<td width='12.5%'>NOM</td>";
				html += "<td width='12.5%'>Fan In</td>";
				html += "<td width='12.5%'>Fan Out</td>";
				html += "<td width='12.5%'>change times</td>";
				html += "<td width='12.5%'>cochange commit times</td>";
				html += "<td width='12.5%'>instability</td>";
				html += "<td width='12.5%'>score</td>";
				html += "</tr>";
				html += "<tr>";
				html += "<td>" + result.loc + "</td>";
				html += "<td>" + result.nom + "</td>";
				html += "<td>" + result.fanIn + "</td>";
				html += "<td>" + result.fanOut + "</td>";
				html += "<td>" + result.changeTimes + "</td>";
				html += "<td>" + result.cochangeCommitTimes + "</td>";
				html += "<td>" + result.instability + "</td>";
				html += "<td>" + result.component.score + "</td>";
				html += "</tr>";
				html += "</table>";
				$("#metric_content").html(html);
			}
		})
	}
	
	var containType = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/contain/type",
			success: function(result) {
				var html = "<ul>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/type/" + result[i].id + "' >";
					html += result[i].name;
					html += "</a></li>";
				}
				html += "</ul>";
				$("#contain_type_content").html(html);
			}
		});
	}
	
	return {
		init: function(){
			_file();
		}
	}
}

