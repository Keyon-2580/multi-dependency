define(['jquery', 'bootstrap', 'bootstrap-multiselect', 'jqplot', 'cytoscapeUtils', 'cytoscape']
	, function ($, bootstrap, bootstrap_multiselect, jqplot, utils, cytoscape) {
	var cyEntry = null;
	
	var firstTestCaseId = null;
	var secondTestCaseId = null;
	
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
//					cyEntry.remove('node');
					cyEntry.remove('edge[type="ShowStructureDependOnCall"]');
					cyEntry.remove('edge[type="ShowStructureDependOn"]');
					cyEntry.remove('edge[type="ShowStructureCall"]');
					cyEntry.remove('edge[type="NoStructureCall"]');
					cyEntry.remove('edge[type="TestCaseExecuteMicroService"]');
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
			console.log(cyEntry.edges()[i].data());
		}
		for(var i = 0; i < cyEntry.edges().length; i++) {
			var type = cyEntry.edges()[i].data().type;
			if(type == "FeatureExecutedByTestCase") {
				var source = cyEntry.edges()[i].data().source;
				var target = cyEntry.edges()[i].data().target;
				removeIds.push(cyEntry.edges()[i].data().id);
//				utils.removeEdge(cyEntry, cyEntry.edges()[i].data().id);
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
			console.log(node);
			console.log(node.data());
			if(node.data().type != "TestCase_success" && node.data().type != "TestCase_fail") {
				return ;
			}
			queryEntryEdge(node.data().id, true);
		})
	};
	var queryMultipleByTestCase = function(testCaseIds) {
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
	var queryMultipleByScenario = function(scenarioIds) {
		cyEntry = null;
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/multiple/microservice/query/scenario",
			data : JSON.stringify(scenarioIds),
			success : function(result) {
				if (result.result == "success") {
					console.log(result.value);
					cyEntry = utils.showDataInCytoscape($("#entry"), result.value.data, "dagre");
					processCytoscape(cyEntry);
				}
			}
		});
	}
	var queryMultipleByFeature = function(featureIds) {
		cyEntry = null;
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/multiple/microservice/query/feature",
			data : JSON.stringify(featureIds),
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
			var showAllScenarios = $("#showAllScenarios").prop('checked');
			var showAllFeatures = $("#showAllFeatures").prop('checked');
			var showAllMicroServices = $("#showAllMicroServices").prop('checked');
			var showStructure = $("#showStructure").prop('checked');
			console.log(showAllScenarios);
			var ids = {
				"ids" : $("#scenarioList").val(),
				"showAllScenarios" : showAllScenarios,
				"showAllFeatures" : showAllFeatures,
				"showAllMicroServices" : showAllMicroServices,
				"showStructure" : showStructure
			};
			console.log(ids);
			queryMultipleByScenario(ids);
		});
		$("#submitTestCase").click(function() {
			var showAllScenarios = $("#showAllScenarios").prop('checked');
			var showAllFeatures = $("#showAllFeatures").prop('checked');
			var showAllMicroServices = $("#showAllMicroServices").prop('checked');
			var showStructure = $("#showStructure").prop('checked');
			var ids = {
				"ids" : $("#testCaseList").val(),
				"showAllScenarios" : showAllScenarios,
				"showAllFeatures" : showAllFeatures,
				"showAllMicroServices" : showAllMicroServices,
				"showStructure" : showStructure
			};
			console.log(ids);
			queryMultipleByTestCase(ids);
		});
		$("#submitFeature").click(function() {
			var showAllScenarios = $("#showAllScenarios").prop('checked');
			var showAllFeatures = $("#showAllFeatures").prop('checked');
			var showAllMicroServices = $("#showAllMicroServices").prop('checked');
			var showStructure = $("#showStructure").prop('checked');
			var ids = {
				"ids" : $("#featureList").val(),
				"showAllScenarios" : showAllScenarios,
				"showAllFeatures" : showAllFeatures,
				"showAllMicroServices" : showAllMicroServices,
				"showStructure" : showStructure
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
	return {
		init : function(){
			_init();
		}
	}
});