var rFile = function(fileId, cytoscapeutil) {
	var _file = function() {
		containType(fileId);
		metric(fileId);
		depends(fileId);
		issues(fileId);
		commits(fileId);
	};
	
	var commits = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/commit/matrix",
			success: function(result) {
				console.log(result);
				var html = "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<td width='30%'></td>";
				for(var i = 0; i < result.files.length; i++) {
					html += "<td>" + i + ":" + "<a target='_blank' href='/relation/file/" + result.files[i].id + "'>";
					html += result.files[i].name;
					html += "</a>(" + result.commitTimes[result.files[i].id] + ")</td>";
				}
				html += "</tr>";
				for(var i = 0; i < result.commits.length; i++) {
					html += "<tr>";
//					html += "<td>" + (i + 1) + ":" + "<a target='_blank' href='/commit/" + result.commits[i].id + "'>" + result.commits[i].commitId + "(" + result.commits[i].merge + " " + result.commits[i].commitFilesSize + ") </a></td>";
					html += "<td>" + (i + 1) + ":" + "<a target='_blank' href='/commit/" + result.commits[i].id + "'>" + result.commits[i].commitId + "(" + result.commits[i].commitFilesSize + ") </a></td>";
					for(var j = 0; j < result.files.length; j++) {
						if(result.update[i][j] == true) {
							html += "<td>T</td>";
						} else {
							html += "<td></td>";
						}
					}
					html += "</tr>";
				}
				/*var html = "<ol>";
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/commit/" + result[i].id + "'>" + result[i].commitId + "</a></li>";
				}
				html += "</ol>";*/
				html += "</table>";
				$("#commit_content").html(html);
			}
		})
	}
	
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
				html += "<td>" + ((result.fanIn + result.fanOut == 0) ? -1 : ((result.fanOut) / (result.fanIn + result.fanOut)).toFixed(2)) + "</td>";
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

