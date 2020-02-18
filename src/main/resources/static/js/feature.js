//<![CDATA[
var featureIdToMicroService = new Map();
var featureIdToResult = new Map();
var features = null;
var showMicroServiceInCatoscape = function(elements, container, featureId, btnInit = null, btnBack = null, btnAnimate = null){
	var cy = cytoscape({
    	container: container,
    	layout: {
    		name: "breadthfirst"
    	},
    	style: [
    		{
    			selector: 'node',
    			style: {
    				'height': 30,
    				'width': 30,
    				'background-color': 'green',
					'content': 'data(name)'
    			}
    		},
			{
    			selector: 'edge',
    			style: {
    				'content': 'data(value)',
    				'curve-style': 'bezier',
    				'width': 2,
    				'line-color': 'green',
                    'target-arrow-shape': 'triangle',
                    'target-arrow-color': 'green'
    			}
    		},
			{
    			selector: 'edge[type="order"]',
    			style: {
    				'content': 'data(value)',
    				'curve-style': 'bezier',
    				'width': 1,
    				'line-color': 'red',
                    'target-arrow-shape': 'triangle',
                    'target-arrow-color': 'red',
                    'color': 'red'
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
			var result = featureIdToResult.get(featureId);
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
//		    							value: "( " + toSpan.order + ", " + call.httpRequestMethod + " )",
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
	cy.on('tap', 'node', function(evt){
		var result = featureIdToResult.get(featureId);
		if(result == null || result.detail == null || result.traceId == null){
			alert("featureId错误");
			return ;
		}
		var node = evt.target;
		console.log(result);
		console.log(node);
		console.log(node.data());
		$.ajax({
	    	type: 'GET',
	    	url: "/feature/show/function/microservice?microserviceId=" + node.data().id + "&traceId=" + result.traceId,
	    	success: function(result) {
	    		console.log(result);
		    	if(result.result == "success") {
		    		
		    	}
	    	}
	    });
	});
	cy.on('tap', 'edge', function(evt){
		var result = featureIdToResult.get(featureId);
		if(result == null || result.detail == null){
			alert("featureId错误");
			return ;
		}
		var edge = evt.target;
		if(edge.data().type == "order") {
			// 弹出详细调用
			console.log(edge.data());
			
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
//					value: "( " + toSpan.order + ", " + call.httpRequestMethod + " )",
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
var showMicroService = function(featureId, container, btnInit, btnBack, btnAnimate) {
    $.ajax({
    	type: 'GET',
    	url: "/feature/show/microservice/" + featureId + "?removeUnuseMicroService=true",
    	success: function(result) {
    		console.log(result);
	    	if(result.result == "success") {
	    		featureIdToResult.set(featureId, result);
	    		var elements = result.value;
	    		var microservices = new Map();
	    		for(var i = 0; i < result.microservice.length; i++) {
	    			microservices.set(result.microservice[i].id, {
	    				id : result.microservice[i].id,
	    				name : result.microservice[i].name,
	    				folder : true
	    			});
	    		}
	    		featureIdToMicroService.set(featureId, microservices);
	    		console.log(featureIdToMicroService);
	    		showMicroServiceInCatoscape(elements, container, featureId, btnInit, btnBack, btnAnimate);
	    	}
    	}
    });
}
var featureToHTML = function(featureIds, cytoscapeDiv) {
	var html = "";
	console.log(featureIds);
	console.log(features);
	var featureNames = features[featureIds[0]].featureName;
	for(var i = 1; i < featureIds.length; i++) {
		featureNames += ", " + features[featureIds[i]].featureName;
	}
	if(featureIds.length > 1) {
		html += "<div class='col-sm-12'><h3>Feature: " + featureNames + "</h3></div><div class='col-sm-12 div_cytoscape_content' id='div_cytoscape_content'></div>";
	}
	for(var i = 0; i < featureIds.length; i++) {
		html += "<div class='col-sm-12'><h3>Feature: " + features[featureIds[i]].featureName + "</h3>" +
				"</div>" +
				"<div class='col-sm-12' style='margin-bottom:10px;'>" +
				"<div class='col-sm-1'><button class='btn btn-default' id='btn_animate_" + i + "'>流程</button></div>" +
				"<div class='col-sm-1'><button class='btn btn-default' id='btn_init_" + i + "'>初始</button></div>" +
				"<div class='col-sm-1'><button class='btn btn-default' id='btn_back_" + i + "'>返回上一歩</button></div>" +
				"</div>" +
				"<div class='col-sm-12 div_cytoscape_content' id='div_cytoscape_content_" + i + "'></div>";
	}
	cytoscapeDiv.html(html);
	if(featureIds.length > 1) {
		showMicroService(featureIds, $("#div_cytoscape_content"));
	}
	for(var i = 0; i < featureIds.length; i++) {
		showMicroService(featureIds[i], $("#div_cytoscape_content_" + i), $("#btn_init_" + i), $("#btn_back_" + i), $("#btn_animate_" + i));
	}
}
var featureIndex = function(featuresFromServer, cytoscapeDiv) {
	features = featuresFromServer;
    $('#features-select').multiselect({
    	includeSelectAllOption: true
    });
    $("#featuresSelectedConfirm").click(function(e){
    	var featureIds = $("#features-select").val();
    	console.log(featureIds);
    	if(featureIds.length == 0) {
    		return;
    	}
    	featureToHTML(featureIds, cytoscapeDiv);
    });
    cytoscapeDiv.html("<div class='col-sm-12'><h3>All Feature</h3></div><div class='col-sm-12 div_cytoscape_content' id='div_cytoscape_content'></div>");
    showMicroService("all", $("#div_cytoscape_content"));
    
}
// ]]>