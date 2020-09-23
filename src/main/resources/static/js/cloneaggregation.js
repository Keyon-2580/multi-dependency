var clone = function(cytoscapeutil) {
	var doublePackagesCloneWithCoChange = function(pck1Id, pck2Id) {
		$("#package_files_clone").html("");
		$.ajax({
			type: "get",
			url: "/clone/package/double/cochange?package1=" + pck1Id + "&package2=" + pck2Id,
			success: function(result) {
				console.log("success");
				var html = result.children.length;
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
				html += "<table class='table table-bordered'>" + "<tr><th>file1</th><th>file2</th><th>type</th><th>value</th><th>cochange</th></tr>";
				var children = result.children;
				var num_type1 = 0;
				var num_type2 = 0;
				var num_type3 = 0;
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
					var type = children[i].fileClone.cloneType;
					switch (type){
						case 'type_1':
							num_type1++;
							break;
						case 'type_2':
							num_type2++;
							break;
						case 'type_3':
							num_type3++;
							break;
					}
					html += "<a target='_blank' href='/clone/file/double?fileId1=" + children[i].file1.id + "&fileId2=" + children[i].file2.id + "'>" + type + "</a>";
					html += "</td>";
					html += "<td>";
					html += "<a target='_blank' href='/clone/compare?id1=" + children[i].file1.id + "&id2=" + children[i].file2.id + "'>" + children[i].fileClone.value + "</a>";
					html += "</td>";
					html += "<td>";
					html += "<a class='cochangeTimes' target='_blank' href='/git/cochange/commits?cochangeId=" + cochangeId + "' index='" + i + "'>" + children[i].cochangeTimes + "</a>";
					html += "</td>";
					html += "</tr>";
				}
				html += "</table>";
				html += "<table class='table table-bordered'>" + "<tr><th>Type_1数量</th><th>Type_2数量</th><th>Type_3数量</th></tr>";
				html += "<td>";
				html += num_type1;
				html += "</td>";
				html += "<td>";
				html += num_type2;
				html += "</td>";
				html += "<td>";
				html += num_type3;
				html += "</td>";
				html += "</table>";
				html += "<div id='fileClonesGraph'></div>"
				$("#package_files_clone").html(html);
				$(".cochangeTimes").click(function() {
				});
				$.ajax({
					type: "get",
					url: "/clone/package/double/graph?package1=" + pck1Id + "&package2=" + pck2Id,
					success: function(result) {
						console.log("success");
						cloneGroupToGraph(result, "fileClonesGraph");
					}
				});
			}
		})
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

		link = link
			.data(bundle(links))
			.enter().append("path")
			.each(function(d) { d.source = d[0], d.target = d[d.length - 1]; })
			.attr("class", "link")
			.attr("d", line);

		node = node
			.data(nodes.filter(function(n) { return !n.children; }))
			.enter().append("text")
			.attr("class", "node")
			.attr("dy", ".31em")
			.attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)"); })
			.style("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
			.text(function(d) { return d.key; })
			.on("mouseover", mouseovered)
			.on("mouseout", mouseouted)
			.call(text => text.append("title").text(function(d) { return d.key; }));

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
		var legendRectSize = 18;
		var legendSpacing = 4;

		var legend = d3.select('svg')
			.append("g")
			.selectAll("g")
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


		legend.append('rect')
			.attr('width', legendRectSize)
			.attr('height', legendRectSize)

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
					if (name.length) {
						node.parent = find(name.substring(0, i = name.lastIndexOf("/")));
						node.parent.children.push(node);
						node.key = name.substring(i + 1);
					}
				}
				return node;
			}

			classes.forEach(function(d) {
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
	};

	var aggregationPackage = function() {
		$("#packages_aggregation").html("");
		$.ajax({
			type: "get",
			url: "/cloneaggregation/package?threshold=10&percentage=0.8",
			success: function(result) {
				console.log("success");
				var html = "<table class='table table-bordered'>";
				html += "<tr><th>目录1</th><th>目录1克隆占比</th><th>目录2</th><th>目录2克隆占比</th><th>克隆文件对数</th><th>总克隆占比</th></tr>";
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
							var len = duplicated.relationPackages.children.length;
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
	var _graph = function() {
		aggregationPackage();
	}
	return {
		init : function() {
			_graph();
		}
	}
}