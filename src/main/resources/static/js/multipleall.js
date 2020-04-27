define(['jquery', 'bootstrap', 'bootstrap-multiselect', 'jqplot', 'cytoscapeUtils', 'cytoscape']
	, function ($, bootstrap, bootstrap_multiselect, jqplot, utils, cytoscape) {
	var cyEntry = null;
	
	var firstTestCaseId = null;
	var secondTestCaseId = null;
	
	var nodeToPosition = new Map();
	
	var queryAll = function() {
		console.log("queryAll")
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/multiple/all",
			success : function(result) {
				if (result.result == "success") {
					console.log(result);
					cyEntry = utils.showDataInCytoscape($("#entry"), result.value.data, "dagre");
					processCytoscape(cyEntry);
				}
			}
		});
	}
	
	var queryEntryEdge = function(testCaseId, callChain = false) {
		
		var testCaseIds = [];
		
		if(firstTestCaseId == null) {
			firstTestCaseId = testCaseId;
			testCaseIds[testCaseIds.length] = firstTestCaseId;
		} else {
			secondTestCaseId = testCaseId;
			testCaseIds[testCaseIds.length] = firstTestCaseId;
			testCaseIds[testCaseIds.length] = secondTestCaseId;
		}
		
		var ids = {
			"ids" : testCaseIds,
			"callChain" : false
		};
		console.log(ids);
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/multiple/microservice/query/edges",
			data : JSON.stringify(ids),
			success : function(result) {
				if (result.result == "success") {
					console.log(result);
					cyEntry.remove('edge[type="all_MicroService_DependOn_MicroService"]');
					cyEntry.remove('edge[type="all_MicroService_call_MicroService"]');
					cyEntry.remove('edge[type="all_FeatureExecutedByTestCase"]');
					cyEntry.remove('edge[type="all_TestCaseExecuteMicroService"]');
					cyEntry.remove('edge[type="NewEdges"]');
					cyEntry.remove('edge[type="NewEdges_Edge1_Edge2"]');
					cyEntry.remove('edge[type="NewEdges_Edge1"]');
					cyEntry.remove('edge[type="NewEdges_Edge2"]');
					var relatedNodes = result.nodes;
					var relatedEdges = result.edges;
					var datas = new Array();
					for(var i = 0; i < relatedEdges.length; i++) {
						cyEntry.remove(cyEntry.$("#" + relatedEdges[i].id));
						var data = {
								group: 'edges',
								data: {
//									type: "NewEdges",
//									id: relatedEdges[i].id,
									type: relatedEdges[i].type == null ? "NewEdges" : relatedEdges[i].type,
									source: relatedEdges[i].source,
									target: relatedEdges[i].target,
									value: relatedEdges[i].value
								}
						}
						datas.push(data);
					}
					cyEntry.add(datas);
					if(firstTestCaseId != null && secondTestCaseId != null) {
						firstTestCaseId = null;
						secondTestCaseId = null;
					}
				}
			}
		});
	}
	var processCytoscape = function(cyEntry) {
		var edges = new Array();
		var removeIds = new Array();
		for(var i = 0; i < cyEntry.edges().length; i++) {
			var type = cyEntry.edges()[i].data().type;
			if(type == "FeatureExecutedByTestCase") {
				var source = cyEntry.edges()[i].data().source;
				var target = cyEntry.edges()[i].data().target;
				removeIds.push(cyEntry.edges()[i].data().id);
				var edge = {
						type: "TestCaseExecuteFeature",
						source: target,
						target: source,
						value: ""
				};
				edges.push({data: edge});
			}
		}
		for(var i = 0; i < removeIds.length; i++){
			utils.removeEdge(cyEntry, removeIds[i]);
		}
		utils.addEdges(cyEntry, edges);
		cyEntry.on('tap', 'node', function(evt){
			var node = evt.target;
			if(node.data().type == "TestCase_success" || node.data().type == "TestCase_fail") {
				queryEntryEdge(node.data().id, true);
			}
		})
	};
	var queryMultipleByTestCase = function(testCaseIds) {
		var nodes = cyEntry.nodes();
		for(var i = 0; i < nodes.length; i++) {
			console.log(nodes[i].data());
			console.log(nodes[i].position());
			var nodeId = nodes[i].data().id;
			var position = nodes[i].position();
			nodeToPosition.set(nodeId, position);
		}
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/multiple/all",
			success : function(result) {
				if (result.result == "success") {
					console.log(result);
					cyEntry.destroy();
					var nodes = result.value.data.nodes;
					for(var i = 0; i < nodes.length; i++) {
						var nodeId = nodes[i].data.id;
						var position = nodeToPosition.get(nodeId);
						nodes[i].position = position;
					}
					console.log(result.value.data);
					cyEntry = utils.showDataInCytoscape($("#entry"), result.value.data, "preset");
					processCytoscape(cyEntry);
					
					$.ajax({
						type : "POST",
						contentType : "application/json",
						dataType : "json",
						url : "/multiple/all/testcase",
						data : JSON.stringify(testCaseIds),
						success : function(result) {
							if (result.result == "success") {
								console.log(result.value);
								cyEntry.remove('edge');
								var relatedEdges = result.value.value.edges;
								cyEntry.add(relatedEdges)
							}
						}
					});
				}
			}
		});
	}
	var queryMultipleByScenario = function(scenarioIds) {
		var nodes = cyEntry.nodes();
		for(var i = 0; i < nodes.length; i++) {
			console.log(nodes[i].data());
			console.log(nodes[i].position());
			var nodeId = nodes[i].data().id;
			var position = nodes[i].position();
			nodeToPosition.set(nodeId, position);
		}
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/multiple/all",
			success : function(result) {
				if (result.result == "success") {
					console.log(result);
					cyEntry.destroy();
					var nodes = result.value.data.nodes;
					for(var i = 0; i < nodes.length; i++) {
						var nodeId = nodes[i].data.id;
						var position = nodeToPosition.get(nodeId);
						nodes[i].position = position;
					}
					console.log(result.value.data);
					cyEntry = utils.showDataInCytoscape($("#entry"), result.value.data, "preset");
					processCytoscape(cyEntry);
					
					$.ajax({
						type : "POST",
						contentType : "application/json",
						dataType : "json",
						url : "/multiple/all/scenario",
						data : JSON.stringify(scenarioIds),
						success : function(result) {
							if (result.result == "success") {
								console.log(result.value);
								cyEntry.remove('edge');
								var relatedEdges = result.value.value.edges;
								cyEntry.add(relatedEdges)
							}
						}
					});
				}
			}
		});
	}
	var queryMultipleByFeature = function(featureIds) {
		var nodes = cyEntry.nodes();
		for(var i = 0; i < nodes.length; i++) {
			console.log(nodes[i].data());
			console.log(nodes[i].position());
			var nodeId = nodes[i].data().id;
			var position = nodes[i].position();
			nodeToPosition.set(nodeId, position);
		}
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/multiple/all",
			success : function(result) {
				if (result.result == "success") {
					console.log(result);
					cyEntry.destroy();
					var nodes = result.value.data.nodes;
					for(var i = 0; i < nodes.length; i++) {
						var nodeId = nodes[i].data.id;
						var position = nodeToPosition.get(nodeId);
						nodes[i].position = position;
					}
					console.log(result.value.data);
					cyEntry = utils.showDataInCytoscape($("#entry"), result.value.data, "preset");
					processCytoscape(cyEntry);
					
					$.ajax({
						type : "POST",
						contentType : "application/json",
						dataType : "json",
						url : "/multiple/all/feature",
						data : JSON.stringify(featureIds),
						success : function(result) {
							if (result.result == "success") {
								console.log(result.value);
								cyEntry.remove('edge');
								var relatedEdges = result.value.value.edges;
								cyEntry.add(relatedEdges)
							}
						}
					});
				}
			}
		});
	}
	
	var _init = function(){
		$("#testCaseList").multiselect({
			enableClickableOptGroups: true,
			enableCollapsibleOptGroups: true,
	       	enableFiltering: true,
			collapseOptGroupsByDefault: true,
			enableCollapsibleOptGroups: true
		});
		$("#featureList").multiselect({
			enableClickableOptGroups: true,
			enableCollapsibleOptGroups: true,
	       	enableFiltering: true,
			collapseOptGroupsByDefault: true,
			enableCollapsibleOptGroups: true
		});
		$("#scenarioList").multiselect({
			enableClickableOptGroups: true,
			enableCollapsibleOptGroups: true,
	       	enableFiltering: true,
			collapseOptGroupsByDefault: true,
			enableCollapsibleOptGroups: true
		});
		$("#submitScenario").click(function() {
			var showStructure = $("#showStructure").prop('checked');
			var showMicroServiceCallLibs = $("#showMicroServiceCallLibs").prop('checked');
			var showClonesInMicroService = $("#showClonesInMicroService").prop('checked');
			var ids = {
				"ids" : $("#scenarioList").val(),
				"showStructure" : showStructure,
				"showMicroServiceCallLibs" : showMicroServiceCallLibs,
				"showClonesInMicroService" : showClonesInMicroService
			};
			console.log(ids);
			queryMultipleByScenario(ids);
		});
		$("#submitTestCase").click(function() {
			var showStructure = $("#showStructure").prop('checked');
			var showMicroServiceCallLibs = $("#showMicroServiceCallLibs").prop('checked');
			var showClonesInMicroService = $("#showClonesInMicroService").prop('checked');
			var ids = {
				"ids" : $("#testCaseList").val(),
				"showStructure" : showStructure,
				"showMicroServiceCallLibs" : showMicroServiceCallLibs,
				"showClonesInMicroService" : showClonesInMicroService
			};
			console.log(ids);
			queryMultipleByTestCase(ids);
		});
		$("#submitFeature").click(function() {
			var showStructure = $("#showStructure").prop('checked');
			var showMicroServiceCallLibs = $("#showMicroServiceCallLibs").prop('checked');
			var showClonesInMicroService = $("#showClonesInMicroService").prop('checked');
			var ids = {
				"ids" : $("#featureList").val(),
				"showStructure" : showStructure,
				"showMicroServiceCallLibs" : showMicroServiceCallLibs,
				"showClonesInMicroService" : showClonesInMicroService
			};
			console.log(ids);
			queryMultipleByFeature(ids);
		});
		
		$("#showImg").click(function() {
			if(cyEntry != null) {
				$('#entry-png-eg').attr('src', cyEntry.png({
					bg: "#ffffff",
					full : true
				}));
				$('#entry-png-eg').css("background-color", "#ffffff");
			}
		})

	};
	var clearMemo = function() {
		$("#clearMemo").click(function() {
			if(cyEntry == null) {
				return ;
			}
			cyEntry = utils.refreshCy(cyEntry);
			processCytoscape(cyEntry);
		});
	};
	return {
		init : function(){
			_init();
			console.log("finish _init");
			queryAll();
			clearMemo();
		}
	}
});