var project = function(graph) {
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
		    	if(result.result == "success") {
		    		showDataInCytoscape($("#graph"), result.value);
		    	} else {
		    		alert(result.msg);
		    	}
	    	}
	    });
	});
};

