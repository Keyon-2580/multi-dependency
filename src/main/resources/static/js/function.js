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
var itemOnclick = function (target){
	if(null != target.innerText && "" != target.innerText){
		var nodeId = $(target).attr("data-nodeid"); 
		var node = $("#tree").treeview('getNode', $(target).attr("data-nodeid"));
		console.log(node);
		var nodeType = node.tags[0];
		var tagId = node.href;
		console.log(node);
		if(nodeType == "feature") {
			var featureIds = new Array();
			featureIds.push(tagId);
			console.log(featureIds);
			featureToHTML(featureIds, $("#graph"));
		}
		if(nodeType == "span") {
			console.log(tagId);
			$.ajax({
		    	type: 'GET',
		    	url: "/function/treeview/span?spanGraphId=" + tagId,
		    	success: function(result) {
		    		console.log(result);
			    	if(result.result == "success") {
			    		showTreeView(result.value, $("#graph"));
			    	}
		    	}
		    });
		}
	}
}