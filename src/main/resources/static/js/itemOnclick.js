var itemOnclick = function (target){
		if(null != target.innerText && "" != target.innerText){
//			console.log($(target));
//			console.log($($(target).parent().parent()[0]).attr("id"));
			var targetDivId = $($(target).parent().parent()[0]).attr("id");
//			console.log(targetDivId);
			var nodeId = $(target).attr("data-nodeid");
			var node = $("#" + targetDivId).treeview('getNode', $(target).attr("data-nodeid"));
			var nodeType = node.tags[0];
			var tagId = node.href;
			if(nodeType == "trace") {
	    		$("#graph").html("");
	    		$("#graph").attr("class", "");
	    		$("#table").hide();
	    		$("#title").text("微服务调用")
				toHTML("trace", tagId, $("#graph"));
			}
			if(nodeType == "testcase") {
	    		$("#graph").html("");
	    		$("#graph").attr("class", "");
	    		$("#table").hide();
	    		$("#title").text("微服务调用")
				toHTML("testcase", tagId, $("#graph"));
			}
			/*if(nodeType == "feature") {
	    		$("#graph").html("");
	    		$("#graph").attr("class", "");
	    		$("#table").hide();
	    		$("#title").text("微服务调用")
				toHTML("feature", tagId, $("#graph"));
			}*/
			if(nodeType == "span") {
				var spanId = tagId;
				$.ajax({
			    	type: 'GET',
			    	url: "/function/treeview/span?spanGraphId=" + tagId,
			    	success: function(result) {
			    		console.log(result);
				    	if(result.result == "success") {
				    		$("#graph").html("");
				    		$("#graph").attr("class", "");
				    		$("#table").hide();
				    		$("#title").text("内部函数调用")
				    		$("#graph").treeview({
				    			data : result.value,
				    			showTags : true,
				    			levels: 1
				    		});
				    	}
			    	}
			    });
			}
		}
	};