var index = function(cytoscapeutil) {
	var cy = null;
	var showZTree = function(nodes, container = $("#ztree")) {
		var setting = {
				callback: {
					onClick: function(event, treeId, treeNode) {
						var id = treeNode.id;
						console.log(id);
						if(id <= 0) {
							return ;
						}
						var type = treeNode.type;
						console.log(type);
						if(type == "Project") {
//							window.location.href = "/project/index?id=" + id;
							window.open("/project/index?id=" + id);
						}
					}
				}	
					
			};
		var zNodes = nodes;
		var zTreeObj = $.fn.zTree.init(container, setting, zNodes);
	}
	var _project = function() {
		var showProjectZTree = function(page) {
			$("#iconProject").text("搜索中...");
			$.ajax({
				type: 'GET',
				url: "/project/all/ztree/structure/" + page,
				success: function(result) {
					console.log(result);
					if(result.result == "success") {
						showZTree(result.values, $("#treeProjects"));
						$("#iconProject").text("");
					}
				}
			});
		}
		showProjectZTree(0);
		$.ajax({
			type: 'GET',
			url: "/project/pages/count",
			success: function(result) {
				console.log(result);
				html = "";
				for(var i = 0; i < result; i++) {
					html += "<a class='treeProjectsPage_a page_a' name='" + i + "'>" + (i + 1) + "</a>&nbsp;";
				}
				$("#treeProjectsPage").html(html);
				$(".treeProjectsPage_a").click(function() {
					showProjectZTree($(this).attr("name"));
				});
			}
		});
	}
	
	var _microservice = function() {
		var showMicroServiceZTree = function(page) {
			$("#iconMicroService").text("搜索中...");
			$.ajax({
				type: 'GET',
				url: "/microservice/all/ztree/projects/" + page,
				success: function(result) {
					console.log(result);
					if(result.result == "success") {
						console.log("rr")
						showZTree(result.values, $("#treeMicroservices"));
						$("#iconMicroService").text("");
					}
				}
			});
		}
		showMicroServiceZTree(0);
		$.ajax({
			type: 'GET',
			url: "/microservice/pages/count",
			success: function(result) {
				console.log(result);
				html = "";
				for(var i = 0; i < result; i++) {
					html += "<a class='treeMicroservicesPage_a page_a' name='" + i + "'>" + (i + 1) + "</a>&nbsp;";
				}
				$("#treeMicroservicesPage").html(html);
				$(".treeMicroservicesPage_a").click(function() {
					showMicroServiceZTree($(this).attr("name"));
				});
			}
		});
	}
	var _index = function() {
		_microservice();
		_project();
	};
	
	return {
		init: function(){
			_index();
		}
	}
}

