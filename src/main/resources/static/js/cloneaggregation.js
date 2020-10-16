// var analysisAggregationPackage = function() {
// 	$.ajax({
// 		type: "get",
// 		url: "/cloneaggregation/analysis?threshold=10&percentage=0.8",
// 		success: function(result) {
// 			console.log("克隆聚合结果数量：" + result);
// 			alert("分析完毕！")
// 		}
// 	})
// }
var showAggregationResult = function() {
	$("#packages_aggregation").html("");
	$.ajax({
		type: "get",
		url: "/cloneaggregation/show?threshold=10&percentage=0.8",
		success: function(result) {
			console.log("success");
			var html = "<table class='table table-bordered'>";
			html += "<tr><th>目录1</th><th>目录1克隆占比</th><th>目录2</th><th>目录2克隆占比</th><th>克隆文件对数</th><th>总克隆占比</th><th>总CoChange占比</th></tr>";
			var tr = function(index, layer, duplicated) {
				var prefix = "";
				for(var i = 0; i < layer; i++) {
					prefix += "|---";
				}
				switch (index) {
					case 0:
						html += "<tr>";
						html += layer == 0 ? "<th>" : "<td>";
						html += prefix + duplicated.package1.directoryPath;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += duplicated.relationNodes1 + "/" + duplicated.allNodes1 + "=" + ((duplicated.relationNodes1 + 0.0) / duplicated.allNodes1).toFixed(2);
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += prefix + duplicated.package2.directoryPath;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += duplicated.relationNodes2 + "/" + duplicated.allNodes2 + "=" + ((duplicated.relationNodes2 + 0.0) / duplicated.allNodes2).toFixed(2);
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						var len = duplicated.clonePairs;
						if(len > 0) {
							html += "<a class='package' href='#package_files_clone' id2='" + duplicated.package2.id + "' id1='" + duplicated.package1.id + "'>" + len + "</a>";
						}
						else {
							html += len;
						}
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += "(" + duplicated.relationNodes1 + "+" + duplicated.relationNodes2 + ")/(" + duplicated.allNodes1 + "+" + duplicated.allNodes2 + ")=" + ((duplicated.relationNodes1 + duplicated.relationNodes2 + 0.0) / (duplicated.allNodes1 + duplicated.allNodes2)).toFixed(2);
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						if(duplicated.packageCochangeTimes < 3){
							html += duplicated.packageCloneCochangeTimes + "/" + duplicated.packageCochangeTimes + "=0.00";
						}else {
							html += duplicated.packageCloneCochangeTimes  + "/" + duplicated.packageCochangeTimes  + "=" + ((duplicated.packageCloneCochangeTimes  + 0.0) / duplicated.packageCochangeTimes ).toFixed(2);
						}
						html += layer == 0 ? "</th>" : "</td>";
						html += "</tr>";
						break;
					case -1:
						html += "<tr style='color: #A9A9A9'>";
						html += layer == 0 ? "<th>" : "<td>";
						html += prefix + duplicated.directoryPath;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += "0/" + duplicated.allNodes + "=0.00";
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += layer == 0 ? "</th>" : "</td>";
						html += "</tr>";
						break;
					case 1:
						html += "<tr style='color: #A9A9A9'>";
						html += layer == 0 ? "<th>" : "<td>";
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += prefix + duplicated.directoryPath;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += "0/" + duplicated.allNodes + "=0.00";
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += layer == 0 ? "</th>" : "</td>";
						html += "</tr>";
						break;
				}
				if(index == 0) {
					for(var key1 = 0; key1 < duplicated.childrenHotspotPackages.length; key1 ++) {
						tr(0, layer + 1, duplicated.childrenHotspotPackages[key1]);
					}

					for(var key2 = 0; key2 < duplicated.childrenOtherPackages1.length; key2 ++) {
						tr(-1, layer + 1, duplicated.childrenOtherPackages1[key2]);
					}

					for(var key3 = 0; key3 < duplicated.childrenOtherPackages2.length; key3 ++) {
						tr(1, layer + 1, duplicated.childrenOtherPackages2[key3]);
					}
				}
			}
			for(var i = 0; i < result.length; i ++) {
				tr(0, 0, result[i]);
			}
			html += "</table>"
			$("#packages_aggregation").html(html);
			$(".package").click(function() {
				doublePackagesCloneWithCoChange($(this).attr("id1"), $(this).attr("id2"));
			});
		}
	})
}