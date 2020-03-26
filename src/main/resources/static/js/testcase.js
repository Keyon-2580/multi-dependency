define(['jquery', 'bootstrap', 'bootstrap-multiselect', 'jqplot', 'utils', 'cytoscape']
	, function ($, bootstrap, bootstrap_multiselect, jqplot, utils, cytoscape) {
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
						var cy = utils.showDataInCytoscape($("#all"), result.value.value, "dagre");
						var title = "";
						$("#showImg").click(function() {
							$('#png-eg').attr('src', cy.png());
						})
						for(var i = 0; i < result.testCases.length; i++) {
							title += result.testCases[i].testCaseName;
							if(i != result.testCases.length - 1) {
								title += ", ";
							}
						}
						console.log(title)
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