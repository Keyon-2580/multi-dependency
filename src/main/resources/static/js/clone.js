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
	var duplicatedPackage = function() {
		$("#packages_duplicated").html("");
		$.ajax({
			type: "get",
			url: "/clone/package/duplicated?threshold=10&percentage=0.8",
			success: function(result) {
				console.log(result);
				var html = "<table class='table table-bordered'>";
				html += "<tr><th>目录1</th><th>目录1克隆占比</th><th>目录2</th><th>目录2克隆占比</th><th>克隆文件对数</th><th>总克隆占比</th></tr>";
				var tr = function(index, layer, duplicated) {
					var prefix = "";
					for(var i = 0; i < layer; i++) {
						prefix += "|--------";
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
							html += duplicated.relationPackages.children.length;
							html += layer == 0 ? "</th>" : "</td>";
							html += layer == 0 ? "<th>" : "<td>";
							html += "(" + duplicated.relationNodes1 + "+" + duplicated.relationNodes2 + ")/(" + duplicated.allNodes1 + "+" + duplicated.allNodes2 + ")=" + ((duplicated.relationNodes1 + duplicated.relationNodes2 + 0.0) / (duplicated.allNodes1 + duplicated.allNodes2)).toFixed(2);
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
				$("#packages_duplicated").html(html);
			}
		})
	}
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
					html += "<a target='_blank' href='/clone/compare?id1=" + children[i].file1.id + "&id2=" + children[i].file2.id + "'>" + children[i].fileClone.value + "</a>";
					html += "</td>";
					html += "<td>";
					html += "<a class='cochangeTimes' target='_blank' href='/git/cochange/commits?cochangeId=" + cochangeId
						+ "' index='" + i + "'>" + children[i].cochangeTimes + "</a>";
					html += "</td>";
					html += "</tr>";
				}
				html += "</table>";
				html += "<div id='fileClonesGraph'></div>"
				$("#package_files_clone").html(html);
				$(".cochangeTimes").click(function() {
					console.log(children[$(this).attr("index")].cochange);
				});
				$.ajax({
					type: "get",
					url: "/clone/package/double/graph?package1=" + pck1Id + "&package2=" + pck2Id,
					success: function(result) {
						console.log(result);
						cloneGroupToGraph(result, "fileClonesGraph");
					}
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
				var html = "<div><span>" + result.length + "</span></div><table class='table table-bordered'>";
				html += "<tr><th>index</th><th>目录</th><th>目录</th>";
				html += "<th>目录1文件数</th><th>目录2文件数</th><th>目录1克隆文件数</th><th>目录2克隆文件数</th><th>目录1占比</th><th>目录2占比</th><th>文件克隆对数</th></tr>";
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
					html += result[i].allNodesInNode1.length;
					html += "</td>";
					html += "<td>";
					html += result[i].allNodesInNode2.length;
					html += "</td>";
					html += "<td>";
					html += result[i].nodesInNode1.length;
					html += "</td>";
					html += "<td>";
					html += result[i].nodesInNode2.length;
					html += "</td>";
					html += "<td>";
					html += (result[i].nodesInNode1.length / result[i].allNodesInNode1.length).toFixed(2);
					html += "</td>";
					html += "<td>";
					html += (result[i].nodesInNode2.length / result[i].allNodesInNode2.length).toFixed(2);
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
				duplicatedPackage();
			}
		});
	}
	/*将克隆组关系数据展现在图上
	*result:数据
	* divId:将被显示的divID
	 */
	var cloneGroupToGraph = function(result,divId) {
		//设置数组
		var clonedata = result
		
		var diameter = 1800,
		radius = diameter / 2,
		innerRadius = radius - 300;
		
		var cluster = d3.layout.cluster()
		.size([360, innerRadius])
		.sort(null)
		.value(function(d) { return d.size; });
		
		var bundle = d3.layout.bundle();
		
		var line = d3.svg.line.radial()
		.interpolate("bundle")
		.tension(.85)
		.radius(function(d) { return d.y; })
		.angle(function(d) { return d.x / 180 * Math.PI; });
		
		
		var svg = d3.select("#" + divId).append("svg")
		.attr("width", diameter)
		.attr("height", diameter)
		.attr("id", "svg1")
		.append("g")
		.attr("transform", "translate(" + radius + "," + radius + ")");
		
		
		var link = svg.append("g").selectAll(".link"),
		node = svg.append("g").selectAll(".node");
		
		//设置数组读取数据
		var nodes = cluster.nodes(packageHierarchy(clonedata)),
		links = packageImports(nodes);
		// var nodes = cluster.nodes(packageClone(classes)),
		//     links = packageCloneImports(nodes);
		
		console.log(nodes)
		
		link = link
		.data(bundle(links))
		.enter().append("path")
		.each(function(d) { d.source = d[0], d.target = d[d.length - 1]; })
		.attr("class", "link")
		.attr("d", line);
		
		node = node
		.data(nodes.filter(function(n) { return !n.children; }))
		.enter().append("text")
		// .style("fill", function (d) { if (checkChangeType(d.key, changes)== 3) { return '#b47500';}
		//                               if (checkChangeType(d.key, changes)== 4) { return '#00b40a';}})
		.attr("class", "node")
		.attr("dy", ".31em")
		.attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)"); })
		.style("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
		.text(function(d) { return d.key; })
		.on("mouseover", mouseovered)
		.on("mouseout", mouseouted)
		.call(text => text.append("title").text(function(d) { return d.key; }));
		// .call(text => text.append("title").text(d => `${node.data.name}
		// ${d.outgoing.length} outgoing
		// ${d.incoming.length} incoming`));
		
		String.prototype.replaceAt=function(index, replacement) {
			return this.substr(0, index) + replacement+ this.substr(index + replacement.length);
		}
		
		String.prototype.replaceAll = function(search, replacement) {
			var target = this;
			return target.replace(new RegExp(search, 'g'), replacement);
		};
		
		
		var width = 360;
		var height = 360;
		var radius = Math.min(width, height) / 2;
		var donutWidth = 75;
		var legendRectSize = 18;                                  // NEW
		var legendSpacing = 4;
		
		var legend = d3.select('svg')
		.append("g")
		.selectAll("g")
		// .data(color.domain())
		//.enter()
		.append('g')
		.attr('class', 'legend')
		.attr('transform', function(d, i) {
			var height = legendRectSize;
			var x = 0;
			var y = (i+1) * height;
			return 'translate(' + x + ',' + y + ')';
		});
		
		d3.select('svg')
		.select("g:nth-child(0)").append('text').text("Component Colors:");
		//.attr('transform', 'translate(0,0)');
		
		
		legend.append('rect')
		.attr('width', legendRectSize)
		.attr('height', legendRectSize)
		// .style('fill', color)
		// .style('stroke', color);
		
		legend.append('text')
		.attr('x', legendRectSize + legendSpacing)
		.attr('y', legendRectSize - legendSpacing)
		.text(function(d) { return d; });
		
		function mouseovered(d) {
			node
			.each(function(n) { n.target = n.source = false; });
			
			link
			.classed("link--target", function(l) { if (l.target === d) return l.source.source = true; })
			.classed("link--source", function(l) { if (l.source === d) return l.target.target = true; })
			.filter(function(l) { return l.target === d || l.source === d; })
			// .style("stroke", function (l) { if (checkOldLink(l, old_links)) { return '#b400ad';}})
			.style("stroke", "#e0230a")
			.each(function() { this.parentNode.appendChild(this); });
			
			node
			.classed("node--target", function(n) { return n.target; })
			.classed("node--source", function(n) { return n.source; });
			
		}
		
		function mouseouted(d) {
			link
			.classed("link--target", false)
			.classed("link--source", false)
			.style("stroke", 'DarkGray');
			
			node
			.classed("node--target", false)
			.classed("node--source", false);
			
		}
		
		d3.select(self.frameElement).style("height", diameter + "px");
		
		// Lazily construct the package hierarchy from class names.
		function packageHierarchy(classes) {
			var map = {};
			
			function find(name, data) {
				var node = map[name], i;
				if (!node) {
					node = map[name] = data || {name: name, children: []};
					console.log(node)
					if (name.length) {
						node.parent = find(name.substring(0, i = name.lastIndexOf("/")));
						node.parent.children.push(node);
						node.key = name.substring(i + 1);
					}
				}
				return node;
			}
			
			// classes.result.forEach(function(d) {
			classes.forEach(function(d) {
				console.log(d)
				find(d.name, d);
			});
			
			return map[""];
		}
		
		// Return a list of imports for the given array of nodes.
		function packageImports(nodes) {
			var map = {},
			imports = [];
			
			// Compute a map from name to node.
			nodes.forEach(function(d) {
				map[d.name] = d;
			});
			
			// For each import, construct a link from the source to target node.
			nodes.forEach(function(d) {
				if (d.imports) d.imports.forEach(function(i) {
					imports.push({source: map[d.name], target: map[i]});
				});
			});
			
			return imports;
		}
		
		//仿写packageHierarchy函数，用于处理clone关系json
		function packageClone(classes) {
			var map = {};
			
			function find(name, data) {
				var node = map[name], i;
				if (!node) {
					node = map[name] = data || {data: {source: name}, children: [], parent: []};
					// console.log(node)
					if (name.length) {
						node.parent = find(name.substring(0, i = name.lastIndexOf(".")));
						node.parent.children.push(node);
						node.key = name.substring(i + 1);
					}
				}
				return node;
			}
			
			classes.value.edges.forEach(function(d) {
				// console.log(d)
				find(d.data.source, d);
			});
			
			return map[""];
		}
		
		// Return a list of imports for the given array of nodes.
		function packageCloneImports(nodes) {
			var map = {},
			imports = [];
			
			// Compute a map from name to node.
			nodes.forEach(function(d) {
				// console.log(d.data.source)
				map[d.source] = d.data.source;
			});
			
			// For each import, construct a link from the source to target node.
			nodes.forEach(function(d) {
				if (d.data.target)
					imports.push({source: map[d.source], target: d.data.target});
			});
			// console.log(imports)
			return imports;
		}
	};
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
		var graph = function() {
			$.ajax({
				type : "get",
				url : "/clone/package/cytoscape",
				success : function(result) {
					console.log(result);
					if(result.result == "success") {
						var cy = cytoscapeutil.showDataInCytoscape($("#graph"), result.value, "dagre");
						cy.style().selector('node[type="Package"]').style({
							'shape' : 'rectangle',
							'width': function(content) {
								return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 9;
							},
							'height': 30,
							'text-valign': 'center',
							'text-halign': 'center',
							'border-width': 1.5,
							'border-color': '#555',
							'background-color': '#f6f6f6',
							'content': 'data(name)'
						}).update();
						cy.style().selector('edge[type="Clone"]').style({
							'content': 'data(value)',
							'curve-style': 'bezier',
							'width': 1,
							'line-color': 'green',
							'line-style': 'dashed',
							'target-arrow-shape' : 'none',
							'font-size' : 20
						}).update();
						var edges = cy.remove('edge[type="Clone"]');
//					cy.layout({name : 'dagre'}).run();
						cy.layout({
							name : "dagre"
						}).run();
						cy.add(edges);
					}
				}
			});
		}
	}
	
	return {
		init : function() {
			_graph();
		}
	}
}
