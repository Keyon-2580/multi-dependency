var clone = function(cytoscapeutil) {
	var showGraph = function(data) {
		var myChart = echarts.init(document.getElementById('package_graph'));
		option = {
				legend: {
					data: data.legend
				},
				series: [{
					type: 'graph',
					layout: 'force',
					animation: false,
					label: {
						position: 'right',
						formatter: '{b}'
					},
					draggable: true,
					data: data.nodes,
					categories: data.categories,
					force: {
						edgeLength: 5,
						repulsion: 20,
						gravity: 0.2
					},
					focusNodeAdjacency: true,
					edges: data.links,
					edgeSymbol: ['circle', 'arrow']
				}]
		};
		
		myChart.setOption(option);
	}
	var drawPic = function(canvas, width, height, result){
		console.log(result);
		var data = result.data;
		console.log(data);
		console.log(canvas);
		var packageIndex = result.packageIndex;
		var cxt = canvas.getContext("2d");
		var per = 10;
		cxt.save();
		cxt.fillStyle="#ffffff";
		cxt.fillRect(0, 0, width, height);
		cxt.restore();
		cxt.strokeStyle = "#000000";
		cxt.lineWidth = 0.1;
		/*for(var i = 0; i < data.length; i++){
			cxt.moveTo(i * per, 0);
			cxt.lineTo(i * per, height);
			cxt.moveTo(0, i * per);
			cxt.lineTo(width, i * per);
		}*/
		cxt.save();
		cxt.strokeStyle = "yellow";
		cxt.lineWidth = 10;
		for(var i = 0; i < packageIndex.length; i++) {
			cxt.moveTo(packageIndex[i] * per ,0);
			cxt.lineTo(packageIndex[i] * per ,height);
			cxt.moveTo(0, packageIndex[i] * per);
			cxt.lineTo(width, packageIndex[i] * per);
		}
		cxt.restore();
		cxt.save();
		for(var i = 0; i < data.length; i++){
			for(var j = 0; j < data[i].length; j++) {
				if("" != data[i][j].type) {
					if(false == data[i][j].differentPackage) {
						cxt.fillStyle="red";
						cxt.fillRect(i * per, j * per, per, per);
					} else {
						cxt.fillStyle="green";
						cxt.fillRect(i * per, j * per, per, per);
					}
				}
			}
		}
		cxt.restore();
		cxt.stroke();
		return cxt;
	};
	var drawPackagePic = function(canvas, width, height, result){
		console.log(result);
		var data = result.matrix;
		console.log(data);
		console.log(canvas);
		var cxt = canvas.getContext("2d");
		var per = 10;
		cxt.save();
		cxt.fillStyle="#ffffff";
		cxt.fillRect(0, 0, width, height);
		cxt.restore();
		cxt.strokeStyle = "#000000";
		cxt.lineWidth = 0.1;
		for(var i = 0; i < data.length; i++){
			cxt.moveTo(i * per, 0);
			cxt.lineTo(i * per, height);
			cxt.moveTo(0, i * per);
			cxt.lineTo(width, i * per);
		}
		cxt.save();
		for(var i = 0; i < data.length; i++){
			for(var j = 0; j < data[i].length; j++) {
				console.log(data[i][j]);
				if(data[i][j].integerValue != 0) {
					if(i == j) {
						cxt.fillStyle="green";
					} else {
						cxt.fillStyle="red";
					}
					cxt.fillRect(i * per, j * per, per, per);
				}
			}
		}
		cxt.restore();
		cxt.stroke();
		return cxt;
	};
	var showTreeMapGraph = function(divId, data) {
		function colorMappingChange(value) {
	        var levelOption = getLevelOption(value);
	        chart.setOption({
	            series: [{
	                levels: levelOption
	            }]
	        });
	    }
	    var formatUtil = echarts.format;
	    function getLevelOption() {
	        return [
	            {
	                itemStyle: {
	                    borderWidth: 0,
	                    gapWidth: 5
	                }
	            },
	            {
	                itemStyle: {
	                    gapWidth: 1
	                }
	            },
	            {
	                colorSaturation: [0.35, 0.5],
	                itemStyle: {
	                    gapWidth: 1,
	                    borderColorSaturation: 0.6
	                }
	            }
	        ];
	    }
		var myChart = echarts.init(document.getElementById(divId));
	    myChart.setOption(option = {
	        title: {
	            text: 'Disk Usage',
	            left: 'center'
	        },
	        tooltip: {
	            formatter: function (info) {
	                var value = info.value;
	                var treePathInfo = info.treePathInfo;
	                var treePath = [];
	                for (var i = 1; i < treePathInfo.length; i++) {
	                    treePath.push(treePathInfo[i].name);
	                }
	                return [
	                    '<div class="tooltip-title">' + formatUtil.encodeHTML(treePath.join('/')) + '</div>',
	                    '文件数: ' + formatUtil.addCommas(value),
	                ].join('');
	            }
	        },
	        series: [
	            {
	                name: 'Disk Usage',
	                type: 'treemap',
	                visibleMin: 300,
	                label: {
	                    show: true,
	                    formatter: '{b}'
	                },
	                itemStyle: {
	                    borderColor: '#fff'
	                },
	                levels: getLevelOption(),
	                data: data
	            }
	        ]
	    });
	};
	var showPackageCytoscapeGraph = function(data) {
		var cy = cytoscapeutil.showDataInCytoscape($("#cloneGroupsDiv"), data, "random");
	};
	var doublePackagesClone = function(pck1Id, pck2Id, index) {
		$("#package_files_clone").html("");
		$.ajax({
			type: "get",
			url: "/clone/package/double?package1=" + pck1Id + "&package2=" + pck2Id,
			success: function(result) {
				console.log(result);
				var html = index + "<h4>" + result.node1.directoryPath 
					+ "</h4>&<h4>" + result.node2.directoryPath 
					+ "</h4>" + result.children.length + "<table class='table table-bordered'>"
					+ "<tr><th>file1</th><th>file2</th><th>type</th><th>value</th></tr>";
				var children = result.children;
				for(var i = 0; i < children.length; i++) {
					html += "<tr>";
					html += "<td>";
					html += "<span>" + children[i].startNode.path + "</span><span> (" + children[i].startNode.lines + ") </span>";
					html += "</td>";
					html += "<td>";
					html += "<span>" + children[i].endNode.path + "</span><span> (" + children[i].endNode.lines + ") </span>";
					html += "</td>";
					html += "<td>";
					html += children[i].cloneType;
					html += "</td>";
					html += "<td>";
					html += children[i].value;
					html += "</td>";
					html += "</tr>";
				}
				html += "</table>"
				$("#package_files_clone").html(html);
			}
		})
	}
	var doublePackagesCloneWithCoChange = function(pck1Id, pck2Id, index) {
		$("#package_files_clone").html("");
		$.ajax({
			type: "get",
			url: "/clone/package/double/cochange?package1=" + pck1Id + "&package2=" + pck2Id,
			success: function(result) {
				console.log(result);
				var html = index + "&nbsp;&nbsp;" + result.children.length;
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>包路径";
				html += "</th>";
				html += "<th>包内文件数";
				html += "</th>";
				html += "<th>克隆文件数";
				html += "</th>";
				html += "</tr>";
				html += "<tr>";
				html += "<td>" + result.pck1.directoryPath;
				html += "</td>";
				html += "<td>" + result.allFiles1.length;
				html += "</td>";
				html += "<td>" + result.cloneFiles1.length;
				html += "</td>";
				html += "</tr>";
				html += "<tr>";
				html += "<td>" + result.pck2.directoryPath;
				html += "</td>";
				html += "<td>" + result.allFiles2.length;
				html += "</td>";
				html += "<td>" + result.cloneFiles2.length;
				html += "</td>";
				html += "</tr>";
				html += "</table>";
				html += "<table class='table table-bordered'>"
				+ "<tr><th>file1</th><th>file2</th><th>type</th><th>value</th><th>cochange</th></tr>";
				var children = result.children;
				for(var i = 0; i < children.length; i++) {
					var cochangeId = children[i].cochange == null ? -1 : children[i].cochange.id;
					html += "<tr>";
					html += "<td>";
					html += "<span>" + children[i].file1.path + "</span><span> (" + children[i].file1.lines + ") </span>";
					html += "</td>";
					html += "<td>";
					html += "<span>" + children[i].file2.path + "</span><span> (" + children[i].file2.lines + ") </span>";
					html += "</td>";
					html += "<td>";
					html += children[i].fileClone.cloneType;
					html += "</td>";
					html += "<td>";
					html += children[i].fileClone.value;
					html += "</td>";
					html += "<td>";
					html += "<a class='cochangeTimes' target='_blank' href='/git/cochange/commits?cochangeId=" + cochangeId
						+ "' index='" + i + "'>" + children[i].cochangeTimes + "</a>";
					html += "</td>";
					html += "</tr>";
				}
				html += "</table>"
				$("#package_files_clone").html(html);
				$(".cochangeTimes").click(function() {
					console.log(children[$(this).attr("index")].cochange);
				});
			}
		})
	}
	var packagesClone = function() {
		$.ajax({
			type: "get",
			url: "/clone/package",
			success:function(result) {
				console.log(result);
				var html = "<div><span>" + result.length + "</span></div><table class='table table-bordered'>";;
				html += "<tr><th>index</th><th>目录</th><th>目录</th><th>文件克隆对数</th></tr>";
				for(var i = 0; i < result.length; i++) {
					html += "<tr>";
					html += "<td>";
					html += i + 1;
					html += "</td>";
					html += "<td>";
					html += result[i].node1.directoryPath;
					html += "</td>";
					html += "<td>";
					html += result[i].node2.directoryPath;
					html += "</td>";
					html += "<td>";
					html += "<a class='package' index='" + (i + 1) + "' href='#package_files_clone' id2='" + result[i].node2.id + "' id1='" + result[i].node1.id + "'>" + result[i].children.length + "</a>";
					html += "</td>";
					html += "</tr>";
				}
				html += "</table>";
				$("#packages_clone").html(html);
				$(".package").click(function() {
					console.log($(this).attr("id1"));
					console.log($(this).attr("id2"));
//					doublePackagesClone($(this).attr("id1"), $(this).attr("id2"), $(this).attr("index"));
					doublePackagesCloneWithCoChange($(this).attr("id1"), $(this).attr("id2"), $(this).attr("index"));
				});
			}
		});
	}
	var _graph = function() {
		packagesClone();
		/*showTreeMapGraph('project_path', [
			{
				value: 1,
				name: "ts-travel-service",
				path1: "Accessibility",
				children: [
					{
						value: 30,
						name: "/ts-travel-service/src/package1",
						path1: "Accounts/Access",
						children: [
							{
								value: 16,
								name: ""
							},
							{
								value: 14,
								name: ""
							}
						]
					}
				]
			},
			{
				value: 2,
				name: "ts-travel2-service",
				path1: "Accounts",
				children: [
					{
						value: 15,
						name: "/ts-travel2-service/src/package1",
						path1: "Accounts/Access",
						children: [
							{
								value: 4,
								name: ""
							},
							{
								value: 7,
								name: ""
							},
							{
								value: 4,
								name: ""
							}
						]
					},
					{
						value: 20,
						name: "/ts-travel2-service/src/package2",
						path1: "Accounts/Authentication",
						children: [
							{
								value: 9,
								name: ""
							},
							{
								value: 11,
								name: ""
							}
						]
					}
				]
			}
			]);*/
		/*$.ajax({
			type : "get",
			url : "/graph/file",
			success : function(result) {
				var width = result.data.length * 10;
				$("#file_graph").attr("width", width);
				$("#file_graph").attr("height", width);
				var canvas = document.getElementById("file_graph");
				drawPic(canvas, width, width, result);
			}
		});*/
		/*$.ajax({
			type:"get",
			url: "/graph/package/clone/90559",
			success:function(result) {
				var width = result.matrix.length * 10;
				$("#package_clone_matrix").attr("width", width);
				$("#package_clone_matrix").attr("height", width);
				var canvas = document.getElementById("package_clone_matrix");
				drawPackagePic(canvas, width, width, result);
			}
		});*/
		/*$.ajax({
			type : "get",
			url : "/graph/package/cytoscape/90559",
			success : function(result) {
				console.log(result);
				showPackageCytoscapeGraph(result.data);
			}
		});*/
		/*$.ajax({
			type : "get",
			url : "/graph/package/structure/90559",
			success : function(result) {
				console.log(result);
				showGraph(result.data);
			}
		});*/
	}
	
	return {
		init : function() {
			_graph();
		}
	}
}
