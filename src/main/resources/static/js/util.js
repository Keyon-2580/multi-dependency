var showTreeView = function(containerDivId, data) {
	containerDivId.treeview({
		data : data,
		showTags : true,
		levels: 1
	});
};

var showDataInCatoscape = function(container, elements, layout="breadthfirst"){
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
    				'height': 15,
    				'width': 15,
    				'background-color': 'red',
					'content': 'data(value)'
    			}
    		},
    		{
    			selector: 'node[type="testcase"]',
    			style: {
    				'height': 15,
    				'width': 15,
    				'background-color': 'green',
					'content': 'data(value)'
    			}
    		},
			{
    			selector: 'edge',
    			style: {
//    				'content': 'data(value)',
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
//    				'content': 'data(value)',
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
	
};


