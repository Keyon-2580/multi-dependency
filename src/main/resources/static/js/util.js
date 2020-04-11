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
	    				'height': nodeSize,
	    				'width': nodeSize,
	    				'background-color': 'blue',
						'content': 'data(name)',
						'font-size' : 15
	    			}
	    		},
	    		{
	    			selector: 'node[type="noMicroService"]',
	    			style: {
	    			    'shape': 'triangle',
	    				'height': nodeSize,
	    				'width': nodeSize,
	    				'background-color': 'black',
						'content': 'data(name)',
						'font-size' : 15
	    			}
	    		},
	    		{
	    			selector: 'node[type="allMicroService"]',
	    			style: {
	    				'height': nodeSize,
	    				'width': nodeSize,
	    				'background-color': 'green',
						'content': 'data(name)',
						'font-size' : 15
	    			}
	    		},
	    		{
	    			selector: 'node[type="selectMicroService"]',
	    			style: {
	    				'height': nodeSize,
	    				'width': nodeSize,
	    				'background-color': 'red',
						'content': 'data(name)',
						'font-size' : 15
	    			}
	    		},
				{
	    			selector: 'edge',
	    			style: {
//	    				'content': 'data(value)',
	    				'curve-style': 'bezier',
	    				'width': 1,
	    				'line-color': 'green',
	                    'target-arrow-shape': 'triangle',
	                    'target-arrow-color': 'green'
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
	                    'target-arrow-color': 'red',
	                    'color': 'red'
	    			}
	    		},
				{
	    			selector: 'edge[type="allTestCase"]',
	    			style: {
//	    				'content': 'data(value)',
	    				'curve-style': 'bezier',
	    				'width': 1,
	    				'line-color': 'green',
	                    'target-arrow-shape': 'triangle',
	                    'target-arrow-color': 'green',
	                    'color': 'green',
						'font-size' : 10,
						'color':"black"
	    			}
	    		},
				{
	    			selector: 'edge[type="selectTestCase"]',
	    			style: {
//	    				'content': 'data(value)',
	    				'curve-style': 'bezier',
	    				'width': 1,
	    				'line-color': 'red',
	                    'target-arrow-shape': 'triangle',
	                    'target-arrow-color': 'red',
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
						'font-size' : 10,
//						'color':"blue"
						'color': colors['data(index)']
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
		test: function(){
			console.log('rrr')
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
