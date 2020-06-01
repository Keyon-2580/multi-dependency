var multiple_microservice_all = function(cytoscapeutil) {
	var cyEntry = null;
	
	var firstTestCaseId = null;
	var secondTestCaseId = null;
	
	var nodeToPosition = new Map();
	
	var showZTree = function(nodes) {
		var setting = {
			callback: {
				onClick: function(event, treeId, treeNode) {
					var id = treeNode.id;
					console.log(id);
					if(id <= 0 || cyEntry == null) {
						return ;
					}
					var node = cyEntry.$('#' + id);
					cyEntry.fit(node, 350);
				}
			}	
				
		};
		var zNodes = nodes;
		var zTreeObj = $.fn.zTree.init($("#ztree"), setting, zNodes);
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
					showZTree(result.value.ztreeNodes);
					console.log(result.value.ztreeNodes);
					console.log(result.value.data);
					cyEntry = cytoscapeutil.showDataInCytoscape($("#entry"), result.value.data, "preset");
					cyEntry.remove('edge');

					if($("#showClonesInMicroService").prop('checked') == true) {
						console.log("true");
						console.log(cyEntry.nodes());
					}
					queryResult = result;
					
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
					cyEntry = cytoscapeutil.showDataInCytoscape($("#entry"), result.value.data, "dagre");
					showZTree(result.value.ztreeNodes);
					queryResult = result;
					processCytoscape(cyEntry);
					setTapNode(cyEntry, result);
				}
				if($("#showClonesInMicroService").prop('checked') == true) {
					console.log("true");
					console.log(cyEntry.nodes());
					cyEntry.$("#11019").data("name", "eeeeee")
					$.ajax({
						type: 'GET',
						url: "/clone/microservice/line",
						success: function(result) {
							console.log(result);
							for(var id in result) {
								cyEntry.$("#" + id).data("height", 50);
								cyEntry.$("#" + id).data("name", result[id].project.name 
										+ "\n文件总行数：" + result[id].allFilesLines + "，文件数：" + result[id].allFiles.length
										+ "\n克隆相关文件行数：" + result[id].allCloneFilesLines + "，文件数：" + result[id].cloneFiles.length
										+ "\n克隆相关方法行数：" + result[id].allCloneFunctionsLines + "，方法数：" + result[id].cloneFunctions.length
								);
							}
						}
					});
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
					cyEntry.batch(function(){
						cyEntry.remove('edge[type="all_MicroService_DependOn_MicroService"]');
						cyEntry.remove('edge[type="all_MicroService_call_MicroService"]');
						cyEntry.remove('edge[type="all_FeatureExecutedByTestCase"]');
						cyEntry.remove('edge[type="all_TestCaseExecuteMicroService"]');
						cyEntry.remove('edge[type="NewEdges"]');
						cyEntry.remove('edge[type="NewEdges_Edge1_Edge2"]');
						cyEntry.remove('edge[type="NewEdges_Edge1"]');
						cyEntry.remove('edge[type="NewEdges_Edge2"]');
					});
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
		cyEntry.batch(function(){
			for(var i = 0; i < removeIds.length; i++){
				cytoscapeutil.removeEdge(cyEntry, removeIds[i]);
			}
			cytoscapeutil.addEdges(cyEntry, edges);
		});
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
				var id = edge.data().id;
				var functions = result.cloneDetail[id];
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
	
	var _multiselect = function() {
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
	};
	
	var _init = function(){
		_multiselect();
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
			cytoscapeutil.showImg(cyEntry, "entry-png-eg");
		})
	};
	var clearMemo = function() {
		$("#clearMemo").click(function() {
			if(cyEntry == null) {
				return ;
			}
			cyEntry = cytoscapeutil.refreshCy(cyEntry);
			processCytoscape(cyEntry);
			setTapNode(cyEntry, null);
		});
	};
	
	var showHistogram = function() {
		var myChart = echarts.init(document.getElementById('main'));
		$.ajax({
			type : "GET",
			url : "/microservice/fanIO",
			success : function(result) {
				console.log(result);
				var xAxisData = [];
				var fanInData = [];
				var fanOutData = [];
				console.log(result.length);
				for(var i = 0; i < result.length; i++) {
					xAxisData[i] = result[i].node.name;
					console.log(xAxisData[i]);
					fanInData[i] = result[i].fanIn.length == 0 ? null : result[i].fanIn.length;
					fanOutData[i] = result[i].fanOut.length == 0 ? null : result[i].fanOut.length;
				}
				var option = {
		        	    tooltip: {
		        	        trigger: 'axis',
		        	        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
		        	            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
		        	        }
		        	    },
		        	    legend: {
		        	        data: ['FAN_IN', 'FAN_OUT']
		        	    },
		        	    grid: {
		        	        left: '3%',
		        	        right: '4%',
		        	        bottom: '3%',
		        	        containLabel: true
		        	    },
		        	    xAxis: [
		        	        {
		        	            type: 'category',
		        	            data: xAxisData,
		        	            axisLabel: {  
		        	                interval:0,  
		        	                rotate:40  
		        	             }  
		        	        }
		        	    ],
		        	    yAxis: [
		        	        {
		        	            type: 'value'
		        	        }
		        	    ],
		        	    series: [
		        	        {
		        	            name: 'FAN_IN',
		        	            type: 'bar',
		        	            stack: 'fan',
		        	            data: fanInData
		        	        },
		        	        {
		        	            name: 'FAN_OUT',
		        	            type: 'bar',
		        	            stack: 'fan',
		        	            data: fanOutData
		        	        }
		        	    ]
		        	};
		        // 使用刚指定的配置项和数据显示图表。
		        myChart.setOption(option);
			}
		});
        // 指定图表的配置项和数据
        var option = {
        	    tooltip: {
        	        trigger: 'axis',
        	        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
        	            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
        	        }
        	    },
        	    legend: {
        	        data: ['直接访问', '邮件营销', '联盟广告', '视频广告', '搜索引擎', '百度', '谷歌', '必应', '其他']
        	    },
        	    grid: {
        	        left: '3%',
        	        right: '4%',
        	        bottom: '3%',
        	        containLabel: true
        	    },
        	    xAxis: [
        	        {
        	            type: 'category',
        	            data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        	        }
        	    ],
        	    yAxis: [
        	        {
        	            type: 'value'
        	        }
        	    ],
        	    series: [
        	        {
        	            name: '直接访问',
        	            type: 'bar',
        	            data: [320, 332, 301, 334, 390, 330, 320]
        	        },
        	        {
        	            name: '邮件营销',
        	            type: 'bar',
        	            stack: '广告',
        	            data: [120, 132, 101, 134, 90, 230, 210]
        	        },
        	        {
        	            name: '联盟广告',
        	            type: 'bar',
        	            stack: '广告',
        	            data: [220, 182, 191, 234, 290, 330, 310]
        	        },
        	        {
        	            name: '视频广告',
        	            type: 'bar',
        	            stack: '广告',
        	            data: [150, 232, 201, 154, 190, 330, 410]
        	        },
        	        {
        	            name: '搜索引擎',
        	            type: 'bar',
        	            data: [862, 1018, 964, 1026, 1679, 1600, 1570],
        	            markLine: {
        	                lineStyle: {
        	                    type: 'dashed'
        	                },
        	                data: [
        	                    [{type: 'min'}, {type: 'max'}]
        	                ]
        	            }
        	        },
        	        {
        	            name: '百度',
        	            type: 'bar',
        	            barWidth: 5,
        	            stack: '搜索引擎',
        	            data: [620, 732, 701, 734, 1090, 1130, 1120]
        	        },
        	        {
        	            name: '谷歌',
        	            type: 'bar',
        	            stack: '搜索引擎',
        	            data: [120, 132, 101, 134, 290, 230, 220]
        	        },
        	        {
        	            name: '必应',
        	            type: 'bar',
        	            stack: '搜索引擎',
        	            data: [60, 72, 71, 74, 190, 130, 110]
        	        },
        	        {
        	            name: '其他',
        	            type: 'bar',
        	            stack: '搜索引擎',
        	            data: [62, 82, 91, 84, 109, 110, 120]
        	        }
        	    ]
        	};

	}
	
	var init = function(){
		_init();
		queryAll();
		clearMemo();
		showHistogram();
	};
	
	return {
		init : function() {
			init();
		}
	}
}

