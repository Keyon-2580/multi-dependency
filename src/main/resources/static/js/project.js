var project = function(cytoscapeutil) {
	var cy = null;
	var _project = function() {
		$('#project_select').multiselect({
			includeSelectAllOption: true
		});
		$("#search").click(function(){
			var projectId = $("#project_select").val();
			var dependency = $("input[name='dependency']:checked").val();
			var level = $("input[name='level']:checked").val();
			$.ajax({
				type: 'GET',
				url: "/project/cytoscape?projectId=" + projectId + "&dependency=" + dependency + "&level=" + level,
				success: function(result) {
					if(result.result == "success") {
						console.log(result.value);
						var nodes = result.value.nodes;
						cy = cytoscapeutil.showDataInCytoscape($("#graph"), result.value, "dagre");
						cy.expandCollapse();
						var api = cy.expandCollapse('get');
						api.collapseAll();
						cy.layout({
							name: "dagre"
						}).run();
					} else {
						alert(result.msg);
					}
				}
			});
		});
		_showImg();
		_clearMemo();
	};
	var _showImg = function(){
		$("#showImg").click(function() {
			console.log("showImg");
			cytoscapeutil.showImg(cy, "entry-png-eg");
		})
	}
	var _clearMemo = function() {
		$("#clearMemo").click(function() {
			console.log("clearMemo");
			if(cy == null) {
				return ;
			}
			cy = cytoscapeutil.refreshCy(cy);
			cy.expandCollapse();
			var api = cy.expandCollapse('get');
			api.collapseAll();
			cy.layout({
				name: "dagre"
			}).run();
		});
	};
	
	return {
		init: function(){
			_project();
		}
	}
}

