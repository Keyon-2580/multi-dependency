var _project = function() {
	var cy = null;
	$('#project_select').multiselect({
		includeSelectAllOption: true
	});
	$.ajax({
		type: 'GET',
		url: "/project/treeview",
		success: function(result) {
			console.log(result);
			if(result.result == "success") {
				showTreeView($("#tree"), result.value)
			}
		}
	});
	
	$("#search").click(function(){
		var projectId = $("#project_select").val();
		var dependency = $("input[name='dependency']:checked").val();
		var level = $("input[name='level']:checked").val();
		$.ajax({
			type: 'GET',
			url: "/project/cytoscape?projectId=" + projectId + "&dependency=" + dependency + "&level=" + level,
			success: function(result) {
				console.log(result);
				console.log(result);
				if(result.result == "success") {
					console.log("success");
					cy = showDataInCytoscape($("#graph"), result.value, "dagre");
				} else {
					alert(result.msg);
				}
			}
		});
	});
	$("#showImg").click(function() {
		console.log("rrr");
		if(cy != null) {
			$('#png-eg').attr('src', cy.png({
				bg: "#ffffff",
				full : true
			}));
			$('#png-eg').css("background-color", "#ffffff");
		}
	})
};

var init = function() {
	_project();
}

