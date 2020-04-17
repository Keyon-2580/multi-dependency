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
		var nodeSize = 15;
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
	    			selector: 'node[type="file"]',
	    			style: {
	    				'height': 30,
	    				'width': 30,
	    				'background-color': 'green',
						'content': 'data(name)'
	    			}
	    		},
	    		{
	    			selector: 'node[type="package"]',
	    			style: {
	    				'height': 30,
	    				'width': 30,
	    				'background-color': 'red',
						'content': 'data(name)'
	    			}
	    		},
	    		{
	    			selector: 'node[type="feature"]',
	    			style: {
	    				'height': 35,
	    				'width': 35,
	    				'background-color': 'red',
						'content': 'data(value)',
						'font-size' : 25
	    			}
	    		},
	    		{
	    			selector: 'node[type="Feature"]',
	    			style: {
	    				'shape' : 'ellipse',
//	    				'width': 'data(length)',
	    				'width': 'label',
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
	    				'height': 35,
	    				'width': 35,
	    				'background-color': 'green',
						'content': 'data(value)',
						'font-size' : 25
	    			}
	    		},
	    		{
	    			selector: 'node[type="TestCase"]',
	    			style: {
	    				'shape' : 'rectangle',
//	    				'width': 'data(length)',
	    				'width': 'label',
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
//	    				'width': 'data(length)',
	    				'width': 'label',
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
//	    				'width': 'data(length)',
	    				'width': 'label',
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
	    				'width': 'label',
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
	    				'width': 'label',
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
	    			selector: 'node[type="noMicroService"]',
	    			style: {
	    				'shape' : 'rectangle',
//	    				'width': 'data(length)',
	    				'width': 'label',
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
	    			selector: 'node[type="allMicroService"]',
	    			style: {
	    				'shape' : 'rectangle',
//	    				'width': 'data(length)',
	    				'width': 'label',
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
	    			selector: 'node[type="selectMicroService"]',
	    			style: {
	    				'shape' : 'rectangle',
//	    				'width': 'data(length)',
	    				'width': 'label',
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
	    			selector: 'edge',
	    			style: {
//	    				'content': 'data(value)',
	    				'curve-style': 'bezier',
	    				'width': 1,
	    				'line-color': 'black',
	                    'target-arrow-shape': 'triangle',
	                    'target-arrow-color': 'black'
	    			}
	    		},
				{
	    			selector: 'edge[type="contain"]',
	    			style: {
//	    				'content': 'data(value)',
	    				'curve-style': 'bezier',
	    				'width': 1,
	    				'line-color': 'red',
	                    'target-arrow-shape': 'triangle',
	                    'target-arrow-color': 'red'
	    			}
	    		},
				{
	    			selector: 'edge[type="allTestCase"]',
	    			style: {
//	    				'content': 'data(value)',
	    				'curve-style': 'bezier',
	    				'width': 1,
	    				'line-color': 'black',
	                    'target-arrow-shape': 'triangle',
	                    'target-arrow-color': 'black'
	    			}
	    		},
				{
	    			selector: 'edge[type="selectTestCase"]',
	    			style: {
//	    				'content': 'data(value)',
	    				'curve-style': 'bezier',
	    				'width': 1,
	    				'line-color': 'green',
	                    'target-arrow-shape': 'triangle',
	                    'target-arrow-color': 'green',
	                    'color': 'red',
						'font-size' : 10,
						'color':"black"
	    			}
	    		},
				{
	    			selector: 'edge[type="TestCase_Call"]',
	    			style: {
//	    				'content': 'data(value)',
	    				'curve-style': 'bezier',
	    				'width': 1,
	    				'line-color': 'blue',
	                    'target-arrow-shape': 'triangle',
	                    'target-arrow-color': 'blue',
						'font-size' : 10
	    			}
	    		},
				{
	    			selector: 'edge[type="GREEN"]',
	    			style: {
//	    				'content': 'data(value)',
	    				'curve-style': 'bezier',
	    				'width': 1,
	    				'line-color': 'green',
	                    'target-arrow-shape': 'triangle',
	                    'target-arrow-color': 'green',
						'font-size' : 10
	    			}
	    		}
	    	],
	    	elements: elements
	    });
		return cy;
	};
	var _addNodes = function(cy, nodes) {
		console.log(nodes);
		console.log(cy);
		console.log(cy.elements());
		for(var i = 0; i < nodes.length; i++) {
			console.log(nodes[i]);
			cy.add({group: 'nodes', data: nodes[i].data})
		}
	};
	var _addEdges = function(cy, edges) {
		console.log(edges);
		for(var i = 0; i < edges.length; i++) {
			console.log(edges[i])
			var data = edges[i].data;
			data["line-color"] = 'yellow';
			cy.add({group: 'edges', data: data})
		}

	};
	return {
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
