define(['jquery', 'bootstrap', 'bootstrap-multiselect', 'jqplot', 'utils', 'cytoscape']
	, function ($, bootstrap, bootstrap_multiselect, jqplot, utils, cytoscape) {
	var cy = null;
	var cyEntry = null;
	var queryEntry = function(testCaseIds) {
		console.log("queryEntry")
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/testcase/microservice/query/entry",
			data : JSON.stringify(testCaseIds),
			success : function(result) {
				console.log(result);
				if (result.result == "success") {
					cyEntry = utils.showDataInCytoscape($("#entry"), result.value.value, "dagre");
				}
			}
		});
	}
	var queryTestCase = function(testCaseIds) {
		$.ajax({
			type : "POST",
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
			console.log(ids);
			queryTestCase(ids);
			console.log(ids);
			queryEntry(ids);
		});
		
		$("#showImg").click(function() {
			if(cy != null) {
				$('#png-eg').attr('src', cy.png({
					bg: "#ffffff",
					full : true
				}));
				$('#png-eg').css("background-color", "#ffffff");
			}
			if(cyEntry != null) {
				$('#entry-png-eg').attr('src', cyEntry.png({
					bg: "#ffffff",
					full : true
				}));
				$('#entry-png-eg').css("background-color", "#ffffff");
			}
		})

	};
	return {
		init : function(){
			_init();
		}
	}
});