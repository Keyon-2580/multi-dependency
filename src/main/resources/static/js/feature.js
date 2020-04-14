var graphIdToResult = new Map();

var showMicroServiceInCytoscape = function(elements, container, nodeGraphId, btnInit = null, btnBack = null, btnAnimate = null){
	var cy = cytoscape({
		container: container,
		layout: {
			name: "dagre"
		},
		style: [
			{
				selector: 'node',
				style: {
					'shape' : 'rectangle',
    				'width': 'data(length)',
    				'height': 25,
    				'text-valign': 'center',
    				'text-halign': 'center',
    				'border-width': 1.5,
    				'border-color': '#555',
    				'background-color': '#f6f6f6',
					'content': 'data(name)'
				}
			},
			{
				selector: 'edge',
				style: {
					'content': 'data(value)',
					'curve-style': 'bezier',
    				'width': 1,
    				'line-color': '#555',
                    'target-arrow-shape': 'triangle',
                    'target-arrow-color': '#555',
                    'font-color' : '#555'
				}
			},
			{
				selector: 'edge[type="order"]',
				style: {
					'content': 'data(value)',
					'curve-style': 'bezier',
    				'width': 1,
    				'line-color': 'green',
                    'target-arrow-shape': 'triangle',
                    'target-arrow-color': 'green',
                    'font-color' : '#555'
				}
			}
			],
			elements: elements
	});
	var currentOrder = 1;
	var initEdges = cy.elements();
	var save = new Array();
	var saveInit = function() {
		save = new Array();
	};
	var savePush = function(edges) {
		save.push(edges);
	};
	var savePop = function() {
		if(save.length == 0) {
			return initEdges;
		} else {
			return save.pop();
		}
	};
	var saveLast = function() {
		if(save.length == 0) {
			return initEdges;
		} else {
			return save[save.length - 1];
		}
	};
	if(btnInit != null) {
		btnInit.click(function() {
			cy.remove('node');
			cy.remove('edge');
			saveInit();
			cy.add(initEdges);
			currentOrder = 1;
		});
	}
	if(btnAnimate != null) {
		btnAnimate.click(function(){
			var count = 0;
			cy.remove('edge');
			var result = graphIdToResult.get(nodeGraphId);
			var detail = result.detail;
			var datas = new Array();
			for(var sourceId in detail){
				var tos = detail[sourceId]["tos"];
				for(var targetId in tos) {
					var calls = tos[targetId]["call"];
					for(var i = 0; i < calls.length; i++) {
						var call = calls[i];
						var fromSpan = call.span;
						var toSpan = call.callSpan;
						if(toSpan.order <= currentOrder) {
							var data = {
									group: 'edges',
									data: {
										type: "order",
										id: fromSpan.id + "_" + toSpan.id,
										source: sourceId,
										target: targetId,
//			    							value: "( " + toSpan.order + ", " + call.httpRequestMethod + " )",
										value: "( " + toSpan.order + " )",
										detail: call
									}
							}
							datas.push(data);
							count++;
							if(count == currentOrder) {
								break;
							}
						}
					}
				}
			}
			currentOrder++;
			cy.add(datas);
		});
	}
	if(btnBack != null) {
		btnBack.click(function() {
			cy.remove('edge');
			cy.remove('node');
			var last = savePop();
			cy.add(last);
		});
	}
	cy.on('tap', 'edge', function(evt){
		if(btnBack == null){
			return ;
		}
		var result = graphIdToResult.get(nodeGraphId);
		var edge = evt.target;
		if(edge.data().type == "order") {
			return;
		}
		var sourceId = edge.source().id();
		var targetId = edge.target().id();
		var detail = result.detail;
		var from = detail[sourceId].from;
		var to = detail[sourceId]["tos"][targetId].to;
		var calls = detail[sourceId]["tos"][targetId].call;
		var datas = new Array();
		for(var i = 0; i < calls.length; i++) {
			var call = calls[i];
			var fromSpan = call.span;
			var toSpan = call.callSpan;
			var data = {
					group: 'edges',
					data: {
						type: "order",
						id: fromSpan.id + "_" + toSpan.id,
						source: sourceId,
						target: targetId,
//						value: "( " + toSpan.order + ", " + call.httpRequestMethod + " )",
						value: "( " + toSpan.order + " )",
						detail: call
					}
			}
			datas.push(data);
		}
		savePush(cy.elements());
		cy.remove(cy.$("#" + edge.id()));
		cy.add(datas);
	})
	
};

var showAPICall = function(traceId, container) {
	var traceIds = [];
	traceIds[traceIds.length] = traceId + "";
	var ids = {
		"ids" : traceIds
	};
	console.log(ids);
	$.ajax({
		type : "POST",
		contentType : "application/json",
		dataType : "json",
		url : "/feature/trace/cytoscape",
		data : JSON.stringify(ids),
		success : function(result) {
			console.log(result);
			if (result.result == "success") {
				console.log(result.value);
				showDataInCytoscape(container, result.value, layout="dagre");
			}
		}
	});
}

var showMicroServiceForFeature = function(id, container) {
	$.ajax({
		type: 'GET',
		url: "/feature/microservicecall/feature?featureGraphId=" + id,
		success: function(result) {
			console.log(result);
			if(result.result == "success") {
				graphIdToResult.set(id, result);
				var elements = result.value;
				showMicroServiceInCytoscape(elements, container, id);
			}
		}
	});
}

var showMicroServiceForTestCase = function(id, container) {
	$.ajax({
		type: 'GET',
		url: "/feature/microservicecall/testcase?testcaseGraphId=" + id,
		success: function(result) {
			console.log(result);
			if(result.result == "success") {
				graphIdToResult.set(id, result);
				var elements = result.value;
				showMicroServiceInCytoscape(elements, container, id);
			}
		}
	});
}

var showMicroServiceForTrace = function(id, container, btnInit, btnBack, btnAnimate) {
	$.ajax({
		type: 'GET',
		url: "/feature/microservicecall/trace?traceGraphId=" + id,
		success: function(result) {
			console.log(result);
			if(result.result == "success") {
				console.log(result);
				graphIdToResult.set(id, result);
				var elements = result.value;
				showMicroServiceInCytoscape(elements, container, id, btnInit, btnBack, btnAnimate);
			}
		}
	});
}

var showMicroServiceAll = function(id, container) {
	$.ajax({
		type: 'GET',
		url: "/feature/microservicecall/all",
		success: function(result) {
			console.log(result);
			if(result.result == "success") {
				console.log(result);
				graphIdToResult.set(id, result);
				console.log(graphIdToResult)
				var elements = result.value;
				showMicroServiceInCytoscape(elements, container, id);
			}
		}
	});
}

var featureInit = function() {
	showFeatureToTestCasesTreeView($("#treeForFeature"));
	showTestCaseToFeaturesTreeView($("#treeForTestCase"));
	showFeatureToTestCasesCytoscape($("#graph"));
};

var showTestCaseToFeaturesTreeView = function(containerDivId) {
	$.ajax({
		type: 'GET',
		url: "/feature/testcaseToFeature/treeview",
		success: function(result) {
			console.log(result);
			if(result.result == "success") {
				console.log(result.value)
				showTreeView(containerDivId, result.value);
			}
		}
	});
};

var showFeatureToTestCasesTreeView = function(containerDivId) {
	$.ajax({
		type: 'GET',
		url: "/feature/testcase/treeview",
		success: function(result) {
			console.log(result);
			if(result.result == "success") {
				console.log(result.value)
				showTreeView(containerDivId, result.value);
			}
		}
	});
};

var showFeatureToTestCasesCytoscape = function(containerDivId) {
	$.ajax({
		type: 'GET',
		url: "/feature/testcase/cytoscape",
		success: function(result) {
			console.log(result);
			if(result.result == "success") {
				console.log(result.value)
				var cy = showDataInCytoscape(containerDivId, result.value, "klay")
				$("#test").click(function(){
					$('#png-eg').attr('src', cy.png());
				});
			}
		}
	});
};

var toHTML = function(nodeType, nodeGraphId, cytoscapeDiv) {
	var html = "";
	if(nodeType == "trace") {
		html += 
			"<div class='col-sm-12 div_cytoscape_content' id='div_cytoscape_content'></div>"+
			"<div class='col-sm-12' style='margin-bottom:10px;'>" +
			"<div class='col-sm-1'><button class='btn btn-default' id='btn_animate'>流程</button></div>" +
			"<div class='col-sm-1'><button class='btn btn-default' id='btn_init'>初始</button></div>" +
			"<div class='col-sm-1'><button class='btn btn-default' id='btn_back'>返回上一歩</button></div>" +
			"</div>" 
			;
		cytoscapeDiv.html(html);
		showMicroServiceForTrace(nodeGraphId, $("#div_cytoscape_content"), $("#btn_init"), $("#btn_back"), $("#btn_animate"))
	} else if(nodeType == "testcase") {
		html = "<div class='col-sm-12 div_cytoscape_content' id='div_cytoscape_content'></div>";
		cytoscapeDiv.html(html);
		showMicroServiceForTestCase(nodeGraphId, $("#div_cytoscape_content"))
	} else if(nodeType == "feature") {
		html = "<div class='col-sm-12 div_cytoscape_content' id='div_cytoscape_content'></div>";
		cytoscapeDiv.html(html);
		showMicroServiceForFeature(nodeGraphId, $("#div_cytoscape_content"))
	} else if(nodeType == "all") {
		html = "<div class='col-sm-12 div_cytoscape_content' id='div_cytoscape_content'></div>";
		cytoscapeDiv.html(html);
		showMicroServiceAll(nodeGraphId, $("#div_cytoscape_content"))
	}
}

var itemOnclick = function (target){
	if(null != target.innerText && "" != target.innerText){
//			console.log($(target));
//			console.log($($(target).parent().parent()[0]).attr("id"));
		var targetDivId = $($(target).parent().parent()[0]).attr("id");
//			console.log(targetDivId);
		var nodeId = $(target).attr("data-nodeid");
		var node = $("#" + targetDivId).treeview('getNode', $(target).attr("data-nodeid"));
		var nodeType = node.tags[0];
		var tagId = node.href;
		if(nodeType == "trace") {
			$("#graph").html("");
			$("#graph").attr("class", "");
			$("#table").hide();
			$("#title").text("微服务调用")
			toHTML("trace", tagId, $("#graph"));
			showAPICall(tagId, $("#graphTestAPI"));
		}
		if(nodeType == "testcase") {
			$("#graph").html("");
			$("#graph").attr("class", "");
			$("#table").hide();
			$("#title").text("微服务调用")
			toHTML("testcase", tagId, $("#graph"));
		}
		/*if(nodeType == "feature") {
	    		$("#graph").html("");
	    		$("#graph").attr("class", "");
	    		$("#table").hide();
	    		$("#title").text("微服务调用")
				toHTML("feature", tagId, $("#graph"));
			}*/
		if(nodeType == "span") {
			var spanId = tagId;
			$.ajax({
				type: 'GET',
				url: "/function/treeview/span?spanGraphId=" + tagId,
				success: function(result) {
					console.log(result);
					if(result.result == "success") {
						$("#graph").html("");
						$("#graph").attr("class", "");
						$("#table").hide();
						$("#title").text("内部函数调用")
						$("#graph").treeview({
							data : result.value,
							showTags : true,
							levels: 1
						});
					}
				}
			});
		}
	}
};

var showTreeView = function(containerDivId, data) {
	containerDivId.treeview({
		data : data,
		showTags : true,
		levels: 1
	});
};
var styleEdgeBlue = {
		'content': 'data(value)',
		'curve-style': 'bezier',
		'width': 1,
		'line-color': 'blue',
		'target-arrow-shape': 'triangle',
		'target-arrow-color': 'blue',
		'font-size' : 20
};
var styleEdgeBlack = {
		'content': 'data(value)',
		'curve-style': 'bezier',
		'width': 1,
		'line-color': 'black',
		'target-arrow-shape': 'triangle',
		'target-arrow-color': 'black',
		'font-size' : 20
};
var styleEdgeGreen = {
		'content': 'data(value)',
		'curve-style': 'bezier',
		'width': 1,
		'line-color': 'green',
		'target-arrow-shape': 'triangle',
		'target-arrow-color': 'green',
		'font-size' : 20
};
var styleEdgeRed = {
		'content': 'data(value)',
		'curve-style': 'bezier',
		'width': 1,
		'line-color': 'red',
		'target-arrow-shape': 'triangle',
		'target-arrow-color': 'red',
		'font-size' : 20
};
var showDataInCytoscape = function(container, elements, layout="breadthfirst") {
	var cy = cytoscape({
    	container: container,
    	layout: {
    		name: layout
    	},
    	style: [
    		{
    			selector: 'node',
    			style: {
    				'shape' : 'rectangle',
    				'width': 'data(length)',
    				'height': 25,
    				'text-valign': 'center',
    				'text-halign': 'center',
    				'border-width': 1.5,
    				'border-color': '#555',
    				'background-color': '#f6f6f6',
					'content': 'data(name)'
    			}
    		},
    		{
    			selector: 'node[type="file"]',
    			style: {
    				'shape' : 'rectangle',
    				'width': 'data(length)',
    				'height': 25,
    				'text-valign': 'center',
    				'text-halign': 'center',
    				'border-width': 1.5,
    				'border-color': '#555',
    				'background-color': '#f6f6f6',
					'content': 'data(name)'
    			}
    		},
    		{
    			selector: 'node[type="package"]',
    			style: {
    				'shape' : 'rectangle',
    				'width': 'data(length)',
    				'height': 25,
    				'text-valign': 'center',
    				'text-halign': 'center',
    				'border-width': 1.5,
    				'border-color': '#555',
    				'background-color': '#f6f6f6',
					'content': 'data(name)'
    			}
    		},
    		{
    			selector: 'node[type="feature"]',
    			style: {
    				'shape' : 'ellipse',
    				'width': 'data(length)',
    				'height': 30,
    				'text-valign': 'center',
    				'text-halign': 'center',
    				'border-width': 1.5,
    				'border-color': '#555',
    				'background-color': '#f6f6f6',
					'content': 'data(name)'
    			}
    		},
    		{
    			selector: 'node[type="testcase"]',
    			style: {
    				'shape' : 'rectangle',
    				'width': 'data(length)',
    				'height': 25,
    				'text-valign': 'center',
    				'text-halign': 'center',
    				'border-width': 1.5,
    				'border-color': '#555',
    				'background-color': '#f6f6f6',
					'content': 'data(name)'
    			}
    		},
    		{
    			selector: 'node[type="Entry"]',
    			style: {
    				'shape' : 'ellipse',
    				'width': 50,
    				'height': 25,
    				'text-valign': 'center',
    				'text-halign': 'center',
    				'border-width': 1.5,
    				'border-color': '#555',
    				'background-color': '#f6f6f6',
					'content': 'data(name)'
    			}
    		},
    		{
    			selector: 'node[type="MicroService"]',
    			style: {
    				'shape' : 'rectangle',
    				'width': 'data(length)',
    				'height': 25,
    				'text-valign': 'top',
    				'text-halign': 'center',
    				'border-width': 1.5,
    				'border-color': '#555',
    				'background-color': '#f6f6f6',
					'content': 'data(name)'
    			}
    		},
    		{
    			selector: 'node[type="API"]',
    			style: {
    				'shape' : 'ellipse',
    				'width': 'data(length)',
    				'height': 25,
    				'text-valign': 'center',
    				'text-halign': 'center',
    				'border-width': 1.5,
    				'border-color': '#555',
    				'background-color': '#f6f6f6',
					'content': 'data(name)'
    			}
    		},
			{
    			selector: 'edge',
    			style: {
//    				'content': 'data(value)',
    				'curve-style': 'bezier',
    				'width': 1,
    				'line-color': '#555',
                    'target-arrow-shape': 'triangle',
                    'target-arrow-color': '#555',
                    'font-color' : '#555'
    			}
    		},
			{
    			selector: 'edge[type="APICall"]',
    			style: styleEdgeBlack
    		},
			{
    			selector: 'edge[type="contain"]',
    			style: {
//    				'content': 'data(value)',
    				'curve-style': 'bezier',
    				'width': 1,
    				'line-color': 'red',
                    'target-arrow-shape': 'triangle',
                    'target-arrow-color': 'red'
    			}
    		}
    	],
    	elements: elements
    });
	return cy;
};