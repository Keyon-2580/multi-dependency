define(['jquery', 'bootstrap', 'bootstrap-multiselect', 'jqplot', 'utils', 'cytoscape']
	, function ($, bootstrap, bootstrap_multiselect, jqplot, utils, cytoscape) {
	var cy = null;
	var queryEntry = function(testCaseId) {
		if(cy == null) {
			return;
		}
		console.log("queryEntry")
		$.ajax({
			type : "POST",
			// 请求的媒体类型
			contentType : "application/json",
			dataType : "json",
			url : "/testcase/microservice/entry",
			data : JSON.stringify(testCaseId),
			success : function(result) {
				console.log(result);
				utils.test();
				if (result.result == "success") {
					utils.addNodes(cy, result.value.value.nodes);
					utils.addEdges(cy, result.value.value.edges);
				}
			}
		});
	}
	var queryTestCase = function(testCaseIds) {
		$.ajax({
			type : "POST",
			// 请求的媒体类型
			contentType : "application/json",
			dataType : "json",
			url : "/testcase/microservice/query/union",
			data : JSON.stringify(testCaseIds),
			success : function(result) {
				console.log(result);
				if (result.result == "success") {
					cy = utils.showDataInCytoscape($("#all"), result.coverageValue.value, "dagre");
					var html = "";
					for(var i = 0; i < result.testCases.length; i++) {
						html += "<a href='#' value='" + i + "' class='query_entry' name='" + result.testCases[i].testCaseId + "'>" + result.testCases[i].testCaseName + "</a>"
						if(i != result.testCases.length - 1) {
							html += "<span>、</span>";
						}
					}
					$("#testCaseTitle").html(html);
					/*$(".query_entry").click(function(){
						var id = {
							"testCaseId" : $(this).attr("name"),
							"type" : $(this).attr("value")
						};
						queryEntry(id);
					});*/
					
				}
			}
		});
	};
	
	var _init = function(){
		$("#testCaseList").multiselect({
			enableClickableOptGroups: true,
			enableCollapsibleOptGroups: true,
	       	enableFiltering: true,
			collapseOptGroupsByDefault: true,
			enableCollapsibleOptGroups: true
		});

		$("#submit").click(function() {
			var ids = {
				"ids" : $("#testCaseList").val()
			};
			queryTestCase(ids);
		});
		
		$("#showImg").click(function() {
			if(cy != null) {
				$('#png-eg').attr('src', cy.png());
			}
		})

	};
	return {
		init : function(){
			_init();
		}
	}
});