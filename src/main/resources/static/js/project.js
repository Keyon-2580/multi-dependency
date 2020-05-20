var project = function(project, cytoscapeutil) {
	var cy = null;
	var showZTree = function(nodes, container = $("#ztree")) {
		var setting = {
		};
		var zNodes = nodes;
		var zTreeObj = $.fn.zTree.init(container, setting, zNodes);
	}
	var _project = function() {
		$('#project_select').multiselect({
			includeSelectAllOption: true
		});
		$("#search").click(function(){
			var dependency = $("input[name='dependency']:checked").val();
			var level = $("input[name='level']:checked").val();
			$.ajax({
				type: 'GET',
				url: "/project/cytoscape?projectId=" + project.id + "&dependency=" + dependency + "&level=" + level,
				success: function(result) {
					if(result.result == "success") {
						console.log(result.value);
						if(result.ztreenode != null) {
							showZTree(result.ztreenode);
						}
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

