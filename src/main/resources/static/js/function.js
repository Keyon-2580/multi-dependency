//<![CDATA[
var tree = $("#tree");
var showTreeView = function(data, treeDiv) {
	treeDiv.treeview({
		data : data,
		showTags : true,
		levels: 1
	});
};
var functionIndex = function(features, graphDiv) {
	$.ajax({
    	type: 'GET',
    	url: "/function/treeview",
    	success: function(result) {
    		console.log(result);
	    	if(result.result == "success") {
	    		showTreeView(result.value, $("#tree"));
	    	}
    	}
    });
	featureIndex(features, graphDiv);
};
var showDataInCatoscape = function(elements, container){
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