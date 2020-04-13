define(['jquery', 'bootstrap', 'bootstrap-multiselect', 'bootstrap-treeview',
	'jqplot', 'utils', 'cytoscape', 'cytoscape-dagre', 'dagre']
	, function ($, bootstrap, bootstrap_multiselect, bootstrap_treeview,
	jqplot, utils, cytoscape, cytoscape_dagre, dagre) {
	
	var _showTreeView = function(containerDivId, data) {
		containerDivId.treeview({
			data : data,
			showTags : true,
			levels: 1
		});
	};
	var colors = {
		"0" : "blue",
		"1" : "yellow"
	};
	var _showDataInCytoscape = function(container, elements, layout="breadthfirst") {
		cytoscape_dagre(cytoscape);
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
		var cy = cytoscape({
	    	container: container,
	    	layout: {
	    		name: "dagre"
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
	    			selector: 'node[type="Feature"]',
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
	    			selector: 'node[type="TestCase"]',
	    			style: {
	    				'shape' : 'rectangle',
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
	    			selector: 'node[type="TestCase_success"]',
	    			style: {
	    				'shape' : 'rectangle',
	    				'width': 'data(length)',
	    				'height': 30,
	    				'text-valign': 'center',
	    				'text-halign': 'center',
	    				'border-width': 1.5,
	    				'border-color': '#555',
	    				'background-color': 'green',
						'content': 'data(name)'
	    			}
	    		},
	    		{
	    			selector: 'node[type="TestCase_fail"]',
	    			style: {
	    				'shape' : 'rectangle',
	    				'width': 'data(length)',
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
	    			selector: 'node[type="MicroService_related"]',
	    			style: {
	    				'shape' : 'hexagon',
	    				'width': 'data(length)',
	    				'height': 30,
	    				'text-valign': 'center',
	    				'text-halign': 'center',
	    				'border-width': 1.5,
	    				'border-color': '#555',
	    				'background-color': 'green',
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
			var cy = _showDataInCytoscape(container, elements, layout);
			
			return cy;
		},
		addNodes: function(cytoscape, nodes) {
			_addNodes(cytoscape, nodes);
		},
		addEdges: function(cytoscape, edges) {
			_addEdges(cytoscape, edges);
		}
	}
});
