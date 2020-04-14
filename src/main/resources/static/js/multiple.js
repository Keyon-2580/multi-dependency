define(['jquery', 'bootstrap', 'bootstrap-multiselect', 'jqplot', 'cytoscapeUtils', 'cytoscape']
	, function ($, bootstrap, bootstrap_multiselect, jqplot, utils, cytoscape) {
	var cyEntry = null;
	
	var queryEntryEdge = function(testCaseId, callChain = false) {
		var testCaseIds = [];
		testCaseIds[testCaseIds.length] = testCaseId;
		var ids = {
			"ids" : testCaseIds,
			"callChain" : false
		};
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/multiple/microservice/query/edges",
			data : JSON.stringify(ids),
			success : function(result) {
				if (result.result == "success") {
					console.log(result);
//					cyEntry.remove('node');
					cyEntry.remove('edge[type="ShowStructureDependOnCall"]');
					cyEntry.remove('edge[type="ShowStructureDependOn"]');
					cyEntry.remove('edge[type="ShowStructureCall"]');
					cyEntry.remove('edge[type="NoStructureCall"]');
					cyEntry.remove('edge[type="TestCaseExecuteMicroService"]');
					cyEntry.remove('edge[type="NewEdges"]');
					var relatedNodes = result.nodes;
					var relatedEdges = result.edges;
					var datas = new Array();
					for(var i = 0; i < relatedEdges.length; i++) {
						cyEntry.remove(cyEntry.$("#" + relatedEdges[i].id));
						var data = {
								group: 'edges',
								data: {
									type: "NewEdges",
//									id: relatedEdges[i].id,
									source: relatedEdges[i].source,
									target: relatedEdges[i].target,
									value: relatedEdges[i].value
								}
						}
						datas.push(data);
					}
					cyEntry.add(datas);
				}
			}
		});
	}
	var processCytoscape = function(cyEntry) {
		console.log(cyEntry.edges());
		var edges = new Array();
		for(var i = 0; i < cyEntry.edges().length; i++) {
			var type = cyEntry.edges()[i].data().type;
			if(type == "TestCaseExecuteFeature") {
				var source = cyEntry.edges()[i].data().source;
				var target = cyEntry.edges()[i].data().target;
				utils.removeEdge(cyEntry, cyEntry.edges()[i].data().id);
				var edge = {
						type: "TestCaseExecuteFeature",
						source: target,
						target: source,
						value: ""
				};
				edges.push({data: edge});
			}
		}
		utils.addEdges(cyEntry, edges);
		cyEntry.on('tap', 'node', function(evt){
			var node = evt.target;
			console.log(node);
			console.log(node.data());
			if(node.data().type != "TestCase_success" && node.data().type != "TestCase_fail") {
				return ;
			}
			queryEntryEdge(node.data().id, true);
		})
	};
	var queryMultiple = function(testCaseIds) {
		cyEntry = null;
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/multiple/microservice/query/testcase",
			data : JSON.stringify(testCaseIds),
			success : function(result) {
				if (result.result == "success") {
					console.log(result.value);
					cyEntry = utils.showDataInCytoscape($("#entry"), result.value.data, "dagre");
					processCytoscape(cyEntry);
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
			var ids = {
				"ids" : $("#testCaseList").val(),
				"showAllFeatures" : false,
				"showAllMicroServices" : true,
				"showStructure" : true
			};
			console.log(ids);
			queryMultiple(ids);
		});
		$("#submitTestCase").click(function() {
			var ids = {
				"ids" : $("#testCaseList").val(),
				"showAllFeatures" : false,
				"showAllMicroServices" : true,
				"showStructure" : true
			};
			console.log(ids);
			queryMultiple(ids);
		});
		$("#submitFeature").click(function() {
			var ids = {
				"ids" : $("#testCaseList").val(),
				"showAllFeatures" : false,
				"showAllMicroServices" : true,
				"showStructure" : true
			};
			console.log(ids);
			queryMultiple(ids);
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
	return {
		init : function(){
			_init();
		}
	}
});