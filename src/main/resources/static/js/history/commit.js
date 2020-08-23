var commit = function(commitId, cytoscapeutil) {
	var _commit = function() {
		relatedFiles(commitId);
		relatedIssues(commitId);
	};
	
	var relatedIssues = function(commitId) {
		$.ajax({
			type: "get",
			url: "/commit/" + commitId + "/issues",
			success: function(result) {
				var html = "<ul>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/issue/" + result[i].id + "' >";
					html += result[i].number + " " + result[i].title;
					html += "</a></li>";
				}
				html += "</ul>";
				$("#issue_content").html(html);
			}
		});
	}
	
	var relatedFiles = function(commitId) {
		$.ajax({
			type: "get",
			url: "/commit/" + commitId + "/files",
			success: function(result) {
				var html = "";
				console.log(result);
				for(var i in result) {
					console.log(i);
					html += "<div><h5>" + i + "</h5></div>"
					html += "<ul>";
					for(var j = 0; j < result[i].length; j++) {
						html += "<li><a target='_blank' href='/relation/file/" + result[i][j].id + "'>" + result[i][j].path + "</a></li>";
					}
					html += "</ul>";
				}
				$("#file_content").html(html);
			}
		});
	}
	
	return {
		init: function(){
			_commit();
		}
	}
}

