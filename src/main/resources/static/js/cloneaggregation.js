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
var showAggregationResultOfJava = function() {
	$("#aggregation_java").html("");
	$.ajax({
		type: "get",
		url: "/cloneaggregation/show/java?threshold=10&percentage=0.8",
		timeout: 0,
		success: function(result) {
			console.log("success");
			var html = "<table class='table table-bordered'>";
			html += "<tr><th>目录1</th><th>目录1克隆占比</th><th>目录2</th><th>目录2克隆占比</th><th>总克隆占比</th><th>包克隆CoChange占比</th><th>克隆文件对数</th></tr>";
			var tr = function(index, layer, duplicated) {
				var prefix = "";
				for(var i = 0; i < layer; i++) {
					prefix += "|---";
				}
				switch (index) {
					case 0:
						var cloneNodesCount1 = duplicated.packagePairRelationData.cloneNodesCount1;
						var cloneNodesCount2 = duplicated.packagePairRelationData.cloneNodesCount2;
						var allNodesCount1 = duplicated.packagePairRelationData.allNodesCount1;
						var allNodesCount2 = duplicated.packagePairRelationData.allNodesCount2;
						var cloneNodesCoChangeTimes = duplicated.packagePairRelationData.cloneNodesCoChangeTimes;
						var allNodesCoChangeTimes = duplicated.packagePairRelationData.allNodesCoChangeTimes;
						var clonePairs = duplicated.packagePairRelationData.clonePairs;
						html += "<tr>";
						html += layer == 0 ? "<th>" : "<td>";
						html += prefix + duplicated.package1.directoryPath;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						var path1CloneRate = cloneNodesCount1 + "/" + allNodesCount1 + "=" + ((cloneNodesCount1 + 0.0) / allNodesCount1).toFixed(2);
						html += path1CloneRate;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += prefix + duplicated.package2.directoryPath;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						var path2CloneRate = cloneNodesCount2 + "/" + allNodesCount2 + "=" + ((cloneNodesCount2 + 0.0) / allNodesCount2).toFixed(2);
						html += path2CloneRate;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						var cloneRate = "(" + cloneNodesCount1 + "+" + cloneNodesCount2 + ")/(" + allNodesCount1 + "+" + allNodesCount2 + ")=" + ((cloneNodesCount1 + cloneNodesCount2 + 0.0) / (allNodesCount1 + allNodesCount2)).toFixed(2);
						html += cloneRate;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th id='cochangeRateId'>" : "<td id='cochangeRateId'>";
						var cochangeRate = "";
						if(allNodesCoChangeTimes < 3){
							cochangeRate = cloneNodesCoChangeTimes + "/" + allNodesCoChangeTimes + "=0.00";
						}
						else {
							cochangeRate = cloneNodesCoChangeTimes  + "/" + allNodesCoChangeTimes  + "=" + ((cloneNodesCoChangeTimes  + 0.0) / allNodesCoChangeTimes).toFixed(2);
						}
						html += cochangeRate;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						if(clonePairs > 0) {
							html += "<a target='_blank' class='package' href='/cloneaggregation/details" +
								"?id1=" + duplicated.package1.id +
								"&id2=" + duplicated.package2.id +
								"&path1=" + duplicated.package1.directoryPath +
								"&path2=" + duplicated.package2.directoryPath +
								"&cloneNodes1=" + cloneNodesCount1 +
								"&allNodes1=" + allNodesCount1 +
								"&cloneNodes2=" + cloneNodesCount2 +
								"&allNodes2=" + allNodesCount2 +
								"&cloneCochangeTimes=" + cloneNodesCoChangeTimes +
								"&allCochangeTimes=" + allNodesCoChangeTimes +
								"&clonePairs=" + clonePairs +
								"'>" + clonePairs + "</a>";
						}
						else {
							html += clonePairs;
						}
						html += layer == 0 ? "</th>" : "</td>";
						html += "</tr>";
						break;
					case -1:
						html += "<tr style='color: #A9A9A9'>";
						html += "<td>";
						html += prefix + duplicated.directoryPath;
						html += "</td>";
						html += "<td>";
						html += "0/" + duplicated.allNodes + "=0.00";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "</tr>";
						break;
					case 1:
						html += "<tr style='color: #A9A9A9'>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += prefix + duplicated.directoryPath;
						html += "</td>";
						html += "<td>";
						html += "0/" + duplicated.allNodes + "=0.00";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "</tr>";
						break;
				}
				if(index == 0) {
					for(var key1 = 0; key1 < duplicated.childrenHotspotPackagePairs.length; key1 ++) {
						tr(0, layer + 1, duplicated.childrenHotspotPackagePairs[key1]);
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
			$("#aggregation_java").html(html);
		},
		error: function () {
			alert("502!");
		}
	})
}

var showAggregationResultOfCpp = function() {
	$("#aggregation_cpp").html("");
	$.ajax({
		type: "get",
		url: "/cloneaggregation/show/cpp?threshold=10&percentage=0.8",
		timeout: 10000,
		success: function(result) {
			console.log("success");
			var html = "<table class='table table-bordered'>";
			html += "<tr><th>目录1</th><th>目录1克隆占比</th><th>目录2</th><th>目录2克隆占比</th><th>总克隆占比</th><th>包克隆CoChange占比</th><th>克隆文件对数</th></tr>";
			var tr = function(index, layer, duplicated) {
				var prefix = "";
				for(var i = 0; i < layer; i++) {
					prefix += "|---";
				}
				switch (index) {
					case 0:
						var cloneNodesCount1 = duplicated.packagePairRelationData.cloneNodesCount1;
						var cloneNodesCount2 = duplicated.packagePairRelationData.cloneNodesCount2;
						var allNodesCount1 = duplicated.packagePairRelationData.allNodesCount1;
						var allNodesCount2 = duplicated.packagePairRelationData.allNodesCount2;
						var cloneNodesCoChangeTimes = duplicated.packagePairRelationData.cloneNodesCoChangeTimes;
						var allNodesCoChangeTimes = duplicated.packagePairRelationData.allNodesCoChangeTimes;
						var clonePairs = duplicated.packagePairRelationData.clonePairs;
						html += "<tr>";
						html += layer == 0 ? "<th>" : "<td>";
						html += prefix + duplicated.package1.directoryPath;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						var path1CloneRate = cloneNodesCount1 + "/" + allNodesCount1 + "=" + ((cloneNodesCount1 + 0.0) / allNodesCount1).toFixed(2);
						html += path1CloneRate;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						html += prefix + duplicated.package2.directoryPath;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						var path2CloneRate = cloneNodesCount2 + "/" + allNodesCount2 + "=" + ((cloneNodesCount2 + 0.0) / allNodesCount2).toFixed(2);
						html += path2CloneRate;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						var cloneRate = "(" + cloneNodesCount1 + "+" + cloneNodesCount2 + ")/(" + allNodesCount1 + "+" + allNodesCount2 + ")=" + ((cloneNodesCount1 + cloneNodesCount2 + 0.0) / (allNodesCount1 + allNodesCount2)).toFixed(2);
						html += cloneRate;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th id='cochangeRateId'>" : "<td id='cochangeRateId'>";
						var cochangeRate = "";
						if(allNodesCoChangeTimes < 3){
							cochangeRate = cloneNodesCoChangeTimes + "/" + allNodesCoChangeTimes + "=0.00";
						}
						else {
							cochangeRate = cloneNodesCoChangeTimes  + "/" + allNodesCoChangeTimes  + "=" + ((cloneNodesCoChangeTimes  + 0.0) / allNodesCoChangeTimes).toFixed(2);
						}
						html += cochangeRate;
						html += layer == 0 ? "</th>" : "</td>";
						html += layer == 0 ? "<th>" : "<td>";
						if(clonePairs > 0) {
							html += "<a target='_blank' class='package' href='/cloneaggregation/details" +
								"?id1=" + duplicated.package1.id +
								"&id2=" + duplicated.package2.id +
								"&path1=" + duplicated.package1.directoryPath +
								"&path2=" + duplicated.package2.directoryPath +
								"&cloneNodes1=" + cloneNodesCount1 +
								"&allNodes1=" + allNodesCount1 +
								"&cloneNodes2=" + cloneNodesCount2 +
								"&allNodes2=" + allNodesCount2 +
								"&cloneCochangeTimes=" + cloneNodesCoChangeTimes +
								"&allCochangeTimes=" + allNodesCoChangeTimes +
								"&clonePairs=" + clonePairs +
								"'>" + clonePairs + "</a>";
						}
						else {
							html += clonePairs;
						}
						html += layer == 0 ? "</th>" : "</td>";
						html += "</tr>";
						break;
					case -1:
						html += "<tr style='color: #A9A9A9'>";
						html += "<td>";
						html += prefix + duplicated.directoryPath;
						html += "</td>";
						html += "<td>";
						html += "0/" + duplicated.allNodes + "=0.00";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "</tr>";
						break;
					case 1:
						html += "<tr style='color: #A9A9A9'>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += prefix + duplicated.directoryPath;
						html += "</td>";
						html += "<td>";
						html += "0/" + duplicated.allNodes + "=0.00";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "</tr>";
						break;
				}
				if(index == 0) {
					for(var key1 = 0; key1 < duplicated.childrenHotspotPackagePairs.length; key1 ++) {
						tr(0, layer + 1, duplicated.childrenHotspotPackagePairs[key1]);
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
			$("#aggregation_cpp").html(html);
		},
		error: function () {
			alert("502!");
		}
	})
}