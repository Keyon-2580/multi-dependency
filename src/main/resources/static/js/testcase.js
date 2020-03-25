define(['jquery', 'bootstrap', 'bootstrap-multiselect', 'jqplot', 'utils']
	, function ($, bootstrap, bootstrap_multiselect, jqplot, utils) {
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
//						utils.showMicroServiceInCytoscape(result.value.value, $("#all"), null);
//						utils.test();
						utils.showDataInCytoscape($("#all"), result.value.value, "breadthfirst");
//						_showDataInCytoscape($("#all"), result.value.value, "breadthfirst")
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