var showTreeView = function(containerDivId, data) {
	containerDivId.treeview({
		data : data,
		showTags : true,
		levels: 1
	});
};

var showDataInCatoscape = function(container, elements, layout="breadthfirst"){
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
    				'height': 30,
    				'width': 30,
    				'background-color': 'red',
					'content': 'data(value)'
    			}
    		},
    		{
    			selector: 'node[type="testcase"]',
    			style: {
    				'height': 30,
    				'width': 30,
    				'background-color': 'green',
					'content': 'data(value)'
    			}
    		},
			{
    			selector: 'edge',
    			style: {
//    				'content': 'data(value)',
    				'curve-style': 'bezier',
    				'width': 2,
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

var featureInit = function() {
	showFeatureToTestCasesTreeView($("#tree"));
	showFeatureToTestCasesCytoscape($("#graph"));
}

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
	    		showDataInCatoscape(containerDivId, result.value, "grid")
	    	}
    	}
    });
}

var itemOnclick = function (target){
	if(null != target.innerText && "" != target.innerText){
		console.log($(target));
		console.log($($(target).parent().parent()[0]).attr("id"));
		var targetDivId = $($(target).parent().parent()[0]).attr("id");
		console.log(targetDivId);
		var nodeId = $(target).attr("data-nodeid");
		var node = $("#" + targetDivId).treeview('getNode', $(target).attr("data-nodeid"));
		console.log(node);
		var nodeType = node.tags[0];
		var tagId = node.href;
		console.log(node);
		console.log(nodeType)
		if(nodeType == "feature") {
			var featureIds = new Array();
			featureIds.push(tagId);
			console.log(featureIds);
			featureToHTML(featureIds, $("#graph"));
		}
		if(nodeType == "microservice") {
			console.log(tagId);
			$.ajax({
		    	type: 'GET',
		    	url: "/function/cytoscape/file?microserviceGraphId=" + tagId + "&callType=file",
		    	success: function(result) {
		    		console.log(result);
			    	if(result.result == "success") {
			    		$("#graph").html("");
			    		$("#graph").attr("class", "div_cytoscape_content");
			    		showDataInCatoscape(result.value, $("#graph"));
			    	}
		    	}
		    });
		}
		if(nodeType == "span") {
			console.log(tagId);
			$.ajax({
		    	type: 'GET',
		    	url: "/function/treeview/span?spanGraphId=" + tagId,
		    	success: function(result) {
		    		console.log(result);
			    	if(result.result == "success") {
			    		$("#graph").html("");
			    		$("#graph").attr("class", "");
			    		showTreeView(result.value, $("#graph"));
			    	}
		    	}
		    });
		}
	}
}