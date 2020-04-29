var multiple_microservice_all = function(){
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
			data : JSON.stringify({
				"showStructure" : $("#showStructure").prop('checked'),
				"showMicroServiceCallLibs" : $("#showMicroServiceCallLibs").prop('checked'),
				"showClonesInMicroService" : $("#showClonesInMicroService").prop('checked'),
				"showCntOfDevUpdMs" : $("#showCntOfDevUpdMs").prop('checked')
			}),
			success : function(result) {
				if (result.result == "success") {
					console.log(result);
					cyEntry = showDataInCytoscape($("#entry"), result.value.data, "dagre");
					queryResult = result;
					processCytoscape(cyEntry);
					setTapNode(cyEntry, result);
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
			url : "/multiple/all/microservice/query/edges",
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
			if(type == "LibraryVersionIsFromLibrary") {
				var source = cyEntry.edges()[i].data().source;
				var target = cyEntry.edges()[i].data().target;
				removeIds.push(cyEntry.edges()[i].data().id);
				var edge = {
						type: "LibraryGroupAndNameContainVersion",
						source: target,
						target: source,
						value: ""
				};
				edges.push({data: edge});
			}
			if(type == "MicroServiceUpdatedByDeveloper") {
				var source = cyEntry.edges()[i].data().source;
				var target = cyEntry.edges()[i].data().target;
				removeIds.push(cyEntry.edges()[i].data().id);
				var edge = {
						type: "DeveloperUpdateMicroService",
						source: target,
						target: source,
						value: cyEntry.edges()[i].data().value
				};
				edges.push({data: edge});
			}
		}
		for(var i = 0; i < removeIds.length; i++){
			removeEdge(cyEntry, removeIds[i]);
		}
		addEdges(cyEntry, edges);
	};
	
	var setTapNode = function(cyEntry, result) {
		cyEntry.on('tap', 'node', function(evt){
			var node = evt.target;
			if(node.data().type == "TestCase_success" || node.data().type == "TestCase_fail") {
				queryEntryEdge(node.data().id, true);
			}
		})
		cyEntry.on('tap', 'edge', function(evt){
			var edge = evt.target;
			if(edge.data().type == "all_MicroService_clone_MicroService") {
				console.log(edge.data().id);
				console.log(result);
				var id = edge.data().id;
				var functions = result.cloneDetail[id];
				console.log(functions);
				if(functions != null) {
					$("#table_clone").html("");
					var html = "";
					for(var i = 0; i < functions.length; i++) {
						html += "<tr>";
						html += "<td>";
						html += functions[i].function1.name + "<br/>" + functions[i].function2.name;
						html += "</td>";
						html += "</tr>";
					}
					$("#table_clone").html(html);
				}
			}
		});
	}
	
	var queryMultipleByTestCaseOrFeatureOrScenario = function(params, queryType) {
		var url = null;
		if(queryType == "TestCase") {
			url = "/multiple/all/testcase";
		} else if(queryType == "Feature") {
			url = "/multiple/all/feature";
		} else if(queryType == "Scenario") {
			url = "/multiple/all/scenario"; 
		}
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
			data : JSON.stringify({
				"showStructure" : $("#showStructure").prop('checked'),
				"showMicroServiceCallLibs" : $("#showMicroServiceCallLibs").prop('checked'),
				"showClonesInMicroService" : $("#showClonesInMicroService").prop('checked'),
				"showCntOfDevUpdMs" : $("#showCntOfDevUpdMs").prop('checked')
			}),
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
					cyEntry = showDataInCytoscape($("#entry"), result.value.data, "preset");
					queryResult = result;
//					processCytoscape(cyEntry);
//					setTapNode(cyEntry);
					
					$.ajax({
						type : "POST",
						contentType : "application/json",
						dataType : "json",
						url : url,
						data : JSON.stringify(params),
						success : function(result) {
							if (result.result == "success") {
								console.log(result.value);
								cyEntry.remove('edge');
								var relatedEdges = result.value.value.edges;
								cyEntry.add(relatedEdges);
								processCytoscape(cyEntry);
								setTapNode(cyEntry, result);
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
			if(isNaN($("#showClonesMinPair").val())){
				alert($("#showClonesMinPair").val() + " 不是数字");
				return ;
			}
			var ids = {
					"ids" : $("#scenarioList").val(),
					"showStructure" : $("#showStructure").prop('checked'),
					"showMicroServiceCallLibs" : $("#showMicroServiceCallLibs").prop('checked'),
					"showClonesInMicroService" : $("#showClonesInMicroService").prop('checked'),
					"showCntOfDevUpdMs" : $("#showCntOfDevUpdMs").prop('checked'),
					"showClonesMinPair" : $("#showClonesMinPair").val()
			};
			console.log(ids);
			queryMultipleByTestCaseOrFeatureOrScenario(ids, "Scenario");
		});
		$("#submitTestCase").click(function() {
			if(isNaN($("#showClonesMinPair").val())){
				alert($("#showClonesMinPair").val() + " 不是数字");
				return ;
			}
			var ids = {
					"ids" : $("#testCaseList").val(),
					"showStructure" : $("#showStructure").prop('checked'),
					"showMicroServiceCallLibs" : $("#showMicroServiceCallLibs").prop('checked'),
					"showClonesInMicroService" : $("#showClonesInMicroService").prop('checked'),
					"showCntOfDevUpdMs" : $("#showCntOfDevUpdMs").prop('checked'),
					"showClonesMinPair" : $("#showClonesMinPair").val()
			};
			console.log(ids);
			queryMultipleByTestCaseOrFeatureOrScenario(ids, "TestCase");
		});
		$("#submitFeature").click(function() {
			if(isNaN($("#showClonesMinPair").val())){
				alert($("#showClonesMinPair").val() + " 不是数字");
				return ;
			}
			var ids = {
					"ids" : $("#featureList").val(),
					"showStructure" : $("#showStructure").prop('checked'),
					"showMicroServiceCallLibs" : $("#showMicroServiceCallLibs").prop('checked'),
					"showClonesInMicroService" : $("#showClonesInMicroService").prop('checked'),
					"showCntOfDevUpdMs" : $("#showCntOfDevUpdMs").prop('checked'),
					"showClonesMinPair" : $("#showClonesMinPair").val()
			};
			console.log(ids);
			queryMultipleByTestCaseOrFeatureOrScenario(ids, "Feature");
		});
		
		$("#showImg").click(function() {
			showImg(cyEntry, "entry-png-eg");
		})
	};
	var clearMemo = function() {
		$("#clearMemo").click(function() {
			if(cyEntry == null) {
				return ;
			}
			cyEntry = refreshCy(cyEntry);
			processCytoscape(cyEntry);
			setTapNode(cyEntry, null);
		});
	};
	var init = function(){
		_init();
		console.log("finish _init");
		queryAll();
		clearMemo();
		$("#browser").treeview();
		
	};
	
	return {
		init : function() {
			init();
		}
	}
}

