define(['jquery', 'bootstrap', 'bootstrap-multiselect', 'bootstrap-treeview',
	'jqplot', 'utils', 'cytoscape', 'cytoscape-dagre', 'dagre', 'cytoscape-klay', 'klayjs']
	, function ($, bootstrap, bootstrap_multiselect, bootstrap_treeview,
	jqplot, utils, cytoscape, cytoscape_dagre, dagre, cytoscape_klay, klayjs) {
	
	var _showTreeView = function(containerDivId, data) {
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
			'line-style': 'solid',
			'font-size' : 20
	};
	var styleEdgeBlack = {
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'black',
			'target-arrow-shape': 'triangle',
			'target-arrow-color': 'black',
			'line-style': 'solid',
			'font-size' : 20
	};
	var styleEdgeGreen = {
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'green',
			'target-arrow-shape': 'triangle',
			'target-arrow-color': 'green',
			'line-style': 'solid',
			'font-size' : 20
	};
	var styleEdgeRed = {
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'red',
			'target-arrow-shape': 'triangle',
			'target-arrow-color': 'red',
			'line-style': 'solid',
			'font-size' : 20
	};
	var styleEdgeDashed = {
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'black',
			'target-arrow-shape': 'triangle',
			'target-arrow-color': 'black',
			'line-style': 'dashed',
			'font-size' : 20
	};
	var styleEdgeClone = {
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'green',
			'line-style': 'dashed',
			'target-arrow-shape' : 'none',
			'font-size' : 20
	}
	var _showDataInCytoscape = function(container, elements, layout="dagre") {
		console.log("_showDataInCytoscape: " + layout);
		cytoscape_klay(cytoscape);
		cytoscape_dagre(cytoscape);
		var cy = cytoscape({
	    	container: container,
	    	layout: {
	    		name: layout
	    	},
	    	textureOnViewport: false,
	    	hideEdgesOnViewport: true,
	    	motionBlurOpacity: true,
	    	boxSelectionEnabled: true,
//	    	pixelRatio: 1,
	    	style: [
	    		{
	    			selector: 'node',
	    			style: {
	    				'shape' : 'rectangle',
//	    				'width': 'data(length)',
	    				'width': function(content) {
	    					return content.data().name.length * 13;
	    				},
	    				'height': 30,
	    				'background-color': '#00FF66',
						'content': 'data(name)'
	    			}
	    		},
	    		{
	    			selector: 'node[type="Package"]',
	    			style: {
	    				'shape' : 'rectangle',
//	    				'width': 'data(length)',
	    				'width': function(content) {
	    					return content.data().name.length * 13;
	    				},
	    				'height': 30,
	    				'text-valign': 'top',
	    				'text-halign': 'center',
	    				'border-width': 1.5,
	    				'border-color': '#555',
	    				'background-color': '#f6f6f6',
						'content': 'data(name)'
	    			}
	    		},
	    		{
	    			selector: 'node[type="File"]',
	    			style: {
	    				'shape' : 'rectangle',
//	    				'width': 'data(length)',
	    				'width': function(content) {
	    					return content.data().name.length * 13;
	    				},
	    				'height': 30,
	    				'text-valign': 'top',
	    				'text-halign': 'center',
	    				'border-width': 1.5,
	    				'border-color': '#555',
	    				'background-color': '#f6f6f6',
						'content': 'data(name)'
	    			}
	    		},
	    		{
	    			selector: 'node[type="Type"]',
	    			style: {
	    				'shape' : 'rectangle',
//	    				'width': 'data(length)',
	    				'width': function(content) {
	    					return content.data().name.length * 13;
	    				},
	    				'height': 30,
	    				'text-valign': 'top',
	    				'text-halign': 'center',
	    				'border-width': 1.5,
	    				'border-color': '#555',
	    				'background-color': '#f6f6f6',
						'content': 'data(name)'
	    			}
	    		},
	    		{
	    			selector: 'node[type="Function"]',
	    			style: {
	    				'shape' : 'rectangle',
	    				'width': function(content) {
	    					return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 8;
	    				},
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
	    			selector: 'node[type="Variable"]',
	    			style: {
	    				'shape' : 'ellipse',
	    				'width': function(content) {
	    					return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 9;
	    				},
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
	    			selector: 'node[type="Feature"]',
	    			style: {
	    				'shape' : 'ellipse',
	    				'width' :  function(content) {
	    					return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 9;
	    				},
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
	    			selector: 'node[type="Scenario"]',
	    			style: {
	    				'shape' : 'rectangle',
	    				'width':  function(content) {
	    					return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 8;
	    				},
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
	    			selector: 'node[type="TestCase"]',
	    			style: {
	    				'shape' : 'rectangle',
	    				'width': function(content) {
	    					return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 8;
	    				},
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
	    			selector: 'node[type="TestCase_success"]',
	    			style: {
	    				'shape' : 'rectangle',
	    				'width' : function(content) {
	    					return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 8;
	    				},
	    				'height': 30,
	    				'text-valign': 'center',
	    				'text-halign': 'center',
	    				'border-width': 1.5,
	    				'border-color': '#555',
	    				'background-color': '#9EEA6A',
	    				'content': 'data(name)'
	    			}
	    		},
	    		{
	    			selector: 'node[type="TestCase_fail"]',
	    			style: {
	    				'shape' : 'rectangle',
	    				'width': function(content) {
	    					return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 8;
	    				},
	    				'height': 30,
	    				'text-valign': 'center',
	    				'text-halign': 'center',
	    				'border-width': 1.5,
	    				'border-color': '#555',
	    				'background-color': 'red',
						'content': 'data(name)'
	    			}
	    		},
	    		{
	    			selector: 'node[type="MicroService"]',
	    			style: {
	    				'shape' : 'hexagon',
//	    				'width': 'data(length)',
	    				'width': function(content) {
	    					return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
	    				},
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
	    			selector: 'node[type="MicroService_related"]',
	    			style: {
	    				'shape' : 'hexagon',
//	    				'width': 'data(length)',
	    				'width': function(content) {
	    					return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
	    				},
	    				'height': 30,
	    				'text-valign': 'center',
	    				'text-halign': 'center',
	    				'border-width': 1.5,
	    				'border-color': '#555',
	    				'background-color': '#00FF66',
						'content': 'data(name)'
	    			}
	    		},
	    		{
	    			selector: 'node[type="Library"]',
	    			style: {
	    				'shape' : 'ellipse',
//	    				'width': 'data(length)',
	    				'width': function(content) {
	    					return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
	    				},
	    				'height': 30,
	    				'text-valign': 'center',
	    				'text-halign': 'center',
	    				'border-width': 1.5,
	    				'border-color': '#555',
	    				'background-color': '#00FF66',
						'content': 'data(name)'
	    			}
	    		},
				{
	    			selector: 'edge',
	    			style: styleEdgeBlack
	    		},
	    		{
	    			selector: 'edge[type="TestCaseExecuteMicroService"]',
	    			style: styleEdgeBlack
	    		},
				{
	    			selector: 'edge[type="ShowStructureDependOnCall"]',
	    			style: styleEdgeGreen
	    		},
				{
	    			selector: 'edge[type="ShowStructureDependOn"]',
	    			style: styleEdgeBlack
	    		},
				{
	    			selector: 'edge[type="ShowStructureCall"]',
	    			style: styleEdgeRed
	    		},
				{
	    			selector: 'edge[type="NoStructureCall"]',
	    			style: styleEdgeBlack
	    		},
				{
	    			selector: 'edge[type="NewEdges"]',
	    			style: styleEdgeBlack
	    		},
				{
	    			selector: 'edge[type="NewEdges_Edge1_Edge2"]',
	    			style: styleEdgeBlue
	    		},
				{
	    			selector: 'edge[type="NewEdges_Edge1"]',
	    			style: styleEdgeGreen
	    		},
				{
	    			selector: 'edge[type="NewEdges_Edge2"]',
	    			style: styleEdgeRed
	    		},
	    		{
	    			selector: 'edge[type="all_Feature_Contain_Feature"]',
	    			style: styleEdgeBlack
	    		},
	    		{
	    			selector: 'edge[type="all_ScenarioDefineTestCase"]',
	    			style: styleEdgeBlack
	    		},
	    		{
	    			selector: 'edge[type="all_TestCaseExecuteMicroService"]',
	    			style: styleEdgeBlack
	    		},
	    		{
	    			selector: 'edge[type="all_FeatureExecutedByTestCase]"]',
	    			style: styleEdgeBlack
	    		},
	    		{
	    			selector: 'edge[type="all_MicroService_call_MicroService"]',
	    			style: styleEdgeBlack
	    		},
	    		{
	    			selector: 'edge[type="all_MicroService_DependOn_MicroService"]',
	    			style: styleEdgeDashed
	    		},
	    		{
	    			selector: 'edge[type="all_MicroService_clone_MicroService"]',
	    			style: styleEdgeClone
	    		}
	    	],
	    	elements: elements
	    });
		return cy;
	};
	var _addNodes = function(cy, nodes) {
		for(var i = 0; i < nodes.length; i++) {
			console.log(nodes[i]);
			cy.add({data: nodes[i].data})
		}
	};
	var _addEdges = function(cy, edges) {
		for(var i = 0; i < edges.length; i++) {
			var data = edges[i].data;
//			data["line-color"] = 'yellow';
			cy.add({data: data})
		}
	};
	
	var _removeEdge = function(cy, edgeId) {
		cy.remove(cy.$("#" + edgeId));
	};
	
	var _removeNode = function(cy, nodeId) {
		cy.remove(cy.$("#" + nodeId));
	};
	
	var _refresh = function(cy) {
		var cyNodes = cy.nodes();
		var cyEdges = cy.edges();
		var newNodes = [];
		var newEdges = [];
		var value = {};
		for(var i = 0; i < cyNodes.length; i++) {
			console.log(cyNodes[i].data());
			console.log(cyNodes[i].position());
			newNodes[i] = {};
			newNodes[i].data = cyNodes[i].data();
			newNodes[i].position = cyNodes[i].position();
		}
		for(var i = 0; i < cyEdges.length; i++) {
			console.log(cyEdges[i].data());
			newEdges[i] = {};
			newEdges[i].data = cyEdges[i].data();
		}
		value.nodes = newNodes;
		value.edges = newEdges;
		cy = _showDataInCytoscape($("#" + cy.container().id), value, "preset");
		return cy;
	};
	
	return {
		removeEdge: function(cy, edgeId) {
			_removeEdge(cy, edgeId);
		},
		test2: function() {
			
		},
		test: function(){
			console.log("eeeeeeeeeeeee");
		},
		showTreeView: function(containerDivId, data) {
			_showTreeView(containerDivId, data)
		},
		showDataInCytoscape: function(container, elements, layout) {
			return _showDataInCytoscape(container, elements, layout);
		},
		addNodes: function(cytoscape, nodes) {
			_addNodes(cytoscape, nodes);
		},
		addEdges: function(cytoscape, edges) {
			_addEdges(cytoscape, edges);
		},
		refreshCy: function(cy) {
			return _refresh(cy);
		}
	}
});
