define(['jquery', 'cytoscape'], function ($, cytoscape){
	var _showTreeView = function(containerDivId, data) {
		containerDivId.treeview({
			data : data,
			showTags : true,
			levels: 1
		});
	};

	var _showDataInCytoscape = function(container, elements, layout="breadthfirst") {
		console.log(elements);
		var cy = cytoscape({
	    	container: container,
	    	layout: {
	    		name: layout
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
	    			selector: 'node[type="noMicroService"]',
	    			style: {
	    				'height': 30,
	    				'width': 30,
	    				'background-color': 'black',
						'content': 'data(name)',
						'font-size' : 25
	    			}
	    		},
	    		{
	    			selector: 'node[type="allMicroService"]',
	    			style: {
	    				'height': 30,
	    				'width': 30,
	    				'background-color': 'green',
						'content': 'data(name)',
						'font-size' : 25
	    			}
	    		},
	    		{
	    			selector: 'node[type="selectMicroService"]',
	    			style: {
	    				'height': 30,
	    				'width': 30,
	    				'background-color': 'red',
						'content': 'data(name)',
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
	    		}
	    	],
	    	elements: elements
	    });
		return cy;
	};
	return {
		test:function() {
			alert("ee")
		},
		showTreeView: function(containerDivId, data) {
			_showTreeView(containerDivId, data)
		},
		showDataInCytoscape: function(container, elements, layout) {
			_showDataInCytoscape(container, elements, layout);
		}
	}
});
