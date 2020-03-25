var init = function(){
	$("#submit").click(function() {
		var ids = {
			"ids" : $("#testCaseList").val()
		};
		/*var data = [
			['Heavy Industry', 12], ['Retail', 9], ['Light Industry', 14],
			['Out of home', 16], ['Commuting', 7], ['Orientation', 9]
			];
		
		$.jqplot('chart', [data], {
			seriesDefaults: {
				renderer: $.jqplot.PieRenderer,
				rendererOptions: {
					showDataLabels: true
				}
			},
			legend: {
				show: true,
				location: "e"
			}
		});*/
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
					showMicroServiceInCytoscape(result.value.value, $("#all"), null);
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
}
