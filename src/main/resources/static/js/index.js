var index = function(cytoscapeutil) {
	var cy = null;
	var showZTree = function(nodes, container = $("#ztree")) {
		console.log(nodes);
		var setting = {
				data: {
					keep: {
						parent: true
					}
				},
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
							window.open("/project/index?id=" + id);
						}
					},
					onExpand: function(event, treeId, treeNode) {
						console.log(treeNode);
						console.log(treeNode.type);
						var id = treeNode.id;
						var isParent =  treeNode.isParent;
						var children = treeNode.children;
						if(treeNode.type == "Function") {
							if(children != null && children.length > 0) {
								return ;
							}
							$("#iconProject").text("搜索中...");
							$.ajax({
								type: 'GET',
								url: "/project/ztree/function/variable?functionId=" + id,
								success: function(result) {
									if(result.result == "success") {
										var projectZTreeObj = $.fn.zTree.getZTreeObj("treeProjects");
										var selectedNode = projectZTreeObj.getNodeByParam("id", id);
										console.log(result.value);
										var newNodes = projectZTreeObj.addNodes(selectedNode, result.value);
										projectZTreeObj.expandNode(selectedNode, true, false, true, true);
										$("#iconProject").text("");
									}
								}
							});
						}
						if(treeNode.type == "Type") {
							if(children != null && children.length > 0) {
								return ;
							}
							$("#iconProject").text("搜索中...");
							var nodeTypes = ["function", "variable"];
							for(var i = 0; i < nodeTypes.length; i++) {
								$.ajax({
									type: 'GET',
									url: "/project/ztree/type/" + nodeTypes[i] + "?typeId=" + id,
									success: function(result) {
										if(result.result == "success") {
											var projectZTreeObj = $.fn.zTree.getZTreeObj("treeProjects");
											var selectedNode = projectZTreeObj.getNodeByParam("id", id);
											console.log(result.value);
											var newNodes = projectZTreeObj.addNodes(selectedNode, result.value);
											projectZTreeObj.expandNode(selectedNode, true, false, true, true);
											$("#iconProject").text("");
										}
									}
								});
							}
						}
						if(treeNode.type == "Namespace") {
							if(children != null && children.length > 0) {
								return ;
							}
							$("#iconProject").text("搜索中...");
							var nodeTypes = ["type", "function", "variable"];
							for(var i = 0; i < nodeTypes.length; i++) {
								$.ajax({
									type: 'GET',
									url: "/project/ztree/namespace/" + nodeTypes[i] + "?namespaceId=" + id,
									success: function(result) {
										if(result.result == "success") {
											var projectZTreeObj = $.fn.zTree.getZTreeObj("treeProjects");
											var selectedNode = projectZTreeObj.getNodeByParam("id", id);
											console.log(result.value);
											var newNodes = projectZTreeObj.addNodes(selectedNode, result.value);
											projectZTreeObj.expandNode(selectedNode, true, false, true, true);
											$("#iconProject").text("");
										}
									}
								});
							}
						}
						if(treeNode.type == "ProjectFile") {
							if(children != null && children.length > 0) {
								return ;
							}
							$("#iconProject").text("搜索中...");
							var nodeTypes = ["namespace", "type", "function", "variable"];
							for(var i = 0; i < nodeTypes.length; i++) {
								$.ajax({
									type: 'GET',
									url: "/project/ztree/file/" + nodeTypes[i] + "?fileId=" + id,
									success: function(result) {
										if(result.result == "success") {
											var projectZTreeObj = $.fn.zTree.getZTreeObj("treeProjects");
											var selectedNode = projectZTreeObj.getNodeByParam("id", id);
											console.log(result.value);
											var newNodes = projectZTreeObj.addNodes(selectedNode, result.value);
											projectZTreeObj.expandNode(selectedNode, true, false, true, true);
											$("#iconProject").text("");
										}
									}
								});
							}
						}
						if(treeNode.type == "Package") {
							if(children != null && children.length > 0) {
								return ;
							}
							$("#iconProject").text("搜索中...");
							$.ajax({
								type: 'GET',
								url: "/project/ztree/file?packageId=" + id,
								success: function(result) {
									if(result.result == "success") {
										var projectZTreeObj = $.fn.zTree.getZTreeObj("treeProjects");
										var selectedNode = projectZTreeObj.getNodeByParam("id", id);
										console.log(result.value);
										var newNodes = projectZTreeObj.addNodes(selectedNode, result.value);
										projectZTreeObj.expandNode(selectedNode, true, false, true, true);
										$("#iconProject").text("");
									}
								}
							});
						}
						if(treeNode.type == "Project") {
							if(children != null && children.length > 0) {
								return ;
							}
							$("#iconProject").text("搜索中...");
							$.ajax({
								type: 'GET',
								url: "/project/ztree/package?projectId=" + id,
								success: function(result) {
									if(result.result == "success") {
										var projectZTreeObj = $.fn.zTree.getZTreeObj("treeProjects");
										var selectedNode = projectZTreeObj.getNodeByParam("id", id);
//										var newNodes = projectZTreeObj.addNodes(selectedNode, [{name:"eee"}, {name:"eee"}]);
										console.log(result.value);
										var newNodes = projectZTreeObj.addNodes(selectedNode, result.value);
										projectZTreeObj.expandNode(selectedNode, true, false, true, true);
										$("#iconProject").text("");
									}
								}
							});
						}
					}
				}	
					
			};
		var zNodes = nodes;
//		zNodes = [
//			{ id:1, pId:0, name:"父节点 1", open:true, isParent:true, type:"Project"},
//			{ id:11, pId:1, name:"叶子节点 1-1"},
//			{ id:12, pId:1, name:"叶子节点 1-2"},
//			{ id:13, pId:1, name:"叶子节点 1-3"},
//			{ id:2, pId:0, name:"父节点 2", open:true},
//			{ id:21, pId:2, name:"叶子节点 2-1"},
//			{ id:22, pId:2, name:"叶子节点 2-2"},
//			{ id:23, pId:2, name:"叶子节点 2-3"},
//			{ id:3, pId:0, name:"父节点 3", open:true},
//			{ id:31, pId:3, name:"叶子节点 3-1"},
//			{ id:32, pId:3, name:"叶子节点 3-2"},
//			{ id:33, pId:3, name:"叶子节点 3-3"}
//		];
		var zTreeObj = $.fn.zTree.init(container, setting, zNodes);
	}
	var _project = function() {
		var showProjectZTree = function(page) {
			$("#iconProject").text("搜索中...");
			$.ajax({
				type: 'GET',
				url: "/project/all/ztree/project/" + page,
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
