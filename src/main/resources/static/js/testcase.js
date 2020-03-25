define(['jquery', 'bootstrap-multiselect', 'jqplot', 'utils', 'cytoscape', 'cytoscape-dagre', 'dagre']
	, function ($, bootstrap_multiselect, jqplot, utils, cytoscape, cytoscape_dagre, dagre) {
	var _showDataInCytoscape = function(container, elements, layout="breadthfirst") {
		console.log(elements);
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
	    			selector: 'node[type="noMicroService"]',
	    			style: {
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
	    				'content': 'data(value)',
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
	    				'content': 'data(value)',
	    				'curve-style': 'bezier',
	    				'width': 1,
	    				'line-color': 'red',
	                    'target-arrow-shape': 'triangle',
	                    'target-arrow-color': 'red',
	                    'color': 'red',
						'font-size' : 10,
						'color':"black"
	    			}
	    		}
	    	],
	    	elements: elements
	    });
		return cy;
	};
	var _init = function(){
		
		$("#submit").click(function() {
			var ids = {
				"ids" : $("#testCaseList").val()
			};
			$.ajax({
				type : "POST",
				// 请求的媒体类型
				contentType : "application/json",
				dataType : "json",
				url : "/testcase/microservice/query",
				data : JSON.stringify(ids),
				success : function(result) {
					console.log(result);
					if (result.result == "success") {
//						showMicroServiceInCytoscape(result.value.value, $("#all"), null);
//						utils.test();
						_showDataInCytoscape($("#all"), result.value.value, "breadthfirst")
						var title = "";
						for(var i = 0; i < result.testCases.length; i++) {
							title += result.testCases[i].testCaseName;
							if(i != result.testCases.length - 1) {
								title += ", ";
							}
						}
						$("#testCaseTitle").text(title)
					}
				}
			});
		});
	};
	return {
		init : function(){
			_init();
		}
	}
});