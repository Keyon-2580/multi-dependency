define(['jquery', 'bootstrap', 'bootstrap-multiselect', 'utils']
	, function ($, bootstrap, bootstrap_multiselect, utils) {
	
	var _project = function() {
		$('#project_select').multiselect({
	    	includeSelectAllOption: true
	    });
		$.ajax({
	    	type: 'GET',
	    	url: "/project/treeview",
	    	success: function(result) {
	    		console.log(result);
	    		if(result.result == "success") {
	    			utils.showTreeView($("#tree"), result.value)
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
			    		utils.showDataInCytoscape($("#graph"), result.value);
			    	} else {
			    		alert(result.msg);
			    	}
		    	}
		    });
		});
	};

	return {
		init : function() {
			_project();
		}
	}
});

